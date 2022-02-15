# kafka-study-notes

Kafka入门学习笔记 | Kafka Getting Started Learning Notes

## Guidance

- 包含两个项目，因为内容不多索性一同上传
- 均为Maven项目
- [kafkaDemo](https://github.com/Aric-Sun/kafka-study-notes/tree/main/kafkaDemo)是使用Java原生实现，使用Maven管理依赖
- [kafkaSpringBootDemo](https://github.com/Aric-Sun/kafka-study-notes/tree/main/kafkaSpringBootDemo)使用Spring Boot实现相关功能，配置集中在`application.yml`，API调用比较简洁
- 重要代码均包含注释说明，一些功能可通过开关不同的注释达到不同运行效果
- 本项目为视频教程[千锋教育最新kafka入门到精通教程|kafka快速入门，阿里P7架构师带你深度解析](https://www.bilibili.com/video/BV1Xy4y1G7zA)的随堂代码，教程大纲参见[Kafka入门到精通](https://bright-boy.gitee.io/technical-notes/#/kafka/kafka)

## Study Log | 学习日志

- 1.19.2022：学习Kafka

    -   基础概念
        -   消息队列
        -   消费模式
        -   基础架构（分区、副本）
    -   基本命令
        -   topic增删查
        -   控制生产者消费者
    -   数据日志分离（控制`logs.dir`为`logs`以外的目录名以避开默认的日志存放位置）

- 1.20.2022：

    - 回顾昨日学习内容，巩固知识点
    - 搞清楚`--broker-list`，`--zookeeper`，`--bootstrap-server`三个参数的区别
    - 学习了解Kafka工作流程
        - 文件存储结构（一个partition分为多个segment，以定长索引的形式加快定位速度，多个segment对应多对`.index`和`.log`，以当前segment的第一条消息的offset命名）
        - 生产者分区策略（指定partition / 只有key-value / 只有value）

- 1.21.2022：继续学习Kafka

    - ISR（选取同步速度快的follower加入，维护一个和leader保持同步的follower集合）
    - ACK应答机制（`acks=0/1/-1`，用户根据数据重要程度选择策略，可能会造成数据丢失或数据重复）
    - 数据一致性问题（利用LEO（每个副本的最后一个offset）和HW（high watermark高水位，所有副本中最小的LEO），保持数据一致，不考虑数据丢失/重复问题）
    - Exactly One语义（由At Least Once(`acks=-1`)+幂等性组成，做到既不重复也不丢失。在Kafka集群中做去重处理，通过`<PID, Partition, SeqNumber>`标记唯一性，其中`PID`是`Producer`单次会话ID，`Sequence`是消息序列号）
    - 消费者分区分配策略（Range按topic分配（可能会造成数量分配不均的问题），RoundRobin（轮询）按消费者组分配（会扁平化打散分配，当同一个组内的消费者订阅不同的topic，会造成数据混乱，所以要使用得保证消费者组的订阅主题相同），当消费者数量发生变化时调用方法）  

- 1.24.2022：继续学习kafka

    - 消费者offset的存储（分为`--zookeeper`（高版本已去除）和本地`--bootstrap-server`） 

    - 测试集群环境下kafka的生产消费，详见下方问题

        - kafka无法启动，报错：`The Cluster ID kVSgfurUQFGGpHMTBqBPiw doesn't match stored clusterId  Some(0Qftv9yBTAmf2iDPSlIk7g) in meta.properties. The broker is trying to join  the wrong cluster. Configured zookeeper.connect may be wrong.`

            <u>解决</u>：删除`logs.dir`目录下`meta.properties`，重新启动kafka

        - kafka创建topic失败，报错：`Replication  factor: 2 larger than available brokers: 1.`

            <u>解决</u>：zookeeper节点间未连通，在`conf`下`zoo.cfg`中末尾追加`server.1=x.x.x.x:2888:3888`等字样，一行一个节点，`2888`表示leader节点端口，`3888`表示leader掉线后follower互连的端口，同时在各个节点的持久化目录`data/`下创建`myid`文件，内容仅有一个数字，用来区分节点。

        - kafka正常启动，节点1创建topic并开启生产者，节点1开启消费者正常，节点2开启消费者后，报`warn:LEADER_NOT_AVAILABLE`

            <u>解决</u>：允许kafka远程连接，打开`config/server.properties`配置文件
            把31行的注释去掉，`listeners=PLAINTEXT://:9092`
            把36行的注释去掉，把`advertised.listeners`值改为`PLAINTEXT://host_ip:9092`

- 1.25.2022：继续学习kafka：  

    - 单播消息和多播消息的实现（本质上是消费者组的运用，同一消费组的消费者不能对一个主题下的同一个分区消费，可以消费同一topic下的多个不同分区）      

    - 搭建双机三节点的复合集群（虚拟机1有zk\*1,kafka\*1，虚拟机2有zk\*1，kafka\*2（端口号不同），两个zk构成zk集群，三个kafka通过zk集群组成kafka集群），可详见下图 

    ![image](https://user-images.githubusercontent.com/59010287/153988797-316d5dae-dd19-4bb3-b0ea-a8eb6b91f791.png)

    - Java上使用Kafka

- 1.26.2022：继续学习kafka：

    -  使用Java实现生产者发送消息并得到返回数据（指定/不指定分区（印证之前学过的生产者分区策略），同步/异步，ACK配置（`ACKS`，重试），消息发送缓冲区）     
    -  使用Java实现消费者客户端接收数据（自动提交/手动提交（同步/异步），单次poll的限制（时长和消息量两个维度））

- 1.27.2022：继续学习kafka：      

    - Java实现Consumer：
        - poll间隔配置（kafka会将消费能力差的踢出消费者组）
        - 健康状态检查（心跳）
        - 指定分区消费
        - 设定从头或者从某个偏移量开始消费
        - 从指定时间开始消费（从时间定位到offset，然后通过偏移量消费）
        - 新消费组的消费offset规则（接收登录后的消息/第一次从头，之后正常从最新offset消费）     
    - 在SpringBoot中使用Kafka

- 1.28.2022：继续学习Kafka：      

    - 使用SpringBoot实现生产者（`KafkaTemplate.send()`）和消费者（`@KafkaListener`设定主题和消费组，可重复注解以声明多个消费者，指定主题的分区并设定单个分区偏移量，可设定并发消费数）      
    - Controller（在zk注册序号最小的broker担任，负责管理集群中所有分区和副本的状态（选举新的副本leader（ISR从左往右），通知副本和分区的变化给其他broker））     
    - Rebalance机制（当消费组里消费者和分区的关系发生变化时触发，前提是没有指明消费分区。开启消费者分区策略`sticky`后，维持原有的关系，将空出的分区重新按照策略分配，否则全部重新洗牌rebalance）     
    - Kafka相关优化
        - 消息丢失
        - 消息重复
        - 顺序消费（同步发送，指定分区）

- 2.10.2022：kafka收尾      

    - kafka相关优化：
        - 消息积压的处理（增加分区和消费者/使用一个消费者把收到的消息批量转发给另一个主题，主题中有多个分区，另外设置多个消费者进行消费）
        - 延时队列（使用主题区分延时的时长，消费者采用轮询的方式**重复**消费，在确定业务未完成的情况下，判断是否超时，超时的直接在数据库标记状态，未超时的记录下offset，后面的消息不再消费，下次从offset处重复poll，重复流程）     
    - 部署安装kafka-eagle

- 2.11.2022：Linux开机时间和关机时间优化：

    - 开机时间，去除无关服务自启，修改开机等待时间

    - 关机时间

        - 设定`/etc/systemd/system.conf`中`DefaultTimeoutStopSec`改为5s（默认90s），等待进程关闭5s，超时强制结束
        - 在`/etc/init.d/redis_6379`命令中的`stop`部分替换原先的关停命令为`$CLIEXEC -p $REDISPORT -a 123456 shutdown`，或替换为`killall $EXEC`
        - 使用`shutdown.sh -force`关闭Tomcat
        - 以上两项设置只是为了好文明，有了5s等待时间的限制，都可以快速关机

    - kafka-eagle修改配置以正常工作，详见下方问题与解决

        - kafka-eagle无法打开网页，error.log：`port  out of range:-1/JMX service url[192.168.35.128:-1] create has error,msg is  cannot be cast to javax.management.remote.JMXConnector`

            <u>解决</u>：JMX端口未设定，RMI域名/IP未设定，在`kafka-server-start.sh`中开放JMX端口处增加`-Djava.rmi.server.hostname`参数可解决：

            ```shell
            export KAFKA_HEAP_OPTS="-Xms2G  -Xmx2G -Djava.rmi.server.hostname=域名/ip"
            export JMX_PORT="9999"
            ```

        - kafka-eagle界面：Kafka其他Broker端口为`-1`

            <u>解决</u>：需要broker都开启JMX，由于第二台虚拟机部署了两个kafka，未避免端口冲突，需要分配不同的`JMX_PORT`，方便起见，创建群起脚本，分别指定`JMX_PORT`和配置文件路径

        - kafka-eagle界面：zookeeper未连接，版本号无法获取

            <u>解决</u>：3.5版本后的zk需要开启`zkCli`的远程命令权限：

            ```shell
            vim zkServer.sh     
            # 在77行后添加如下内容
            ZOOMAIN="-Dzookeeper.4lw.commands.whitelist=* ${ZOOMAIN}"
            ```
            
