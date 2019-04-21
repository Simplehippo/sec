package com.sec.service.portal;

import com.google.common.collect.Maps;
import com.sec.common.Codes;
import com.sec.common.Resp;
import com.sec.dao.ShippingMapper;
import com.sec.pojo.Shipping;
import com.sec.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    @Autowired
    private UserService userService;

    public Resp add(Shipping shipping) {
        User loginUser = userService.getUserByToken();
        Integer userId = loginUser.getId();

        if(userId == null || shipping == null) {
            return Resp.error(Codes.ILLEGAL_ARGUMENT.getCode(), "参数错误!");
        }

        shipping.setUserId(userId);
        int rowCount = shippingMapper.insertSelective(shipping);
        if(rowCount > 0) {
            Map<String, Integer> map = Maps.newHashMap();
            map.put("shippingId", shipping.getId());
            return Resp.success("新增地址成功!", map);
        }

        return Resp.error(Codes.ILLEGAL_ARGUMENT.getCode(), "新增地址失败!");
    }

    public Resp delete(Integer shippingId) {
        User loginUser = userService.getUserByToken();
        Integer userId = loginUser.getId();

        if(userId == null || shippingId == null) {
            return Resp.error(Codes.ILLEGAL_ARGUMENT.getCode(), "参数错误!");
        }

        //避免横向越权
        int rowCount = shippingMapper.deleteByUserIdShippingId(userId, shippingId);
        if(rowCount > 0) {
            return Resp.success("删除地址成功!");
        }
        return Resp.error(Codes.ILLEGAL_ARGUMENT.getCode(), "删除地址失败!");
    }

    public Resp update(Shipping shipping) {
        User loginUser = userService.getUserByToken();
        Integer userId = loginUser.getId();

        if(userId == null || shipping == null) {
            return Resp.error(Codes.ILLEGAL_ARGUMENT.getCode(), "参数错误!");
        }

        //避免横向越权
        shipping.setUserId(userId);
        int rowCount = shippingMapper.updateByUserIdShippingSelective(shipping);
        if(rowCount > 0) {
            return Resp.success("修改地址成功!");
        }
        return Resp.error(Codes.ILLEGAL_ARGUMENT.getCode(), "修改地址失败!");
    }

    public Resp select(Integer shippingId) {
        User loginUser = userService.getUserByToken();
        Integer userId = loginUser.getId();

        if(userId == null || shippingId == null) {
            return Resp.error(Codes.ILLEGAL_ARGUMENT.getCode(), "参数错误!");
        }

        //避免横向越权
        Shipping shipping = shippingMapper.selectByUserIdShippingId(userId, shippingId);
        if(shipping != null) {
            return Resp.success("查询地址成功!", shipping);
        }
        return Resp.error(Codes.ILLEGAL_ARGUMENT.getCode(), "查询地址失败!");
    }

    public Resp list() {
        User loginUser = userService.getUserByToken();
        Integer userId = loginUser.getId();
        List<Shipping> shippingList = shippingMapper.selectByUserId(userId);
        return Resp.success("查询列表成功!", shippingList);
    }
}
