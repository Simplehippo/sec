package com.sec.interceptor;


import com.sec.exception.FlowLimitException;
import com.sec.service.func.RedisService;
import com.sec.util.IPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class FlowLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(FlowLimitInterceptor.class);

    @Autowired
    private RedisService redisService;

    // 限制一个ip一分钟只能请求60次
    private static final Long MAX_COUNT_PER_MINUTE = 100000L;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = IPUtil.getIpAddress(request);
        Integer count = redisService.get(RedisService.FLOW_LIMIT_PREFIX, ip, Integer.class);
        // todo 需要同步, 避免并发下不安全, 简单同步一下, 此时单应用有效同步
        if(count == null) {
            // 只让一个初始化成功, 其他失败者接着向下执行
            synchronized (this) {
                count = redisService.get(RedisService.FLOW_LIMIT_PREFIX, ip, Integer.class);
                if(count == null) {
                    redisService.set(RedisService.FLOW_LIMIT_PREFIX, ip, 1, 60);
//                    log.info("ip: {}  request count: {} (init)", ip, 1);
                    return true;
                }
            }
        }
        // 此时到了这一步count有两种情况: 都视为一次请求处理
        // 1. 尝试初始化失败的
        // 2. 初始化之后, 才正常过来的请求
        Long newCount = redisService.incr(RedisService.FLOW_LIMIT_PREFIX, ip);
        if(newCount > MAX_COUNT_PER_MINUTE) {
            // 减回去, 虽然没什么必要?
            redisService.desr(RedisService.FLOW_LIMIT_PREFIX, ip);
            throw new FlowLimitException();
        }
//        log.info("ip: {}  request count: {}", ip, newCount);
        return true;
    }
}
