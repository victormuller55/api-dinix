package br.net.convertix.dinix.config;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NomesCamposApi {

    public static final String HEADER_AUTORIZACAO = "autorizacao";

    private static final Pattern IDENTIFICADOR = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");

    private static final Map<String, String> CAMPOS = Map.ofEntries(
            Map.entry("token", "token"),
            Map.entry("tokenType", "tipo_token"),
            Map.entry("userId", "id_usuario"),
            Map.entry("name", "nome"),
            Map.entry("email", "email"),
            Map.entry("photoUrl", "url_foto"),
            Map.entry("password", "senha"),
            Map.entry("currentPassword", "senha_atual"),
            Map.entry("newPassword", "senha_nova"),
            Map.entry("expiresAt", "expira_em"),
            Map.entry("id", "id"),
            Map.entry("active", "ativo"),
            Map.entry("createdAt", "criado_em"),
            Map.entry("updatedAt", "atualizado_em"),
            Map.entry("bankName", "nome_banco"),
            Map.entry("accountType", "tipo_conta"),
            Map.entry("initialBalance", "saldo_inicial"),
            Map.entry("currentBalance", "saldo_atual"),
            Map.entry("color", "cor"),
            Map.entry("description", "descricao"),
            Map.entry("icon", "icone"),
            Map.entry("kind", "tipo"),
            Map.entry("parentCategoryId", "id_categoria_pai"),
            Map.entry("systemDefault", "padrao_sistema"),
            Map.entry("accountId", "id_conta"),
            Map.entry("bank", "banco"),
            Map.entry("creditLimit", "limite"),
            Map.entry("usedLimit", "limite_usado"),
            Map.entry("availableLimit", "limite_disponivel"),
            Map.entry("closingDay", "dia_fechamento"),
            Map.entry("dueDay", "dia_vencimento"),
            Map.entry("address", "endereco"),
            Map.entry("city", "cidade"),
            Map.entry("state", "estado"),
            Map.entry("latitude", "latitude"),
            Map.entry("longitude", "longitude"),
            Map.entry("brand", "marca"),
            Map.entry("categoryId", "id_categoria"),
            Map.entry("averagePrice", "preco_medio"),
            Map.entry("purchaseDate", "data_compra"),
            Map.entry("purchaseTime", "hora_compra"),
            Map.entry("totalAmount", "valor_total"),
            Map.entry("locationId", "id_local"),
            Map.entry("paymentMethod", "forma_pagamento"),
            Map.entry("financialAccountId", "id_conta"),
            Map.entry("creditCardId", "id_cartao_credito"),
            Map.entry("notes", "observacoes"),
            Map.entry("numberOfInstallments", "qtd_parcelas"),
            Map.entry("firstInstallmentDate", "data_primeira_parcela"),
            Map.entry("tagIds", "ids_etiquetas"),
            Map.entry("items", "itens"),
            Map.entry("installmentAmount", "valor_parcela"),
            Map.entry("installments", "parcelas"),
            Map.entry("tags", "etiquetas"),
            Map.entry("productId", "id_produto"),
            Map.entry("productName", "nome_produto"),
            Map.entry("quantity", "quantidade"),
            Map.entry("unitPrice", "preco_unitario"),
            Map.entry("totalPrice", "preco_total"),
            Map.entry("installmentNumber", "numero_parcela"),
            Map.entry("totalInstallments", "total_parcelas"),
            Map.entry("amount", "valor"),
            Map.entry("dueDate", "data_vencimento"),
            Map.entry("status", "status"),
            Map.entry("paidAt", "pago_em"),
            Map.entry("receivedDate", "data_recebimento"),
            Map.entry("recurring", "recorrente"),
            Map.entry("sourceAccountId", "id_conta_origem"),
            Map.entry("destinationAccountId", "id_conta_destino"),
            Map.entry("transferDate", "data_transferencia"),
            Map.entry("startDate", "data_inicio"),
            Map.entry("endDate", "data_fim"),
            Map.entry("recurrence", "recorrencia"),
            Map.entry("billingDay", "dia_cobranca"),
            Map.entry("nextBillingDate", "data_proxima_cobranca"),
            Map.entry("chargeToday", "pagamento_hoje"),
            Map.entry("lastPaidYear", "ano_ultimo_pagamento"),
            Map.entry("lastPaidMonth", "mes_ultimo_pagamento"),
            Map.entry("cancelledAt", "cancelado_em"),
            Map.entry("monthlyTotal", "total_mensal"),
            Map.entry("yearlyTotal", "total_anual"),
            Map.entry("nextPayments", "proximos_pagamentos"),
            Map.entry("subscriptionId", "id_assinatura"),
            Map.entry("date", "data"),
            Map.entry("institution", "instituicao"),
            Map.entry("type", "tipo"),
            Map.entry("ticker", "ticker"),
            Map.entry("currentValue", "valor_atual"),
            Map.entry("totalInvested", "total_investido"),
            Map.entry("profitLoss", "lucro_prejuizo"),
            Map.entry("profitabilityPercent", "percentual_rentabilidade"),
            Map.entry("investmentId", "id_investimento"),
            Map.entry("price", "preco"),
            Map.entry("transactionDate", "data_transacao"),
            Map.entry("amountLimit", "valor_limite"),
            Map.entry("categoryName", "nome_categoria"),
            Map.entry("spent", "gasto"),
            Map.entry("remaining", "restante"),
            Map.entry("usedPercentage", "percentual_usado"),
            Map.entry("month", "mes"),
            Map.entry("year", "ano"),
            Map.entry("targetAmount", "valor_alvo"),
            Map.entry("currentAmount", "valor_atual"),
            Map.entry("remainingAmount", "valor_restante"),
            Map.entry("completedPercentage", "percentual_concluido"),
            Map.entry("monthlyRequired", "mensal_necessario"),
            Map.entry("estimatedCompletion", "previsao_conclusao"),
            Map.entry("targetDate", "data_alvo"),
            Map.entry("purchaseId", "id_compra"),
            Map.entry("installmentId", "id_parcela"),
            Map.entry("incomeId", "id_receita"),
            Map.entry("transferId", "id_transferencia"),
            Map.entry("countsInMonthlyResult", "conta_no_resultado_mensal"),
            Map.entry("affectsAccountBalance", "altera_saldo_conta"),
            Map.entry("income", "receitas"),
            Map.entry("expenses", "despesas"),
            Map.entry("investments", "investimentos"),
            Map.entry("available", "disponivel"),
            Map.entry("expensesByCategory", "despesas_por_categoria"),
            Map.entry("upcomingPayments", "proximos_pagamentos"),
            Map.entry("creditCards", "cartoes_credito"),
            Map.entry("referenceYear", "ano"),
            Map.entry("referenceMonth", "mes"),
            Map.entry("subscriptions", "assinaturas"),
            Map.entry("totalIncome", "total_receitas"),
            Map.entry("totalExpenses", "total_despesas"),
            Map.entry("totalInvestments", "total_investimentos"),
            Map.entry("totalTransfers", "total_transferencias"),
            Map.entry("availableBalance", "saldo_disponivel"),
            Map.entry("expensePercentage", "percentual_despesas"),
            Map.entry("investmentPercentage", "percentual_investimentos"),
            Map.entry("expensesComparison", "comparativo_despesas"),
            Map.entry("topPurchaseLocations", "principais_locais"),
            Map.entry("topProducts", "principais_produtos"),
            Map.entry("title", "titulo"),
            Map.entry("total", "total"),
            Map.entry("count", "quantidade"),
            Map.entry("breakdown", "detalhamento"),
            Map.entry("message", "mensagem"),
            Map.entry("code", "codigo"),
            Map.entry("verified", "verificado"),
            Map.entry("read", "lido"),
            Map.entry("days", "dias"),
            Map.entry("events", "eventos"),
            Map.entry("expectedIncome", "receitas_previstas"),
            Map.entry("committedExpenses", "despesas_comprometidas"),
            Map.entry("expectedInvestments", "investimentos_previstos"),
            Map.entry("expectedAvailable", "disponivel_previsto"),
            Map.entry("accountsBalance", "saldo_contas"),
            Map.entry("investmentsValue", "valor_investimentos"),
            Map.entry("debts", "dividas"),
            Map.entry("netWorth", "patrimonio"),
            Map.entry("totalSpent", "total_gasto"),
            Map.entry("lastPurchaseDate", "data_ultima_compra"),
            Map.entry("category", "categoria"),
            Map.entry("percentage", "percentual"),
            Map.entry("previousTotal", "total_anterior"),
            Map.entry("variationPercent", "percentual_variacao"),
            Map.entry("transactionId", "id_transacao"),
            Map.entry("fileName", "nome_arquivo"),
            Map.entry("fileUrl", "url_arquivo"),
            Map.entry("contentType", "tipo_conteudo"),
            Map.entry("itens", "itens"),
            Map.entry("numPag", "num_pag"),
            Map.entry("maxPag", "max_pag"),
            Map.entry("maxItens", "max_itens"),
            Map.entry("itensPag", "itens_pag"),
            Map.entry("timestamp", "data_hora"),
            Map.entry("error", "erro"),
            Map.entry("path", "caminho"),
            Map.entry("fieldErrors", "erros_campos")
    );

    private NomesCamposApi() {
    }

    public static String json(String javaName) {
        if (javaName == null || javaName.isBlank()) {
            return javaName;
        }
        String mapped = CAMPOS.get(javaName);
        if (mapped != null) {
            return mapped;
        }
        return javaName.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    public static String caminhoJson(String javaPath) {
        if (javaPath == null || javaPath.isBlank()) {
            return javaPath;
        }
        Matcher matcher = IDENTIFICADOR.matcher(javaPath);
        StringBuilder out = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(out, Matcher.quoteReplacement(json(matcher.group())));
        }
        matcher.appendTail(out);
        return out.toString();
    }
}
