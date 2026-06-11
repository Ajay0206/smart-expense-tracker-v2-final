package com.expensepro.service;

import com.expensepro.model.*;
import com.expensepro.repository.CategoryRepository;
import com.expensepro.repository.ExpenseRepository;
import com.expensepro.repository.UserRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;

@Service
public class ExpenseService {

    @Autowired UserRepository     userRepo;
    @Autowired ExpenseRepository  expRepo;
    @Autowired CategoryRepository catRepo;

    private User getUser(Long id) {
        return userRepo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Expense getExpense(Long id, Long userId) {
        return expRepo.findById(id)
                .filter(e -> e.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Expense not found"));
    }

    @Transactional
    public Expense create(Long userId, ExpenseReq r) {
        User user = getUser(userId);
        Category cat = catRepo.findById(r.categoryId).orElseThrow();
        return expRepo.save(Expense.builder()
                .user(user).category(cat)
                .title(r.title).note(r.note)
                .amount(r.amount).expenseDate(r.expenseDate)
                .paymentMode(r.paymentMode != null ? r.paymentMode : Expense.PaymentMode.CASH)
                .isRecurring(Boolean.TRUE.equals(r.isRecurring))
                .build());
    }

    @Transactional
    public Expense update(Long expId, Long userId, ExpenseReq r) {
        Expense e = getExpense(expId, userId);
        Category cat = catRepo.findById(r.categoryId).orElseThrow();
        e.setTitle(r.title); e.setNote(r.note); e.setAmount(r.amount);
        e.setExpenseDate(r.expenseDate); e.setCategory(cat);
        e.setPaymentMode(r.paymentMode); e.setIsRecurring(r.isRecurring);
        return expRepo.save(e);
    }

    @Transactional
    public void delete(Long expId, Long userId) {
        expRepo.delete(getExpense(expId, userId));
    }

    public Page<Expense> list(Long userId, int page, int size) {
        return expRepo.findByUserIdOrderByExpenseDateDescCreatedAtDesc(
                userId, PageRequest.of(page, size));
    }

    public Page<Expense> search(Long userId, Long catId,
                                 LocalDate from, LocalDate to,
                                 String q, int page, int size) {
        return expRepo.search(userId, catId, from, to, q, PageRequest.of(page, size));
    }

    public Map<String, Object> dashboard(Long userId) {
        User user = getUser(userId);
        LocalDate now = LocalDate.now();
        int y = now.getYear(), m = now.getMonthValue();

        BigDecimal thisMonth = expRepo.monthTotal(userId, y, m);
        BigDecimal lastMonth = expRepo.monthTotal(userId,
                m == 1 ? y - 1 : y, m == 1 ? 12 : m - 1);
        BigDecimal thisYear  = expRepo.yearTotal(userId, y);
        long txnCount        = expRepo.countByUserId(userId);

        BigDecimal limit = user.getMonthlyLimit();
        BigDecimal limitPct = limit.compareTo(BigDecimal.ZERO) > 0
                ? thisMonth.divide(limit, 4, RoundingMode.HALF_UP)
                          .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        List<Map<String, Object>> cats = new ArrayList<>();
        for (Object[] row : expRepo.categoryBreakdown(userId, y, m)) {
            BigDecimal amt = (BigDecimal) row[4];
            BigDecimal pct = thisMonth.compareTo(BigDecimal.ZERO) > 0
                    ? amt.divide(thisMonth, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;
            cats.add(Map.of(
                "id", row[0], "name", row[1], "icon", row[2],
                "color", row[3], "amount", amt,
                "count", row[5], "pct", pct
            ));
        }

        List<Map<String, Object>> trend = new ArrayList<>();
        for (Object[] row : expRepo.monthlyTrend(userId)) {
            int yr = (int)(Integer) row[0], mo = (int)(Integer) row[1];
            trend.add(Map.of(
                "year", yr, "month", mo,
                "label", Month.of(mo).getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + yr,
                "amount", row[2], "count", row[3]
            ));
        }
        if (trend.size() > 12) trend = trend.subList(0, 12);

        List<Map<String, Object>> daily = new ArrayList<>();
        for (Object[] row : expRepo.dailySpending(userId, y, m)) {
            daily.add(Map.of("day", row[0], "amount", row[1]));
        }

        List<Map<String, Object>> recent = new ArrayList<>();
        for (Expense e : expRepo.findTop6ByUserIdOrderByCreatedAtDesc(userId)) {
            recent.add(toMap(e));
        }

        double changePct = 0;
        String changeDir = "none";
        if (lastMonth.compareTo(BigDecimal.ZERO) > 0) {
            changePct = thisMonth.subtract(lastMonth)
                    .divide(lastMonth, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
            changeDir = changePct >= 0 ? "up" : "down";
        }

        List<Map<String, Object>> sortedTrend = new ArrayList<>(trend);
        sortedTrend.sort(Comparator.comparingInt((Map<String, Object> t) ->
                (int) t.get("year") * 100 + (int) t.get("month")));

        Map<String, Object> dash = new HashMap<>();
        dash.put("thisMonth",    thisMonth);
        dash.put("lastMonth",    lastMonth);
        dash.put("thisYear",     thisYear);
        dash.put("txnCount",     txnCount);
        dash.put("monthlyLimit", limit);
        dash.put("limitPct",     limitPct);
        dash.put("changePct",    Math.abs(changePct));
        dash.put("changeDir",    changeDir);
        dash.put("categories",   cats);
        dash.put("trend",        sortedTrend);
        dash.put("daily",        daily);
        dash.put("recent",       recent);
        dash.put("currency",     user.getCurrency());
        return dash;
    }

    public Map<String, Object> toMap(Expense e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",            e.getId());
        m.put("title",         e.getTitle());
        m.put("note",          e.getNote());
        m.put("amount",        e.getAmount());
        m.put("expenseDate",   e.getExpenseDate());
        m.put("paymentMode",   e.getPaymentMode());
        m.put("isRecurring",   e.getIsRecurring());
        m.put("categoryId",    e.getCategory().getId());
        m.put("categoryName",  e.getCategory().getName());
        m.put("categoryIcon",  e.getCategory().getIcon());
        m.put("categoryColor", e.getCategory().getColor());
        m.put("createdAt",     e.getCreatedAt());
        return m;
    }

    @Data
    public static class ExpenseReq {
        public String title, note;
        public BigDecimal amount;
        public LocalDate expenseDate;
        public Long categoryId;
        public Expense.PaymentMode paymentMode;
        public Boolean isRecurring;
    }
}
