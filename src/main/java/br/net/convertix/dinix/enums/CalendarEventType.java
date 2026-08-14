package br.net.convertix.dinix.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CalendarEventType {
    RECURRING_EXPENSE("despesa_recorrente"),
    SUBSCRIPTION("assinatura"),
    INSTALLMENT("parcela"),
    INCOME("receita"),
    CREDIT_CARD_DUE("vencimento_cartao"),
    INVESTMENT("investimento");

    private final String json;

    CalendarEventType(String json) {
        this.json = json;
    }

    @JsonValue
    public String toJson() {
        return json;
    }

    @JsonCreator
    public static CalendarEventType fromJson(String value) {
        return EnumJson.parse(CalendarEventType.class, value, CalendarEventType::toJson);
    }
}
