package com.sec.dao;

import com.sec.pojo.Product;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Product record);

    int insertSelective(Product record);

    Product selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Product record);

    int updateByPrimaryKey(Product record);





    List<Product> selectByKeywordAndLimit(@Param("keyword") String keyword,
                                          @Param("offset") Integer offset,
                                          @Param("pageSize") Integer pageSize);

    int decrementStockById(Integer productId);

    List<Product> selectAllByPageInfo(@Param("offset") Integer offset,
                                      @Param("pageSize") Integer pageSize);
}