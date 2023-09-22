package com.genios.bowling.exception;

/**
 * Indicates that the game is already finished
 */
public class GameAlreadyFinishedException extends RuntimeException {
    public GameAlreadyFinishedException(String message) {
        super(message);
    }
}
