package com.sec.service.func;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {
    public static final int DEFAULT_EXPIRE_TIME = 60 * 60 * 24;
    public static final int NOT_EXPIRE = -1;

    // 用户相关
    public static final String USER_TOKEN_PREFIX = "user-login-token-";
    public static final String USER_REGISTER_CODE_PREFIX = "user-register-code-";

    // 秒杀相关
    public static final String SECKILL_ERROR_ID_PREFIX = "seckill-error-id-";
    public static final String SECKILL_PRODUCT_PREFIX = "seckill-product-";
    public static final String SECKILL_STOCK_PREFIX = "seckill-stock-";
    public static final String SECKILL_EXPOSE_PREFIX = "seckill-expose-";
    public static final String SECKILL_SUCCESS_PREFIX = "seckill-success-";

    // 限流相关
    public static final String FLOW_LIMIT_PREFIX = "flow-limit-prefix-";


    @Autowired
    private RedisTemplate redisTemplate;



    public <T> T get(String key, Class<T> clazz) {
        Object obj = redisTemplate.opsForValue().get(key);
        return (T)obj;
    }

    public <T> T get(String prefix, String key, Class<T> clazz) {
        Object obj = redisTemplate.opsForValue().get(prefix + key);
        return (T)obj;
    }



    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value, DEFAULT_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    public void set(String prefix, String key, Object value) {
        redisTemplate.opsForValue().set(prefix + key, value, DEFAULT_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    public void set(String prefix, String key, Object value, int second) {
        redisTemplate.opsForValue().set(prefix + key, value, second, TimeUnit.SECONDS);
    }

    public void set(String prefix, String key, Object value, int time, TimeUnit unit) {
        redisTemplate.opsForValue().set(prefix + key, value, time, unit);
    }



    public void expire(String prefix, String key) {
        redisTemplate.expire(prefix + key, DEFAULT_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    public void expire(String prefix, String key, int second) {
        redisTemplate.expire(prefix + key, second, TimeUnit.SECONDS);
    }

    public void expire(String prefix, String key, int time, TimeUnit unit) {
        redisTemplate.expire(prefix + key, time, unit);
    }



    public void del(String key) {
        redisTemplate.delete(key);
    }

    public void del(String prefix, String key) {
        redisTemplate.delete(prefix + key);
    }



    public Long desr(String prefix, String key) {
        return redisTemplate.opsForValue().decrement(prefix + key);
    }

    public Long incr(String prefix, String key) {
        return redisTemplate.opsForValue().increment(prefix + key);
    }
}
