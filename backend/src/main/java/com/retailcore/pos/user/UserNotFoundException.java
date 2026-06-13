package com.retailcore.pos.user;

import com.retailcore.pos.common.exception.ResourceNotFoundException;

public class UserNotFoundException extends ResourceNotFoundException {

    public UserNotFoundException(Long id) {
        super("User not found with id: " + id);
    }

    public UserNotFoundException(String email) {
        super("User not found with email: " + email);
    }
}
