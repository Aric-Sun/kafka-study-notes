package com.github.aricsun.kafka.spring.boot.demo.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * @author AricSun
 * @date 2022.01.28 9:51
 */
@Component
@Slf4j
public class MyKafkaConsumer {

    /**
     * function: 消费者consumer
     * *@KafkaListener 声明消费者，可以多个重复注解以声明多个消费者，自动隐式生成注解@KafkaListeners
     * topicPartitions 可以用来指定主题的分区，也可以接收多个主题
     * partitionOffsets 可以用来设置初始偏移量
     * concurrency 表示同组下的消费者个数，也就是并发消费数，建议<=分区数
     * @param record ConsumerRecord 表明对 poll 的消息的每一条执行操作，
     *               ConsumerRecords也可以，就是自己实现遍历
     * @param ack 用来手动提交offset
     */
    @KafkaListener(topics = "${spring.kafka.template.default-topic}",
                    groupId = "${spring.kafka.consumer.group-id}")
    /*@KafkaListener(groupId = "${spring.kafka.consumer.group-id}",
            topicPartitions = {@TopicPartition(topic = "topic1", partitions = {"0","1"}),
                    @TopicPartition(topic = "topic2", partitions = "0",
                        // 表示分区0正常读（受auto-offset-reset: earliest影响），分区1从offset-100开始读，
                            partitionOffsets = @PartitionOffset(partition = "1", initialOffset = "100"))},
            concurrency = "2")*/
    public void listenGroup(ConsumerRecord<String, String> record, Acknowledgment ack){
        String value = record.value();
        log.info(value);
        log.info(String.valueOf(record));
        // 手动提交offset
        ack.acknowledge();
    }
}
