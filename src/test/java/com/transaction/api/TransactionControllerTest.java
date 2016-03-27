package com.transaction.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transaction.TransactionServer;
import com.transaction.service.Transaction;
import com.transaction.service.TransactionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(TransactionServer.class)
@WebIntegrationTest
public class TransactionControllerTest {

    private static final String BASE_URL = "http://localhost:8080/transactionservice";
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private RestTemplate template = new TestRestTemplate();

    @Autowired
    private TransactionService transactionService;

    @Test
    public void createTransaction() throws JsonProcessingException {
        HttpEntity<String> httpEntity = getTransactionHttpEntity(BigDecimal.valueOf(5000L), "cars", null);
        ResponseEntity<Map> response =
                template.exchange(BASE_URL + "/transaction/72146", HttpMethod.PUT, httpEntity, Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Transaction createdTransaction = transactionService.getById(72146L);
        assertThat(createdTransaction.getAmount(), equalTo(BigDecimal.valueOf(5000L)));
        assertThat(createdTransaction.getType(), equalTo("cars"));
        assertThat(createdTransaction.getParentId(), is(nullValue()));

        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = response.getBody();
        assertThat(responseBody.get("status"), equalTo("ok"));
        assertThat(responseBody.size(), equalTo(1));
    }

    @Test
    public void createTransactionWithoutAmount() throws JsonProcessingException {
        HttpEntity<String> httpEntity = getTransactionHttpEntity(null, "cars", null);
        ResponseEntity<Map> response =
                template.exchange(BASE_URL + "/transaction/651941", HttpMethod.PUT, httpEntity, Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void createTransactionInvalidParent() throws JsonProcessingException {
        HttpEntity<String> httpEntity = getTransactionHttpEntity(BigDecimal.valueOf(5000L), "cars", 9724517L);
        ResponseEntity<Map> response =
                template.exchange(BASE_URL + "/transaction/64322908", HttpMethod.PUT, httpEntity, Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void updateTransaction() throws JsonProcessingException {
        HttpEntity<String> createHttpEntity = getTransactionHttpEntity(BigDecimal.valueOf(5000L), "cars", null);
        ResponseEntity<Map> createResponse =
                template.exchange(BASE_URL + "/transaction/9871236", HttpMethod.PUT, createHttpEntity, Map.class);

        HttpEntity<String> updateHttpEntity = getTransactionHttpEntity(new BigDecimal("11000.14"), "motors", null);
        ResponseEntity<Map> updateResponse =
                template.exchange(BASE_URL + "/transaction/9871236", HttpMethod.PUT, updateHttpEntity, Map.class);

        assertThat(createResponse.getStatusCode(), equalTo(HttpStatus.OK));
        @SuppressWarnings("unchecked")
        Map<String, String> createResponseBody = createResponse.getBody();
        assertThat(createResponseBody.get("status"), equalTo("ok"));
        assertThat(createResponseBody.size(), equalTo(1));

        assertThat(updateResponse.getStatusCode(), equalTo(HttpStatus.OK));
        @SuppressWarnings("unchecked")
        Map<String, String> updateResponseBody = updateResponse.getBody();
        assertThat(updateResponseBody.get("status"), equalTo("ok"));
        assertThat(updateResponseBody.size(), equalTo(1));

        Transaction createdTransaction = transactionService.getById(9871236L);
        assertThat(createdTransaction.getAmount(), equalTo(new BigDecimal("11000.14")));
        assertThat(createdTransaction.getType(), equalTo("motors"));
        assertThat(createdTransaction.getParentId(), is(nullValue()));

    }

    @Test
    public void testReadById() {
        transactionService.createOrUpdate(new Transaction(791698L, new BigDecimal("33.23"), "test type", null));

        ResponseEntity<Map> response = template.getForEntity(BASE_URL + "/transaction/791698", Map.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody.get("amount"), equalTo(33.23));
        assertThat(responseBody.get("type"), equalTo("test type"));
        assertThat(responseBody.get("parent_id"), equalTo(null));

    }

    @Test
    public void testReadInvalidId() {
        ResponseEntity<String> response = template.getForEntity(BASE_URL + "/transaction/1312345", String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testSum() throws JsonProcessingException {
        Transaction parentTransaction = new Transaction(78912306L, new BigDecimal("542.32"), "test type", null);
        transactionService.createOrUpdate(parentTransaction);

        Transaction childTransaction = new Transaction(98764322L, new BigDecimal(2342), "test type1", 78912306L);
        transactionService.createOrUpdate(childTransaction);

        ResponseEntity<Map> response = template.getForEntity(BASE_URL + "/sum/78912306", Map.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody.get("sum"), equalTo(parentTransaction.getAmount()
                .add(childTransaction.getAmount()).doubleValue()));

    }

    @Test
    public void testSumInvalidId() {
        ResponseEntity<Map> response = template.getForEntity(BASE_URL + "/sum/61912", Map.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testIdListByType() throws JsonProcessingException {
        Transaction parentTransaction = new Transaction(9823174L, new BigDecimal("542.32"), "type1", null);
        transactionService.createOrUpdate(parentTransaction);

        Transaction childTransaction = new Transaction(981645393L, new BigDecimal(2342), "type2", 9823174L);
        transactionService.createOrUpdate(childTransaction);

        ResponseEntity<List> response = template.getForEntity(BASE_URL + "/types/type1", List.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        @SuppressWarnings("unchecked")
        List<Long> responseBody = response.getBody();
        assertThat(responseBody, equalTo(Arrays.asList(9823174)));

    }

    @Test
    public void testIdListByNotExistingType() throws JsonProcessingException {
        ResponseEntity<List> response = template.getForEntity(BASE_URL + "/types/not_existing_type", List.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        @SuppressWarnings("unchecked")
        List<Long> responseBody = response.getBody();
        assertThat(responseBody.size(), equalTo(0));
    }

    private HttpEntity<String> getTransactionHttpEntity(BigDecimal amount, String type, Long parentId)
            throws JsonProcessingException {
        Map<String, Object> requestBody = new HashMap<>();
        if (amount != null) {
            requestBody.put("amount", amount);
        }
        if (type != null) {
            requestBody.put("type", type);
        }
        if (parentId != null) {
            requestBody.put("parent_id", parentId);
        }
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.set("Connection", "Close");

        return new HttpEntity<>(OBJECT_MAPPER.writeValueAsString(requestBody), requestHeaders);
    }

}