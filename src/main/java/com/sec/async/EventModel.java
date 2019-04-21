package com.sec.async;

import java.util.HashMap;
import java.util.Map;

public class EventModel {

    /**
     * 事件类型
     */
    private EventType type;

    /**
     * 事件现场的数据
     */
    private Map<String, Object> params = new HashMap<>();


    public EventModel() {

    }

    public EventModel(Map<String, Object> params) {
        this.params = params;
    }

    public EventModel(EventType type, Map<String, Object> params) {
        this.type = type;
        this.params = params;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }
}
