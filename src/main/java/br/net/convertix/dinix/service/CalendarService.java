package br.net.convertix.dinix.service;

import br.net.convertix.dinix.dto.response.CalendarDayResponse;
import br.net.convertix.dinix.dto.response.CalendarEventResponse;
import br.net.convertix.dinix.dto.response.CalendarResponse;
import br.net.convertix.dinix.entity.Income;
import br.net.convertix.dinix.entity.Installment;
import br.net.convertix.dinix.entity.RecurringExpense;
import br.net.convertix.dinix.entity.Subscription;
import br.net.convertix.dinix.enums.CalendarEventType;
import br.net.convertix.dinix.enums.InstallmentStatus;
import br.net.convertix.dinix.repository.IncomeRepository;
import br.net.convertix.dinix.repository.InstallmentRepository;
import br.net.convertix.dinix.util.MoneyUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CalendarService {

    private final RecurringExpenseService recurringExpenseService;
    private final SubscriptionService subscriptionService;
    private final InstallmentRepository installmentRepository;
    private final IncomeRepository incomeRepository;
    private final CreditCardService creditCardService;

    public CalendarService(
            RecurringExpenseService recurringExpenseService,
            SubscriptionService subscriptionService,
            InstallmentRepository installmentRepository,
            IncomeRepository incomeRepository,
            CreditCardService creditCardService) {
        this.recurringExpenseService = recurringExpenseService;
        this.subscriptionService = subscriptionService;
        this.installmentRepository = installmentRepository;
        this.incomeRepository = incomeRepository;
        this.creditCardService = creditCardService;
    }

    @Transactional(readOnly = true)
    public CalendarResponse calendar(UUID userId, int month, int year) {
        YearMonth yearMonth = YearMonth.of(year, month);
        Map<LocalDate, List<CalendarEventResponse>> events = new LinkedHashMap<>();

        for (RecurringExpense expense : recurringExpenseService.activeOf(userId)) {
            if (recurringExpenseService.occursIn(expense, yearMonth)) {
                add(events, MoneyUtils.atDayOfMonth(yearMonth, expense.getDueDay()),
                        new CalendarEventResponse(CalendarEventType.RECURRING_EXPENSE, expense.getName(), expense.getAmount()));
            }
        }
        for (Subscription subscription : subscriptionService.activeOf(userId)) {
            if (subscriptionService.occursIn(subscription, yearMonth)) {
                add(events, MoneyUtils.atDayOfMonth(yearMonth, subscription.getBillingDay()),
                        new CalendarEventResponse(CalendarEventType.SUBSCRIPTION, subscription.getName(), subscription.getAmount()));
            }
        }
        for (Installment installment : installmentRepository.findByPurchaseUserIdAndDueDateBetweenAndStatusIn(
                userId, yearMonth.atDay(1), yearMonth.atEndOfMonth(),
                List.of(InstallmentStatus.PENDING, InstallmentStatus.OVERDUE, InstallmentStatus.PAID))) {
            add(events, installment.getDueDate(), new CalendarEventResponse(
                    CalendarEventType.INSTALLMENT,
                    installment.getPurchase().getDescription() + " "
                            + installment.getInstallmentNumber() + "/" + installment.getTotalInstallments(),
                    installment.getAmount()));
        }
        for (Income income : incomeRepository.findByUserIdAndActiveTrueAndRecurringTrue(userId)) {
            LocalDate date = MoneyUtils.atDayOfMonth(yearMonth, income.getReceivedDate().getDayOfMonth());
            add(events, date, new CalendarEventResponse(CalendarEventType.INCOME, income.getDescription(), income.getAmount()));
        }
        creditCardService.listAll(userId).forEach(card -> {
            LocalDate due = MoneyUtils.atDayOfMonth(yearMonth, card.dueDay());
            add(events, due, new CalendarEventResponse(
                    CalendarEventType.CREDIT_CARD_DUE, "Fatura " + card.name(), card.usedLimit()));
        });

        List<CalendarDayResponse> days = events.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new CalendarDayResponse(entry.getKey(), entry.getValue()))
                .toList();
        return new CalendarResponse(days);
    }

    private void add(Map<LocalDate, List<CalendarEventResponse>> events, LocalDate date, CalendarEventResponse event) {
        events.computeIfAbsent(date, key -> new ArrayList<>()).add(event);
    }
}
