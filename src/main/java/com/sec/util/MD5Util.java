package com.sec.util;


import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

/**
 * MD5通用类
 * 
 * @author 浩令天下
 * @since 2017.04.15
 * @version 1.0.0_1
 * 
 */
public class MD5Util {
    //盐
    private static final String slat = PropertiesUtil.getProperty("user.salt");


    /**
     * 生成md5
     * @return
     */
    public static String getMD5(String originStr) {
        String base = originStr + slat;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    /**
     * MD5验证方法
     */
    public static boolean verify(String originStr, String rightMd5) {
        //根据传入的密钥进行验证
        String originMd5 = getMD5(originStr);
        return StringUtils.equals(originMd5, rightMd5);
    }
}