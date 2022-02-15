package com.xtjmap.aric.kafkaDemo.kafka;

import com.xtjmap.aric.kafkaDemo.common.Constants;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 测试生产者发送消息（异步），指定分区
 * @author AricSun
 * @date 2022.01.26 10:46
 */
public class MySimpleProducer2 {

    public static void main(String[] args) throws InterruptedException {
        Properties props = new Properties();
        // 设置bootstrap-server
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Constants.BOOTSTRAP_SERVER);
        // 把发送消息的key和value从字符串序列化为字节数组
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // 创建生产者客户端
        Producer<String, String> producer = new KafkaProducer<>(props);

        /*
         * 未指定发送分区，具体发送的分区计算公式：hash(key)%partitionNum
         * ProducerRecord：要发送的一条消息记录，
         * key 决定了要发往哪个分区，value是具体要发送的消息
         */
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(
                Constants.TOPIC_NAME,1, "dog", "今天你终于叫了我的名字 虽然叫错了 但是没关系 我马上就去改名");

        // 异步回调方式发送消息
        producer.send(producerRecord, (recordMetadata, e) -> {
            if (null != e){
                System.err.println("发送消息失败：" + Arrays.toString(e.getStackTrace()));
            }
            if (null != recordMetadata){
                System.out.println("异步发送方式回调结果：" + "topic-" + recordMetadata.topic() +
                        "| partition-" + recordMetadata.partition() +
                        "| offset-" + recordMetadata.offset());
            }
        });

        Thread.sleep(1000000000000L);  // 等待回调

        producer.close();
    }
}
