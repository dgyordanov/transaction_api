package com.transaction.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Transaction service which stores transactions in the memory. It uses a HashMap to store the transactions.
 * Every Transaction stores its children, so it will be easy to go through the all parent/child
 * transaction tree recursively.
 * <p>
 * The implementation is thread safe.
 */
@Service
public class InMemoryTransactionService implements TransactionService {

    private Validator validator;

    // Store transactions as a HashMap in order to achieve a fast access by key - O(1)
    // As the transactions are in parent/child structure, it doesn't make sense to store them in a tree because
    // it will not be sorted and either access by key and add an element is with O(n)
    private Map<Long, Transaction> transactionStorage;

    // Store the transaction ids organised by type in a separate data structure in order to achieve fast read.
    // The price is that the index should be updated on write.
    private Map<String, Set<Long>> transactionIdsByTypeIndex;

    @Autowired
    public InMemoryTransactionService(Validator validator) {
        this.validator = validator;
        transactionStorage = new ConcurrentHashMap<>();
        transactionIdsByTypeIndex = new ConcurrentHashMap<>();
    }

    @Override
    public Transaction getById(@NotNull Long transactionId) {
        if (transactionId == null) {
            throw new IllegalArgumentException("Transaction id could not be null");
        }

        Transaction result = transactionStorage.get(transactionId);

        if (result == null) {
            throw new IllegalArgumentException(String.format("No transaction for id %d found", transactionId));
        }

        return result;
    }

    @Override
    public Set<Long> getTransactionIdsByType(@NotNull String type) {
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("Transaction type could not be null or empty");
        }
        return transactionIdsByTypeIndex.get(type);
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

        transactionIdsByTypeIndex.putIfAbsent(transaction.getType(), Collections.synchronizedSet(new HashSet<>()));
        transactionIdsByTypeIndex.get(transaction.getType()).add(transaction.getId());

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
        if (transaction.getId().equals(transaction.getParentId())) {
            throw new IllegalArgumentException("Parent could not point to self");
        }
        if (transaction.getAmount().scale() > 2) {
            throw new IllegalArgumentException("Amount shouldn't have more then 2 digits after the '.' sign");
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

            if (!oldTransaction.getType().equals(transaction.getType())) {
                // Remove the old record from the index only if the type is changed
                transactionIdsByTypeIndex.get(oldTransaction.getType()).remove(oldTransaction.getId());
            }
        }
    }

}
