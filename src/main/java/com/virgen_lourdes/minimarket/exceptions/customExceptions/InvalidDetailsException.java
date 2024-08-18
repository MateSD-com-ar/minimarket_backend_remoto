package com.virgen_lourdes.minimarket.exceptions.customExceptions;

public class InvalidDetailsException extends RuntimeException{
    public InvalidDetailsException(String message) {
        super(message);
    }
}
