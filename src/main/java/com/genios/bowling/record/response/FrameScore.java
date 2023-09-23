package com.genios.bowling.record.response;

import java.util.List;

/**
 * Contains the score of the frame and its rolls.
 */
public record FrameScore(Long frameId, Integer frameNumber, boolean isFinalScore, Integer score, List<RollScore> rolls) {

}
