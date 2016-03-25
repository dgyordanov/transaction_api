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
import java.util.HashMap;
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
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("amount", "5000");
        requestBody.put("type", "cars");
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> httpEntity =
                new HttpEntity<>(OBJECT_MAPPER.writeValueAsString(requestBody), requestHeaders);

        ResponseEntity<Map> response =
                template.exchange(BASE_URL + "/transaction/1", HttpMethod.PUT, httpEntity, Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Transaction createdTransaction = transactionService.getById(1L);
        assertThat(createdTransaction.getAmount(), equalTo(BigDecimal.valueOf(5000L)));
        assertThat(createdTransaction.getType(), equalTo("cars"));
        assertThat(createdTransaction.getParentId(), is(nullValue()));

        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = response.getBody();
        assertThat(responseBody.get("status"), equalTo("ok"));
        assertThat(responseBody.size(), equalTo(1));
    }

    @Test
    public void updateTransaction() {

    }

    @Test
    public void testReadInvalidId() {
        ResponseEntity<String> response = template.getForEntity(BASE_URL + "/transaction/1", String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
    }

}