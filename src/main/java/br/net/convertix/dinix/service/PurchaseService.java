package br.net.convertix.dinix.service;

import br.net.convertix.dinix.dto.request.CreatePurchaseItemRequest;
import br.net.convertix.dinix.dto.request.CreatePurchaseRequest;
import br.net.convertix.dinix.dto.request.UpdatePurchaseRequest;
import br.net.convertix.dinix.dto.response.PageResponse;
import br.net.convertix.dinix.dto.response.PurchaseResponse;
import br.net.convertix.dinix.entity.Category;
import br.net.convertix.dinix.entity.CreditCard;
import br.net.convertix.dinix.entity.FinancialAccount;
import br.net.convertix.dinix.entity.FinancialTransaction;
import br.net.convertix.dinix.entity.Installment;
import br.net.convertix.dinix.entity.Product;
import br.net.convertix.dinix.entity.Purchase;
import br.net.convertix.dinix.entity.PurchaseItem;
import br.net.convertix.dinix.entity.PurchaseLocation;
import br.net.convertix.dinix.entity.Subscription;
import br.net.convertix.dinix.entity.Tag;
import br.net.convertix.dinix.entity.User;
import br.net.convertix.dinix.enums.InstallmentStatus;
import br.net.convertix.dinix.enums.PaymentMethod;
import br.net.convertix.dinix.exception.BusinessException;
import br.net.convertix.dinix.exception.ResourceNotFoundException;
import br.net.convertix.dinix.mapper.PurchaseMapper;
import br.net.convertix.dinix.repository.FinancialTransactionRepository;
import br.net.convertix.dinix.repository.InstallmentRepository;
import br.net.convertix.dinix.repository.ProductRepository;
import br.net.convertix.dinix.repository.PurchaseRepository;
import br.net.convertix.dinix.repository.TagRepository;
import br.net.convertix.dinix.util.MoneyUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;
    private final TagRepository tagRepository;
    private final InstallmentRepository installmentRepository;
    private final FinancialTransactionRepository transactionRepository;
    private final AuthService authService;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final CreditCardService creditCardService;
    private final LocationService locationService;
    private final LedgerService ledgerService;
    private final PurchaseMapper purchaseMapper;

    public PurchaseService(
            PurchaseRepository purchaseRepository,
            ProductRepository productRepository,
            TagRepository tagRepository,
            InstallmentRepository installmentRepository,
            FinancialTransactionRepository transactionRepository,
            AuthService authService,
            AccountService accountService,
            CategoryService categoryService,
            CreditCardService creditCardService,
            LocationService locationService,
            LedgerService ledgerService,
            PurchaseMapper purchaseMapper) {
        this.purchaseRepository = purchaseRepository;
        this.productRepository = productRepository;
        this.tagRepository = tagRepository;
        this.installmentRepository = installmentRepository;
        this.transactionRepository = transactionRepository;
        this.authService = authService;
        this.accountService = accountService;
        this.categoryService = categoryService;
        this.creditCardService = creditCardService;
        this.locationService = locationService;
        this.ledgerService = ledgerService;
        this.purchaseMapper = purchaseMapper;
    }

    @Transactional
    public PurchaseResponse create(UUID userId, CreatePurchaseRequest request) {
        User user = authService.getActive(userId);
        validatePayment(request);
        Category category = categoryService.getOwnedOrNull(userId, request.categoryId());
        PurchaseLocation location = locationService.getOwnedOrNull(userId, request.locationId());
        FinancialAccount account = request.financialAccountId() != null
                ? accountService.getOwned(userId, request.financialAccountId()) : null;
        CreditCard card = creditCardService.getOwnedOrNull(userId, request.creditCardId());

        int installmentsCount = request.numberOfInstallments() == null || request.numberOfInstallments() < 1
                ? 1 : request.numberOfInstallments();
        BigDecimal total = MoneyUtils.of(request.totalAmount());
        BigDecimal[] parts = MoneyUtils.splitInstallments(total, installmentsCount);

        Purchase purchase = Purchase.builder()
                .user(user)
                .description(request.description())
                .purchaseDate(request.purchaseDate())
                .purchaseTime(request.purchaseTime() != null ? request.purchaseTime() : LocalTime.now())
                .totalAmount(total)
                .category(category)
                .location(location)
                .paymentMethod(request.paymentMethod())
                .financialAccount(account)
                .creditCard(card)
                .notes(request.notes())
                .numberOfInstallments(installmentsCount)
                .installmentAmount(parts[0])
                .firstInstallmentDate(resolveFirstDueDate(request, card))
                .active(true)
                .build();

        addItems(userId, purchase, request.items());
        purchase.setTags(resolveTags(userId, request.tagIds()));
        generateInstallments(purchase, parts);
        Purchase saved = purchaseRepository.save(purchase);
        postLedger(saved);
        return purchaseMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<PurchaseResponse> list(
            UUID userId,
            Pageable pageable,
            Integer mes,
            Integer ano,
            LocalDate dataInicio,
            LocalDate dataFim,
            List<LocalDate> dias) {
        Pageable ordenado = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(
                        Sort.Order.desc("purchaseDate"),
                        Sort.Order.desc("purchaseTime"),
                        Sort.Order.desc("createdAt")
                )
        );

        Page<Purchase> pagina;
        if (dias != null && !dias.isEmpty()) {
            pagina = purchaseRepository.findByUserAndDates(userId, dias, ordenado);
        } else {
            LocalDate inicio = dataInicio;
            LocalDate fim = dataFim;
            if (inicio == null && fim == null && mes != null && ano != null) {
                YearMonth competencia = YearMonth.of(ano, mes);
                inicio = competencia.atDay(1);
                fim = competencia.atEndOfMonth();
            }
            if (inicio != null && fim != null) {
                pagina = purchaseRepository.findByUserAndPeriod(userId, inicio, fim, ordenado);
            } else {
                pagina = purchaseRepository.findByUserIdAndActiveTrue(userId, ordenado);
            }
        }
        return PageResponse.from(pagina.map(purchaseMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public PurchaseResponse get(UUID userId, UUID id) {
        return purchaseMapper.toResponse(getOwned(userId, id));
    }

    @Transactional
    public PurchaseResponse update(UUID userId, UUID id, UpdatePurchaseRequest request) {
        Purchase purchase = getOwned(userId, id);
        purchase.setDescription(request.description());
        purchase.setPurchaseDate(request.purchaseDate());
        if (request.purchaseTime() != null) {
            purchase.setPurchaseTime(request.purchaseTime());
        }
        purchase.setCategory(categoryService.getOwnedOrNull(userId, request.categoryId()));
        purchase.setLocation(locationService.getOwnedOrNull(userId, request.locationId()));
        if (request.paymentMethod() != null) {
            purchase.setPaymentMethod(request.paymentMethod());
        }
        purchase.setNotes(request.notes());
        purchase.setTags(resolveTags(userId, request.tagIds()));
        return purchaseMapper.toResponse(purchaseRepository.save(purchase));
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        Purchase purchase = getOwned(userId, id);
        purchase.setActive(false);
        for (Installment installment : purchase.getInstallments()) {
            if (installment.getStatus() != InstallmentStatus.CANCELLED) {
                installment.setStatus(InstallmentStatus.CANCELLED);
            }
        }
        transactionRepository.findByPurchaseIdAndActiveTrue(purchase.getId())
                .forEach(ledgerService::reverse);
        purchaseRepository.save(purchase);
    }

    @Transactional
    public Installment payInstallment(UUID userId, UUID installmentId) {
        Installment installment = installmentRepository.findByIdAndPurchaseUserId(installmentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Parcela não encontrada"));
        if (installment.getStatus() == InstallmentStatus.PAID) {
            throw new BusinessException("Parcela já está paga");
        }
        installment.setStatus(InstallmentStatus.PAID);
        installment.setPaidAt(LocalDateTime.now());
        Purchase purchase = installment.getPurchase();
        if (purchase.getPaymentMethod() == PaymentMethod.CREDIT_CARD && purchase.getFinancialAccount() != null) {
            ledgerService.postCardPayment(
                    purchase.getUser(),
                    purchase.getFinancialAccount(),
                    installment.getAmount(),
                    LocalDate.now(),
                    "Pagamento parcela " + installment.getInstallmentNumber() + "/" + installment.getTotalInstallments()
                            + " - " + purchase.getDescription());
        }
        return installmentRepository.save(installment);
    }

    public Purchase getOwned(UUID userId, UUID id) {
        return purchaseRepository.findByIdAndUserIdAndActiveTrue(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Compra não encontrada"));
    }

    @Transactional
    public Purchase chargeSubscription(Subscription subscription, LocalDate chargeDate) {
        validateSubscriptionPayment(subscription);
        User user = subscription.getUser();
        PaymentMethod method = subscription.getPaymentMethod();
        FinancialAccount account = subscription.getAccount();
        CreditCard card = subscription.getCreditCard();
        BigDecimal total = MoneyUtils.of(subscription.getAmount());
        BigDecimal[] parts = MoneyUtils.splitInstallments(total, 1);
        LocalDate firstDue = method == PaymentMethod.CREDIT_CARD && card != null
                ? creditInvoiceDueDate(chargeDate, card)
                : chargeDate;

        Purchase purchase = Purchase.builder()
                .user(user)
                .description(subscription.getName())
                .purchaseDate(chargeDate)
                .purchaseTime(LocalTime.now())
                .totalAmount(total)
                .category(subscription.getCategory())
                .paymentMethod(method)
                .financialAccount(method == PaymentMethod.CREDIT_CARD ? null : account)
                .creditCard(card)
                .notes("Assinatura")
                .numberOfInstallments(1)
                .installmentAmount(parts[0])
                .firstInstallmentDate(firstDue)
                .active(true)
                .build();

        generateInstallments(purchase, parts);
        Purchase saved = purchaseRepository.save(purchase);
        postLedger(saved);
        return saved;
    }

    private void validateSubscriptionPayment(Subscription subscription) {
        if (subscription.getPaymentMethod() == PaymentMethod.CREDIT_CARD && subscription.getCreditCard() == null) {
            throw new BusinessException("Cartão de crédito é obrigatório para essa assinatura");
        }
        if (subscription.getPaymentMethod() != PaymentMethod.CREDIT_CARD && subscription.getAccount() == null) {
            throw new BusinessException("Conta financeira é obrigatória para essa assinatura");
        }
    }

    private void validatePayment(CreatePurchaseRequest request) {
        if (request.paymentMethod() == PaymentMethod.CREDIT_CARD && request.creditCardId() == null) {
            throw new BusinessException("Cartão de crédito é obrigatório para essa forma de pagamento");
        }
        if (request.paymentMethod() != PaymentMethod.CREDIT_CARD && request.financialAccountId() == null) {
            throw new BusinessException("Conta financeira é obrigatória para essa forma de pagamento");
        }
    }

    private void addItems(UUID userId, Purchase purchase, List<CreatePurchaseItemRequest> items) {
        if (items == null) {
            return;
        }
        for (CreatePurchaseItemRequest itemRequest : items) {
            Product product = null;
            if (itemRequest.productId() != null) {
                product = productRepository.findByIdAndUserId(itemRequest.productId(), userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
                updateAveragePrice(product, itemRequest.unitPrice());
            }
            BigDecimal totalPrice = MoneyUtils.of(itemRequest.unitPrice().multiply(itemRequest.quantity()));
            PurchaseItem item = PurchaseItem.builder()
                    .purchase(purchase)
                    .product(product)
                    .quantity(itemRequest.quantity())
                    .unitPrice(MoneyUtils.of(itemRequest.unitPrice()))
                    .totalPrice(totalPrice)
                    .build();
            purchase.getItems().add(item);
        }
    }

    private void updateAveragePrice(Product product, BigDecimal newPrice) {
        if (product.getAveragePrice() == null) {
            product.setAveragePrice(MoneyUtils.of(newPrice));
        } else {
            product.setAveragePrice(MoneyUtils.of(product.getAveragePrice().add(newPrice).divide(BigDecimal.valueOf(2), MoneyUtils.SCALE, MoneyUtils.ROUNDING)));
        }
        productRepository.save(product);
    }

    private Set<Tag> resolveTags(UUID userId, List<UUID> tagIds) {
        Set<Tag> tags = new HashSet<>();
        if (tagIds == null) {
            return tags;
        }
        for (UUID tagId : tagIds) {
            tags.add(tagRepository.findByIdAndUserId(tagId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tag não encontrada")));
        }
        return tags;
    }

    private void generateInstallments(Purchase purchase, BigDecimal[] parts) {
        LocalDate firstDue = purchase.getFirstInstallmentDate();
        for (int i = 0; i < parts.length; i++) {
            Installment installment = Installment.builder()
                    .purchase(purchase)
                    .installmentNumber(i + 1)
                    .totalInstallments(parts.length)
                    .amount(parts[i])
                    .dueDate(firstDue.plusMonths(i))
                    .status(InstallmentStatus.PENDING)
                    .build();
            purchase.getInstallments().add(installment);
        }
        purchase.setInstallmentAmount(parts.length == 1 ? parts[0] : parts[Math.min(1, parts.length - 1)]);
        if (parts.length > 1) {
            purchase.setInstallmentAmount(parts[1]);
        }
    }

    private LocalDate resolveFirstDueDate(CreatePurchaseRequest request, CreditCard card) {
        if (request.firstInstallmentDate() != null) {
            return request.firstInstallmentDate();
        }
        if (request.paymentMethod() == PaymentMethod.CREDIT_CARD && card != null) {
            return creditInvoiceDueDate(request.purchaseDate(), card);
        }
        return request.purchaseDate();
    }

    private LocalDate creditInvoiceDueDate(LocalDate purchaseDate, CreditCard card) {
        YearMonth invoiceMonth = purchaseDate.getDayOfMonth() <= card.getClosingDay()
                ? YearMonth.from(purchaseDate)
                : YearMonth.from(purchaseDate).plusMonths(1);
        return MoneyUtils.atDayOfMonth(invoiceMonth, card.getDueDay());
    }

    private void postLedger(Purchase purchase) {
        boolean credit = purchase.getPaymentMethod() == PaymentMethod.CREDIT_CARD;
        for (Installment installment : purchase.getInstallments()) {
            String description = purchase.getNumberOfInstallments() > 1
                    ? purchase.getDescription() + " (" + installment.getInstallmentNumber()
                    + "/" + installment.getTotalInstallments() + ")"
                    : purchase.getDescription();
            ledgerService.postExpense(
                    purchase.getUser(),
                    installment.getAmount(),
                    installment.getDueDate(),
                    description,
                    credit ? null : purchase.getFinancialAccount(),
                    purchase.getCreditCard(),
                    purchase.getCategory(),
                    purchase,
                    installment,
                    !credit
            );
        }
    }
}
