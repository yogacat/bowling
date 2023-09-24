package com.genios.bowling.exception.frame;

/**
 * Indicates that requested frame does not exist.
 */
public class FrameNotFoundException extends RuntimeException {

    public FrameNotFoundException(String message) {
        super(message);
    }
}
