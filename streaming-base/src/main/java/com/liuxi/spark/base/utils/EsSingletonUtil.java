package com.liuxi.spark.base.utils;

/**
 * Created by lx11915 on 2018/1/26.
 */
public class EsSingletonUtil {
    private static EsSingletonUtil instance;

    private static ESHelper helper = null;

    private static void setHelper() {
        EsSingletonUtil.helper = new ESHelper();
        EsSingletonUtil.helper.setHosts(ConfigUtil.get("elasticsearch.hosts"));
        EsSingletonUtil.helper.setClusterName(ConfigUtil.get("elasticsearch.clustername"));
        try {
            EsSingletonUtil.helper.init();
        } catch (ESConnectException e) {
            e.printStackTrace();
            System.out.println("setHelper -> " + e.getMessage());
        }
    }

    public synchronized static ESHelper getHelper() {
        if (EsSingletonUtil.helper == null){
            EsSingletonUtil.setHelper();
        }
        return helper;
    }

    public synchronized static EsSingletonUtil getInstance() {
        if(instance == null) {
            instance = new EsSingletonUtil();
        }
        return instance;
    }
}
