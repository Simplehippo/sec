package com.sec.controller.rest;

import com.sec.common.Resp;
import com.sec.pojo.Shipping;
import com.sec.service.portal.ShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shipping")
public class ShippingController {

    @Autowired
    private ShippingService shippingService;

    @RequestMapping("/add")
    public Resp add(Shipping shipping) {
        return shippingService.add(shipping);
    }

    @RequestMapping("/delete")
    public Resp delete(@RequestParam("id") Integer shippingId) {
        return shippingService.delete(shippingId);
    }

    @RequestMapping("/update")
    public Resp update(Shipping shipping) {
        return shippingService.update(shipping);
    }

    @RequestMapping("/select")
    public Resp select(@RequestParam("id") Integer shippingId) {
        return shippingService.select(shippingId);
    }

    @RequestMapping("/list")
    public Resp list() {
        return shippingService.list();
    }

}
