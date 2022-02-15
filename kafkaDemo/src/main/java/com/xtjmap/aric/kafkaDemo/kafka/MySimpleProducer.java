package com.xtjmap.aric.kafkaDemo.kafka;

import com.xtjmap.aric.kafkaDemo.common.Constants;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * 测试生产者发送消息（同步），不指定分区
 * @author AricSun
 * @date 2022.01.26 10:10
 */
public class MySimpleProducer {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
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
                Constants.TOPIC_NAME, "myKey", "helloKafka11");

        // 同步方式发送消息，得到消息发送的元数据并输出
        RecordMetadata recordMetadata = producer.send(producerRecord).get();
        System.out.println("同步发送方式回调结果：" + "topic-" + recordMetadata.topic() +
                "| partition-" + recordMetadata.partition() +
                "| offset-" + recordMetadata.offset());
    }
}
