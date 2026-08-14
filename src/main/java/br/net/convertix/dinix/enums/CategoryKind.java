package br.net.convertix.dinix.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CategoryKind {
    INCOME("receita"),
    EXPENSE("despesa"),
    BOTH("ambos");

    private final String json;

    CategoryKind(String json) {
        this.json = json;
    }

    @JsonValue
    public String toJson() {
        return json;
    }

    @JsonCreator
    public static CategoryKind fromJson(String value) {
        return EnumJson.parse(CategoryKind.class, value, CategoryKind::toJson);
    }
}
