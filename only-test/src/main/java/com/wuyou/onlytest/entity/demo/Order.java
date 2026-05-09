package com.wuyou.onlytest.entity.demo;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(name = "Order", description = "订单实体")
@Data
@TableName("demo_order")
public class Order {

    @Schema(description = "订单 ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "订单号", example = "ORD20240101001")
    private String orderNo;

    @Schema(description = "用户 ID", example = "1")
    private Long userId;

    @Schema(description = "订单总金额", example = "100.00")
    private BigDecimal totalAmount;

    @Schema(description = "订单状态：0-待支付，1-已支付，2-已取消", example = "0")
    /** 0-pending 1-paid 2-cancelled */
    private Integer status;

    @Schema(description = "订单项列表")
    @TableField(exist = false)
    private List<OrderItem> items;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "是否删除：0-未删除，1-已删除", example = "0")
    @TableLogic
    private Integer deleted;
}
