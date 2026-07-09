package com.aybashim.backend.parser;

import com.aybashim.backend.model.Transaction;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HadiParser implements BankParser {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Pattern LINE_PATTERN = Pattern.compile(
            "^(\\d{2}/\\d{2}/\\d{4})\\s+(.+?)\\s+(\\+?[\\d,]+\\.\\d{2})$"
    );

    @Override
    public List<Transaction> parse(String text, MultipartFile file) {
        List<Transaction> transactions = new ArrayList<>();

        for (String line : text.split("\\R")) {
            Matcher matcher = LINE_PATTERN.matcher(line.trim());
            if (!matcher.find()) {
                continue;
            }

            String amountText = matcher.group(3);
            boolean credit = amountText.startsWith("+");
            BigDecimal amount = new BigDecimal(amountText.replace("+", "").replace(",", ""));

            Transaction transaction = new Transaction();
            transaction.setDate(LocalDate.parse(matcher.group(1), FORMATTER));
            transaction.setDescription(matcher.group(2).trim());
            transaction.setAmount(amount);
            transaction.setType(credit ? "CREDIT" : "DEBIT");
            transaction.setBankName("Hadi");
            transaction.setSourceFile(file.getOriginalFilename());
            transactions.add(transaction);
        }

        return transactions;
    }
}
