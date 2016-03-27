package com.transaction.storage;

import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread safe in memory index. Stores IDs list per type in a HashMap.
 *
 * @author Diyan Yordanov
 */
@Repository
public class InMemoryTransactionIdsByTypeIndex implements TransactionIdsByTypeIndex {

    private Map<String, Set<Long>> transactionIdsByTypeMap = new ConcurrentHashMap<>();

    @Override
    public Set<Long> getIds(String type) {
        return transactionIdsByTypeMap.get(type);
    }

    @Override
    public void save(Transaction transaction) {
        transactionIdsByTypeMap.putIfAbsent(transaction.getType(), Collections.synchronizedSet(new HashSet<>()));
        transactionIdsByTypeMap.get(transaction.getType()).add(transaction.getId());
    }

    @Override
    public void removeId(Transaction transaction) {
        transactionIdsByTypeMap.get(transaction.getType()).remove(transaction.getId());
    }
}
