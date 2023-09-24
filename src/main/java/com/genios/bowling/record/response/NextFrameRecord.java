package com.genios.bowling.record.response;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Contains data about the next available frame and roll for the user
 */
public record NextFrameRecord(Long userId, @Valid @Min(1) @Max(10) Integer frameNumber,
                              @Valid @Min(1) @Max(3) Integer rollNumber) {

}
