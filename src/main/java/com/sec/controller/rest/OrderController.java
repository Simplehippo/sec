package com.sec.controller.rest;

import com.sec.common.Const;
import com.sec.common.Resp;
import com.sec.service.protal.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @RequestMapping("/poll")
    public Resp poll(Integer productId){
        return orderService.pollOrderDetail(productId);
    }

    @RequestMapping("/detail")
    public Resp detail(Long orderNo){
        return orderService.getOrderDetail(orderNo);
    }

    @RequestMapping("/update")
    public Resp update(Long orderNo, Integer shippingId) {
        return orderService.updateOrder(orderNo, shippingId);
    }

    @RequestMapping("/cancel")
    public Resp cancel(Long orderNo){
        return orderService.cancel(orderNo);
    }

    @RequestMapping("/list")
    public Resp list(@RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                     @RequestParam(value = "pageSize",defaultValue = "10") Integer pageSize){
        return orderService.getOrderList(pageNum, pageSize);
    }

    @RequestMapping("/pay")
    public Resp pay(Long orderNo) {
        return orderService.pay(orderNo);
    }

    @RequestMapping("/alipay_call_back")
    public Object alipayCallback(HttpServletRequest request) {
        Resp response;
        try {
            response = orderService.alipayCallback(request);
        } catch (Exception e) {
            return Const.AlipayCallback.RESPONSE_FAILED;
        }

        if(response.isSuccess()) {
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        }
        return Const.AlipayCallback.RESPONSE_FAILED;
    }
}
