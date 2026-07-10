package com.aybashim.backend.service;

import com.aybashim.backend.dto.TransactionImportResponse;
import com.aybashim.backend.model.AppUser;
import com.aybashim.backend.model.MainCategory;
import com.aybashim.backend.model.SubCategory;
import com.aybashim.backend.model.Transaction;
import com.aybashim.backend.model.TransactionType;
import com.aybashim.backend.repository.TransactionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository repository;
    private final CategoryClassifier categoryClassifier;
    private final CurrentUser currentUser;

    public TransactionService(TransactionRepository repository, CategoryClassifier categoryClassifier, CurrentUser currentUser) {
        this.repository = repository;
        this.categoryClassifier = categoryClassifier;
        this.currentUser = currentUser;
    }

    private List<Transaction> filterExcluded(List<Transaction> transactions) {
        return transactions.stream()
                .filter(tx -> tx.getSubCategory() != SubCategory.SELF_TRANSFER)
                .collect(Collectors.toList());
    }

    public List<Transaction> getAll() {
        return filterExcluded(repository.findByUser(currentUser.get()));
    }

    public Transaction save(Transaction transaction) {
        AppUser user = currentUser.get();
        normalizeAndValidate(transaction);
        transaction.setUser(user);
        categoryClassifier.categorize(transaction, user.getName());
        return repository.save(transaction);
    }

    public TransactionImportResponse saveAll(List<Transaction> transactions) {
        AppUser user = currentUser.get();
        List<Transaction> saved = new ArrayList<>();
        int duplicateCount = 0;

        for (Transaction tx : transactions) {
            try {
                normalizeAndValidate(tx);
                tx.setUser(user);
                categoryClassifier.categorize(tx, user.getName());
                saved.add(repository.save(tx));
            } catch (DataIntegrityViolationException e) {
                duplicateCount++;
            }
        }

        return new TransactionImportResponse(transactions.size(), saved.size(), duplicateCount, saved);
    }

    public List<Transaction> recategorizeAll() {
        AppUser user = currentUser.get();
        List<Transaction> transactions = repository.findByUser(user);
        transactions.forEach(transaction -> categoryClassifier.recategorize(transaction, user.getName()));
        return repository.saveAll(transactions);
    }

    public List<Transaction> getByBank(String bankName) {
        return filterExcluded(repository.findByUserAndBankName(currentUser.get(), bankName));
    }

    public List<Transaction> getByType(String type) {
        return filterExcluded(repository.findByUserAndType(currentUser.get(), normalizeType(type)));
    }

    public List<Transaction> getByMainCategory(MainCategory mainCategory) {
        return filterExcluded(repository.findByUserAndMainCategory(currentUser.get(), mainCategory));
    }

    public List<Transaction> getBySubCategory(SubCategory subCategory) {
        if (subCategory == SubCategory.SELF_TRANSFER) {
            return getSelfTransfers();
        }

        return filterExcluded(repository.findByUserAndSubCategory(currentUser.get(), subCategory));
    }

    public List<Transaction> getSelfTransfers() {
        return repository.findByUserAndSubCategory(currentUser.get(), SubCategory.SELF_TRANSFER);
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
        return filterExcluded(repository.findByUserAndBankNameAndType(currentUser.get(), bankName, normalizeType(type)));
    }

    public List<Transaction> getByDescription(String keyword) {
        return filterExcluded(repository.findByUserAndDescriptionContainingIgnoreCase(currentUser.get(), keyword));
    }

    public Map<String, Map<String, BigDecimal>> getMonthlySummary() {
        AppUser user = currentUser.get();
        return toMonthlySummary(repository.getMonthlySummary(user, user.getName(), SubCategory.SELF_TRANSFER));
    }

    public Map<String, Map<String, BigDecimal>> getMonthlyMainCategorySummary() {
        AppUser user = currentUser.get();
        return toMonthlySummary(repository.getMonthlyMainCategorySummary(user, user.getName(), SubCategory.SELF_TRANSFER));
    }

    public Map<String, Map<String, BigDecimal>> getMonthlySubCategorySummary() {
        AppUser user = currentUser.get();
        return toMonthlySummary(repository.getMonthlySubCategorySummary(user, user.getName(), SubCategory.SELF_TRANSFER));
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

    private void normalizeAndValidate(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction is required");
        }
        if (transaction.getDate() == null) {
            throw new IllegalArgumentException("Transaction date is required");
        }
        if (transaction.getDescription() == null || transaction.getDescription().isBlank()) {
            throw new IllegalArgumentException("Transaction description is required");
        }
        if (transaction.getAmount() == null) {
            throw new IllegalArgumentException("Transaction amount is required");
        }
        if (transaction.getAmount().signum() < 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }

        transaction.setDescription(transaction.getDescription().trim());
        transaction.setType(normalizeType(transaction.getType()));
        if (transaction.getBankName() != null) {
            transaction.setBankName(transaction.getBankName().trim());
        }
        if (transaction.getSourceFile() != null) {
            transaction.setSourceFile(transaction.getSourceFile().trim());
        }
    }

    private String normalizeType(String type) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Transaction type is required");
        }

        String normalized = type.trim().toUpperCase(Locale.ROOT);
        try {
            return TransactionType.valueOf(normalized).name();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported transaction type: " + type);
        }
    }
}
