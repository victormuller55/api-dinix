package br.net.convertix.dinix.dto.response;

import br.net.convertix.dinix.enums.InvestmentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record InvestmentResponse(
        UUID id,
        String name,
        String institution,
        InvestmentType type,
        String ticker,
        BigDecimal currentValue,
        BigDecimal quantity,
        BigDecimal totalInvested,
        BigDecimal profitLoss,
        BigDecimal profitabilityPercent,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
