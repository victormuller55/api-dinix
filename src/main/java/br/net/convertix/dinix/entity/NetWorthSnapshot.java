package br.net.convertix.dinix.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "net_worth_snapshots", uniqueConstraints = {
        @UniqueConstraint(name = "uk_net_worth_user_period", columnNames = {"user_id", "period_month", "period_year"})
})
public class NetWorthSnapshot extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "period_month", nullable = false)
    private Integer month;

    @Column(name = "period_year", nullable = false)
    private Integer year;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal accountsBalance;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal investmentsValue;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal debts;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal netWorth;
}
