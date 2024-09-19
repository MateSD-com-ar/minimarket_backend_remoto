package com.virgen_lourdes.minimarket.exceptions.customExceptions;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
