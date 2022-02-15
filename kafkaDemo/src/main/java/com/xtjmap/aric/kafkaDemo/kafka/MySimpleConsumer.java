package com.xtjmap.aric.kafkaDemo.kafka;

import com.xtjmap.aric.kafkaDemo.common.Constants;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * 最基本的消费者实现
 * @author AricSun
 * @date 2022.01.26 15:44
 */
public class MySimpleConsumer {
    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Constants.BOOTSTRAP_SERVER);
        // 消费者组
        props.put(ConsumerConfig.GROUP_ID_CONFIG, Constants.CONSUMER_GROUP_NAME);
        // 反序列化器
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        // 创建消费者客户端
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        // 订阅主题列表
        consumer.subscribe(Collections.singleton(Constants.TOPIC_NAME));

        // 长轮询
        while (true){
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));  // poll: 轮询
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("收到消息：partition=%d，offset=%d，key=%s，value=%s%n",
                        record.partition(),record.offset(),record.key(),record.value());
            }
        }
    }
}
