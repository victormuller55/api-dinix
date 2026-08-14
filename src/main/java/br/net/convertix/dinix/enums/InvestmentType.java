package br.net.convertix.dinix.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum InvestmentType {
    STOCK("acao"),
    ETF("etf"),
    FUND("fundo"),
    FIXED_INCOME("renda_fixa"),
    CRYPTO("cripto"),
    SAVINGS("poupanca"),
    OTHER("outro");

    private final String json;

    InvestmentType(String json) {
        this.json = json;
    }

    @JsonValue
    public String toJson() {
        return json;
    }

    @JsonCreator
    public static InvestmentType fromJson(String value) {
        return EnumJson.parse(InvestmentType.class, value, InvestmentType::toJson);
    }
}
