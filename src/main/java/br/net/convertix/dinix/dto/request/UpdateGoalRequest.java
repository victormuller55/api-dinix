package br.net.convertix.dinix.dto.request;

import br.net.convertix.dinix.enums.GoalStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateGoalRequest(
        @NotBlank @Size(max = 180) String name,
        @Size(max = 255) String description,
        @NotNull @Positive BigDecimal targetAmount,
        @NotNull BigDecimal currentAmount,
        LocalDate targetDate,
        GoalStatus status
) {
}
