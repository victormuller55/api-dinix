package br.net.convertix.dinix.enums;

import java.util.function.Function;

public final class EnumJson {

    private EnumJson() {
    }

    public static <E extends Enum<E>> E parse(Class<E> type, String value, Function<E, String> toJson) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        for (E constant : type.getEnumConstants()) {
            if (toJson.apply(constant).equalsIgnoreCase(normalized) || constant.name().equalsIgnoreCase(normalized)) {
                return constant;
            }
        }
        throw new IllegalArgumentException("Valor inválido para " + type.getSimpleName() + ": " + value);
    }
}
