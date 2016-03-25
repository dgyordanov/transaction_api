package com.transaction.api;

import com.transaction.service.Transaction;
import com.transaction.service.TransactionService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
@RequestMapping("/transactionservice")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ModelMapper modelMapper;

    @RequestMapping(value = "/transaction/{transaction_id}", method = PUT, consumes = APPLICATION_JSON_VALUE)
    public void createOrUpdate(@PathVariable("transaction_id") Long id, @RequestBody TransactionDTO transactionDTO) {
        Transaction transaction = convertToModel(id, transactionDTO);
        transactionService.createOrUpdate(transaction);
    }

    @RequestMapping(value = "/transaction/{transaction_id}", method = GET, produces = APPLICATION_JSON_VALUE)
    public TransactionDTO read(@PathVariable("transaction_id") Long id) {
        Transaction transaction = transactionService.getbyId(id);
        TransactionDTO transactionDTO = convertToDto(transaction);

        System.out.println(transaction);

        return transactionDTO;
    }

    @RequestMapping(value = "types/{type}", method = GET, produces = APPLICATION_JSON_VALUE)
    private List<Long> getTransactionIdsByType(@PathVariable("type") String type) {
        return transactionService.getTransactionIdsByType(type);
    }

    @RequestMapping(value = "sum/{transaction_id}", method = GET, produces = APPLICATION_JSON_VALUE)
    private TransactionsSumDTO getTransactionsSum(@PathVariable("transaction_id") Long id) {
        BigDecimal sum = transactionService.calculateTransactionsSum(id);
        return new TransactionsSumDTO(sum);
    }

    private TransactionDTO convertToDto(Transaction transaction) {
        return modelMapper.map(transaction, TransactionDTO.class);
    }

    private Transaction convertToModel(Long id, TransactionDTO transactionDTO) {
        Transaction transaction = modelMapper.map(transactionDTO, Transaction.class);
        transaction.setId(id);
        return transaction;
    }
}
