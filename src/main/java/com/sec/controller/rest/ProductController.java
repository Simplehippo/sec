package com.sec.controller.rest;

import com.sec.common.Resp;
import com.sec.service.portal.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @RequestMapping(value = "/detail")
    public Resp getDetails(@RequestParam("id") Integer productId) {
        return productService.getProductDetail(productId);
    }

    @RequestMapping(value = "/search")
    public Resp list(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                               @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return productService.getListByKeywordAndLimit(keyword, pageNum, pageSize);
    }

}