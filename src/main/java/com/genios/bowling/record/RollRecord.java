package com.genios.bowling.record;

/**
 * Contains information about the current roll, whether it was a strike, spare or a miss
 */
public record RollRecord(Integer rollNumber, Integer knockedPins, Status status) {

}
