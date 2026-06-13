package com.retailcore.pos.auth;

public class InactiveUserException extends RuntimeException {

    public InactiveUserException() {
        super("User account is inactive");
    }
}
