package com.virgen_lourdes.minimarket.validation;

import com.virgen_lourdes.minimarket.entity.Product;
import com.virgen_lourdes.minimarket.entity.enums.RoleProduct;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

// Clase que implementa la lógica de validación
public class ProductValidator implements ConstraintValidator<ValidProduct, Product> {

    @Override
    public boolean isValid(Product product, ConstraintValidatorContext context) {
        if (product == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Product cannot be null")
                    .addConstraintViolation();
            return false;
        }

        boolean isValid = true;

        if (product.getRoleProduct() == RoleProduct.Verduleria || product.getRoleProduct() == RoleProduct.Carniceria) {
            isValid = product.getName() != null && !product.getName().isEmpty() &&
                    product.getPrice() != null;

            if (!isValid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Name and price are required for Verduleria or Carniceria")
                        .addConstraintViolation();
            }
        } else if (product.getRoleProduct() == RoleProduct.Almacen) {
            isValid = product.getName() != null && !product.getName().isEmpty() &&
                    product.getBrand() != null && !product.getBrand().isEmpty() &&
                    product.getCode() != null && !product.getCode().isEmpty() &&
                    product.getPrice() != null &&
                    product.getStock() >= 0;

            if (!isValid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("All fields except unitMeasure are required for Almacen")
                        .addConstraintViolation();
            }
        } else {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Invalid RoleProduct")
                    .addConstraintViolation();
            return false;
        }

        return isValid;
    }
}
