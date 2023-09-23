package com.genios.bowling.record.request;

import jakarta.validation.constraints.NotNull;

/**
 * Contains information about the roll that must be saved.
 */
public record Roll(@NotNull Integer frameNumber, @NotNull Integer rollNumber, @NotNull Integer pins) {

}
