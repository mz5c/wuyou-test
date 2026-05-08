package com.wuyou.onlytest.controller.demo;

import com.wuyou.common.result.Result;
import com.wuyou.onlytest.entity.demo.Product;
import com.wuyou.onlytest.service.demo.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    @GetMapping("/{id}/nocache")
    public Result<Product> getByIdNoCache(@PathVariable Long id) {
        return Result.success(productService.getByIdManualCache(id));
    }

    @PostMapping("/{id}/deduct")
    public Result<Void> deductStock(@PathVariable Long id, @RequestParam int quantity) {
        productService.deductStock(id, quantity);
        return Result.success(null);
    }
}
