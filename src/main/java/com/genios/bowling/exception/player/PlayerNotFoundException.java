package com.genios.bowling.exception.player;

/**
 * Indicates that no player was found.
 */
public class PlayerNotFoundException extends RuntimeException {

    public PlayerNotFoundException(String message) {
        super(message);
    }
}
