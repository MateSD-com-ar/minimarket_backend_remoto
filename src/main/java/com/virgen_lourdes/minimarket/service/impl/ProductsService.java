package com.virgen_lourdes.minimarket.service.impl;

import com.virgen_lourdes.minimarket.entity.Product;
import com.virgen_lourdes.minimarket.repository.IProductsRepository;
import com.virgen_lourdes.minimarket.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductsService implements IProductService {

    @Autowired
    private IProductsRepository productsRepository;

    //crear producto
    @Override
    public void saveProduct(Product product) {
        if (productsRepository.existsByCode(product.getCode())) {
            throw new RuntimeException("The provided code already exists"); //si ya existe un codigo en la bd lanzamos excepcion
        }
        try {
            productsRepository.save(product);
        } catch (Exception e) {
            throw new RuntimeException("Error creating product");
        }
    }

    //traer un producto por id
    @Override
    public Product getProduct(Long idProduct) {
        Product product = productsRepository.findById(idProduct).orElseThrow(() -> new RuntimeException("Product not found"));
        return product;
    }

    //traer todos los productos
    @Override
    public List<Product> getAllProducts() {
        List<Product> productList = productsRepository.findAll();
        if (productList.isEmpty()) {
            throw new RuntimeException("There are no registered products");
        }
        return productList;
    }

    //eliminar un producto por id
    @Override
    public void deleteProduct(Long idProduct) {
        try {
            productsRepository.deleteById(idProduct);
        } catch (Exception e) {
            throw new RuntimeException("Error deleted product");
        }
    }

    //editar producto
    @Override
    public void editProduct(Long idProduct, Product product) {
        try {
            productsRepository.findById(idProduct).map(product2 -> {
                product2.setName(product.getName());
                product2.setDescription((product.getDescription()));
                product2.setCode(product.getCode());
                product2.setPrice(product.getPrice());
                product2.setRoleProduct(product.getRoleProduct());
                product2.setUnitMeasure(product.getUnitMeasure());
                product2.setStock(product.getStock());
                if (!product2.getCode().equals(product.getCode()) && productsRepository.existsByCode(product.getCode())) {
                    throw new RuntimeException("The provided code already exists");
                } else {
                    productsRepository.save(product2);
                    return "Edited producted";
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Error edit product");
        }
    }

    //traer todos los productos por el nombre
    @Override
    public List<Product> getProductsName(String name) {
        List<Product> productList = productsRepository.findByName(name);
        if (productList.isEmpty()) {
            throw new RuntimeException("There is no product with that name");
        }
        return productList;
    }

    //traer producto por codigo
    @Override
    public Product getProductCode(String code) {
        return productsRepository.findByCode(code).orElseThrow(() -> new RuntimeException("There is no product with that name"));
    }
}
