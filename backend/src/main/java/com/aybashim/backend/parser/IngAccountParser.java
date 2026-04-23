package com.aybashim.backend.parser;

import com.aybashim.backend.model.Transaction;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IngAccountParser implements BankParser {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final Pattern LINE_PATTERN = Pattern.compile(
            "^(\\d{2}\\.\\d{2}\\.\\d{4})\\s+(.+?)\\s+([+-]?[\\d,\\.]+)\\s+[\\d,\\.]+"
    );

    @Override
    public List<Transaction> parse(String text) {
        List<Transaction> transactions = new ArrayList<>();

        List<String> blocks = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String line : text.split("\n")) {
            line = line.trim();
            if (line.matches("^\\d{2}\\.\\d{2}\\.\\d{4}.*")) {
                if (!current.isEmpty()) blocks.add(current.toString());
                current = new StringBuilder(line);
            } else if (!current.isEmpty()) {
                current.append(" ").append(line);
            }
        }
        if (!current.isEmpty()) blocks.add(current.toString());

        for (String block : blocks) {
            System.out.println("BLOCK: " + block);
            Matcher matcher = LINE_PATTERN.matcher(block.trim());
            if (matcher.find()) {
                Transaction tx = getTransaction(matcher);
                transactions.add(tx);
            }
        }

        return transactions;
    }

    private static @NonNull Transaction getTransaction(Matcher matcher) {
        String tutarStr = matcher.group(3)
                .replace(",", "");

        BigDecimal amount = new BigDecimal(tutarStr);
        String type = amount.compareTo(BigDecimal.ZERO) >= 0 ? "CREDIT" : "DEBIT";

        Transaction tx = new Transaction();
        tx.setDate(LocalDate.parse(matcher.group(1), FORMATTER));
        tx.setDescription(matcher.group(2).trim());
        tx.setAmount(amount.abs());
        tx.setType(type);
        tx.setBankName("ING");
        return tx;
    }
}