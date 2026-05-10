package com.wuyou.onlytest.controller.demo;

import com.wuyou.common.result.Result;
import com.wuyou.common.result.ResultCode;
import com.wuyou.onlytest.entity.demo.Product;
import com.wuyou.onlytest.service.demo.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "商品管理", description = "商品库存管理接口")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "根据 ID 查询商品")
    @GetMapping("/{id}")
    public Result<Product> getById(@Parameter(description = "商品 ID") @PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    @Operation(summary = "查询商品（不查缓存）")
    @GetMapping("/{id}/nocache")
    public Result<Product> getByIdNoCache(@Parameter(description = "商品 ID") @PathVariable Long id) {
        return Result.success(productService.getByIdManualCache(id));
    }

    @Operation(summary = "扣减库存")
    @PostMapping("/{id}/deduct")
    public Result<Void> deductStock(@Parameter(description = "商品 ID") @PathVariable Long id, 
                                    @Parameter(description = "扣减数量", required = true) @RequestParam int quantity) {
        productService.deductStock(id, quantity);
        return Result.success(null);
    }

    @Operation(summary = "乐观锁扣减库存", description = "使用 @Version 乐观锁防止并发冲突")
    @PostMapping("/{id}/deduct-optimistic")
    public Result<Void> deductOptimistic(@Parameter(description = "商品 ID") @PathVariable Long id, 
                                         @Parameter(description = "扣减数量", required = true) @RequestParam int quantity) {
        Product product = productService.getById(id);
        if (product == null) {
            return Result.fail(ResultCode.NOT_FOUND.getCode(), "product not found");
        }
        if (product.getStock() < quantity) {
            return Result.fail(ResultCode.BIZ_ERROR.getCode(), "insufficient stock");
        }
        Product update = new Product();
        update.setId(product.getId());
        update.setStock(product.getStock() - quantity);
        update.setVersion(product.getVersion());
        boolean success = productService.updateById(update);
        if (!success) {
            return Result.fail(ResultCode.BIZ_ERROR.getCode(), "optimistic lock conflict, retry");
        }
        productService.evictCache(id);
        return Result.success(null);
    }
}
