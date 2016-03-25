package com.transaction.service;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

public class Transaction {

    @NotNull
    private Long id;

    @NotNull
    private BigDecimal amount;

    @NotEmpty
    private String type;

    private Long parentId;

    private List<Transaction> children;

    public Transaction() {
        children = new LinkedList<>();
    }

    public Transaction(Long id, BigDecimal amount, String type, Long parentId) {
        this.id = id;
        this.amount = amount;
        this.type = type;
        this.parentId = parentId;
        children = new LinkedList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public List<Transaction> getChildren() {
        return children;
    }

    public void setChildren(List<Transaction> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", amount=" + amount +
                ", type='" + type + '\'' +
                ", parentId=" + parentId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transaction that = (Transaction) o;

        if (!id.equals(that.id)) return false;
        if (!amount.equals(that.amount)) return false;
        if (!type.equals(that.type)) return false;
        return parentId != null ? parentId.equals(that.parentId) : that.parentId == null;

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + amount.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + (parentId != null ? parentId.hashCode() : 0);
        return result;
    }

}
