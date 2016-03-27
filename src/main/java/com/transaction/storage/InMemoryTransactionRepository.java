package com.transaction.storage;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Concurrent safe in memory transaction storage which stores transactions in a HashMap
 *
 * @author Diyan Yordanov
 */
@Repository
public class InMemoryTransactionRepository implements TransactionRepository {

    // Store transactions as a HashMap in order to achieve a fast access by key - O(1)
    // As the transactions are in parent/child structure, it doesn't make sense to store them in a tree because
    // it will not be sorted and either access by key and add an element is with O(n)
    private Map<Long, Transaction> transactionStorage = new ConcurrentHashMap<>();

    @Override
    public void save(Transaction transaction) {
        transactionStorage.put(transaction.getId(), transaction);
    }

    @Override
    public Transaction read(Long id) {
        return transactionStorage.get(id);
    }

}
