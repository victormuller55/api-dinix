package br.net.convertix.dinix.service;

import br.net.convertix.dinix.dto.request.CreateGoalRequest;
import br.net.convertix.dinix.dto.request.UpdateGoalRequest;
import br.net.convertix.dinix.dto.response.GoalResponse;
import br.net.convertix.dinix.dto.response.PageResponse;
import br.net.convertix.dinix.entity.FinancialGoal;
import br.net.convertix.dinix.entity.User;
import br.net.convertix.dinix.enums.GoalStatus;
import br.net.convertix.dinix.exception.ResourceNotFoundException;
import br.net.convertix.dinix.repository.FinancialGoalRepository;
import br.net.convertix.dinix.util.MoneyUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class GoalService {

    private final FinancialGoalRepository goalRepository;
    private final AuthService authService;

    public GoalService(FinancialGoalRepository goalRepository, AuthService authService) {
        this.goalRepository = goalRepository;
        this.authService = authService;
    }

    @Transactional
    public GoalResponse create(UUID userId, CreateGoalRequest request) {
        User user = authService.getActive(userId);
        FinancialGoal goal = FinancialGoal.builder()
                .user(user)
                .name(request.name())
                .description(request.description())
                .targetAmount(MoneyUtils.of(request.targetAmount()))
                .currentAmount(MoneyUtils.of(request.currentAmount() == null ? BigDecimal.ZERO : request.currentAmount()))
                .targetDate(request.targetDate())
                .status(GoalStatus.ACTIVE)
                .build();
        return toResponse(goalRepository.save(goal));
    }

    @Transactional(readOnly = true)
    public PageResponse<GoalResponse> list(UUID userId, Pageable pageable) {
        return PageResponse.from(goalRepository.findByUserId(userId, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public GoalResponse get(UUID userId, UUID id) {
        return toResponse(getOwned(userId, id));
    }

    @Transactional
    public GoalResponse update(UUID userId, UUID id, UpdateGoalRequest request) {
        FinancialGoal goal = getOwned(userId, id);
        goal.setName(request.name());
        goal.setDescription(request.description());
        goal.setTargetAmount(MoneyUtils.of(request.targetAmount()));
        goal.setCurrentAmount(MoneyUtils.of(request.currentAmount()));
        goal.setTargetDate(request.targetDate());
        if (request.status() != null) {
            goal.setStatus(request.status());
        }
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(GoalStatus.COMPLETED);
        }
        return toResponse(goalRepository.save(goal));
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        goalRepository.delete(getOwned(userId, id));
    }

    private FinancialGoal getOwned(UUID userId, UUID id) {
        return goalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Meta não encontrada"));
    }

    private GoalResponse toResponse(FinancialGoal goal) {
        BigDecimal remaining = goal.getTargetAmount().subtract(goal.getCurrentAmount()).max(BigDecimal.ZERO);
        BigDecimal completed = MoneyUtils.percentage(goal.getCurrentAmount(), goal.getTargetAmount());
        BigDecimal monthlyRequired = MoneyUtils.zero();
        LocalDate estimated = null;
        if (goal.getTargetDate() != null && remaining.compareTo(BigDecimal.ZERO) > 0) {
            long months = Math.max(ChronoUnit.MONTHS.between(LocalDate.now(), goal.getTargetDate()), 1);
            monthlyRequired = remaining.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_EVEN);
        }
        return new GoalResponse(
                goal.getId(), goal.getName(), goal.getDescription(), goal.getTargetAmount(),
                goal.getCurrentAmount(), remaining, completed, monthlyRequired, estimated,
                goal.getTargetDate(), goal.getStatus(), goal.getCreatedAt(), goal.getUpdatedAt());
    }
}
