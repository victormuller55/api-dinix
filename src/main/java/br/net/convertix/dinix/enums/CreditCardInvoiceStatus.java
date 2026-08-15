package br.net.convertix.dinix.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CreditCardInvoiceStatus {
    CURRENT("atual"),
    UPCOMING("proxima"),
    CLOSED("fechada"),
    PAID("paga");

    private final String json;

    CreditCardInvoiceStatus(String json) {
        this.json = json;
    }

    @JsonValue
    public String toJson() {
        return json;
    }

    @JsonCreator
    public static CreditCardInvoiceStatus fromJson(String value) {
        return EnumJson.parse(CreditCardInvoiceStatus.class, value, CreditCardInvoiceStatus::toJson);
    }

    public boolean countsTowardUsedLimit() {
        return this == CURRENT || this == UPCOMING || this == CLOSED;
    }
}
