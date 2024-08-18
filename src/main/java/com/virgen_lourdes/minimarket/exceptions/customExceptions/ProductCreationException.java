package com.virgen_lourdes.minimarket.exceptions.customExceptions;

public class ProductCreationException extends RuntimeException{
    public ProductCreationException(String message){
        super(message);
    }
}
