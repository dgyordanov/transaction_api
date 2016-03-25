package com.transaction.service;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {

    Transaction getbyId(Long transactionId);

    List<Long> getTransactionIdsByType(String type);

    BigDecimal calculateTransactionsSum(Long transactionId);

    void createOrUpdate(Transaction transaction);

}
