package com.sec.interceptor;


import com.sec.exception.FlowLimitException;
import com.sec.service.func.RedisService;
import com.sec.service.portal.UserService;
import com.sec.util.IPUtil;
import com.sec.util.ThreadLocalUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class FlowLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(FlowLimitInterceptor.class);

    @Autowired
    private RedisService redisService;

    @Autowired
    private UserService userService;

    /**
     * 限流的时间区间, 单位为秒
     */
    private static final long FLOW_LIMIT_TIMEOUT = 60;

    /**
     * 总流量控制的hash键名
     */
    private static final String TOTAL_FLOW_LIMIT_KEY = RedisService.FLOW_LIMIT_PREFIX + "total_flow";

    /**
     * 整个系统在时间区间内能透过的总请求数
     * -1代表无限制
     */
    private static final long TOTAL_FLOW_LIMIT_THRESHOLD = -1;

    /**
     * 一个ip在时间区间内最高的请求数
     * -1代表无限制
     */
    private static final long IP_FLOW_LIMIT_THRESHOLD = 120;

    /**
     * 一个用户在时间区间内最高的请求数
     * -1代表无限制
     */
    private static final long USER_FLOW_LIMIT_THRESHOLD = 120;


    /**
     * todo 流量限制。
     *
     * @param request
     * @param response
     * @param handler
     * @return
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 总流量限制, 保护系统。
        redisService.setnx(TOTAL_FLOW_LIMIT_KEY, 0, FLOW_LIMIT_TIMEOUT);
        if (redisService.incr(TOTAL_FLOW_LIMIT_KEY) > TOTAL_FLOW_LIMIT_THRESHOLD
                && TOTAL_FLOW_LIMIT_THRESHOLD > 0) {
            redisService.decr(TOTAL_FLOW_LIMIT_KEY);
            throw new FlowLimitException();
        }


        // 对ip限制
        String ip = IPUtil.getIpAddress(request);
        String ipKey = RedisService.FLOW_LIMIT_IP_PREFIX + ip;
        redisService.setnx(ipKey, 0, FLOW_LIMIT_TIMEOUT);
        if (redisService.incr(ipKey) > IP_FLOW_LIMIT_THRESHOLD
                && IP_FLOW_LIMIT_THRESHOLD > 0) {
            redisService.decr(ipKey);
            throw new FlowLimitException();
        }

        // 对用户限制
        Integer userId = userService.getUserIdByToken();
        String userKey = RedisService.FLOW_LIMIT_USER_PREFIX + userId;
        redisService.setnx(userKey, 0, FLOW_LIMIT_TIMEOUT);
        if (redisService.incr(userKey) > USER_FLOW_LIMIT_THRESHOLD
                && USER_FLOW_LIMIT_THRESHOLD > 0) {
            redisService.decr(userKey);
            throw new FlowLimitException();
        }

        return true;
    }

}
