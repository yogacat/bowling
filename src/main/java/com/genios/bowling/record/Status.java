package com.genios.bowling.record;

import lombok.Getter;

/**
 * Contains additional info about the roll.
 */
public enum Status {
    STRIKE("X"), SPARE("/"), MISS("-"), NONE(null);

    @Getter
    private final String state;

    Status(String state) {
        this.state = state;
    }
}
