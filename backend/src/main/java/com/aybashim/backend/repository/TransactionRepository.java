package com.aybashim.backend.repository;

import com.aybashim.backend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByBankName(String bankName);

    List<Transaction> findByType(String type);

    List<Transaction> findByDateBetween(LocalDate start, LocalDate end);

    List<Transaction> findByBankNameAndType(String bankName, String type);

    List<Transaction> findByDescriptionContainingIgnoreCase(String keyword);
}