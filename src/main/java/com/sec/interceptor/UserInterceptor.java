package com.sec.interceptor;

import com.sec.common.Const;
import com.sec.service.func.RedisService;
import com.sec.util.ThreadLocalUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class UserInterceptor implements HandlerInterceptor {

    public static final Logger log = LoggerFactory.getLogger(UserInterceptor.class);

    @Autowired
    private RedisService redisService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Cookie[] cookies = request.getCookies();
        if(cookies != null) {
            for(Cookie cookie : cookies) {
                if(Const.COOKIE_TOKEN_NAME.equals(cookie.getName())) {
//                    log.info("<!>--> UserInterceptor preHandle(): cookie_name: {}, val: {}", cookie.getName(), cookie.getValue());
                    ThreadLocalUtil.setThreadLocalToken(cookie.getValue());
                    flushTokenExpire(cookie.getValue(), response);
                    return true;
                }
            }
        } else {
//            log.info("<!>--> UserInterceptor preHandle(): cookies is empty");
        }

        String paramToken = request.getParameter("token");
        if(paramToken != null) {
            ThreadLocalUtil.setThreadLocalToken(paramToken);
            flushTokenExpire(paramToken, response);
            return true;
        } else {
//            log.info("<!>--> UserInterceptor preHandle(): paramToken is null");
        }

        return true;
    }


    /**
     * 此时还有请求说明用户还在使用
     * 刷新token过期时间
     */
    private void flushTokenExpire(String token, HttpServletResponse response) {
        // 刷新redis中缓存token过期时间
        String key = RedisService.USER_TOKEN_PREFIX + token;
        redisService.expire(key);

        // 刷新Cookie
        Cookie cookie = new Cookie(Const.COOKIE_TOKEN_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(RedisService.DEFAULT_EXPIRE_TIME);
        cookie.setPath("/");
        response.addCookie(cookie);
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//        log.info("<!>--> UserInterceptor postHandle(): 销毁ThreadLocal变量防止内存泄露");
        ThreadLocalUtil.removeThreadLocalToken(); // 防止内存泄露
    }
}
