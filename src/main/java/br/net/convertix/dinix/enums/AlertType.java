package br.net.convertix.dinix.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AlertType {
    BILL_DUE("conta_vencendo"),
    CREDIT_CARD_DUE("cartao_vencendo"),
    SUBSCRIPTION_DUE("assinatura_vencendo"),
    BUDGET_WARNING("orcamento_alerta"),
    BUDGET_EXCEEDED("orcamento_estourado"),
    UNUSUAL_EXPENSE("despesa_incomum");

    private final String json;

    AlertType(String json) {
        this.json = json;
    }

    @JsonValue
    public String toJson() {
        return json;
    }

    @JsonCreator
    public static AlertType fromJson(String value) {
        return EnumJson.parse(AlertType.class, value, AlertType::toJson);
    }
}
