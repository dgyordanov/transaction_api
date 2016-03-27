package com.transaction.service;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * Service which stores and manages transactions
 *
 * @author Diyan Yordanov
 */
public interface TransactionService {

    /**
     * Read transaction by ID
     *
     * @param transactionId - the ID of the transaction which needs to be read
     * @return - transaction with the requested id
     * @throws IllegalArgumentException in case of invalid or null transactionId
     */
    Transaction getById(Long transactionId);

    /**
     * Return a collection of transaction IDs for a given type
     *
     * @param type - the transaction type for which the IDs should be retrieved
     * @return - list of transactions IDs for the given type
     * @throws IllegalArgumentException in case of invalid type
     */
    Collection<Long> getTransactionIdsByType(String type);

    /**
     * Calculates the sum of all transactions that are transitively linked by their parentId to a given transaction
     *
     * @param transactionId the id of the top level transaction
     * @return Sum of all transactions that are transitively linked
     * @throws IllegalArgumentException in case of invalid transaction ID
     */
    BigDecimal calculateTransactionsSum(Long transactionId);

    /**
     * Create or update a transaction by ID. If a transaction with this ID exists, it will be updated
     * otherwise a new transaction will be created
     *
     * @param transaction - the transaction to be created or updated
     * @throws IllegalArgumentException in case of invalid transaction bean state
     * @throws ParentNotFoundException  in case of invalid parent ID
     */
    void createOrUpdate(Transaction transaction);

}
