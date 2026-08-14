package br.net.convertix.dinix.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RecurrenceType {
    MONTHLY("mensal"),
    YEARLY("anual"),
    WEEKLY("semanal"),
    CUSTOM("personalizado");

    private final String json;

    RecurrenceType(String json) {
        this.json = json;
    }

    @JsonValue
    public String toJson() {
        return json;
    }

    @JsonCreator
    public static RecurrenceType fromJson(String value) {
        return EnumJson.parse(RecurrenceType.class, value, RecurrenceType::toJson);
    }
}
