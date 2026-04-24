package com.aybashim.backend.service;

import com.aybashim.backend.model.Transaction;
import com.aybashim.backend.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    public List<Transaction> getAll() {
        return repository.findAll();
    }

    public Transaction save(Transaction transaction) {
        return repository.save(transaction);
    }

    public List<Transaction> saveAll(List<Transaction> transactions) {
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
        return repository.findByBankName(bankName);
    }

    public List<Transaction> getByType(String type) {
        return repository.findByType(type);
    }

    public List<Transaction> getByDateRange(LocalDate start, LocalDate end) {
        return repository.findByDateBetween(start, end);
    }

    public List<Transaction> getByBankAndType(String bankName, String type) {
        return repository.findByBankNameAndType(bankName, type);
    }

    public List<Transaction> getByDescription(String keyword) {
        return repository.findByDescriptionContainingIgnoreCase(keyword);
    }
}