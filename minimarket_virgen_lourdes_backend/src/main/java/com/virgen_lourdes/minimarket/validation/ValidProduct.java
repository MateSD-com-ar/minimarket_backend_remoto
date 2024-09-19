package com.virgen_lourdes.minimarket.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ProductValidator.class) // Asocia esta anotación con la clase ProductValidator
@Target({ElementType.TYPE}) // Puede ser aplicada a clases
@Retention(RetentionPolicy.RUNTIME) // La anotación se mantiene en tiempo de ejecución
public @interface ValidProduct {
    String message() default "error when saving products, empty fields"; // Mensaje de error
    Class<?>[] groups() default {}; // Permite agrupar restricciones
    Class<? extends Payload>[] payload() default {}; // Para información adicional (generalmente no se usa)
}
