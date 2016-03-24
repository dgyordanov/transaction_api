package com.transaction.api;

import com.transaction.service.Transaction;
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
    private ModelMapper modelMapper;

    @RequestMapping(value = "/transaction/{transaction_id}", method = PUT, consumes = APPLICATION_JSON_VALUE)
    public void createOrUpdate(@PathVariable("transaction_id") Long id, @RequestBody TransactionDTO transactionDTO) {
        Transaction transaction = convertToModel(id, transactionDTO);
        System.out.println(transaction);
    }

    @RequestMapping(value = "/transaction/{transaction_id}", method = GET, produces = APPLICATION_JSON_VALUE)
    public TransactionDTO read(@PathVariable("transaction_id") Long id) {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setParentId(2L);
        transaction.setAmount(new BigDecimal("2312.23"));
        transaction.setType("type");

        TransactionDTO transactionDTO = convertToDto(transaction);
        System.out.println(transaction);

        return transactionDTO;
    }

    @RequestMapping(value = "types/{type}", method = GET, produces = APPLICATION_JSON_VALUE)
    private List<Long> getTransactionIdsByType(@PathVariable("type") String type) {
        return Arrays.asList(1234L, 213412L, 121L, 2341L);
    }

    @RequestMapping(value = "sum/{transaction_id}", method = GET, produces = APPLICATION_JSON_VALUE)
    private TransactionsSumDTO getTransactionSum(@PathVariable("transaction_id") Long id) {
        return new TransactionsSumDTO(new BigDecimal("9210.32"));
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
