package com.virgen_lourdes.minimarket.exceptions.customExceptions;

public class AuthenticationFailedException extends RuntimeException {

    public AuthenticationFailedException(String message) {
        super(message);
    }
}
