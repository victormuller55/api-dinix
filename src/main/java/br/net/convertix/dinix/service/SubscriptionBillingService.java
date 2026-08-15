package br.net.convertix.dinix.service;

import br.net.convertix.dinix.entity.Purchase;
import br.net.convertix.dinix.entity.Subscription;
import br.net.convertix.dinix.enums.RecurrenceType;
import br.net.convertix.dinix.repository.SubscriptionRepository;
import br.net.convertix.dinix.util.MoneyUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;

@Service
public class SubscriptionBillingService {

    private final SubscriptionRepository repository;
    private final PurchaseService purchaseService;

    public SubscriptionBillingService(
            SubscriptionRepository repository,
            PurchaseService purchaseService) {
        this.repository = repository;
        this.purchaseService = purchaseService;
    }

    @Transactional
    public Purchase chargeToday(Subscription subscription) {
        LocalDate today = LocalDate.now();
        YearMonth month = YearMonth.from(today);
        Purchase purchase = purchaseService.chargeSubscription(subscription, today);
        markPaid(subscription, month, today);
        return purchase;
    }

    @Transactional
    public Purchase chargeForMonth(Subscription subscription, LocalDate chargeDate, YearMonth paidMonth) {
        Purchase purchase = purchaseService.chargeSubscription(subscription, chargeDate);
        markPaid(subscription, paidMonth, chargeDate);
        return purchase;
    }

    private void markPaid(Subscription subscription, YearMonth paidMonth, LocalDate lastBillingDate) {
        subscription.setLastPaidYear(paidMonth.getYear());
        subscription.setLastPaidMonth(paidMonth.getMonthValue());
        subscription.setNextBillingDate(computeNextBillingDate(subscription, lastBillingDate));
        repository.save(subscription);
    }

    public LocalDate computeNextBillingDate(Subscription subscription, LocalDate lastBillingDate) {
        return switch (subscription.getRecurrence() != null ? subscription.getRecurrence() : RecurrenceType.MONTHLY) {
            case WEEKLY -> lastBillingDate.plusWeeks(1);
            case YEARLY -> lastBillingDate.plusYears(1);
            default -> MoneyUtils.atDayOfMonth(
                    YearMonth.from(lastBillingDate).plusMonths(1),
                    subscription.getBillingDay());
        };
    }

    public LocalDate resolveInitialNextBillingDate(LocalDate startDate, int billingDay) {
        LocalDate next = MoneyUtils.atDayOfMonth(YearMonth.from(startDate), billingDay);
        if (next.isBefore(startDate)) {
            next = MoneyUtils.atDayOfMonth(YearMonth.from(startDate).plusMonths(1), billingDay);
        }
        return next;
    }
}
