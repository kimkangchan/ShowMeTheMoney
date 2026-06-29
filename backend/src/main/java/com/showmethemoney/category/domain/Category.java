package com.showmethemoney.category.domain;

public class Category {

    private Long uuid;
    private String code;
    private String codeNumber;
    private String name;
    private Integer type; // 0=EXPENSE, 1=INCOME

    public Long getUuid() { return uuid; }
    public String getCode() { return code; }
    public String getCodeNumber() { return codeNumber; }
    public String getName() { return name; }
    public Integer getType() { return type; }
}
