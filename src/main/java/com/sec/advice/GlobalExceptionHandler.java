package com.sec.advice;

import com.sec.common.Codes;
import com.sec.common.Resp;
import com.sec.exception.FlowLimitException;
import com.sec.exception.EmailException;
import com.sec.exception.NoLoginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一捕捉异常，返回给前台一个json信息，前台根据这个信息显示对应的提示，或者做页面的跳转。
 */
@ControllerAdvice
public class GlobalExceptionHandler {


    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 限流异常
     * @param e
     * @return
     */
    @ExceptionHandler(value = FlowLimitException.class)
    @ResponseBody
    public Resp flowLimitException(FlowLimitException e) {
        // log.info("<!>--> FlowLimitException");
        return Resp.error(Codes.FLOW_LIMIT);
    }


    /**
     * 未登录异常
     * @param e
     * @return
     */
    @ExceptionHandler(value = NoLoginException.class)
    @ResponseBody
    public Resp noLoginException(NoLoginException e) {
        log.info("<!>--> NoLoginException");
        return Resp.error(Codes.NO_LOGIN);
    }


    /**
     * 邮箱异常
     * @param e
     * @return
     */
    @ExceptionHandler(value = EmailException.class)
    @ResponseBody
    public Resp emailException(EmailException e) {
        log.info("<!>--> EmailException");
        String msg = "邮件发送失败!";
        return Resp.error(Codes.ILLEGAL_ARGUMENT.getCode(), msg);
    }



    /**
     * 数据校验异常
     * @param e
     * @return
     */
    @ExceptionHandler(value = BindException.class)
    @ResponseBody
    public Resp validException(BindException  e) {
        BindingResult bindingResult = e.getBindingResult();
        Map<String, String> map = new HashMap<>();

        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            map.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        String errorMsg = map.get("email");
        log.info("<!>--> validException : Invalid Request");
        return Resp.error(Codes.ILLEGAL_ARGUMENT.getCode(), errorMsg);
    }


    /**
     * IO异常
     * @param e
     * @return
     */
    @ExceptionHandler(IOException.class)
    @ResponseBody
    public Resp ioError(IOException e) {
        String eMsg = e.getMessage();
        log.info("<!>--> io错误：{}", eMsg);
        return Resp.error(Codes.IO_ERROR);
    }
}