package com.showmethemoney.recurringitem.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RecurringItem {

    private Long id;
    private Long userId;
    private Long categoryId;
    private String categoryCode;
    private String categoryName;
    private Integer type; // 0=EXPENSE, 1=INCOME
    private String name;
    private BigDecimal amount;
    private Integer billingDay;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getCategoryId() { return categoryId; }
    public String getCategoryCode() { return categoryCode; }
    public String getCategoryName() { return categoryName; }
    public Integer getType() { return type; }
    public String getName() { return name; }
    public BigDecimal getAmount() { return amount; }
    public Integer getBillingDay() { return billingDay; }
    public Boolean getIsActive() { return isActive; }
    public LocalDateTime getDeletedAt() { return deletedAt; }

    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public void setType(Integer type) { this.type = type; }
    public void setName(String name) { this.name = name; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setBillingDay(Integer billingDay) { this.billingDay = billingDay; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
