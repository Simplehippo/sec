package com.sec.async;

public enum EventType {
    REMIND(0),
    SECKILL(1)
   ;

    private int value;

    EventType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
