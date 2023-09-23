package com.genios.bowling.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.genios.bowling.exception.FrameNotFoundException;
import com.genios.bowling.persistance.entity.Frame;
import com.genios.bowling.persistance.entity.Player;
import com.genios.bowling.persistance.entity.Roll;
import com.genios.bowling.persistance.repository.FrameRepository;
import com.genios.bowling.persistance.repository.PlayerRepository;
import com.genios.bowling.persistance.repository.RollRepository;
import com.genios.bowling.record.NextFrameRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GameServiceTest {

    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private FrameRepository frameRepository;
    @Autowired
    private RollRepository rollRepository;
    @Autowired
    private GameService gameService;

    @Test
    void shouldReturnTrueWhenGameIsAlreadyOver() {
        //given
        Player player = new Player(1L, "Max", 100, true, List.of());
        playerRepository.save(player);

        //then
        assertTrue(gameService.isGameOver(1L));
    }

    @Test
    void shouldReturnTrueWhenNoFramesAreLeftNoStrike() {
        //given
        Player player = new Player(1L, "Max", 0, false, List.of());
        playerRepository.save(player);
        Frame lastFrame = new Frame(1L, 10, 1L, 0, player, List.of());
        frameRepository.save(lastFrame);
        Roll roll1 = new Roll(1L, 1L, 1, 7, "/", lastFrame);
        Roll roll2 = new Roll(2L, 1L, 2, 3, "/", lastFrame);
        rollRepository.saveAll(List.of(roll1, roll2));

        //then
        assertTrue(gameService.isGameOver(1L));
    }

    @Test
    void shouldReturnTrueWhenNoFramesAreLeftLastStrike() {
        //given
        Player player = new Player(1L, "Max", 0, false, List.of());
        playerRepository.save(player);
        Frame lastFrame = new Frame(1L, 10, 1L, 0, player, List.of());
        frameRepository.save(lastFrame);
        Roll roll1 = new Roll(1L, 1L, 1, 10, "X", lastFrame);
        Roll roll2 = new Roll(2L, 1L, 2, 3, "/", lastFrame);
        Roll roll3 = new Roll(3L, 1L, 3, 7, "/", lastFrame);
        rollRepository.saveAll(List.of(roll1, roll2, roll3));

        //then
        assertTrue(gameService.isGameOver(1L));
    }

    @Test
    void shouldReturnFalseWhenLastFrameStrike() {
        //given
        Player player = new Player(1L, "Max", 0, false, List.of());
        playerRepository.save(player);
        Frame lastFrame = new Frame(1L, 10, 1L, 0, player, List.of());
        frameRepository.save(lastFrame);
        Roll roll1 = new Roll(1L, 1L, 1, 10, "X", lastFrame);
        Roll roll2 = new Roll(2L, 1L, 2, 3, "/", lastFrame);
        rollRepository.saveAll(List.of(roll1, roll2));

        //then
        assertFalse(gameService.isGameOver(1L));
    }

    @Test
    void shouldReturnNextFrameWhenNoRollsLeftNotLastFrame() {
        //given
        Player player = new Player(1L, "Max", 0, false, List.of());
        playerRepository.save(player);
        Frame lastFrame = new Frame(1L, 5, 1L, 0, player, List.of());
        frameRepository.save(lastFrame);
        Roll roll1 = new Roll(1L, 1L, 1, 10, "X", lastFrame);
        Roll roll2 = new Roll(2L, 1L, 2, 3, "/", lastFrame);
        rollRepository.saveAll(List.of(roll1, roll2));

        //when
        NextFrameRecord nextFrameRecord = gameService.getNextFrame(1L);

        //then
        assertNotNull(nextFrameRecord);
        assertEquals(6, nextFrameRecord.frameNumber());
        assertEquals(1, nextFrameRecord.rollNumber());
    }

    @Test
    void shouldReturnNextFrameWhenRollsLeftNotLastFrame() {
        //given
        Player player = new Player(1L, "Max", 0, false, List.of());
        playerRepository.save(player);
        Frame lastFrame = new Frame(1L, 5, 1L, 0, player, List.of());
        frameRepository.save(lastFrame);
        Roll roll1 = new Roll(1L, 1L, 1, 10, "X", lastFrame);
        rollRepository.saveAll(List.of(roll1));

        //when
        NextFrameRecord nextFrameRecord = gameService.getNextFrame(1L);

        //then
        assertNotNull(nextFrameRecord);
        assertEquals(5, nextFrameRecord.frameNumber());
        assertEquals(2, nextFrameRecord.rollNumber());
    }

    @Test
    void shouldReturnNextFrameWhenFirstRollLastFrame() {
        //given
        Player player = new Player(1L, "Max", 0, false, List.of());
        playerRepository.save(player);
        Frame lastFrame = new Frame(1L, 10, 1L, 0, player, List.of());
        frameRepository.save(lastFrame);
        Roll roll1 = new Roll(1L, 1L, 1, 10, "X", lastFrame);
        rollRepository.saveAll(List.of(roll1));

        //when
        NextFrameRecord nextFrameRecord = gameService.getNextFrame(1L);

        //then
        assertNotNull(nextFrameRecord);
        assertEquals(10, nextFrameRecord.frameNumber());
        assertEquals(2, nextFrameRecord.rollNumber());
    }

    @Test
    void shouldReturnNextFrameWhenSecondRollLastFrameStrike() {
        //given
        Player player = new Player(1L, "Max", 0, false, List.of());
        playerRepository.save(player);
        Frame lastFrame = new Frame(1L, 10, 1L, 0, player, List.of());
        frameRepository.save(lastFrame);
        Roll roll1 = new Roll(1L, 1L, 1, 10, "X", lastFrame);
        Roll roll2 = new Roll(2L, 1L, 2, 10, "X", lastFrame);
        rollRepository.saveAll(List.of(roll1, roll2));

        //when
        NextFrameRecord nextFrameRecord = gameService.getNextFrame(1L);

        //then
        assertNotNull(nextFrameRecord);
        assertEquals(10, nextFrameRecord.frameNumber());
        assertEquals(3, nextFrameRecord.rollNumber());
    }

    @Test
    void shouldSaveRollResultWhenNewFrame() {
        //given
        long userId = 1L;
        long frameId;
        int frameNumber = 1;
        int rollNumber = 1;
        int pins = 3;
        Player player = new Player(userId, "Max", 0, false, List.of());
        playerRepository.save(player);
        NextFrameRecord nextFrameRecord = new NextFrameRecord(userId, frameNumber, rollNumber);

        //when
        gameService.saveRollResult(nextFrameRecord, pins);

        //then
        Optional<Frame> optionalFrame = frameRepository.findOneByUserIdAndFrameNumber(userId, frameNumber);
        assertTrue(optionalFrame.isPresent());
        Frame frame = optionalFrame.get();
        frameId = frame.getId();

        Optional<Roll> optionalRoll = rollRepository.findOneByFrameIdAndRollNumber(frameId, rollNumber);
        assertTrue(optionalRoll.isPresent());
        Roll roll = optionalRoll.get();
        assertEquals(pins, roll.getPins());
        assertNull(roll.getStatus());
    }

    @Test
    void shouldSaveRollResultWhenSameFrame() {
        //given
        long userId = 1L;
        long frameId = 1L;
        int frameNumber = 1;
        int rollNumber = 1;
        int pins = 3;
        Player player = new Player(userId, "Max", 0, false, List.of());
        playerRepository.save(player);
        Frame lastFrame = new Frame(frameId, frameNumber, userId, 0, player, List.of());
        frameRepository.save(lastFrame);
        NextFrameRecord nextFrameRecord = new NextFrameRecord(userId, frameNumber, rollNumber);

        //when
        gameService.saveRollResult(nextFrameRecord, pins);

        //then
        Optional<Frame> optionalFrame = frameRepository.findOneByUserIdAndFrameNumber(userId, frameNumber);
        assertTrue(optionalFrame.isPresent());
        Frame frame = optionalFrame.get();
        frameId = frame.getId();

        Optional<Roll> optionalRoll = rollRepository.findOneByFrameIdAndRollNumber(frameId, rollNumber);
        assertTrue(optionalRoll.isPresent());
        Roll roll = optionalRoll.get();
        assertEquals(pins, roll.getPins());
        assertNull(roll.getStatus());
    }

    @Test
    void shouldSaveRollResultWhenFirstRollStrike() {
        //given
        long userId = 1L;
        long frameId;
        int frameNumber = 1;
        int rollNumber = 1;
        int pins = 10;
        Player player = new Player(userId, "Max", 0, false, List.of());
        playerRepository.save(player);
        NextFrameRecord nextFrameRecord = new NextFrameRecord(userId, frameNumber, rollNumber);

        //when
        gameService.saveRollResult(nextFrameRecord, pins);

        //then
        Optional<Frame> optionalFrame = frameRepository.findOneByUserIdAndFrameNumber(userId, frameNumber);
        assertTrue(optionalFrame.isPresent());
        Frame frame = optionalFrame.get();
        frameId = frame.getId();

        Optional<Roll> optionalRoll = rollRepository.findOneByFrameIdAndRollNumber(frameId, rollNumber);
        assertTrue(optionalRoll.isPresent());
        Roll roll = optionalRoll.get();
        assertEquals(pins, roll.getPins());
        assertEquals("X", roll.getStatus());
    }

    @Test
    void shouldSaveRollResultWhenTwoRolesSpare() {
        //given
        long userId = 1L;
        long frameId = 1L;
        int frameNumber = 1;
        int rollNumber = 2;
        int pins = 3;
        Player player = new Player(userId, "Max", 0, false, List.of());
        playerRepository.save(player);
        Frame lastFrame = new Frame(frameId, frameNumber, userId, 0, player, List.of());
        frameRepository.save(lastFrame);
        Roll firstRoll = new Roll(1L, frameId, 1, 7, null, lastFrame);
        rollRepository.save(firstRoll);
        NextFrameRecord nextFrameRecord = new NextFrameRecord(userId, frameNumber, rollNumber);

        //when
        gameService.saveRollResult(nextFrameRecord, pins);

        //then
        Optional<Frame> optionalFrame = frameRepository.findOneByUserIdAndFrameNumber(userId, frameNumber);
        assertTrue(optionalFrame.isPresent());
        Frame frame = optionalFrame.get();
        frameId = frame.getId();

        Optional<Roll> optionalRoll = rollRepository.findOneByFrameIdAndRollNumber(frameId, rollNumber);
        assertTrue(optionalRoll.isPresent());
        Roll roll = optionalRoll.get();
        assertEquals(pins, roll.getPins());
        assertEquals("/", roll.getStatus());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void shouldSaveRollResultWhenRollMiss(int rollNumber) {
        //given
        long userId = 1L;
        long frameId = 1L;
        int frameNumber = 1;
        int pins = 0;
        Player player = new Player(userId, "Max", 0, false, List.of());
        playerRepository.save(player);
        Frame lastFrame = new Frame(frameId, frameNumber, userId, 0, player, List.of());
        frameRepository.save(lastFrame);
        NextFrameRecord nextFrameRecord = new NextFrameRecord(userId, frameNumber, rollNumber);

        //when
        gameService.saveRollResult(nextFrameRecord, pins);

        //then
        Optional<Frame> optionalFrame = frameRepository.findOneByUserIdAndFrameNumber(userId, frameNumber);
        assertTrue(optionalFrame.isPresent());
        Frame frame = optionalFrame.get();
        frameId = frame.getId();

        Optional<Roll> optionalRoll = rollRepository.findOneByFrameIdAndRollNumber(frameId, rollNumber);
        assertTrue(optionalRoll.isPresent());
        Roll roll = optionalRoll.get();
        assertEquals(pins, roll.getPins());
        assertEquals("-", roll.getStatus());
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3})
    void shouldThrowExceptionWhenFrameDoesNotExistNotTheFirstRoll(int rollNumber) {
        //given
        long userId = 1L;
        int frameNumber = 1;
        int pins = 3;
        Player player = new Player(userId, "Max", 0, false, List.of());
        playerRepository.save(player);
        NextFrameRecord nextFrameRecord = new NextFrameRecord(userId, frameNumber, rollNumber);

        //when
        FrameNotFoundException thrown = Assertions.assertThrows(FrameNotFoundException.class,
            () -> gameService.saveRollResult(nextFrameRecord, pins));

        //then
        assertEquals("Frame with number 1 not found. For the roll that's higher than one it must exist",
            thrown.getMessage());
    }
}