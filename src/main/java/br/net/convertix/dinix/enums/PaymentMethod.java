package br.net.convertix.dinix.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentMethod {
    CASH("dinheiro"),
    PIX("pix"),
    DEBIT_CARD("cartao_debito"),
    CREDIT_CARD("cartao_credito"),
    BANK_TRANSFER("transferencia"),
    BOLETO("boleto"),
    OTHER("outro");

    private final String json;

    PaymentMethod(String json) {
        this.json = json;
    }

    @JsonValue
    public String toJson() {
        return json;
    }

    @JsonCreator
    public static PaymentMethod fromJson(String value) {
        return EnumJson.parse(PaymentMethod.class, value, PaymentMethod::toJson);
    }
}
