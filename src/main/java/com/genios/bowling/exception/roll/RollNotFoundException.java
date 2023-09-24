package com.genios.bowling.exception.roll;

/**
 * Indicates that the roll does not exist.
 */
public class RollNotFoundException extends RuntimeException {

    public RollNotFoundException(String message) {
        super(message);
    }
}
