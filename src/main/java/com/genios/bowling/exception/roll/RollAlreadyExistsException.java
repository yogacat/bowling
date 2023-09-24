package com.genios.bowling.exception.roll;

/**
 * Indicates that the roll is already saved
 */
public class RollAlreadyExistsException extends RuntimeException {

    public RollAlreadyExistsException(String message) {
        super(message);
    }

}
