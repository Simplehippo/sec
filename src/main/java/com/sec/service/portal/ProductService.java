package com.sec.service.portal;

import com.sec.common.Codes;
import com.sec.common.Resp;
import com.sec.dao.ProductMapper;
import com.sec.pojo.Product;
import com.sec.util.PropertiesUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    public Resp getProductDetail(Integer productId) {
        if(productId == null) {
            return Resp.error(Codes.ILLEGAL_ARGUMENT.getCode(),"参数错误!");
        }

        Product product = productMapper.selectByPrimaryKey(productId);
        if(product == null) {
            return Resp.error(Codes.ILLEGAL_ARGUMENT.getCode(),"商品已下架或被删除");
        }

        product.setImagePrefix(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return Resp.success("获得商品详情成功", product);
    }

    public Resp getListByKeywordAndLimit(String keyword, Integer pageNum, Integer pageSize) {
        if(StringUtils.isBlank(keyword)) {
            return Resp.error(Codes.ILLEGAL_ARGUMENT.getCode(), "不合法的参数");
        }

        Integer offset = (pageNum - 1) * pageSize;
        List<Product> products = productMapper.selectByKeywordAndLimit(keyword.trim(), offset, pageSize);
        List<Product> retProducts = products.stream().map(e -> {
            e.setImagePrefix(PropertiesUtil.getProperty("ftp.server.http.prefix"));
            return e;
        }).collect(Collectors.toList());
        return Resp.success(retProducts);
    }

}
