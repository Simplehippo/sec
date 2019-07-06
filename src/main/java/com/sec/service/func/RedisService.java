package com.sec.service.func;

import com.sec.util.ConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {
    // 分隔符
    public static final String DELIMITER = ":";

    // 过期时间
    public static final int DEFAULT_EXPIRE_TIME = 60 * 60 * 24;
    public static final int NOT_EXPIRE = -1;

    // 用户
    public static final String USER_TOKEN_PREFIX = "user:login:token:";
    public static final String USER_REGISTER_CODE_PREFIX = "user:register:code:";

    // 秒杀
    public static final String SECKILL_PRODUCT_PREFIX = "seckill:product:";
    public static final String SECKILL_SUCCESS_PREFIX = "seckill:success:";
    public static final String SECKILL_ORDER_PREFIX = "seckill:order:";

    // 限流
    public static final String FLOW_LIMIT_PREFIX = "flow:limit:prefix:";


    @Autowired
    private RedisTemplate redisTemplate;






    public <T> T get(String key, Class<T> clazz) {
        Object obj = redisTemplate.opsForValue().get(key);
        return (T)obj;
    }

    public <T> T get(String prefix, String key, Class<T> clazz) {
        return get(prefix + key, clazz);
    }

    public void set(String key, Object value, long time, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, time, unit);
    }

    public void set(String key, Object value) {
        set(key, value, DEFAULT_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    public void set(String key, Object value, long second) {
        set(key, value, second, TimeUnit.SECONDS);
    }

    public void set(String prefix, String key, Object value) {
        set(prefix + key, value, DEFAULT_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    public void setnx(String key, Object value) {
        setnx(key, value, DEFAULT_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    public void setnx(String key, Object value, long second) {
        setnx(key, value, second, TimeUnit.SECONDS);
    }

    public void setnx(String key, Object value, long time, TimeUnit unit) {
        redisTemplate.opsForValue().setIfAbsent(key, value, time, unit);
    }

    public Long decr(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    public Long incr(String key) {
        return redisTemplate.opsForValue().increment(key);
    }







    public void hmput(String key, Map value) {
        redisTemplate.opsForHash().putAll(key, value);
    }

    public void hmput(String prefix, String key, Map value) {
        hmput(prefix + key, value);
    }

    public <T> T hmget(String key, Class<T> clazz) {
        Map<String, Object> entries = redisTemplate.opsForHash().entries(key);
        return ConvertUtil.mapToObject(entries, clazz);
    }

    public <T> T hmget(String prefix, String key, Class<T> clazz) {
        return hmget(prefix + key, clazz);
    }

    public boolean hmexist(String key) {
        return redisTemplate.opsForHash().keys(key).size() != 0;
    }

    public boolean hexist(String key, String hk) {
        return redisTemplate.opsForHash().hasKey(key, hk);
    }

    public void hput(String key, String hk, Object hv) {
        redisTemplate.opsForHash().put(key, hk, hv);
    }

    public <T> T  hget(String key, String hk, Class<T> clazz) {
        return (T)redisTemplate.opsForHash().get(key, hk);
    }

    public long hdecr(String key, String hk) {
        return redisTemplate.opsForHash().increment(key, hk, -1);
    }

    public long hincr(String key, String hk) {
        return redisTemplate.opsForHash().increment(key, hk, 1);
    }







    public void sadd(String key, Object value) {
        redisTemplate.opsForSet().add(key, value);
    }

    public boolean sismember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    public boolean srem(String key, Object value) {
        return redisTemplate.opsForSet().remove(key, value) != 0;
    }






    public void expire(String key) {
        expire(key, DEFAULT_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    public void expire(String key, long second) {
        expire(key, second, TimeUnit.SECONDS);
    }

    public void expire(String key, Timestamp endTime) {
        long expire = endTime.getTime() - System.currentTimeMillis();
        expire(key, expire, TimeUnit.MILLISECONDS);
    }

    public void expire(String key, long time, TimeUnit unit) {
        redisTemplate.expire(key, time, unit);
    }

    public void del(String key) {
        redisTemplate.delete(key);
    }

    public void del(String prefix, String key) {
        del(prefix + key);
    }

}
