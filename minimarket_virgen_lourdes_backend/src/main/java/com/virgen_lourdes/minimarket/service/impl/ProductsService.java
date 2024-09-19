package com.virgen_lourdes.minimarket.service.impl;

import com.virgen_lourdes.minimarket.entity.Product;
import com.virgen_lourdes.minimarket.entity.enums.RoleProduct;
import com.virgen_lourdes.minimarket.exceptions.customExceptions.ProductCreationException;
import com.virgen_lourdes.minimarket.exceptions.customExceptions.ProductNotFoundException;
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
        if (product.getRoleProduct()== RoleProduct.Almacen){
            if (productsRepository.existsByCode(product.getCode())) {
                throw new ProductCreationException("The provided code already exists"); //si ya existe un codigo en la bd lanzamos excepcion
            }
            try {
                productsRepository.save(product);
            } catch (Exception e) {
                throw new ProductCreationException("Error creating product. Check that there are no empty fields");
            }
        }
        if (product.getRoleProduct()==RoleProduct.Verduleria || product.getRoleProduct()==RoleProduct.Carniceria){
            try {
                product.setCode(null);
                productsRepository.save(product);
            } catch (Exception e) {
                throw new ProductCreationException("Error creating product. Check that there are no empty fields");
            }
        }
        if (product.getRoleProduct()!=RoleProduct.Almacen && product.getRoleProduct()!=RoleProduct.Carniceria && product.getRoleProduct()!= RoleProduct.Verduleria){
            throw new ProductCreationException("Error creating product. Check that there are no empty fields or that the fields are correctly completed");
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
        Product product2 = productsRepository.findById(idProduct)
                .orElseThrow(() -> new ProductNotFoundException("Product not found. Error editing product"));

        boolean isUpdated = false;

        if (product.getName() != null) {
            product2.setName(product.getName());
            isUpdated = true;
        }
        if (product.getBrand() != null) {
            product2.setBrand(product.getBrand());
            isUpdated = true;
        }
        if (product.getCode() != null) {
            if (!product2.getCode().equals(product.getCode()) && productsRepository.existsByCode(product.getCode())) {
                throw new RuntimeException("The provided code already exists");
            }
            product2.setCode(product.getCode());
            isUpdated = true;
        }
        if (product.getPrice() != null) {
            product2.setPrice(product.getPrice());
            isUpdated = true;
        }
        if (product.getRoleProduct() != null) {
            product2.setRoleProduct(product.getRoleProduct());
            isUpdated = true;
        }
        if (product.getUnitMeasure() != null) {
            product2.setUnitMeasure(product.getUnitMeasure());
            isUpdated = true;
        }
        if (product.getStock() > 0) {
            product2.setStock(product.getStock());
            isUpdated = true;
        }

        if (!isUpdated) {
            throw new RuntimeException("No valid fields to update. Error editing product");
        }

        try {
            productsRepository.save(product2);
        } catch (Exception e) {
            throw new RuntimeException("Error editing product: Empty required fields" );
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
        return productsRepository.findByCode(code).orElseThrow(() -> new RuntimeException("There is no product with that code"));
    }

    @Override
    public List<Product> getProductsAlmacen() {
        List<Product> productList = productsRepository.findByRoleProduct(RoleProduct.Almacen);
        if(productList.isEmpty()){
            throw new RuntimeException("There are no products in stock");
        }
        return productList;
    }
}
