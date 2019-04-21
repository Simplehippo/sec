package com.sec.async;

import java.util.List;

public interface EventHandler {

    /**
     * 处理事件
     * @param model
     */
    void doHandle(EventModel model);


    /**
     * 支持的事件类型
     * @return
     */
    List<EventType> getSupportEventTypes();
}
