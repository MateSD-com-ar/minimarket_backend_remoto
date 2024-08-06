package com.virgen_lourdes.minimarket.service;

import com.virgen_lourdes.minimarket.entity.Product;
import com.virgen_lourdes.minimarket.entity.enums.RoleProduct;

import java.util.List;

public interface IProductService {
    public void saveProduct(Product product);
    public Product getProduct(Long idProduct);
    public List<Product> getAllProducts();
    public void deleteProduct(Long idProduct);
    public void editProduct(Long idProduct, Product product);
    public List<Product> getProductsName(String name);
    public Product getProductCode (String code);
    public List<Product> getProductsAlmacen();
}
