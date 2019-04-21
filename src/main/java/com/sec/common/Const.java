package com.sec.common;

public class Const {

    public static final String COOKIE_TOKEN_NAME = "token";

    public interface  AlipayCallback{
        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
        String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";

        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAILED = "failed";
    }

    public enum OrderStatus {
        CANCELED(0, "已取消"),
        NO_PAY(1, "未支付"),
        PAID(2, "已付款"),
        SHIPPED(3, "已发货"),
        ORDER_SUCCESS(4, "订单完成"),
        ORDER_CLOSE(5, "订单关闭");

        private int code;
        private String value;

        OrderStatus(int code, String value) {
            this.code = code;
            this.value = value;
        }

        public static OrderStatus codeOf(int code){
            for(OrderStatus orderStatusEnum : values()){
                if(orderStatusEnum.getCode() == code){
                    return orderStatusEnum;
                }
            }
            throw new RuntimeException("没有找到对应的枚举");
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

}
