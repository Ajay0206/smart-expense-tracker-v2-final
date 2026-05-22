package com.expensepro.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "expenses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Expense {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 120)
    @NotBlank
    private String title;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(nullable = false, precision = 15, scale = 2)
    @NotNull @Positive
    private BigDecimal amount;

    @Column(name = "expense_date", nullable = false)
    @NotNull
    private LocalDate expenseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", length = 20)
    private PaymentMode paymentMode = PaymentMode.CASH;

    @Column(name = "is_recurring")
    private Boolean isRecurring = false;

    @Column(name = "receipt_url", length = 500)
    private String receiptUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum PaymentMode {
        CASH, UPI, CARD, NET_BANKING, WALLET, OTHER
    }
}
