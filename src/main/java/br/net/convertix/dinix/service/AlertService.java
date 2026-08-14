package br.net.convertix.dinix.service;

import br.net.convertix.dinix.dto.response.AlertResponse;
import br.net.convertix.dinix.dto.response.BudgetResponse;
import br.net.convertix.dinix.dto.response.PageResponse;
import br.net.convertix.dinix.entity.FinancialAlert;
import br.net.convertix.dinix.entity.Installment;
import br.net.convertix.dinix.entity.Subscription;
import br.net.convertix.dinix.entity.User;
import br.net.convertix.dinix.enums.AlertType;
import br.net.convertix.dinix.enums.InstallmentStatus;
import br.net.convertix.dinix.exception.ResourceNotFoundException;
import br.net.convertix.dinix.repository.FinancialAlertRepository;
import br.net.convertix.dinix.repository.InstallmentRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
public class AlertService {

    private final FinancialAlertRepository alertRepository;
    private final InstallmentRepository installmentRepository;
    private final BudgetService budgetService;
    private final SubscriptionService subscriptionService;
    private final AuthService authService;

    public AlertService(
            FinancialAlertRepository alertRepository,
            InstallmentRepository installmentRepository,
            BudgetService budgetService,
            SubscriptionService subscriptionService,
            AuthService authService) {
        this.alertRepository = alertRepository;
        this.installmentRepository = installmentRepository;
        this.budgetService = budgetService;
        this.subscriptionService = subscriptionService;
        this.authService = authService;
    }

    @Transactional
    public PageResponse<AlertResponse> list(UUID userId, Pageable pageable) {
        refresh(userId);
        return PageResponse.from(alertRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable).map(this::toResponse));
    }

    @Transactional
    public AlertResponse markRead(UUID userId, UUID id) {
        FinancialAlert alert = alertRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta não encontrado"));
        alert.setReadFlag(true);
        return toResponse(alertRepository.save(alert));
    }

    public void refresh(UUID userId) {
        User user = authService.getActive(userId);
        LocalDate today = LocalDate.now();
        installmentRepository.findByStatusAndDueDateBefore(InstallmentStatus.PENDING, today)
                .stream()
                .filter(i -> i.getPurchase().getUser().getId().equals(userId))
                .forEach(i -> {
                    i.setStatus(InstallmentStatus.OVERDUE);
                    installmentRepository.save(i);
                    createIfAbsent(user, AlertType.BILL_DUE, "Parcela atrasada",
                            i.getPurchase().getDescription(), i.getAmount(), i.getDueDate(),
                            "installment-" + i.getId());
                });
        YearMonth month = YearMonth.now();
        for (BudgetResponse budget : budgetService.list(userId, month.getMonthValue(), month.getYear())) {
            if (budget.usedPercentage().compareTo(BigDecimal.valueOf(100)) >= 0) {
                createIfAbsent(user, AlertType.BUDGET_EXCEEDED, "Orçamento excedido",
                        budget.categoryName(), budget.spent(), null, "budget-exceeded-" + budget.id());
            } else if (budget.usedPercentage().compareTo(BigDecimal.valueOf(80)) >= 0) {
                createIfAbsent(user, AlertType.BUDGET_WARNING, "Orçamento próximo do limite",
                        budget.categoryName(), budget.spent(), null, "budget-warn-" + budget.id());
            }
        }
        for (Subscription subscription : subscriptionService.activeOf(userId)) {
            if (subscription.getNextBillingDate() != null
                    && !subscription.getNextBillingDate().isAfter(today.plusDays(3))) {
                createIfAbsent(user, AlertType.SUBSCRIPTION_DUE, "Assinatura próxima",
                        subscription.getName(), subscription.getAmount(), subscription.getNextBillingDate(),
                        "sub-" + subscription.getId() + "-" + subscription.getNextBillingDate());
            }
        }
    }

    private void createIfAbsent(
            User user, AlertType type, String title, String message, BigDecimal amount,
            LocalDate dueDate, String key) {
        if (alertRepository.existsByUserIdAndReferenceKey(user.getId(), key)) {
            return;
        }
        alertRepository.save(FinancialAlert.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .amount(amount)
                .dueDate(dueDate)
                .readFlag(false)
                .referenceKey(key)
                .build());
    }

    private AlertResponse toResponse(FinancialAlert alert) {
        return new AlertResponse(
                alert.getId(), alert.getType(), alert.getTitle(), alert.getMessage(),
                alert.getAmount(), alert.getDueDate(), alert.isReadFlag(), alert.getCreatedAt());
    }
}
