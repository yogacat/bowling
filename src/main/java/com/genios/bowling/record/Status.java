package com.genios.bowling.record;

/**
 * Contains additional info about the roll.
 */
public enum Status {
    STRIKE("X"), SPARE("/"), MISS("-"), NONE(null);

    private String state;

    Status(String state) {
        this.state = state;
    }
}
