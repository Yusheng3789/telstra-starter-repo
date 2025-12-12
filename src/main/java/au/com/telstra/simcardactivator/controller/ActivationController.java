package au.com.telstra.simcardactivator.controller;

import au.com.telstra.simcardactivator.model.ActivationRequest;
import au.com.telstra.simcardactivator.model.ActivationResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/sim")
public class ActivationController {

    @Autowired
    private RestTemplate restTemplate;


    @PostMapping("/activate")
    public ResponseEntity<String> activateSim(@RequestBody ActivationRequest request) {

        System.out.println("Received activation request:");
        System.out.println("ICCID: " + request.getIccid());
        System.out.println("Customer email: " + request.getCustomerEmail());

        // 1. 构造发给 actuator 的请求体，只需要 iccid
        String actuatorUrl = "http://localhost:8444/actuate";

        Map<String, String> body = new HashMap<>();
        body.put("iccid", request.getIccid());

        // 2. 调用 actuator 微服务，期望返回 ActivationResult（里面有 success:boolean）
        ActivationResult result = restTemplate.postForObject(
                actuatorUrl,
                body,
                ActivationResult.class
        );

        boolean success = result != null && result.isSuccess();

        // 3. 在控制台打印成功或失败（这是任务要求）
        if (success) {
            System.out.println("Activation SUCCESS for ICCID: " + request.getIccid());
        } else {
            System.out.println("Activation FAILED for ICCID: " + request.getIccid());
        }

        // 4. 同时给调用方一个简单的文本响应
        String responseMessage = "Activation result for ICCID " + request.getIccid() + ": " + success;
        return ResponseEntity.ok(responseMessage);
    }

}
