package com.liuxi.spark.base.core;

import org.apache.spark.api.java.function.Function;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

/**
 * Created by lx11915 on 2018/1/18.
 */
public class TestAbstractStreamingKafka extends AbstractStreamingKafka {
    @Override
    public void init(boolean isDebug) {

    }

    @Override
    protected void doCheckPoint(JavaStreamingContext jsc) {

    }

    @Override
    protected void doCreateDSteamDAG(JavaPairDStream<String, String> inputData) {
        inputData.map((Function<Tuple2<String, String>, String>) tuple2 -> {
            System.out.println(">>>> " + tuple2._2);
            return tuple2._2();
        }).print();
    }
}
