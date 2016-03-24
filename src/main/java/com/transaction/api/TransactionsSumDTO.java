package com.transaction.api;

import java.math.BigDecimal;

public class TransactionsSumDTO {

    private BigDecimal sum;

    public TransactionsSumDTO(BigDecimal sum) {
        this.sum = sum;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }
}
