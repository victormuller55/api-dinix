package br.net.convertix.dinix.entity;

import br.net.convertix.dinix.enums.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "financial_transactions")
public class FinancialTransaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @Column(length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private FinancialAccount account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_card_id")
    private CreditCard creditCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id")
    private Purchase purchase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "installment_id")
    private Installment installment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "income_id")
    private Income income;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investment_transaction_id")
    private InvestmentTransaction investmentTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id")
    private Transfer transfer;

    /**
     * Quando true, o valor entra no resultado mensal (receita, despesa ou investimento).
     * Transferências e liquidações de fatura ficam false para evitar duplicidade.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean countsInMonthlyResult = true;

    /**
     * Quando true, o valor altera o saldo da conta bancária vinculada.
     * Despesas de cartão de crédito ficam false até a liquidação da fatura.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean affectsAccountBalance = true;

    /**
     * Direção do efeito no saldo da conta: true aumenta, false diminui.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean inflow = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
