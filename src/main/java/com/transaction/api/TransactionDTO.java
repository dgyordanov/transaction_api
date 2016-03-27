package com.transaction.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Transaction DTO which represents a transaction in the REST service layer.
 * <p>
 *
 * @author Diyan Yordanov
 */
public class TransactionDTO {

    private BigDecimal amount;

    private String type;

    @JsonProperty("parent_id")
    private Long parentId;

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
}
