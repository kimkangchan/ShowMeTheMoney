package com.showmethemoney.transaction.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Transaction {

    private Long id;
    private Long userId;
    private Long categoryId;
    private String categoryCode;
    private String categoryName;
    private Integer type; // 0=EXPENSE, 1=INCOME
    private BigDecimal amount;
    private String memo;
    private LocalDate transactionDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getCategoryId() { return categoryId; }
    public String getCategoryCode() { return categoryCode; }
    public String getCategoryName() { return categoryName; }
    public Integer getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getMemo() { return memo; }
    public LocalDate getTransactionDate() { return transactionDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }

    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public void setType(Integer type) { this.type = type; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setMemo(String memo) { this.memo = memo; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }
}
