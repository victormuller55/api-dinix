package br.net.convertix.dinix.service;

import br.net.convertix.dinix.entity.Subscription;
import br.net.convertix.dinix.enums.RecurrenceType;
import br.net.convertix.dinix.repository.SubscriptionRepository;
import br.net.convertix.dinix.util.MoneyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class SubscriptionBillingService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionBillingService.class);

    private final SubscriptionRepository repository;
    private final PurchaseService purchaseService;

    public SubscriptionBillingService(
            SubscriptionRepository repository,
            PurchaseService purchaseService) {
        this.repository = repository;
        this.purchaseService = purchaseService;
    }

    @Transactional
    public void chargeToday(Subscription subscription) {
        LocalDate today = LocalDate.now();
        purchaseService.chargeSubscription(subscription, today);
        subscription.setNextBillingDate(computeNextBillingDate(subscription, today));
        repository.save(subscription);
    }

    @Transactional
    public void chargeIfDue(Subscription subscription, LocalDate referenceDate) {
        LocalDate next = subscription.getNextBillingDate();
        if (next == null || next.isAfter(referenceDate)) {
            return;
        }
        purchaseService.chargeSubscription(subscription, next);
        subscription.setNextBillingDate(computeNextBillingDate(subscription, next));
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

    @Scheduled(cron = "0 0 6 * * *")
    @Transactional
    public void processDueSubscriptions() {
        LocalDate today = LocalDate.now();
        List<Subscription> due = repository.findByActiveTrueAndNextBillingDateLessThanEqual(today);
        for (Subscription subscription : due) {
            try {
                chargeIfDue(subscription, today);
            } catch (Exception ex) {
                log.warn("Falha ao cobrar assinatura {}: {}", subscription.getId(), ex.getMessage());
            }
        }
    }
}
