package com.wuyou.onlytest.mapper.demo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wuyou.onlytest.entity.demo.Order;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface OrderMapper extends BaseMapper<Order> {

    List<Order> selectOrderWithItems(@Param("orderId") Long orderId);
}
