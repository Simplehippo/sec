package com.sec.util;


import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

/**
 * MD5通用类
 */
public class MD5Util {

    /**
     * 盐值
     */
    private static final String SALT = PropertiesUtil.getProperty("user.salt");


    /**
     * 生成md5
     *
     * @return
     */
    public static String getMD5(String originStr) {
        String base = originStr + SALT;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    /**
     * MD5验证方法
     */
    public static boolean verify(String originStr, String rightMd5) {
        // 根据传入的密钥进行验证
        String originMd5 = getMD5(originStr);
        return StringUtils.equals(originMd5, rightMd5);
    }
}