package com.transaction.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
public class InMemoryTransactionService implements TransactionService {

    @Autowired
    private Validator validator;

    @Override
    public Transaction getbyId(Long transactionId) {
        return null;
    }

    @Override
    public List<Long> getTransactionIdsByType(String type) {
        return null;
    }

    @Override
    public BigDecimal calculateTransactionsSum(Long transactionId) {
        return null;
    }

    @Override
    public void createOrUpdate(@NotNull Transaction transaction) {
        Set<ConstraintViolation<Transaction>> errors = validator.validate(transaction);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Validation error");
        }
    }

}
