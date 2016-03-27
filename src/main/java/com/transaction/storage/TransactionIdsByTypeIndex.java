package com.transaction.storage;

import java.util.Set;

/**
 * Transaction IDs per type index. Used for faster read of transaction IDs by type.
 * Store the transaction ids organised by type in a separate data structure in order to achieve fast read.
 * The price is that the index should be updated on write.
 *
 * @author Diyan Yordanov
 */
public interface TransactionIdsByTypeIndex {

    /**
     * Get transaction IDs for a given type
     *
     * @param type - the transaction type for which the IDs should be read
     * @return IDs for all transactions for the given type
     */
    Set<Long> getIds(String type);

    /**
     * Save transaction ID in the index for a transaction
     *
     * @param transaction - the new transaction which should be added in the index
     */
    void save(Transaction transaction);

    /**
     * Remove a transaction from the index
     *
     * @param transaction - the transaction to be removed
     */
    void removeId(Transaction transaction);
}
