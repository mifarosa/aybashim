package com.aybashim.backend.service;

import com.aybashim.backend.model.Transaction;
import com.aybashim.backend.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository repository;

    @Value("${app.exclude.keywords}")
    private String excludeKeywords;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    private List<Transaction> filterExcluded(List<Transaction> transactions) {
        List<String> keywords = Arrays.asList(excludeKeywords.split(","));
        return transactions.stream()
                .filter(tx -> {
                    System.out.println("Filtering: " + tx.getDescription() + " | " + tx.getType());
                    if (!tx.getType().equals("DEBIT")) return true;
                    return keywords.stream()
                            .noneMatch(kw -> tx.getDescription().toLowerCase()
                                    .contains(kw.trim().toLowerCase()));
                })
                .collect(Collectors.toList());
    }

    public List<Transaction> getAll() {
        return filterExcluded(repository.findAll());
    }

    public Transaction save(Transaction transaction) {
        return repository.save(transaction);
    }

    public List<Transaction> saveAll(List<Transaction> transactions) {
        System.out.println("excludeKeywords: " + excludeKeywords);
        List<Transaction> saved = new ArrayList<>();
        for (Transaction tx : transactions) {
            try {
                saved.add(repository.save(tx));
            } catch (Exception e) {
                System.out.println("Duplicate atlandı: " + tx.getDescription() + " - " + tx.getDate());
            }
        }
        return saved;
    }

    public List<Transaction> getByBank(String bankName) {
        return filterExcluded(repository.findByBankName(bankName));
    }

    public List<Transaction> getByType(String type) {
        return filterExcluded(repository.findByType(type));
    }

    public List<Transaction> getByDateRange(LocalDate start, LocalDate end) {
        return filterExcluded(repository.findByDateBetween(start, end));
    }

    public List<Transaction> getByBankAndType(String bankName, String type) {
        return filterExcluded(repository.findByBankNameAndType(bankName, type));
    }

    public List<Transaction> getByDescription(String keyword) {
        return filterExcluded(repository.findByDescriptionContainingIgnoreCase(keyword));
    }

    public Map<String, Map<String, BigDecimal>> getMonthlySummary() {
        Map<String, Map<String, BigDecimal>> summary = new LinkedHashMap<>();

        for (Object[] row : repository.getMonthlySummary(excludeKeywords.trim())) {
            String month = (String) row[0];
            String type  = (String) row[1];
            BigDecimal total = (BigDecimal) row[2];

            summary.computeIfAbsent(month, k -> new HashMap<>())
                    .put(type, total);
        }

        return summary;
    }
}