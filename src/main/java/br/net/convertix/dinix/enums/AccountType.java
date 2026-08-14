package br.net.convertix.dinix.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AccountType {
    CHECKING("conta_corrente"),
    SAVINGS("poupanca"),
    INVESTMENT("investimento"),
    CASH("dinheiro"),
    OTHER("outro");

    private final String json;

    AccountType(String json) {
        this.json = json;
    }

    @JsonValue
    public String toJson() {
        return json;
    }

    @JsonCreator
    public static AccountType fromJson(String value) {
        return EnumJson.parse(AccountType.class, value, AccountType::toJson);
    }
}
