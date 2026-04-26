package com.aybashim.backend.repository;

import com.aybashim.backend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT FUNCTION('TO_CHAR', t.date, 'YYYY-MM') as month, " +
            "t.type, SUM(t.amount) as total " +
            "FROM Transaction t " +
            "WHERE NOT (t.type = 'DEBIT' AND LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "GROUP BY FUNCTION('TO_CHAR', t.date, 'YYYY-MM'), t.type " +
            "ORDER BY month")
    List<Object[]> getMonthlySummary(@Param("keyword") String keyword);
}