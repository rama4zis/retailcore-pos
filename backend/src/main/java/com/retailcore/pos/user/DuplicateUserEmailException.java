package com.retailcore.pos.user;

import com.retailcore.pos.common.exception.DuplicateResourceException;

public class DuplicateUserEmailException extends DuplicateResourceException {

    public DuplicateUserEmailException(String email) {
        super("User email already exists: " + email);
    }
}
