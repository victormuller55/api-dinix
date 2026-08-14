package br.net.convertix.dinix.dto.response;

import br.net.convertix.dinix.enums.GoalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record GoalResponse(
        UUID id,
        String name,
        String description,
        BigDecimal targetAmount,
        BigDecimal currentAmount,
        BigDecimal remainingAmount,
        BigDecimal completedPercentage,
        BigDecimal monthlyRequired,
        LocalDate estimatedCompletion,
        LocalDate targetDate,
        GoalStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
