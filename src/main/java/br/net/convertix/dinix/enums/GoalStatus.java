package br.net.convertix.dinix.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum GoalStatus {
    ACTIVE("ativa"),
    COMPLETED("concluida"),
    CANCELLED("cancelada");

    private final String json;

    GoalStatus(String json) {
        this.json = json;
    }

    @JsonValue
    public String toJson() {
        return json;
    }

    @JsonCreator
    public static GoalStatus fromJson(String value) {
        return EnumJson.parse(GoalStatus.class, value, GoalStatus::toJson);
    }
}
