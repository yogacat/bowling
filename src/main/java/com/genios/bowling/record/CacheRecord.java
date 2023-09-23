package com.genios.bowling.record;

/**
 * Structure used in a cache to be able to calculate bonuses on spare and strike without recalculating the whole thing.
 */
public record CacheRecord(int frameNumber, int rollNumber, int pins, int bonus) {

}
