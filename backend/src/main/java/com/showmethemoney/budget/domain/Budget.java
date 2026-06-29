package com.showmethemoney.budget.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Budget {

    private Long uuid;
    private Long uuidUser;
    private String yearMonth; // "2026-06" 형태로 저장
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getUuid() { return uuid; }
    public Long getUuidUser() { return uuidUser; }
    public String getYearMonth() { return yearMonth; }
    public BigDecimal getAmount() { return amount; }

    public void setUuidUser(Long uuidUser) { this.uuidUser = uuidUser; }
    public void setYearMonth(String yearMonth) { this.yearMonth = yearMonth; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
