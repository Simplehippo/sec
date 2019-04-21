package com.sec.controller.rest;

import com.sec.common.Resp;
import com.sec.service.protal.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    @RequestMapping("/expose")
    public Resp exposeUrl(Integer productId) {
        return seckillService.exposeUrl(productId);
    }

    @RequestMapping("/execute/{certificate}")
    public Resp executeSeckill(@PathVariable("certificate") String certificate, Integer productId) {
        return seckillService.executeSeckill(certificate, productId);
    }

}
