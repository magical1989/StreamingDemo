package com.liuxi.spark.starter;

import com.liuxi.spark.service.AbstractStreamingKafkaService;

/**
 * Created by lx11915 on 2018/2/27.
 */
public class App
{
    public static void main( String[] args )
    {
        new AbstractStreamingKafkaService().createDStreamDAG();
    }
}
