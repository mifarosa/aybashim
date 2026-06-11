package com.aybashim.backend.repository;

import com.aybashim.backend.model.MainCategory;
import com.aybashim.backend.model.SubCategory;
import com.aybashim.backend.model.Transaction;
import com.aybashim.backend.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUser(AppUser user);

    List<Transaction> findByUserIsNull();

    List<Transaction> findByUserAndBankName(AppUser user, String bankName);

    List<Transaction> findByUserAndType(AppUser user, String type);

    List<Transaction> findByUserAndMainCategory(AppUser user, MainCategory mainCategory);

    List<Transaction> findByUserAndSubCategory(AppUser user, SubCategory subCategory);

    List<Transaction> findByUserAndDateBetween(AppUser user, LocalDate start, LocalDate end);

    List<Transaction> findByUserAndDateBetweenAndMainCategory(AppUser user, LocalDate start, LocalDate end, MainCategory mainCategory);

    List<Transaction> findByUserAndDateBetweenAndSubCategory(AppUser user, LocalDate start, LocalDate end, SubCategory subCategory);

    List<Transaction> findByUserAndBankNameAndType(AppUser user, String bankName, String type);

    List<Transaction> findByUserAndDescriptionContainingIgnoreCase(AppUser user, String keyword);

    @Query("SELECT FUNCTION('TO_CHAR', t.date, 'YYYY-MM') as month, " +
            "t.type, SUM(t.amount) as total " +
            "FROM Transaction t " +
            "WHERE t.user = :user " +
            "AND (t.subCategory IS NULL OR t.subCategory <> :ignoredSubCategory) " +
            "AND NOT (t.type = 'DEBIT' AND LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "GROUP BY FUNCTION('TO_CHAR', t.date, 'YYYY-MM'), t.type " +
            "ORDER BY month")
    List<Object[]> getMonthlySummary(
            @Param("user") AppUser user,
            @Param("keyword") String keyword,
            @Param("ignoredSubCategory") SubCategory ignoredSubCategory);

    @Query("SELECT FUNCTION('TO_CHAR', t.date, 'YYYY-MM') as month, " +
            "t.mainCategory, SUM(t.amount) as total " +
            "FROM Transaction t " +
            "WHERE t.user = :user " +
            "AND t.mainCategory IS NOT NULL " +
            "AND (t.subCategory IS NULL OR t.subCategory <> :ignoredSubCategory) " +
            "AND NOT (t.type = 'DEBIT' AND LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "GROUP BY FUNCTION('TO_CHAR', t.date, 'YYYY-MM'), t.mainCategory " +
            "ORDER BY month")
    List<Object[]> getMonthlyMainCategorySummary(
            @Param("user") AppUser user,
            @Param("keyword") String keyword,
            @Param("ignoredSubCategory") SubCategory ignoredSubCategory);

    @Query("SELECT FUNCTION('TO_CHAR', t.date, 'YYYY-MM') as month, " +
            "t.subCategory, SUM(t.amount) as total " +
            "FROM Transaction t " +
            "WHERE t.user = :user " +
            "AND t.subCategory IS NOT NULL " +
            "AND t.subCategory <> :ignoredSubCategory " +
            "AND NOT (t.type = 'DEBIT' AND LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "GROUP BY FUNCTION('TO_CHAR', t.date, 'YYYY-MM'), t.subCategory " +
            "ORDER BY month")
    List<Object[]> getMonthlySubCategorySummary(
            @Param("user") AppUser user,
            @Param("keyword") String keyword,
            @Param("ignoredSubCategory") SubCategory ignoredSubCategory);
}
