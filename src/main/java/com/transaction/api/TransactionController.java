package com.transaction.api;

import com.transaction.service.ParentNotFoundException;
import com.transaction.storage.Transaction;
import com.transaction.service.TransactionService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

/**
 * Rest controller which exposes transactionservice endpoints.
 * It is a thin rest controller which only wrap the service itself and add some exception handling
 * in order to be return meaningful responses and correct response codes.
 * <p>
 *
 * @author Diyan Yordanov
 */
@RestController
@RequestMapping("/transactionservice")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ModelMapper modelMapper;

    private static final Map<String, String> okResponseBody = new HashMap<>();

    static {
        okResponseBody.put("status", "ok");
    }

    @RequestMapping(value = "/transaction/{transaction_id}", method = PUT, consumes = APPLICATION_JSON_VALUE)
    public Map<String, String> createOrUpdate(
            @PathVariable("transaction_id") Long id, @RequestBody TransactionDTO transactionDTO) {
        Transaction transaction = convertToModel(id, transactionDTO);
        transactionService.createOrUpdate(transaction);
        return okResponseBody;
    }

    @RequestMapping(value = "/transaction/{transaction_id}", method = GET, produces = APPLICATION_JSON_VALUE)
    public TransactionDTO read(@PathVariable("transaction_id") Long id) {
        Transaction transaction = transactionService.getById(id);
        return convertToDto(transaction);
    }

    @RequestMapping(value = "types/{type}", method = GET, produces = APPLICATION_JSON_VALUE)
    private Collection<Long> getTransactionIdsByType(@PathVariable("type") String type) {
        return transactionService.getTransactionIdsByType(type);
    }

    @RequestMapping(value = "sum/{transaction_id}", method = GET, produces = APPLICATION_JSON_VALUE)
    private HashMap<String, BigDecimal> getTransactionsSum(@PathVariable("transaction_id") Long id) {
        BigDecimal sum = transactionService.calculateTransactionsSum(id);
        HashMap<String, BigDecimal> responseBody = new HashMap<>();
        responseBody.put("sum", sum);

        return responseBody;
    }

    @ExceptionHandler({IllegalArgumentException.class, ParentNotFoundException.class})
    void handleBadRequests(HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value());
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
