package com.aybashim.backend.parser;

import com.aybashim.backend.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IngBankParser implements BankParser {

    private static final DateTimeFormatter FORMATTER = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private static final Pattern LINE_PATTERN = Pattern.compile(
        "^(\\d{2}/\\d{2}/\\d{4})\\s+(.+?)\\s+([\\d,\\.]+)\\s*(\\+)?$"
    );

    @Override
    public List<Transaction> parse(String text) {
        List<Transaction> transactions = new ArrayList<>();

        for (String line : text.split("\n")) {
            Matcher matcher = LINE_PATTERN.matcher(line.trim());
            if (matcher.find()) {
                Transaction tx = new Transaction();
                tx.setDate(LocalDate.parse(matcher.group(1), FORMATTER));
                String aciklama = matcher.group(2).trim();
                aciklama = aciklama.replaceAll("\\s+\\d+\\.\\d+$", "").trim();
                tx.setDescription(aciklama);
                String tutarStr = matcher.group(3).replace(",", "");
                tx.setAmount(new BigDecimal(tutarStr));
                tx.setType(matcher.group(4) != null ? "CREDIT" : "DEBIT");
                tx.setBankName("ING");
                transactions.add(tx);
            }
        }

        return transactions;
    }
}