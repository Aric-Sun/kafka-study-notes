package com.xtjmap.aric.kafkaDemo.kafka;

import com.alibaba.fastjson.JSON;
import com.xtjmap.aric.kafkaDemo.common.Constants;
import com.xtjmap.aric.kafkaDemo.entity.Order;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author AricSun
 * @date 2022.01.25 16:59
 */
public class MyProducer {

    public static void main(String[] args) throws InterruptedException {
        Properties props = new Properties();
        // 设置bootstrap-server
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Constants.BOOTSTRAP_SERVER);
        // 把发送消息的key和value从字符串序列化为字节数组
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        /*
         * ACKs应答机制，只存在于同步情况下，异步不需要等待ACK应答
         * 0：只管发，类似于异步
         * 1：等待leader写入完成
         * -1/all：等待ISR同步完成，
         * ---------------------------------
         * acks=0：表示producer不需要等待任何broker确认收到消息的回复，就可以继续发送下一条消息。性能最高，但是最容易丢消息。
         * acks=1：至少要等待Leader已经成功将数据写入本地log，但是不需要等待所有follower是否成功写入。就可以继续发送下一条消息。
         *    这种情况下，如果follower没有成功备份数据，而此时Leader又挂掉，则消息会丢失。
         * acks=-1或a1l：需要等待min.insync.replicas（默认为1，推荐配置大于等于2）这个参数配置的副本个数都成功写入日志，
         *    这种策略会保证只要有一个备份存活就不会丢失数据。这是最强的数据保证。一般除非是金融级别，或跟钱打交道的场景才会使用这种配置。
         */
//        props.put(ProducerConfig.ACKS_CONFIG, "1");

        // 重试次数：3
//        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        // 重试间隔, 300ms, 超过此值没收到ACK，重新发送
//        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 300);

        // 缓冲区设置，存放于生产者上
        // 缓冲区大小，默认32MB，消息不是立即发送的
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);  // 32MB
        // kafka线程，从缓冲区拉取满16KB，就发送，默认16KB
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);  // 16KB
        // kafka线程，超过10ms，不管batch满没满16KB，都会发送，默认0：立即发送
        props.put(ProducerConfig.LINGER_MS_CONFIG, 10);  // 10ms

        // 创建生产者客户端
        Producer<String, String> producer = new KafkaProducer<>(props);

        // 要发送五条消息
        int msgNum = 5;
        // 并发编程中的门栓
        final CountDownLatch countDownLatch = new CountDownLatch(msgNum);
        for (int i=0; i<5; ++i){
            Order order = new Order((long)i, i);
            /*
             * 未指定发送分区，具体发送的分区计算公式：hash(key)%partitionNum
             * ProducerRecord：要发送的一条消息记录，
             */
            ProducerRecord<String, String> producerRecord = new ProducerRecord<>(
                    Constants.TOPIC_NAME, order.getOrderID().toString(), JSON.toJSONString(order));

            // 同步发送
            /*try {
                RecordMetadata recordMetadata = producer.send(producerRecord).get();  // 这句话就是同步了，因为在等返回值
                // =======阻塞=======
                System.out.println("同步发送方式回调结果：" + "topic-" + recordMetadata.topic() +
                        "| partition-" + recordMetadata.partition() +
                        "| offset-" + recordMetadata.offset());
            } catch (InterruptedException e) {
                e.printStackTrace();
                //1，记录日志预警系统 +1
                // 2，设置时间间隔1s同步的方式再次发送，如果还不行 日志预警 人工介入
                try {
                    Thread.sleep(1000);
                    // 等待消息发送成功的同步阻塞方法
                    RecordMetadata metadata = producer.send(producerRecord).get();
                } catch (Exception ex) {
                    // 人工介入
                    ex.printStackTrace();
                }
            } catch (ExecutionException e){
                e.printStackTrace();
            }*/


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
                countDownLatch.countDown();  // 执行一次就-1（构造的时候参数是5）
            });
        }
        // 判断countDownLatch是不是0，如果不是就等待5s(可以不填)，一直等到0才结束这个语句
        countDownLatch.await(5, TimeUnit.SECONDS);
        producer.close();
    }
}
