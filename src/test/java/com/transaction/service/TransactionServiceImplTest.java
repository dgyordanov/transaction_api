package com.transaction.service;

import com.transaction.storage.InMemoryTransactionIdsByTypeIndex;
import com.transaction.storage.InMemoryTransactionRepository;
import com.transaction.storage.Transaction;
import org.junit.Before;
import org.junit.Test;

import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class TransactionServiceImplTest {

    private TransactionService transactionService;

    @Before
    public void setup() {
        // Create a new instance of the service in order to start with empty transaction store
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        transactionService = new TransactionServiceImpl(factory.getValidator(), new InMemoryTransactionRepository(),
                new InMemoryTransactionIdsByTypeIndex());
    }

    @Test
    public void testCreateWithoutParent() {
        Transaction transaction = new Transaction(234523L, new BigDecimal("23422.55"), "test type", null);
        transactionService.createOrUpdate(transaction);

        Transaction storedTransaction = transactionService.getById(234523L);
        assertThat(storedTransaction, is(equalTo(transaction)));
    }

    @Test
    public void testCreateWithParent() {
        Transaction parentTransaction = new Transaction(5187623L, new BigDecimal("542.32"), "test type", null);
        transactionService.createOrUpdate(parentTransaction);

        Transaction childTransaction = new Transaction(7652321L, new BigDecimal("22.35"), "test type", 5187623L);
        transactionService.createOrUpdate(childTransaction);

        Transaction storedParentTransaction = transactionService.getById(5187623L);
        assertThat(storedParentTransaction.getChildren().size(), is(equalTo(1)));
    }

    @Test
    public void testUpdate() {
        Transaction transaction = new Transaction(5187623L, new BigDecimal("542.32"), "test type", null);
        transactionService.createOrUpdate(transaction);

        Transaction updateTransaction = new Transaction(5187623L, new BigDecimal("198.11"), "test type2", null);
        transactionService.createOrUpdate(updateTransaction);

        Transaction storedTransaction = transactionService.getById(5187623L);
        assertThat(storedTransaction, is(equalTo(updateTransaction)));
    }

    @Test
    public void testUpdateParent() {
        Transaction parentTransaction1 = new Transaction(65324523L, new BigDecimal("542.32"), "test type", null);
        transactionService.createOrUpdate(parentTransaction1);

        Transaction parentTransaction2 = new Transaction(67621421L, new BigDecimal("542.32"), "test type", null);
        transactionService.createOrUpdate(parentTransaction2);

        Transaction childTransaction = new Transaction(5187623L, new BigDecimal("198.11"), "test type2", 65324523L);
        transactionService.createOrUpdate(childTransaction);

        Transaction updateChildTransaction = new Transaction(5187623L, new BigDecimal("198.11"), "test type2", 67621421L);
        transactionService.createOrUpdate(updateChildTransaction);

        Transaction storedParentTransaction1 = transactionService.getById(65324523L);
        Transaction storedParentTransaction2 = transactionService.getById(67621421L);

        assertThat(storedParentTransaction1.getChildren().size(), is(equalTo(0)));
        assertThat(storedParentTransaction2.getChildren().size(), is(equalTo(1)));
    }

    @Test
    public void testUpdateSameParentTwice() {
        Transaction parentTransaction = new Transaction(342512L, new BigDecimal("542.32"), "test type", null);
        transactionService.createOrUpdate(parentTransaction);

        Transaction childTransaction = new Transaction(2539826L, new BigDecimal("198.11"), "test type2", 342512L);
        transactionService.createOrUpdate(childTransaction);

        Transaction updateChildTransaction = new Transaction(2539826L, new BigDecimal("198.11"), "test type2", 342512L);
        transactionService.createOrUpdate(updateChildTransaction);

        Transaction storedParentTransaction = transactionService.getById(342512L);

        assertThat(storedParentTransaction.getChildren().size(), is(equalTo(1)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnableToPointSelfAsParent() {
        Transaction parentTransaction = new Transaction(23768L, new BigDecimal("542.32"), "test type", null);
        transactionService.createOrUpdate(parentTransaction);

        Transaction childTransaction = new Transaction(23768L, new BigDecimal("198.11"), "test type2", 23768L);
        transactionService.createOrUpdate(childTransaction);
    }

    @Test(expected = ParentNotFoundException.class)
    public void testUnableToCreateWithNotExistingParent() {
        Transaction transaction = new Transaction(2345287L, new BigDecimal("22.35"), "test type", 67593458L);
        transactionService.createOrUpdate(transaction);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnableToCreateWithoutId() {
        Transaction transaction = new Transaction(null, new BigDecimal("22.35"), "test type", null);
        transactionService.createOrUpdate(transaction);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnableToCreateWithoutAmount() {
        Transaction transaction = new Transaction(568736L, null, "test type", null);
        transactionService.createOrUpdate(transaction);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnableToCreateWithoutType() {
        Transaction transaction = new Transaction(568736L, new BigDecimal("22.35"), null, null);
        transactionService.createOrUpdate(transaction);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnableToCreateWithZeroAmount() {
        Transaction transaction = new Transaction(568736L, new BigDecimal(0), null, null);
        transactionService.createOrUpdate(transaction);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnableToCreateWithNull() {
        transactionService.createOrUpdate(null);
    }

    @Test
    public void testCreateWithNegativeAmount() {
        Transaction transaction = new Transaction(568736L, new BigDecimal("-12.43"), "test type", null);
        transactionService.createOrUpdate(transaction);
        Transaction updatedTransaction = transactionService.getById(568736L);
        assertThat(transaction.getAmount(), is(equalTo(updatedTransaction.getAmount())));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNegativeAmountInvalidScale() {
        Transaction transaction = new Transaction(568736L, new BigDecimal("-12.437"), "test type", null);
        transactionService.createOrUpdate(transaction);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithPositiveAmountInvalidScale() {
        Transaction transaction = new Transaction(568736L, new BigDecimal("13.728"), "test type", null);
        transactionService.createOrUpdate(transaction);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithInvalidAmount() {
        Transaction transaction = new Transaction(568736L, new BigDecimal("2134.241"), "test type", null);
        transactionService.createOrUpdate(transaction);
    }

    @Test
    public void testSumOfTransactions() {
        Transaction parentTransaction = new Transaction(5187623L, new BigDecimal("542.32"), "test type", null);
        transactionService.createOrUpdate(parentTransaction);

        Transaction childTransaction1 = new Transaction(98437L, new BigDecimal(2342), "test type1", 5187623L);
        transactionService.createOrUpdate(childTransaction1);

        Transaction childTransaction2 = new Transaction(214789L, new BigDecimal("23512.34"), "test type", 5187623L);
        transactionService.createOrUpdate(childTransaction2);

        Transaction childTransaction3 = new Transaction(47256L, new BigDecimal("11.01"), "test type2", 5187623L);
        transactionService.createOrUpdate(childTransaction3);

        Transaction childTransaction4 = new Transaction(76564325L, new BigDecimal("41.21"), "test type", 47256L);
        transactionService.createOrUpdate(childTransaction4);

        BigDecimal sum = transactionService.calculateTransactionsSum(5187623L);
        assertThat(sum, is(equalTo(
                parentTransaction.getAmount()
                        .add(childTransaction1.getAmount())
                        .add(childTransaction2.getAmount())
                        .add(childTransaction3.getAmount())
                        .add(childTransaction4.getAmount()))));
    }

    @Test
    public void testSumWithoutChildren() {
        Transaction transaction = new Transaction(76564325L, new BigDecimal("41.21"), "test type", null);
        transactionService.createOrUpdate(transaction);

        BigDecimal sum = transactionService.calculateTransactionsSum(76564325L);
        assertThat(sum, is(equalTo(transaction.getAmount())));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSumOfTransactionsInvalidId() {
        transactionService.calculateTransactionsSum(4327623L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSumOfTransactionsNullId() {
        transactionService.calculateTransactionsSum(null);
    }

    @Test
    public void testIdsByType() {
        Transaction transaction1 = new Transaction(5187623L, new BigDecimal("542.32"), "test type", null);
        transactionService.createOrUpdate(transaction1);

        Transaction transaction2 = new Transaction(98437L, new BigDecimal(2342), "test type1", 5187623L);
        transactionService.createOrUpdate(transaction2);

        Transaction transaction3 = new Transaction(214789L, new BigDecimal("23512.34"), "test type", 5187623L);
        transactionService.createOrUpdate(transaction3);

        Transaction transaction4 = new Transaction(47256L, new BigDecimal("11.01"), "test type2", 5187623L);
        transactionService.createOrUpdate(transaction4);

        Collection<Long> transactionIds = transactionService.getTransactionIdsByType("test type");
        assertThat(transactionIds, hasItem(5187623L));
        assertThat(transactionIds, hasItem(214789L));
        assertThat(transactionIds, not(hasItem(98437L)));
        assertThat(transactionIds, not(hasItem(47256L)));
    }

    @Test
    public void testIdsByTypeChangeType() {
        Transaction transaction = new Transaction(5187623L, new BigDecimal("542.32"), "test type", null);
        transactionService.createOrUpdate(transaction);

        Transaction updatedTransaction = new Transaction(5187623L, new BigDecimal(2342), "test type1", null);
        transactionService.createOrUpdate(updatedTransaction);

        Collection<Long> testTypeIds = transactionService.getTransactionIdsByType("test type");
        Collection<Long> testType1Ids = transactionService.getTransactionIdsByType("test type1");
        assertThat(testTypeIds.size(), is(equalTo(0)));
        assertThat(testType1Ids.size(), is(equalTo(1)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIdsByTypeNullType() {
        transactionService.getTransactionIdsByType(null);
    }

    @Test
    public void testIdsByTypeNotExistingType() {
        Collection<Long> testTypeIds = transactionService.getTransactionIdsByType("not existing one");
        assertThat(testTypeIds.size(), is(equalTo(0)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadByInvalidId() {
        transactionService.getById(757435L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadByNullId() {
        transactionService.getById(null);
    }
}