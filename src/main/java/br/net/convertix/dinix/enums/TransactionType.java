package br.net.convertix.dinix.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TransactionType {
    INCOME("receita"),
    EXPENSE("despesa"),
    INVESTMENT("investimento"),
    TRANSFER("transferencia");

    private final String json;

    TransactionType(String json) {
        this.json = json;
    }

    @JsonValue
    public String toJson() {
        return json;
    }

    @JsonCreator
    public static TransactionType fromJson(String value) {
        return EnumJson.parse(TransactionType.class, value, TransactionType::toJson);
    }
}
