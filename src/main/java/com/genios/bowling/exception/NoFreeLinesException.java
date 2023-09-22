package com.genios.bowling.exception;

/**
 * Exception indicating that there are no available lines to play are left.
 */
public class NoFreeLinesException extends RuntimeException {

    public NoFreeLinesException(String message) {
        super(message);
    }
}
