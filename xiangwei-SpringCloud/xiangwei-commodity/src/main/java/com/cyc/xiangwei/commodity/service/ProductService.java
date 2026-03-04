package com.cyc.xiangwei.commodity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cyc.xiangwei.commodity.entity.Product;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ProductService extends IService<Product> {
    //    List<Map<String, Object>> getDailySales(Integer merchantId);
//    List<Map<String, Object>> getProductPieData(Integer merchantId);
    void addProduct(Product product, String username, Integer userId);

    void deleteProduct(Integer productId, Integer type, Integer userId);

    void updateProduct(Product product, String username, Integer userId, Integer type);
}

