package br.net.convertix.dinix.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum InstallmentStatus {
    PENDING("pendente"),
    PAID("pago"),
    OVERDUE("atrasado"),
    CANCELLED("cancelado");

    private final String json;

    InstallmentStatus(String json) {
        this.json = json;
    }

    @JsonValue
    public String toJson() {
        return json;
    }

    @JsonCreator
    public static InstallmentStatus fromJson(String value) {
        return EnumJson.parse(InstallmentStatus.class, value, InstallmentStatus::toJson);
    }
}
