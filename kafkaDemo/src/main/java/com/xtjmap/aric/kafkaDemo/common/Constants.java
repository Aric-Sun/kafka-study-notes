package com.xtjmap.aric.kafkaDemo.common;

/**
 * 常量
 * @author AricSun
 * @date 2022.01.26 10:48
 */
public interface Constants {
    // 主题名称
    String TOPIC_NAME = "bigdata";  // 两个分区 两个副本
    // kafka节点
    String BOOTSTRAP_SERVER = "192.168.35.128:9092,192.168.35.129:9092,192.168.35.129:9093";
    // 消费者组
    String CONSUMER_GROUP_NAME = "testGroup";

}
