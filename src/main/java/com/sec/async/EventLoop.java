package com.sec.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class EventLoop implements InitializingBean, ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(EventLoop.class);

    private ApplicationContext applicationContext;

    private Map<EventType, List<EventHandler>> config = new HashMap<>();

    private LinkedBlockingQueue<EventModel> blockQueue = new LinkedBlockingQueue<>();

    public void putEvent(EventModel model) {
        try {
            blockQueue.put(model);
        } catch (InterruptedException e) {
            log.error("事件循环生产者被中断了!");
            e.printStackTrace();
        }
    }

    private void consume() {
        new Thread(() -> {
            while(true) {
                EventModel model = null;
                try {
                    model = blockQueue.take();
                } catch (InterruptedException e) {
                    log.error("事件循环消费者被中断了!");
                    e.printStackTrace();
                }

                if(!config.containsKey(model.getType())) {
                    log.error("无法识别的事件源!");
                    continue;
                }

                // 执行handler处理事件
                for(EventHandler eventHandler : config.get(model.getType())) {
                    eventHandler.doHandle(model);
                }
            }
        }, "事件循环消费者线程").start();
    }


    @Override
    public void afterPropertiesSet() {
        // 加载支持的事件类型和相对应的handler
        Map<String, EventHandler> beans = applicationContext.getBeansOfType(EventHandler.class);
        if(beans != null) {
            for(Map.Entry<String, EventHandler> entry : beans.entrySet()) {
                EventHandler handler = entry.getValue();
                List<EventType> eventTypes = handler.getSupportEventTypes();
                if(eventTypes != null) {
                    for(EventType eventType : eventTypes) {
                        if(!config.containsKey(eventType)) {
                            config.put(eventType, new ArrayList<>());
                        }
                        config.get(eventType).add(handler);
                    }
                }
            }
        }

        // 启动消费
        consume();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
