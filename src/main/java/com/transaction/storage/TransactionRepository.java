package com.transaction.storage;

/**
 * Store transactions
 *
 * @author Diyan Yordanov
 */
public interface TransactionRepository {

    /**
     * Create or update a transaction
     *
     * @param transaction - the transaction to be created or updated
     */
    void save(Transaction transaction);

    /**
     * Read transaciton by ID
     *
     * @param id - the ID of the transaction to be read
     * @return - a transaction if found or null otherwise
     */
    Transaction read(Long id);
}
