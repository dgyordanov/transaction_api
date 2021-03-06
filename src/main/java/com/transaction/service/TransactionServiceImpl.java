package com.transaction.service;

import com.transaction.storage.Transaction;
import com.transaction.storage.TransactionIdsByTypeIndex;
import com.transaction.storage.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;

/**
 * Transaction service which stores transactions in the memory. It uses a HashMap to store the transactions.
 * Every Transaction stores its children, so it will be easy to go through the all parent/child
 * transaction tree recursively.
 * <p>
 * The implementation is thread safe.
 * <p>
 *
 * @author Diyan Yordanov
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    private Validator validator;

    private TransactionRepository transactionRepository;

    private TransactionIdsByTypeIndex transactionIdsByTypeIndex;

    @Autowired
    public TransactionServiceImpl(Validator validator, TransactionRepository transactionRepository,
                                  TransactionIdsByTypeIndex transactionIdsByTypeIndex) {
        this.validator = validator;
        this.transactionRepository = transactionRepository;
        this.transactionIdsByTypeIndex = transactionIdsByTypeIndex;
    }

    @Override
    public Transaction getById(@NotNull Long transactionId) {
        if (transactionId == null) {
            throw new IllegalArgumentException("Transaction id could not be null");
        }

        Transaction result = transactionRepository.read(transactionId);

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
        Set<Long> result = transactionIdsByTypeIndex.getIds(type);
        return result != null ? result : Collections.EMPTY_SET;
    }

    @Override
    public BigDecimal calculateTransactionsSum(@NotNull Long transactionId) {
        if (transactionId == null) {
            throw new IllegalArgumentException("Transaction Id is null");
        }

        Transaction transaction = transactionRepository.read(transactionId);
        if (transaction == null) {
            throw new IllegalArgumentException(String.format("Transaction for id %d not found", transactionId));
        }

        // Recursive invocation for all children
        BigDecimal childrenSum = transaction.getChildren().stream()
                .map(childTransaction -> calculateTransactionsSum(childTransaction.getId()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return transaction.getAmount().add(childrenSum);
    }

    @Override
    public void createOrUpdate(@NotNull Transaction transaction) {
        validateCreateUpdateInput(transaction);
        processOldTransaction(transaction);

        transactionRepository.save(transaction);
        transactionIdsByTypeIndex.save(transaction);

        if (transaction.getParentId() != null) {
            Transaction parent = transactionRepository.read(transaction.getParentId());
            parent.getChildren().add(transaction);
        }
    }

    private void validateCreateUpdateInput(@NotNull Transaction transaction) {
        Set<ConstraintViolation<Transaction>> errors = validator.validate(transaction);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.format("Validation error(s): %s",
                    errors.stream()
                            .map(error -> String.format("'%s' %s", error.getPropertyPath().toString(), error.getMessage()))
                            .reduce("", (s1, s2) -> s1 + s2 + ";")));
        }
        if (transaction.getParentId() != null && transactionRepository.read(transaction.getParentId()) == null) {
            throw new ParentNotFoundException(String.format("Invalid parent id: %s", transaction.getParentId()));
        }
        if (transaction.getId().equals(transaction.getParentId())) {
            throw new IllegalArgumentException("Parent could not point to self");
        }
        if (transaction.getAmount().scale() > 2) {
            throw new IllegalArgumentException("Amount shouldn't have more then 2 digits after the '.' sign");
        }

    }

    private void processOldTransaction(@NotNull Transaction transaction) {
        Transaction oldTransaction = transactionRepository.read(transaction.getId());
        if (oldTransaction != null) {
            if (oldTransaction.getParentId() != null) {
                // If the old transaction has a parent, removeId it from its children
                Transaction oldTransactionPartent = transactionRepository.read(oldTransaction.getParentId());
                oldTransactionPartent.getChildren().remove(oldTransaction);
            }
            // Apply the children map to the new transaction
            transaction.setChildren(oldTransaction.getChildren());

            if (!oldTransaction.getType().equals(transaction.getType())) {
                // Remove the old record from the index only if the type is changed
                transactionIdsByTypeIndex.removeId(oldTransaction);
            }
        }

    }

}
