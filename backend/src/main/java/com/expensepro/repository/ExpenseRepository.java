package com.expensepro.repository;

import com.expensepro.model.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Page<Expense> findByUserIdOrderByExpenseDateDescCreatedAtDesc(Long userId, Pageable p);

    List<Expense> findByUserIdAndExpenseDateBetweenOrderByExpenseDateDesc(
            Long userId, LocalDate from, LocalDate to);

    @Query("SELECT COALESCE(SUM(e.amount),0) FROM Expense e " +
           "WHERE e.user.id=:uid AND YEAR(e.expenseDate)=:y AND MONTH(e.expenseDate)=:m")
    BigDecimal monthTotal(@Param("uid") Long uid, @Param("y") int y, @Param("m") int m);

    @Query("SELECT COALESCE(SUM(e.amount),0) FROM Expense e " +
           "WHERE e.user.id=:uid AND YEAR(e.expenseDate)=:y")
    BigDecimal yearTotal(@Param("uid") Long uid, @Param("y") int y);

    @Query("SELECT e.category.id, e.category.name, e.category.icon, e.category.color, " +
           "SUM(e.amount), COUNT(e) " +
           "FROM Expense e " +
           "WHERE e.user.id=:uid AND YEAR(e.expenseDate)=:y AND MONTH(e.expenseDate)=:m " +
           "GROUP BY e.category.id, e.category.name, e.category.icon, e.category.color " +
           "ORDER BY SUM(e.amount) DESC")
    List<Object[]> categoryBreakdown(@Param("uid") Long uid, @Param("y") int y, @Param("m") int m);

    @Query("SELECT YEAR(e.expenseDate), MONTH(e.expenseDate), SUM(e.amount), COUNT(e) " +
           "FROM Expense e WHERE e.user.id=:uid " +
           "GROUP BY YEAR(e.expenseDate), MONTH(e.expenseDate) " +
           "ORDER BY YEAR(e.expenseDate) DESC, MONTH(e.expenseDate) DESC")
    List<Object[]> monthlyTrend(@Param("uid") Long uid);

    @Query("SELECT DAY(e.expenseDate), SUM(e.amount) " +
           "FROM Expense e " +
           "WHERE e.user.id=:uid AND YEAR(e.expenseDate)=:y AND MONTH(e.expenseDate)=:m " +
           "GROUP BY DAY(e.expenseDate) ORDER BY DAY(e.expenseDate)")
    List<Object[]> dailySpending(@Param("uid") Long uid, @Param("y") int y, @Param("m") int m);

    @Query("SELECT e FROM Expense e WHERE e.user.id=:uid " +
           "AND (:cid IS NULL OR e.category.id=:cid) " +
           "AND (:from IS NULL OR e.expenseDate>=:from) " +
           "AND (:to   IS NULL OR e.expenseDate<=:to) " +
           "AND (:q    IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%',:q,'%'))) " +
           "ORDER BY e.expenseDate DESC")
    Page<Expense> search(@Param("uid") Long uid, @Param("cid") Long cid,
                         @Param("from") LocalDate from, @Param("to") LocalDate to,
                         @Param("q") String q, Pageable p);

    List<Expense> findTop6ByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserId(Long userId);
}
