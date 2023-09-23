package com.genios.bowling.exception;

/**
 * Indicates that the game was not finished yet.
 */
public class GameNotFinishedException extends RuntimeException {

    public GameNotFinishedException(String message) {
        super(message);
    }
}
