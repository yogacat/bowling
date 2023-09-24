package com.genios.bowling.record.response;

import java.util.List;

/**
 * Contains all the frames and the score for the player.
 */
public record IntermediateScore(Long userId, String name, boolean isGameOver, Integer finalScore,
                                List<FrameScore> frames) {

}
