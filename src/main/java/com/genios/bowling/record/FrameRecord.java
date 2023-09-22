package com.genios.bowling.record;

import java.util.List;

/**
 * Contains information about the current frame and rolls within the frame
 */
public record FrameRecord(Long userId, Integer frameNumber, List<RollRecord> rolls, Integer frameScore) {

}
