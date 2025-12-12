package stepDefinitions;

import au.com.telstra.simcardactivator.SimCardActivator;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ContextConfiguration;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import org.junit.jupiter.api.Assertions;

import java.util.HashMap;
import java.util.Map;


@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(classes = SimCardActivator.class, loader = SpringBootContextLoader.class)
public class SimCardActivatorStepDefinitions {
    @Autowired
    private TestRestTemplate restTemplate;

    private ResponseEntity<String> lastActivationResponse;

    @When("I activate a SIM card with ICCID {string} and email {string}")
    public void i_activate_a_sim_card_with_iccid_and_email(String iccid, String email) {

        String url = "http://localhost:8080/api/sim/activate";

        // 构造请求体 JSON
        Map<String, String> body = new HashMap<>();
        body.put("iccid", iccid);
        body.put("customerEmail", email);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);

        // 通过 restTemplate 调用你的微服务
        lastActivationResponse = restTemplate.postForEntity(
                url,
                requestEntity,
                String.class
        );

        // 简单断言一下调用没有挂掉
        Assertions.assertTrue(
                lastActivationResponse.getStatusCode().is2xxSuccessful(),
                "Activation request did not return 2xx"
        );
    }

    @Then("the activation record with id {long} should be active")
    public void the_activation_record_with_id_should_be_active(Long id) {

        String url = "http://localhost:8080/api/sim/query?simCardId=" + id;

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        // 确认查到了记录
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(), "Expected HTTP 200 when querying activation record");

        Map body = response.getBody();
        Assertions.assertNotNull(body, "Response body should not be null");

        Object activeValue = body.get("active");
        Assertions.assertNotNull(activeValue, "'active' field should be present in response");

        boolean active = (Boolean) activeValue;

        Assertions.assertTrue(active, "Expected activation record with id " + id + " to be active, but it was inactive");
    }

    @Then("the activation record with id {long} should be inactive")
    public void the_activation_record_with_id_should_be_inactive(Long id) {

        String url = "http://localhost:8080/api/sim/query?simCardId=" + id;

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        // 确认查到了记录
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(), "Expected HTTP 200 when querying activation record");

        Map body = response.getBody();
        Assertions.assertNotNull(body, "Response body should not be null");

        Object activeValue = body.get("active");
        Assertions.assertNotNull(activeValue, "'active' field should be present in response");

        boolean active = (Boolean) activeValue;

        Assertions.assertFalse(active, "Expected activation record with id " + id + " to be inactive, but it was active");
    }



}