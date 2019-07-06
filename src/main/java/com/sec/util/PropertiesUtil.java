package com.sec.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

public class PropertiesUtil {

    private static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);

    /**
     * 自定义配置屬性
     */
    private static Properties props;


    /**
     * 加载自定义配置文件
     */
    static {
        String fileName = "custom.properties";
        props = new Properties();
        try {
            props.load(new InputStreamReader(PropertiesUtil.class.getClassLoader().getResourceAsStream(fileName), "UTF-8"));
        } catch (IOException e) {
            logger.error("配置文件读取失败", e);
        }
    }


    /**
     * 获取自定义配置属性
     * @param key
     * @return
     */
    public static String getProperty(String key) {
        String value = props.getProperty(key.trim());
        if (value == null) {
            return null;
        }

        return value.trim();
    }


    /**
     * 获取自定义配置属性，若无，返回传入的默认值
     * @param key
     * @return
     */
    public static String getProperty(String key, String defaultValue) {
        String value = props.getProperty(key.trim());
        if (value == null) {
            return defaultValue;
        }

        return value.trim();
    }

}
