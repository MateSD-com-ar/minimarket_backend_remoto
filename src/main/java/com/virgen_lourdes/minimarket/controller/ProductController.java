package com.virgen_lourdes.minimarket.controller;

import com.virgen_lourdes.minimarket.entity.Product;
import com.virgen_lourdes.minimarket.exceptions.customExceptions.ProductCreationException;
import com.virgen_lourdes.minimarket.exceptions.customExceptions.ProductNotFoundException;
import com.virgen_lourdes.minimarket.service.IProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
@Validated
public class ProductController {

    @Autowired
    private IProductService productService;

    @GetMapping("/get")
    ResponseEntity<?> getAllProducts(){
        try{
            List<Product> productList = productService.getAllProducts();
            return ResponseEntity.ok(productList);
            } catch (RuntimeException e){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/get/{idProduct}")
    ResponseEntity<?> getProductId (@PathVariable Long idProduct){
        try{
            Product product = productService.getProduct(idProduct);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/get/name")
    ResponseEntity<?> getProductName(@RequestParam String name){
        try{
            List<Product> productList = productService.getProductsName(name);
            return ResponseEntity.ok(productList);
        } catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/get/code")
    ResponseEntity<?> getProductCode(@RequestParam String code){
        try{
            Product product = productService.getProductCode(code);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/get/almacen")
    ResponseEntity<?> getProductsAlmacen(){
        try{
            List<Product> productList = productService.getProductsAlmacen();
            return ResponseEntity.ok(productList);
        } catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{idProduct}")
    ResponseEntity<?> deleteProduct(@PathVariable Long idProduct){
        try{
            productService.deleteProduct(idProduct);
            return ResponseEntity.ok("Product deleted");
        } catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        try{
            productService.saveProduct(product);
            return ResponseEntity.ok("Product create");
        } catch (ProductCreationException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/edit/{idProduct}")
    ResponseEntity<?> editProduct(@PathVariable Long idProduct, @RequestBody Product product){
        try{
            productService.editProduct(idProduct, product);
            return ResponseEntity.ok(productService.getProduct(idProduct));
        } catch (ProductNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
