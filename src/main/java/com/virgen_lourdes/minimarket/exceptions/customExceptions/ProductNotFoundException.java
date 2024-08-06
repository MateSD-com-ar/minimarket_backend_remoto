package com.virgen_lourdes.minimarket.exceptions.customExceptions;

public class ProductNotFoundException extends RuntimeException{
    public ProductNotFoundException(String message){
        super(message);
    }
}
