package com.cyc.xiangwei.commodity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyc.xiangwei.commodity.entity.Product;
import com.cyc.xiangwei.commodity.service.ProductService;
import com.cyc.xiangwei.common.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
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

        if (userId == null) {
            return Result.error("401", "未登录");
        }
        if (type == null || type != 1) {
            return Result.error("403", "非商家无权访问");
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

        String typeStr = request.getHeader("type");
        // 如果 type 是 1 (商家)，拒绝普通用户列表页的访问。允许 type=0(管理员), type=2(普通用户), 甚至未登录(null)查看
        if ("1".equals(typeStr)) {
            return Result.error("403", "商家无权访问买家商品大厅");
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
        String typeStr = request.getHeader("type");
        if (!"0".equals(typeStr)) {
            return Result.error("403", "非管理员无权访问");
        }
        Product newProduct = new Product();
        newProduct.setId(product.getId());
        newProduct.setCategoryname(product.getCategoryname());
        productService.updateById(newProduct);
        return Result.success();
    }


    @GetMapping("/user/productOne/{id}")
    public Result<?> productOne(@PathVariable Integer id, HttpServletRequest request) {
        // 商品详情通常是全网公开的，或者是为了给订单微服务(Feign)提供数据。
        // 因此这里【去掉所有 type 限制】，任何人/任何微服务都可以查商品信息。

        LambdaQueryWrapper<Product> query = new LambdaQueryWrapper<>();
        query.eq(Product::getId, id);
        Product product = productService.getOne(query);

        if (product == null || product.getStatus() != 1) {
            return Result.error("405", "产品不存在或已下架");
        }
        return Result.success(product);
    }

    @PostMapping("/add")
    public Result<?> addProduct(@RequestBody Product product, HttpServletRequest request) {

        Integer userId = getIntegerHeader(request, "userId");
        String username = request.getHeader("username"); // 假设网关也把 username 放进了 header
        Integer type = getIntegerHeader(request, "type");

        if (userId == null) {
            return Result.error("401", "未登录");
        }
        if (type == null || type != 1) {
            return Result.error("403", "非商家无权操作");
        }
        try {
            // 如果 username 没有从网关传过来，可以给个默认值或者通过 UserFeignClient 去查
            username = StringUtils.hasText(username) ? username : "商家" + userId;
            productService.addProduct(product, username, userId);
        } catch (Exception e) {
            return Result.error("500", e.getMessage());
        }
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteProduct(@PathVariable Integer id, HttpServletRequest request) {

        Integer userId = getIntegerHeader(request, "userId");
        Integer type = getIntegerHeader(request, "type");

        if (userId == null) {
            return Result.error("401", "未登录");
        }

        try {
            productService.deleteProduct(id, type, userId);
        } catch (Exception e) {
            return Result.error("500", e.getMessage());
        }
        return Result.success();
    }

    @PutMapping("/update")
    public Result<?> updateProduct(@RequestBody Product product, HttpServletRequest request) {

        Integer userId = getIntegerHeader(request, "userId");
        String username = request.getHeader("username");
        Integer type = getIntegerHeader(request, "type");

        if (userId == null) {
            return Result.error("401", "未登录");
        }

        try {
            username = StringUtils.hasText(username) ? username : "商家" + userId;
            productService.updateProduct(product, username, userId, type);
        } catch (Exception e) {
            return Result.error("500", e.getMessage());
        }
        return Result.success();
    }

    @PutMapping("/status")
    public Result<?> changeStatus(@RequestParam Integer id,
                                  @RequestParam Integer status,
                                  HttpServletRequest request) {

        Integer userId = getIntegerHeader(request, "userId");
        Integer type = getIntegerHeader(request, "type");

        if (userId == null) {
            return Result.error("401", "未登录");
        }

        Product dbProduct = productService.getById(id);
        if (dbProduct == null) {
            return Result.error("404", "商品不存在");
        }

        if (type == null || type != 1 || !userId.equals(dbProduct.getMerchantId())) {
            return Result.error("403", "无权操作");
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