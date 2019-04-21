package com.sec.pojo;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Order {
    private Integer id;

    private Long orderNo;

    private Integer userId;

    private Integer productId;

    private Integer shippingId;

    private BigDecimal payment;

    private Integer status;

    private Timestamp paymentTime;

    private Timestamp sendTime;

    private Timestamp finishTime;

    private Timestamp closeTime;

    private Timestamp createTime;

    private Timestamp updateTime;

    public Order(Integer id, Long orderNo, Integer userId, Integer productId, Integer shippingId, BigDecimal payment, Integer status, Timestamp paymentTime, Timestamp sendTime, Timestamp finishTime, Timestamp closeTime, Timestamp createTime, Timestamp updateTime) {
        this.id = id;
        this.orderNo = orderNo;
        this.userId = userId;
        this.productId = productId;
        this.shippingId = shippingId;
        this.payment = payment;
        this.status = status;
        this.paymentTime = paymentTime;
        this.sendTime = sendTime;
        this.finishTime = finishTime;
        this.closeTime = closeTime;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public Order() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Long orderNo) {
        this.orderNo = orderNo;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getShippingId() {
        return shippingId;
    }

    public void setShippingId(Integer shippingId) {
        this.shippingId = shippingId;
    }

    public BigDecimal getPayment() {
        return payment;
    }

    public void setPayment(BigDecimal payment) {
        this.payment = payment;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Timestamp getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(Timestamp paymentTime) {
        this.paymentTime = paymentTime;
    }

    public Timestamp getSendTime() {
        return sendTime;
    }

    public void setSendTime(Timestamp sendTime) {
        this.sendTime = sendTime;
    }

    public Timestamp getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Timestamp finishTime) {
        this.finishTime = finishTime;
    }

    public Timestamp getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(Timestamp closeTime) {
        this.closeTime = closeTime;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}