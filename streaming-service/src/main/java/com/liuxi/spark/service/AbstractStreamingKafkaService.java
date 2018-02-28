package com.liuxi.spark.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.liuxi.spark.base.core.AbstractStreamingKafka;
import com.liuxi.spark.base.utils.ConfigUtil;
import com.liuxi.spark.base.utils.EsSingletonUtil;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.Optional;
import org.apache.spark.api.java.function.*;
import org.apache.spark.streaming.Seconds;
import org.apache.spark.streaming.State;
import org.apache.spark.streaming.StateSpec;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

/**
 * Created by lx11915 on 2018/2/24.
 */
public class AbstractStreamingKafkaService extends AbstractStreamingKafka {
    @Override
    public void init(boolean isDebug) {
//        if (!isDebug) {
        EsSingletonUtil.getInstance();
        EsSingletonUtil.getHelper();
//        }
    }

    @Override
    protected void doCheckPoint(JavaStreamingContext jsc) {
        jsc.checkpoint(ConfigUtil.get("checkpoint.dir"));
    }

    @Override
    protected void doCreateDSteamDAG(JavaPairDStream<String, String> inputData) {
        int windowDuration = ConfigUtil.getInt("kmp.spark.streaming.window_duration", 60);
        int slideDuration = ConfigUtil.getInt("kmp.spark.streaming.slide_duration", 1);
        int parallelism = ConfigUtil.getInt("spark.default.parallelism", 1);

        String esIndex = ConfigUtil.get("elasticsearch.index");
        String esType = ConfigUtil.get("elasticsearch.type");
        String esCnt = ConfigUtil.get("elasticsearch.cnt");
        String esCntCreatedate = ConfigUtil.get("elasticsearch.cntcreatedate");

        JavaPairDStream<String, Integer> pair = inputData.mapToPair((PairFunction<Tuple2<String, String>, String, Integer>) tuple -> {
            if (tuple != null && !tuple._2.isEmpty()) {
                return new Tuple2<>(tuple._2, 1);
            }
            return null;
        }).filter((Function<Tuple2<String, Integer>, Boolean>) func -> {
            if (func != null) return true;
            return false;
        }).reduceByKeyAndWindow((a, b) -> a + b,
                (a, b) -> a - b,
                Seconds.apply(windowDuration),
                Seconds.apply(slideDuration),
                parallelism,
                (Function<Tuple2<String, Integer>, Boolean>) f -> f._2 != 0
        );
        pair.mapWithState(StateSpec.function((Function3<String, Optional<Integer>, State<Integer>, Tuple2<String, Integer>>) (s, optional, state) -> {
            int opt = optional.orElse(0);
            if (state.isTimingOut()) {
                return new Tuple2<>(s, opt);
            }
            if (state.exists()) {
                if (opt == 0) {
                    state.remove();
                    return null;
                }
                if (state.get() == opt) {
                    return null;
                }
                state.update(opt);
                return new Tuple2<>(s, opt);
            }
            if (opt == 0) {
                return null;
            }
            state.update(opt);
            return new Tuple2<>(s, opt);
        }).timeout(Seconds.apply(2 * windowDuration)))
        .foreachRDD((VoidFunction<JavaRDD<Tuple2<String, Integer>>>) rdd -> {
            try {
                rdd.foreachPartition(p -> {
                    Date date = new Date();
                    int i = 0;
                    try {
                        Map<String, Map<String, Object>> bulk = new HashMap<>();
                        while (p.hasNext()) {
                            Tuple2<String, Integer> tp = p.next();
                            if (tp == null || tp._1.isEmpty()) continue;
                            bulk.put(tp._1, new HashMap<String, Object>() {{
                                put(esCnt, tp._2);
                                put(esCntCreatedate, date);
                            }});
                            i++;
                            if (i % 200 == 0) {
                                EsSingletonUtil.getHelper().executeBulkUpsert(esIndex, esType, bulk);
                                bulk.clear();
                            }
                        }
                        if (!bulk.isEmpty()) {
                            EsSingletonUtil.getHelper().executeBulkUpsert(esIndex, esType, bulk);
                            bulk = null;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println(">>> foreachrdd err" + e.getMessage());
                    } finally {
                        System.out.println("new -> " + i + " cost(" + (System.currentTimeMillis() - date.getTime()) + "ms)");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("===========> foreachRDD rdd None" + e.getMessage());
            }
            return;
        });
    }
}
