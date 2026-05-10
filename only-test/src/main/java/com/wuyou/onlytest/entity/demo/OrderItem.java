package com.wuyou.onlytest.entity.demo;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(name = "OrderItem", description = "订单项实体")
@Data
@TableName("demo_order_item")
public class OrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "订单项 ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "订单 ID", example = "1")
    private Long orderId;

    @Schema(description = "商品名称", example = "iPhone 15")
    private String productName;

    @Schema(description = "数量", example = "2")
    private Integer quantity;

    @Schema(description = "单价", example = "6999.00")
    private BigDecimal price;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
