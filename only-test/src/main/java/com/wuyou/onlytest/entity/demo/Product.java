package com.wuyou.onlytest.entity.demo;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(name = "Product", description = "商品实体")
@Data
@TableName("demo_product")
public class Product {

    @Schema(description = "商品 ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "商品名称", example = "iPhone 15")
    private String name;

    @Schema(description = "商品价格", example = "6999.00")
    private BigDecimal price;

    @Schema(description = "库存数量", example = "100")
    private Integer stock;

    @Schema(description = "版本号", example = "1")
    @Version
    private Integer version;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
