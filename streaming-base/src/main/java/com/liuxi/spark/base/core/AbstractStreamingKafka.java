package com.liuxi.spark.base.core;

import com.liuxi.spark.base.utils.ConfigUtil;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;

import java.util.*;

/**
 * Created by lx11915 on 2018/1/18.
 */
public abstract class AbstractStreamingKafka extends AbstractStreaming {
    @Override
    protected JavaPairDStream<String, String> getInputDStream(JavaStreamingContext jsc) {

        Map<String, Integer> topicMap = new HashMap<>();
        String[] topics = ConfigUtil.get("kafka.topics").split(",");
        for (String topic : topics) {
            topicMap.put(topic, 1);
        }
        String zkAddress = ConfigUtil.get("kafka.zookeeper_host");
        String group = ConfigUtil.get("kafka.group");

        // 通过 zookeeper 地址、 consumer group 来创建 Kafka stream
        return KafkaUtils.createStream(jsc, zkAddress, group, topicMap);
    }
}
