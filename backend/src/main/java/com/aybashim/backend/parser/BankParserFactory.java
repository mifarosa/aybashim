package com.aybashim.backend.parser;

public class BankParserFactory {

    public static BankParser getParser(String bankName) {
        return switch (bankName.toUpperCase()) {
            case "ING" -> new IngBankParser();
            default -> throw new IllegalArgumentException(
                "Bilinmeyen banka: " + bankName
            );
        };
    }
}