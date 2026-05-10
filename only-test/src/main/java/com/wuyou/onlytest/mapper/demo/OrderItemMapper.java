package com.wuyou.onlytest.mapper.demo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wuyou.onlytest.entity.demo.OrderItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    @Insert("<script>" +
            "INSERT INTO demo_order_item(order_id, product_name, quantity, price, create_time) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.orderId}, #{item.productName}, #{item.quantity}, #{item.price}, NOW())" +
            "</foreach>" +
            "</script>")
    int insertBatch(@Param("list") List<OrderItem> items);

    int insertBatchXml(@Param("list") List<OrderItem> items);
}
