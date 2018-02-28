package com.liuxi.spark.base.core;

import com.liuxi.spark.base.utils.ConfigUtil;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Seconds;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;
import java.io.Serializable;
import java.util.UUID;

/**
 * Created by lx11915 on 2018/1/18.
 */
public abstract class AbstractStreaming implements Serializable {

    public abstract void init(boolean isDebug);

    public void createDStreamDAG() {
        try {
            // 线下需要打开
            if (ConfigUtil.get("offline.debug.checkpoint.dir") != null && !ConfigUtil.get("offline.debug.checkpoint.dir").isEmpty()) {
                System.setProperty("hadoop.home.dir", ConfigUtil.get("offline.debug.hadoop.home.dir"));
            }

            boolean isDebug = "debug".equals(ConfigUtil.get("offline.debug"));

            init(isDebug);

            if (isDebug) {
                System.out.println("debug");
                JavaStreamingContext jsc = createStreamingContext();
                this.doCreateDSteamDAG(jsc.socketTextStream(ConfigUtil.get("offline.debug.socket.ip"), ConfigUtil.getInt("offline.debug.socket.port", 8888))
                    .mapToPair(p -> new Tuple2<>(UUID.randomUUID().toString().replaceAll("-", ""), p))
                );
                jsc.checkpoint(ConfigUtil.get("offline.debug.checkpoint.dir"));
                jsc.start();
                jsc.awaitTermination();
            } else {
                JavaStreamingContext jsc = createStreamingContext();
                this.doCreateDSteamDAG(getInputDStream(jsc));
                this.doCheckPoint(jsc);
                jsc.start();
                jsc.awaitTermination();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
////      checkpoint
//        JavaStreamingContextFactory contextFactory = () -> {
//            JavaStreamingContext jsc = createStreamingContext();
//
////        this.doCreateDSteamDAG(getInputDStream(jsc));
//            doCreateDSteamDAG(jsc.socketTextStream("10.101.72.14", 8333).mapToPair(p -> new Tuple2<>(UUID.randomUUID().toString().replaceAll("-", ""), p)));
//
//            doCheckPoint(jsc);
//
////                jsc.start();
////                jsc.awaitTermination();
//            return jsc;
//        };
//
//        JavaStreamingContext context = JavaStreamingContext.getOrCreate("h:/data/chk_streaming", contextFactory);
//        context.start();
//        context.awaitTermination();
    }

    private JavaStreamingContext createStreamingContext() {
        SparkConf sparkConf = createSparkConf();
        return new JavaStreamingContext(sparkConf, Seconds.apply(ConfigUtil.getInt("kmp.spark.streaming.duration", 1)));// spark batch handler time
    }

    private SparkConf createSparkConf() {
        SparkConf sparkConf = new SparkConf();
        ConfigUtil.getSparkParams().forEach((k, v) -> {
            sparkConf.set(k, v);
        });

        return sparkConf;
    }

    /**
     * 创建输入流(root DStream)
     *
     * @param jsc
     * @return
     */
    protected abstract JavaPairDStream<String, String> getInputDStream(JavaStreamingContext jsc);

    /**
     * 做checkpoint
     *
     * @param jsc
     */
    protected abstract void doCheckPoint(JavaStreamingContext jsc);

    /**
     * 做业务处理
     *
     * @param inputData
     */
    protected abstract void doCreateDSteamDAG(JavaPairDStream<String, String> inputData);
}
