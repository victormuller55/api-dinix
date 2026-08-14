package br.net.convertix.dinix.service;

import br.net.convertix.dinix.dto.request.CreateSubscriptionRequest;
import br.net.convertix.dinix.dto.response.NextPaymentItemResponse;
import br.net.convertix.dinix.dto.response.PageResponse;
import br.net.convertix.dinix.dto.response.SubscriptionResponse;
import br.net.convertix.dinix.dto.response.SubscriptionSummaryResponse;
import br.net.convertix.dinix.entity.Subscription;
import br.net.convertix.dinix.entity.User;
import br.net.convertix.dinix.enums.RecurrenceType;
import br.net.convertix.dinix.exception.ResourceNotFoundException;
import br.net.convertix.dinix.repository.SubscriptionRepository;
import br.net.convertix.dinix.util.MoneyUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class SubscriptionService {

    private final SubscriptionRepository repository;
    private final AuthService authService;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final CreditCardService creditCardService;

    private final SubscriptionBillingService subscriptionBillingService;

    public SubscriptionService(
            SubscriptionRepository repository,
            AuthService authService,
            AccountService accountService,
            CategoryService categoryService,
            CreditCardService creditCardService,
            SubscriptionBillingService subscriptionBillingService) {
        this.repository = repository;
        this.authService = authService;
        this.accountService = accountService;
        this.categoryService = categoryService;
        this.creditCardService = creditCardService;
        this.subscriptionBillingService = subscriptionBillingService;
    }

    @Transactional
    public SubscriptionResponse create(UUID userId, CreateSubscriptionRequest request) {
        User user = authService.getActive(userId);
        RecurrenceType recurrence = request.recurrence() != null ? request.recurrence() : RecurrenceType.MONTHLY;
        LocalDate next = subscriptionBillingService.resolveInitialNextBillingDate(
                request.startDate(), request.billingDay());
        Subscription subscription = Subscription.builder()
                .user(user)
                .name(request.name())
                .description(request.description())
                .amount(MoneyUtils.of(request.amount()))
                .category(categoryService.getOwnedOrNull(userId, request.categoryId()))
                .paymentMethod(request.paymentMethod())
                .account(request.accountId() != null ? accountService.getOwned(userId, request.accountId()) : null)
                .creditCard(creditCardService.getOwnedOrNull(userId, request.creditCardId()))
                .billingDay(request.billingDay())
                .startDate(request.startDate())
                .nextBillingDate(next)
                .recurrence(recurrence)
                .active(true)
                .build();
        Subscription saved = repository.save(subscription);
        if (Boolean.TRUE.equals(request.chargeToday())) {
            subscriptionBillingService.chargeToday(saved);
        }
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<SubscriptionResponse> list(UUID userId, Pageable pageable) {
        return PageResponse.from(repository.findByUserIdAndActiveTrue(userId, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> listActive(UUID userId) {
        return repository.findByUserIdAndActiveTrue(userId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse get(UUID userId, UUID id) {
        return toResponse(getOwned(userId, id));
    }

    @Transactional
    public SubscriptionResponse update(UUID userId, UUID id, CreateSubscriptionRequest request) {
        Subscription subscription = getOwned(userId, id);
        subscription.setName(request.name());
        subscription.setDescription(request.description());
        subscription.setAmount(MoneyUtils.of(request.amount()));
        subscription.setCategory(categoryService.getOwnedOrNull(userId, request.categoryId()));
        subscription.setPaymentMethod(request.paymentMethod());
        subscription.setAccount(request.accountId() != null ? accountService.getOwned(userId, request.accountId()) : null);
        subscription.setCreditCard(creditCardService.getOwnedOrNull(userId, request.creditCardId()));
        subscription.setBillingDay(request.billingDay());
        subscription.setStartDate(request.startDate());
        subscription.setRecurrence(request.recurrence() != null ? request.recurrence() : subscription.getRecurrence());
        return toResponse(repository.save(subscription));
    }

    @Transactional
    public void cancel(UUID userId, UUID id) {
        Subscription subscription = getOwned(userId, id);
        subscription.setActive(false);
        subscription.setCancelledAt(LocalDateTime.now());
        repository.save(subscription);
    }

    @Transactional(readOnly = true)
    public SubscriptionSummaryResponse summary(UUID userId) {
        List<Subscription> active = repository.findByUserIdAndActiveTrue(userId);
        BigDecimal monthly = MoneyUtils.zero();
        for (Subscription subscription : active) {
            monthly = monthly.add(toMonthly(subscription));
        }
        List<NextPaymentItemResponse> nextPayments = active.stream()
                .sorted(Comparator.comparing(Subscription::getNextBillingDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(10)
                .map(s -> new NextPaymentItemResponse(s.getId(), s.getName(), s.getAmount(), s.getNextBillingDate()))
                .toList();
        return new SubscriptionSummaryResponse(monthly, monthly.multiply(BigDecimal.valueOf(12)), nextPayments);
    }

    public List<Subscription> activeOf(UUID userId) {
        return repository.findByUserIdAndActiveTrue(userId);
    }

    public BigDecimal toMonthly(Subscription subscription) {
        return switch (subscription.getRecurrence()) {
            case YEARLY -> MoneyUtils.of(subscription.getAmount().divide(BigDecimal.valueOf(12), MoneyUtils.SCALE, MoneyUtils.ROUNDING));
            case WEEKLY -> MoneyUtils.of(subscription.getAmount().multiply(BigDecimal.valueOf(4)));
            default -> MoneyUtils.of(subscription.getAmount());
        };
    }

    public boolean occursIn(Subscription subscription, YearMonth month) {
        LocalDate start = subscription.getStartDate();
        LocalDate occurrence = MoneyUtils.atDayOfMonth(month, subscription.getBillingDay());
        if (occurrence.isBefore(start)) {
            return false;
        }
        return switch (subscription.getRecurrence()) {
            case YEARLY -> occurrence.getMonthValue() == start.getMonthValue();
            default -> true;
        };
    }

    private Subscription getOwned(UUID userId, UUID id) {
        return repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Assinatura não encontrada"));
    }

    private SubscriptionResponse toResponse(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getId(), subscription.getName(), subscription.getDescription(), subscription.getAmount(),
                subscription.getCategory() != null ? subscription.getCategory().getId() : null,
                subscription.getPaymentMethod(),
                subscription.getAccount() != null ? subscription.getAccount().getId() : null,
                subscription.getCreditCard() != null ? subscription.getCreditCard().getId() : null,
                subscription.getBillingDay(), subscription.getStartDate(), subscription.getNextBillingDate(),
                subscription.getRecurrence(), subscription.isActive(), subscription.getCancelledAt(),
                subscription.getCreatedAt(), subscription.getUpdatedAt());
    }
}
