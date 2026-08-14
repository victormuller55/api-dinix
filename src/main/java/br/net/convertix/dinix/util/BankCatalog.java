package br.net.convertix.dinix.util;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class BankCatalog {

    private record BankInfo(String displayName, String color) {
    }

    private static final String DEFAULT_COLOR = "#FF9800";

    private static final Map<String, BankInfo> BANKS = linkedMap();

    private BankCatalog() {
    }

    public static String displayName(String bankName) {
        return resolve(bankName).displayName();
    }

    public static String color(String bankName) {
        return resolve(bankName).color();
    }

    private static BankInfo resolve(String bankName) {
        if (bankName == null || bankName.isBlank()) {
            return new BankInfo("", DEFAULT_COLOR);
        }
        String normalized = normalize(bankName);
        BankInfo direct = BANKS.get(normalized);
        if (direct != null) {
            return direct;
        }
        Optional<Map.Entry<String, BankInfo>> partial = BANKS.entrySet().stream()
                .filter(entry -> normalized.contains(entry.getKey()) || entry.getKey().contains(normalized))
                .findFirst();
        if (partial.isPresent()) {
            return partial.get().getValue();
        }
        return new BankInfo(bankName.trim(), DEFAULT_COLOR);
    }

    private static String normalize(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
        return normalized;
    }

    private static Map<String, BankInfo> linkedMap() {
        Map<String, BankInfo> banks = new LinkedHashMap<>();
        banks.put("nubank", new BankInfo("Nubank", "#820AD1"));
        banks.put("picpay", new BankInfo("PicPay", "#21C25E"));
        banks.put("mercadopago", new BankInfo("Mercado Pago", "#00BCFF"));
        banks.put("itau", new BankInfo("Itaú", "#EC7000"));
        banks.put("inter", new BankInfo("Inter", "#FF7A00"));
        banks.put("bradesco", new BankInfo("Bradesco", "#CC092F"));
        banks.put("santander", new BankInfo("Santander", "#EC0000"));
        banks.put("caixa", new BankInfo("Caixa", "#0066A1"));
        banks.put("bancodobrasil", new BankInfo("Banco do Brasil", "#003D7A"));
        banks.put("c6bank", new BankInfo("C6 Bank", "#121212"));
        banks.put("c6", new BankInfo("C6 Bank", "#121212"));
        banks.put("btgpactual", new BankInfo("BTG Pactual", "#001E62"));
        banks.put("btg", new BankInfo("BTG Pactual", "#001E62"));
        banks.put("xp", new BankInfo("XP", "#000000"));
        banks.put("pagbank", new BankInfo("PagBank", "#42A936"));
        banks.put("pagseguro", new BankInfo("PagBank", "#42A936"));
        banks.put("neon", new BankInfo("Neon", "#161C3E"));
        banks.put("next", new BankInfo("Next", "#00FF5F"));
        banks.put("original", new BankInfo("Original", "#00A857"));
        banks.put("sicoob", new BankInfo("Sicoob", "#003B43"));
        banks.put("sicredi", new BankInfo("Sicredi", "#3DAE2B"));
        banks.put("stone", new BankInfo("Stone", "#00A868"));
        banks.put("safra", new BankInfo("Safra", "#151D43"));
        banks.put("cora", new BankInfo("Cora", "#FE3E6D"));
        banks.put("digio", new BankInfo("Digio", "#00275C"));
        return banks;
    }
}
