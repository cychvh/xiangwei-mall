package com.cyc.xiangwei.commodity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyc.xiangwei.commodity.entity.Product;
import com.cyc.xiangwei.commodity.service.ProductService;
import com.cyc.xiangwei.common.utils.Result;
import com.cyc.xiangwei.common.utils.ResultCodeEnum;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/product")
public class productController {

    private final ProductService productService;

    public productController(ProductService productService) {
        this.productService = productService;
    }

    // 💡 辅助方法：安全地从 Header 提取 Integer 类型的参数
    private Integer getIntegerHeader(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "1") int pageNum,
                          @RequestParam(defaultValue = "5") int pageSize,
                          @RequestParam(defaultValue = "") String search,
                          HttpServletRequest request) {


        Integer userId = getIntegerHeader(request, "userId");
        Integer type = getIntegerHeader(request, "type");

        if (userId == null || type == null) {
            return Result.error(ResultCodeEnum.UNAUTHORIZED);
        }
        if (type != 1) {
            return Result.error(ResultCodeEnum.FORBIDDEN);
        }

        LambdaQueryWrapper<Product> query = new LambdaQueryWrapper<>();
        query.eq(Product::getMerchantId, userId);

        if (StringUtils.hasText(search)) {
            query.like(Product::getName, search);
        }

        Page<Product> page = productService.page(new Page<>(pageNum, pageSize), query);
        return Result.success(page);
    }

    @GetMapping("/AU/list")
    public Result<?> listForUser(@RequestParam(defaultValue = "1") int pageNum,
                                 @RequestParam(defaultValue = "8") int pageSize,
                                 @RequestParam(defaultValue = "") String search,
                                 HttpServletRequest request) {

        Integer type = getIntegerHeader(request, "type");
        if (type == null) {
            return Result.error(ResultCodeEnum.UNAUTHORIZED);
        }
        if (type == 1) {
            return Result.error(ResultCodeEnum.FORBIDDEN);
        }

        LambdaQueryWrapper<Product> query = new LambdaQueryWrapper<>();
        query.eq(Product::getStatus, 1);

        if (StringUtils.hasText(search)) {
            query.like(Product::getName, search);
        }

        Page<Product> page = productService.page(new Page<>(pageNum, pageSize), query);
        return Result.success(page);
    }

    @PutMapping("/admin/update")
    public Result<?> updateFromAdmin(@RequestBody Product product, HttpServletRequest request) {
        Integer type = getIntegerHeader(request, "type");
        if (type == null) {
            return Result.error(ResultCodeEnum.UNAUTHORIZED);
        }
        if (type != 0) {
            return Result.error(ResultCodeEnum.FORBIDDEN, "非管理员无权访问");
        }
        Product newProduct = new Product();
        newProduct.setId(product.getId());
        newProduct.setCategoryname(product.getCategoryname());
        productService.updateById(newProduct);
        return Result.success();
    }


    @GetMapping("/user/productOne/{id}")
    public Result<?> productOne(@PathVariable Integer id, HttpServletRequest request) {

        LambdaQueryWrapper<Product> query = new LambdaQueryWrapper<>();
        query.eq(Product::getId, id);
        Product product = productService.getOne(query);

        if (product == null || product.getStatus() != 1) {
            return Result.error(ResultCodeEnum.NOT_FOUND, "产品不存在或已下架");
        }
        return Result.success(product);
    }

    @PostMapping("/add")
    public Result<?> addProduct(@Validated @RequestBody Product product, HttpServletRequest request) {
        Integer userId = getIntegerHeader(request, "userId");
        String username = request.getHeader("username");
        Integer type = getIntegerHeader(request, "type");

        if (userId == null || type == null) {
            return Result.error(ResultCodeEnum.UNAUTHORIZED);
        }
        if (type != 1) {
            return Result.error(ResultCodeEnum.FORBIDDEN);
        }

        username = StringUtils.hasText(username) ? username : "商家" + userId;
        productService.addProduct(product, username, userId);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteProduct(@PathVariable Integer id, HttpServletRequest request) {

        Integer userId = getIntegerHeader(request, "userId");
        Integer type = getIntegerHeader(request, "type");

        if (userId == null || type == null) {
            return Result.error(ResultCodeEnum.UNAUTHORIZED);
        }
        if (type != 1) {
            return Result.error(ResultCodeEnum.FORBIDDEN);
        }

        productService.deleteProduct(id, type, userId);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<?> updateProduct(@Validated @RequestBody Product product, HttpServletRequest request) {
        Integer userId = getIntegerHeader(request, "userId");
        String username = request.getHeader("username");
        Integer type = getIntegerHeader(request, "type");

        if (userId == null || type == null) {
            return Result.error(ResultCodeEnum.UNAUTHORIZED);
        }
        if (type != 1) {
            return Result.error(ResultCodeEnum.FORBIDDEN);
        }

        username = StringUtils.hasText(username) ? username : "商家" + userId;
        productService.updateProduct(product, username, userId, type);
        return Result.success();
    }

    @PutMapping("/status")
    public Result<?> changeStatus(@RequestParam Integer id,
                                  @RequestParam Integer status,
                                  HttpServletRequest request) {

        Integer userId = getIntegerHeader(request, "userId");
        Integer type = getIntegerHeader(request, "type");

        if (userId == null) {
            return Result.error(ResultCodeEnum.UNAUTHORIZED);
        }

        Product dbProduct = productService.getById(id);
        if (dbProduct == null) {
            return Result.error(ResultCodeEnum.ERROR, "商品不存在");
        }

        if (type == null || type != 1 || !userId.equals(dbProduct.getMerchantId())) {
            return Result.error(ResultCodeEnum.FORBIDDEN);
        }

        Product update = new Product();
        update.setId(id);
        update.setStatus(status);

        productService.updateById(update);
        return Result.success();
    }

    @PutMapping("/internal/deductStock")
    public Result<?> deductStock(@RequestBody Product product) {
        // 直接更新数据库（里面包含了计算好的新库存）
        productService.updateById(product);
        return Result.success();
    }
}