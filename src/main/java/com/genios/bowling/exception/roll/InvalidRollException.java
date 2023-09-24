package com.genios.bowling.exception.roll;

/**
 * Indicates that roll data is invalid
 */
public class InvalidRollException extends RuntimeException {

    public InvalidRollException(String message) {
        super(message);
    }
}
