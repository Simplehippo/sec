package com.sec.async.handler;

import com.sec.async.EventHandler;
import com.sec.async.EventModel;
import com.sec.async.EventType;
import com.sec.service.func.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


@Component
public class RemindHandler implements EventHandler {

    AtomicInteger threadNo = new AtomicInteger(0);

    private ExecutorService executor = new ThreadPoolExecutor(
            10,
            30,
            0,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(10000),
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "remind handler thread - " + threadNo.getAndIncrement());
                    return thread;
                }
            },
            new ThreadPoolExecutor.AbortPolicy());

    @Autowired
    private MailService mailService;


    @Override
    public void doHandle(EventModel model) {
        Map<String, Object> params = model.getParams();
        Runnable task = new Runnable() {
            @Override
            public void run() {
                // 邮箱提醒用户关注的商品秒杀即将开始
//                mailService.sendHtmlMail("xxx", "xxx", "xxx");
            }
        };
        executor.execute(task);
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.REMIND);
    }
}
