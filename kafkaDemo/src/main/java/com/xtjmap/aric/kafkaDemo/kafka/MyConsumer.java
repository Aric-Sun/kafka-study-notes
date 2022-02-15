package com.xtjmap.aric.kafkaDemo.kafka;

import com.xtjmap.aric.kafkaDemo.common.Constants;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.*;

/**
 * @author AricSun
 * @date 2022.01.26 15:44
 */
public class MyConsumer {
    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Constants.BOOTSTRAP_SERVER);
        // 消费者组
        props.put(ConsumerConfig.GROUP_ID_CONFIG, Constants.CONSUMER_GROUP_NAME);
        // 反序列化器
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        // 自动提交配置，会丢消息，因为提交完消费者挂了，下一次就读不到还没消费但已经提交offset的消息了
        // 自动提交offset，默认就是true
//        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        // 自动提交offset的间隔时间
//        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);

        //手动提交offset
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        /*
        * 当消费主题的是一个新的消费组，或者指定offset的消费方式，offset不存在，那么应该如何消费
        * Latest（默认）：只消费自己启动之后发送到主题的消息
        * earliest：第一次从头开始消费，以后按照消费offset记录继续消费，这个需要区别于consumer.seekToBeginning（每次都从头开始消费）
        * */
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // 心跳
        // consumer给broker发送心跳的时间间隔
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 1000);  // 1s
        // 超过10s未收到心跳，踢出消费者组，reBalance，将分区分配给其他消费者
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 10 * 1000);

        // 一次poll最多拉取多少条消息，和下面的1000ms从两个维度限制单次拉取
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        // 最大poll间隔，两次间隔超过30s，kafka认为消费能力低，将消费者踢出消费者组，其间隔包括了消费的时间
        // 每次踢出要进行一次reBalance，消耗性能，可以更改MAX_POLL_RECORDS_CONFIG使poll更加频繁避免reBalance
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 30 * 1000);

        // 创建消费者客户端
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        TopicPartition topicPartition = new TopicPartition(Constants.TOPIC_NAME, 0);
        // 订阅主题列表
        consumer.subscribe(Collections.singleton(Constants.TOPIC_NAME));
        // 指定消费分区
//        consumer.assign(Collections.singleton(new TopicPartition(Constants.TOPIC_NAME, 1)));
        // 指定回溯消费，
//        consumer.assign(Collections.singleton(topicPartition));  // 一样的代码
//        consumer.seekToBeginning(Collections.singleton(topicPartition));
        // 指定offset消费
//        consumer.assign(Collections.singleton(topicPartition));  // 一样的代码
//        consumer.seek(topicPartition, 25);

        // 从指定时间开始消费======================start
        // 查找主题下所有的分区
        /*List<PartitionInfo> partitions = consumer.partitionsFor(Constants.TOPIC_NAME);
        long fetchDateTime = new Date().getTime() - 1000 * 60 * 60;  // 取得一小时前的时间
        Map<TopicPartition, Long> map = new HashMap<>();
        // 封装map参数
//        for (PartitionInfo partition : partitions){
//            map.put(new TopicPartition(partition.topic(), partition.partition()), fetchDateTime);
//        }
        // lambda表达式，虽然差不多
        partitions.forEach(p -> map.put(new TopicPartition(p.topic(), p.partition()), fetchDateTime));
        // 根据时间、分区和主题的组合，查找到这个时间点往后的第一条消息的offset
        // 文档机翻：每个分区返回的偏移量是对应分区中时间戳大于或等于给定时间戳的最早偏移量
        Map<TopicPartition, OffsetAndTimestamp> partitionOffsetMap = consumer.offsetsForTimes(map);
        // 指定分区（集）
        consumer.assign(partitionOffsetMap.keySet());
        // Entry是用来避免在遍历map中每次get(key)都要再遍历一遍map，其本身就含有K-V,不需要再查询
        for (Map.Entry<TopicPartition, OffsetAndTimestamp> entry : partitionOffsetMap.entrySet()){
            TopicPartition key = entry.getKey();
            OffsetAndTimestamp value = entry.getValue();
            if (null==key || null==value) continue;
            long offset = value.offset();
            System.out.println("partition-"+key.partition()+"|offset-"+offset);
//            consumer.assign(Collections.singleton(key));
            consumer.seek(key, offset);  // 设定偏移量
            consumeOnce(consumer);  // 短消费，三次poll不到消息就停止消费
        }*/
        // 从指定时间开始消费======================end

        // 长轮询
        while (true){
            // 当轮询了n次后都没有数据，且时间达到1000ms，则会结束 此次 poll，进而重新进入下一次循环
            // 简单来说，就是一次poll最多耗时1000ms，除此之外的限制是一次拉的条数，在props设置
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));  // poll: 轮询
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("收到消息：partition=%d，offset=%d，key=%s，value=%s%n",
                        record.partition(),record.offset(),record.key(),record.value());
            }
            // 消费完
            if (records.count() > 0) {  // 有数据
                // 手动同步提交，会阻塞到offset提交成功，一般用这个，一般提交完也没什么业务逻辑了
                consumer.commitSync();

                // 手动异步提交offset，不会阻塞，可以继续执行后面的业务逻辑
                /*consumer.commitAsync((offset, e) -> {
                    System.err.println("commit failed for "+offset);
                    System.err.println("commit failed exception "+Arrays.toString(e.getStackTrace()));
                });*/
            }
        }
    }

    /*
     * function: 简易的消费，用来规避死循环的, 三次poll不到消息就停止消费
     * @param [consumer]
     * @return void
     */
    private static void consumeOnce(KafkaConsumer<String, String> consumer){
        int cnt = 3;
        while (true){
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));  // 缩短至100ms
            // 三次poll不到消息就停止消费
            if (cnt==0) return;
            if (records.count() == 0) cnt--;
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("收到消息：partition=%d，offset=%d，key=%s，value=%s%n",
                        record.partition(),record.offset(),record.key(),record.value());
            }
            if (records.count() > 0) {
                consumer.commitSync();
            }
        }
    }
}
