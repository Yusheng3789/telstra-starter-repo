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
import au.com.telstra.simcardactivator.model.SimCardActivation;
import au.com.telstra.simcardactivator.repository.SimCardActivationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;



@RestController
@RequestMapping("/api/sim")
public class ActivationController {

    @Autowired
    private SimCardActivationRepository simCardActivationRepository;

    @Autowired
    private RestTemplate restTemplate;


    @PostMapping("/activate")
    public ResponseEntity<String> activateSim(@RequestBody ActivationRequest request) {

        System.out.println("Received activation request:");
        System.out.println("ICCID: " + request.getIccid());
        System.out.println("Customer email: " + request.getCustomerEmail());

        // 1. 调用 actuator 微服务
        String actuatorUrl = "http://localhost:8444/actuate";

        Map<String, String> body = new HashMap<>();
        body.put("iccid", request.getIccid());

        ActivationResult result = restTemplate.postForObject(
                actuatorUrl,
                body,
                ActivationResult.class
        );

        boolean success = result != null && result.isSuccess();

        // 2. 打印是否成功
        if (success) {
            System.out.println("Activation SUCCESS for ICCID: " + request.getIccid());
        } else {
            System.out.println("Activation FAILED for ICCID: " + request.getIccid());
        }

        // 3. 保存一条记录到数据库
        SimCardActivation record =
                new SimCardActivation(request.getIccid(), request.getCustomerEmail(), success);

        simCardActivationRepository.save(record);

        // 打印数据库生成的 id，方便后面用来查询
        System.out.println("Saved activation record with id: " + record.getId());


        // 4. 返回简单响应（现在先保留）
        String responseMessage = "Activation result for ICCID " + request.getIccid()
                + ": " + success + " (record id: " + record.getId() + ")";
        return ResponseEntity.ok(responseMessage);
    }

    @GetMapping("/query")
    public ResponseEntity<?> getSimCardActivation(@RequestParam("simCardId") Long simCardId) {

        // 从数据库按 id 查询
        Optional<SimCardActivation> optional = simCardActivationRepository.findById(simCardId);

        if (!optional.isPresent()) {
            // 查不到就返回 404
            return ResponseEntity.notFound().build();
        }

        SimCardActivation activation = optional.get();

        // 构造符合要求的 JSON 结构（不返回 id）
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("iccid", activation.getIccid());
        responseBody.put("customerEmail", activation.getCustomerEmail());
        responseBody.put("active", activation.isActive());

        return ResponseEntity.ok(responseBody);
    }



}
