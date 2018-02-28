package com.liuxi.spark.base.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by lx11915 on 2018/2/24.
 */
public class ConfigUtil {

    private static final Properties props;

    static {
        props = new Properties();
        try {
            props.load(ConfigUtil.class.getResourceAsStream("/profile.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }

    public static Map<String, String> getParamsWithKeyPrefix(String prefix) {
        Map<String, String> params = new HashMap<>();

        props.keySet().forEach(key -> {
            String skey = key.toString();
            if (skey.startsWith(prefix)) {
                params.put(skey, ConfigUtil.get(skey));
            }
        });

        return params;
    }

    public static Map<String, String> getSparkParams() {
        return getParamsWithKeyPrefix("spark.");
    }

    public static int getInt(String key, int defVal) {
        String val = get(key);
        if(val == null || val.isEmpty()) {
            return defVal;
        }

        return Integer.parseInt(val);
    }
}
