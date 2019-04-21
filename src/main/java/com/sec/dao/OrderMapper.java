package com.sec.dao;

import com.sec.pojo.Order;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Order record);

    int insertSelective(Order record);

    Order selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Order record);

    int updateByPrimaryKey(Order record);






    Order selectByUserIdOrderNo(@Param("userId") Integer userId,
                                @Param("orderNo") Long orderNo);

    Order selectByOrderNo(Long orderNo);

    List<Order> selectByUserIdAndLimit(@Param("userId") Integer userId,
                                       @Param("offset") Integer offset,
                                       @Param("pageSize") Integer pageSize);

    Integer countByUserId(Integer userId);
}