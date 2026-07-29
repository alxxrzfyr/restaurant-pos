package com.restaurant.pos.exception;

public class CategoryInUseException extends PosException {

    public CategoryInUseException(int itemCount) {
        super("Cannot delete category with " + itemCount + " menu item(s) still assigned to it.");
    }
}
