package com.restaurant.pos.exception;

public class DuplicateUsernameException extends PosException {

    public DuplicateUsernameException(String username) {
        super("Username already exists: " + username);
    }
}
