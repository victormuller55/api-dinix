-- Extrato/razão de crédito: usa data da compra (não vencimento da fatura).
UPDATE financial_transactions t
INNER JOIN purchases p ON t.purchase_id = p.id
INNER JOIN installments i ON t.installment_id = i.id
SET t.transaction_date = DATE_ADD(p.purchase_date, INTERVAL (i.installment_number - 1) MONTH),
    t.updated_at = CURRENT_TIMESTAMP(6)
WHERE t.affects_account_balance = 0
  AND t.credit_card_id IS NOT NULL
  AND t.active = 1
  AND t.purchase_id IS NOT NULL
  AND t.installment_id IS NOT NULL;
