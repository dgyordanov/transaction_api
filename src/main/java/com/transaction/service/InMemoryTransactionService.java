package com.transaction.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryTransactionService implements TransactionService {

    private Validator validator;

    private Map<Long, Transaction> transactionStorage = new ConcurrentHashMap<>();

    @Autowired
    public InMemoryTransactionService(Validator validator) {
        this.validator = validator;
    }

    @Override
    public Transaction getbyId(@NotNull Long transactionId) {
        return transactionStorage.get(transactionId);
    }

    @Override
    public List<Long> getTransactionIdsByType(String type) {
        return null;
    }

    @Override
    public BigDecimal calculateTransactionsSum(@NotNull Long transactionId) {
        if (transactionId == null) {
            throw new IllegalArgumentException("Transaciton Id is null");
        }

        Transaction transaction = transactionStorage.get(transactionId);
        if (transaction == null) {
            throw new IllegalArgumentException(String.format("Transaction for id %d not found", transactionId));
        }

        // Recursive invokation for all children
        BigDecimal childrenSum = transaction.getChildren().stream()
                .map(childTransaction -> calculateTransactionsSum(childTransaction.getId()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return transaction.getAmount().add(childrenSum);
    }

    @Override
    public void createOrUpdate(@NotNull Transaction transaction) {
        validateCreateUpdateInput(transaction);
        processOldTransaction(transaction);

        transactionStorage.put(transaction.getId(), transaction);

        if (transaction.getParentId() != null) {
            Transaction parent = transactionStorage.get(transaction.getParentId());
            parent.getChildren().add(transaction);
        }
    }

    private void validateCreateUpdateInput(@NotNull Transaction transaction) {
        Set<ConstraintViolation<Transaction>> errors = validator.validate(transaction);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.format("Validation error: %s", errors));
        }
        if (transaction.getParentId() != null && !transactionStorage.containsKey(transaction.getParentId())) {
            throw new ParentNotFoundException(String.format("Invalid parentId: %s", transaction.getParentId()));
        }
        if (transaction.getAmount().equals(BigDecimal.valueOf(0L))) {
            throw new IllegalArgumentException("Transaction amount can not be null");
        }
    }

    private void processOldTransaction(@NotNull Transaction transaction) {
        Transaction oldTransaction = transactionStorage.get(transaction.getId());
        if (oldTransaction != null) {
            if (oldTransaction.getParentId() != null) {
                // If the old transaction has a parent, remove it from its children
                Transaction oldTransactionPartent = transactionStorage.get(oldTransaction.getParentId());
                oldTransactionPartent.getChildren().remove(oldTransaction);
            }
            // Apply the children map to the new transaction
            transaction.setChildren(oldTransaction.getChildren());
        }
    }

}
