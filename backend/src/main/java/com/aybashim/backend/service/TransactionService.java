package com.aybashim.backend.service;

import com.aybashim.backend.model.AppUser;
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
    private final CurrentUser currentUser;

    @Value("${app.exclude.keywords}")
    private String excludeKeywords;

    public TransactionService(TransactionRepository repository, CategoryClassifier categoryClassifier, CurrentUser currentUser) {
        this.repository = repository;
        this.categoryClassifier = categoryClassifier;
        this.currentUser = currentUser;
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
        return filterExcluded(repository.findByUser(currentUser.get()));
    }

    public Transaction save(Transaction transaction) {
        transaction.setUser(currentUser.get());
        categoryClassifier.categorize(transaction);
        return repository.save(transaction);
    }

    public List<Transaction> saveAll(List<Transaction> transactions) {
        System.out.println("excludeKeywords: " + excludeKeywords);
        List<Transaction> saved = new ArrayList<>();
        for (Transaction tx : transactions) {
            try {
                tx.setUser(currentUser.get());
                categoryClassifier.categorize(tx);
                saved.add(repository.save(tx));
            } catch (Exception e) {
                System.out.println("Duplicate atlandı: " + tx.getDescription() + " - " + tx.getDate());
            }
        }
        return saved;
    }

    public List<Transaction> recategorizeAll() {
        List<Transaction> transactions = repository.findByUser(currentUser.get());
        transactions.forEach(categoryClassifier::recategorize);
        return repository.saveAll(transactions);
    }

    public List<Transaction> getByBank(String bankName) {
        return filterExcluded(repository.findByUserAndBankName(currentUser.get(), bankName));
    }

    public List<Transaction> getByType(String type) {
        return filterExcluded(repository.findByUserAndType(currentUser.get(), type));
    }

    public List<Transaction> getByMainCategory(MainCategory mainCategory) {
        return filterExcluded(repository.findByUserAndMainCategory(currentUser.get(), mainCategory));
    }

    public List<Transaction> getBySubCategory(SubCategory subCategory) {
        if (subCategory == SubCategory.SELF_TRANSFER) {
            return repository.findByUserAndSubCategory(currentUser.get(), subCategory);
        }

        return filterExcluded(repository.findByUserAndSubCategory(currentUser.get(), subCategory));
    }

    public List<Transaction> getByDateRange(LocalDate start, LocalDate end) {
        return filterExcluded(repository.findByUserAndDateBetween(currentUser.get(), start, end));
    }

    public List<Transaction> getByMonthAndMainCategory(YearMonth month, MainCategory mainCategory) {
        return filterExcluded(repository.findByUserAndDateBetweenAndMainCategory(
                currentUser.get(),
                month.atDay(1),
                month.atEndOfMonth(),
                mainCategory
        ));
    }

    public List<Transaction> getByMonthAndSubCategory(YearMonth month, SubCategory subCategory) {
        if (subCategory == SubCategory.SELF_TRANSFER) {
            return repository.findByUserAndDateBetweenAndSubCategory(
                    currentUser.get(),
                    month.atDay(1),
                    month.atEndOfMonth(),
                    subCategory
            );
        }

        return filterExcluded(repository.findByUserAndDateBetweenAndSubCategory(
                currentUser.get(),
                month.atDay(1),
                month.atEndOfMonth(),
                subCategory
        ));
    }

    public List<Transaction> getByBankAndType(String bankName, String type) {
        return filterExcluded(repository.findByUserAndBankNameAndType(currentUser.get(), bankName, type));
    }

    public List<Transaction> getByDescription(String keyword) {
        return filterExcluded(repository.findByUserAndDescriptionContainingIgnoreCase(currentUser.get(), keyword));
    }

    public Map<String, Map<String, BigDecimal>> getMonthlySummary() {
        return toMonthlySummary(repository.getMonthlySummary(currentUser.get(), excludeKeywords.trim(), SubCategory.SELF_TRANSFER));
    }

    public Map<String, Map<String, BigDecimal>> getMonthlyMainCategorySummary() {
        return toMonthlySummary(repository.getMonthlyMainCategorySummary(currentUser.get(), excludeKeywords.trim(), SubCategory.SELF_TRANSFER));
    }

    public Map<String, Map<String, BigDecimal>> getMonthlySubCategorySummary() {
        return toMonthlySummary(repository.getMonthlySubCategorySummary(currentUser.get(), excludeKeywords.trim(), SubCategory.SELF_TRANSFER));
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
