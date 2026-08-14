package br.net.convertix.dinix.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum InvestmentTransactionType {
    BUY("compra"),
    SELL("venda"),
    DEPOSIT("aporte"),
    WITHDRAW("resgate"),
    DIVIDEND("dividendo"),
    INTEREST("juros");

    private final String json;

    InvestmentTransactionType(String json) {
        this.json = json;
    }

    @JsonValue
    public String toJson() {
        return json;
    }

    @JsonCreator
    public static InvestmentTransactionType fromJson(String value) {
        return EnumJson.parse(InvestmentTransactionType.class, value, InvestmentTransactionType::toJson);
    }
}
