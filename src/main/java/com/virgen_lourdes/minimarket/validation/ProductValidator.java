package com.virgen_lourdes.minimarket.validation;

import com.virgen_lourdes.minimarket.entity.Product;
import com.virgen_lourdes.minimarket.entity.enums.RoleProduct;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

// Clase que implementa la lógica de validación
public class ProductValidator implements ConstraintValidator<ValidProduct, Product> {
    @Override
    public boolean isValid(Product product, ConstraintValidatorContext constraintValidatorContext) {
        if(product.getRoleProduct()== RoleProduct.Verduleria || product.getRoleProduct() == RoleProduct.Carniceria){ // Validación para Verduleria y Carniceria: solo name y description es obligatorio
            return product.getName()!= null && !product.getName().isEmpty() && product.getDescription()!=null && !product.getDescription().isEmpty();
        } else if(product.getRoleProduct() == RoleProduct.Almacen){ // Validación para Almacen: todos los campos excepto unitMeasure son obligatorios
            return product.getName()!= null && !product.getName().isEmpty() &&
                    product.getCode()!=null && !product.getCode().isEmpty() &&
                    product.getPrice() != null && product.getPrice()>0 &&
                    product.getStock()>0 && product.getDescription() != null && !product.getDescription().isEmpty();
        }
        return true;
    }
}
