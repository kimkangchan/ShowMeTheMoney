package com.showmethemoney.category.domain;

public class Category {

    private Long id;
    private String code;
    private String codeNumber;
    private String name;
    private Integer type; // 0=EXPENSE, 1=INCOME

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getCodeNumber() { return codeNumber; }
    public String getName() { return name; }
    public Integer getType() { return type; }
}
