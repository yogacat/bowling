package com.genios.bowling.record;

/**
 * Contains data about the next available frame and roll for the user
 */
public record NextFrameRecord(Long userId, Integer frameNumber, Integer rollNumber) {

}
