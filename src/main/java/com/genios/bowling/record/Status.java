package com.genios.bowling.record;

import lombok.Getter;

/**
 * Contains additional info about the roll.
 */
public enum Status {
    STRIKE("X", 2), SPARE("/", 1), MISS("-", 0), NONE(null, 0);

    @Getter
    private final String state;
    @Getter
    private final int bonus;

    Status(String state, int bonus) {
        this.state = state;
        this.bonus = bonus;
    }

    public static Status fromString(String value) {
        for (Status status : Status.values()) {
            if (status.state != null && status.state.equals(value)) {
                return status;
            }
        }
        return NONE;
    }
}
