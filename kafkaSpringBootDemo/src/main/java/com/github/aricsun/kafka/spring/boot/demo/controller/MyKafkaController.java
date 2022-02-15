package com.github.aricsun.kafka.spring.boot.demo.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author AricSun
 * @date 2022.01.28 9:29
 */
@RestController
@RequestMapping("/msg")
public class MyKafkaController {

    @Value("${spring.kafka.template.default-topic}")
    private String TOPIC_NAME;

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    // producer
    @PostMapping
    public String sendMessage(@RequestParam String message){

        kafkaTemplate.send(TOPIC_NAME, "testMsg", message);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msg", "send successfully!");
        return jsonObject.toJSONString();
    }
}
