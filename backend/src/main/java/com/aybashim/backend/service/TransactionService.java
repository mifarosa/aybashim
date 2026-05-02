package com.aybashim.backend.service;

import com.aybashim.backend.model.Transaction;
import com.aybashim.backend.model.MainCategory;
import com.aybashim.backend.model.SubCategory;
import com.aybashim.backend.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository repository;
    private final CategoryClassifier categoryClassifier;

    @Value("${app.exclude.keywords}")
    private String excludeKeywords;

    public TransactionService(TransactionRepository repository, CategoryClassifier categoryClassifier) {
        this.repository = repository;
        this.categoryClassifier = categoryClassifier;
    }

    private List<Transaction> filterExcluded(List<Transaction> transactions) {
        List<String> keywords = Arrays.asList(excludeKeywords.split(","));
        return transactions.stream()
                .filter(tx -> {
                    System.out.println("Filtering: " + tx.getDescription() + " | " + tx.getType());
                    if (tx.getSubCategory() == SubCategory.SELF_TRANSFER) return false;
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
        categoryClassifier.categorize(transaction);
        return repository.save(transaction);
    }

    public List<Transaction> saveAll(List<Transaction> transactions) {
        System.out.println("excludeKeywords: " + excludeKeywords);
        List<Transaction> saved = new ArrayList<>();
        for (Transaction tx : transactions) {
            try {
                categoryClassifier.categorize(tx);
                saved.add(repository.save(tx));
            } catch (Exception e) {
                System.out.println("Duplicate atlandı: " + tx.getDescription() + " - " + tx.getDate());
            }
        }
        return saved;
    }

    public List<Transaction> recategorizeAll() {
        List<Transaction> transactions = repository.findAll();
        transactions.forEach(categoryClassifier::recategorize);
        return repository.saveAll(transactions);
    }

    public List<Transaction> getByBank(String bankName) {
        return filterExcluded(repository.findByBankName(bankName));
    }

    public List<Transaction> getByType(String type) {
        return filterExcluded(repository.findByType(type));
    }

    public List<Transaction> getByMainCategory(MainCategory mainCategory) {
        return filterExcluded(repository.findByMainCategory(mainCategory));
    }

    public List<Transaction> getBySubCategory(SubCategory subCategory) {
        if (subCategory == SubCategory.SELF_TRANSFER) {
            return repository.findBySubCategory(subCategory);
        }

        return filterExcluded(repository.findBySubCategory(subCategory));
    }

    public List<Transaction> getByDateRange(LocalDate start, LocalDate end) {
        return filterExcluded(repository.findByDateBetween(start, end));
    }

    public List<Transaction> getByMonthAndMainCategory(YearMonth month, MainCategory mainCategory) {
        return filterExcluded(repository.findByDateBetweenAndMainCategory(
                month.atDay(1),
                month.atEndOfMonth(),
                mainCategory
        ));
    }

    public List<Transaction> getByMonthAndSubCategory(YearMonth month, SubCategory subCategory) {
        if (subCategory == SubCategory.SELF_TRANSFER) {
            return repository.findByDateBetweenAndSubCategory(
                    month.atDay(1),
                    month.atEndOfMonth(),
                    subCategory
            );
        }

        return filterExcluded(repository.findByDateBetweenAndSubCategory(
                month.atDay(1),
                month.atEndOfMonth(),
                subCategory
        ));
    }

    public List<Transaction> getByBankAndType(String bankName, String type) {
        return filterExcluded(repository.findByBankNameAndType(bankName, type));
    }

    public List<Transaction> getByDescription(String keyword) {
        return filterExcluded(repository.findByDescriptionContainingIgnoreCase(keyword));
    }

    public Map<String, Map<String, BigDecimal>> getMonthlySummary() {
        return toMonthlySummary(repository.getMonthlySummary(excludeKeywords.trim(), SubCategory.SELF_TRANSFER));
    }

    public Map<String, Map<String, BigDecimal>> getMonthlyMainCategorySummary() {
        return toMonthlySummary(repository.getMonthlyMainCategorySummary(excludeKeywords.trim(), SubCategory.SELF_TRANSFER));
    }

    public Map<String, Map<String, BigDecimal>> getMonthlySubCategorySummary() {
        return toMonthlySummary(repository.getMonthlySubCategorySummary(excludeKeywords.trim(), SubCategory.SELF_TRANSFER));
    }

    private Map<String, Map<String, BigDecimal>> toMonthlySummary(List<Object[]> rows) {
        Map<String, Map<String, BigDecimal>> summary = new LinkedHashMap<>();

        for (Object[] row : rows) {
            String month = (String) row[0];
            String category = row[1].toString();
            BigDecimal total = (BigDecimal) row[2];

            summary.computeIfAbsent(month, k -> new LinkedHashMap<>())
                    .put(category, total);
        }

        return summary;
    }
}
