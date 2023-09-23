package com.genios.bowling.record.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request for the rest controller with the player name.
 */
public record Player(@NotBlank String name) {

}
