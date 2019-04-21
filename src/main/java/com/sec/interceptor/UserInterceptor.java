package com.sec.interceptor;

import com.sec.common.Const;
import com.sec.util.ThreadLocalUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class UserInterceptor implements HandlerInterceptor {

    public static final Logger log = LoggerFactory.getLogger(UserInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Cookie[] cookies = request.getCookies();
        if(cookies != null) {
            for(Cookie cookie : cookies) {
                if(Const.COOKIE_TOKEN_NAME.equals(cookie.getName())) {
//                    log.info("<!>--> UserInterceptor preHandle(): cookie_name: {}, val: {}", cookie.getName(), cookie.getValue());
                    ThreadLocalUtil.setThreadLocalToken(cookie.getValue());
                    return true;
                }
            }
        } else {
//            log.info("<!>--> UserInterceptor preHandle(): cookies is empty");
        }

        String paramToken = request.getParameter("token");
        if(paramToken != null) {
            ThreadLocalUtil.setThreadLocalToken(paramToken);
        } else {
//            log.info("<!>--> UserInterceptor preHandle(): paramToken is null");
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//        log.info("<!>--> UserInterceptor postHandle(): 销毁ThreadLocal变量防止内存泄露");
        ThreadLocalUtil.removeThreadLocalToken(); // 防止内存泄露
    }
}
