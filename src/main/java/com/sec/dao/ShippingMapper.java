package com.sec.dao;

import com.sec.pojo.Shipping;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShippingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Shipping record);

    int insertSelective(Shipping record);

    Shipping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Shipping record);

    int updateByPrimaryKey(Shipping record);






    int deleteByUserIdShippingId(@Param("userId") Integer userId,
                                 @Param("shippingId") Integer shippingId);

    int updateByUserIdShippingSelective(Shipping shipping);

    Shipping selectByUserIdShippingId(@Param("userId") Integer userId,
                                      @Param("shippingId") Integer shippingId);

    List<Shipping> selectByUserId(Integer userId);

    List<Shipping> selectAllByUserIdAndPageInfo(@Param("userId") Integer userId,
                                                @Param("offset") Integer offset,
                                                @Param("pageSize") Integer pageSize);
}