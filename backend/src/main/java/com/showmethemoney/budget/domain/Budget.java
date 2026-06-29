package com.showmethemoney.budget.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Budget {

    private Long id;
    private Long userId;
    private String yearMonth; // "2026-06" 형태로 저장
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getYearMonth() { return yearMonth; }
    public BigDecimal getAmount() { return amount; }

    public void setUserId(Long userId) { this.userId = userId; }
    public void setYearMonth(String yearMonth) { this.yearMonth = yearMonth; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
