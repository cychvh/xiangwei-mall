package com.cyc.xiangwei.commodity.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cyc.xiangwei.commodity.dao.ProductMapper;
import com.cyc.xiangwei.commodity.entity.Product;
import com.cyc.xiangwei.commodity.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Override
    public void addProduct(Product product, String username, Integer userId) {
        product.setMerchantId(userId);
        baseMapper.insert(product);
    }

    @Override
    public void deleteProduct(Integer productId, Integer type, Integer userId) {
        Product dbProduct = baseMapper.selectById(productId);
        if (dbProduct == null) {
            throw new RuntimeException("商品不存在");
        }

        if (type != 1 || !userId.equals(dbProduct.getMerchantId())) {
            throw new RuntimeException("无权删除该商品");
        }

        baseMapper.deleteById(productId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(Product product, String username, Integer userId, Integer type) {
        Product dbProduct = baseMapper.selectById(product.getId());
        if (dbProduct == null) {
            throw new RuntimeException("商品不存在");
        }

        if (type != 1 || !userId.equals(dbProduct.getMerchantId())) {
            throw new RuntimeException("无权修改该商品");
        }

        Product update = new Product();
        update.setId(product.getId());
        update.setName(product.getName());
        update.setPrice(product.getPrice());
        update.setStock(product.getStock());
        update.setOriginplace(product.getOriginplace());
        update.setCategoryname(product.getCategoryname());

        if (StringUtils.hasText(product.getImageurl())) {
            update.setImageurl(product.getImageurl());
        }

        baseMapper.updateById(update);
    }
}
