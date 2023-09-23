package com.genios.bowling.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.genios.bowling.persistance.entity.Frame;
import com.genios.bowling.persistance.entity.Player;
import com.genios.bowling.persistance.repository.FrameRepository;
import com.genios.bowling.persistance.repository.PlayerRepository;
import com.genios.bowling.persistance.repository.RollRepository;
import com.genios.bowling.record.NextFrameRecord;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class GameServiceScenarioTest {

    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private FrameRepository frameRepository;
    @Autowired
    private RollRepository rollRepository;
    @Autowired
    private GameService gameService;

    @Test
    @Transactional
    void shouldReturn167() {
        //given
        long userId = 1L;
        Player player = new Player(1L, "Max", 0, false, List.of());
        playerRepository.save(player);

        //when
        gameService.saveRollResult(new NextFrameRecord(userId, 1, 1), 10);
        gameService.saveRollResult(new NextFrameRecord(userId, 2, 1), 7);
        gameService.saveRollResult(new NextFrameRecord(userId, 2, 2), 3);
        gameService.saveRollResult(new NextFrameRecord(userId, 3, 1), 9);
        gameService.saveRollResult(new NextFrameRecord(userId, 3, 2), 0);
        gameService.saveRollResult(new NextFrameRecord(userId, 4, 1), 10);
        gameService.saveRollResult(new NextFrameRecord(userId, 5, 1), 0);
        gameService.saveRollResult(new NextFrameRecord(userId, 5, 2), 8);
        gameService.saveRollResult(new NextFrameRecord(userId, 6, 1), 0);
        gameService.saveRollResult(new NextFrameRecord(userId, 6, 2), 10);
        gameService.saveRollResult(new NextFrameRecord(userId, 7, 1), 0);
        gameService.saveRollResult(new NextFrameRecord(userId, 7, 2), 6);
        gameService.saveRollResult(new NextFrameRecord(userId, 8, 1), 10);
        gameService.saveRollResult(new NextFrameRecord(userId, 9, 1), 10);
        gameService.saveRollResult(new NextFrameRecord(userId, 10, 1), 10);
        gameService.saveRollResult(new NextFrameRecord(userId, 10, 2), 8);
        gameService.saveRollResult(new NextFrameRecord(userId, 10, 3), 1);

        //then
        Optional<Frame> optional1 = frameRepository.findOneByUserIdAndFrameNumber(userId, 1);
        assertTrue(optional1.isPresent());
        assertEquals(20, optional1.get().getFrameScore());
        Optional<Frame> optional2 = frameRepository.findOneByUserIdAndFrameNumber(userId, 2);
        assertTrue(optional2.isPresent());
        assertEquals(39, optional2.get().getFrameScore());
        Optional<Frame> optional3 = frameRepository.findOneByUserIdAndFrameNumber(userId, 3);
        assertTrue(optional3.isPresent());
        assertEquals(48, optional3.get().getFrameScore());
        Optional<Frame> optional4 = frameRepository.findOneByUserIdAndFrameNumber(userId, 4);
        assertTrue(optional4.isPresent());
        assertEquals(66, optional4.get().getFrameScore());
        Optional<Frame> optional5 = frameRepository.findOneByUserIdAndFrameNumber(userId, 5);
        assertTrue(optional5.isPresent());
        assertEquals(74, optional5.get().getFrameScore());
        Optional<Frame> optional6 = frameRepository.findOneByUserIdAndFrameNumber(userId, 6);
        assertTrue(optional6.isPresent());
        assertEquals(84, optional6.get().getFrameScore());
        Optional<Frame> optional7 = frameRepository.findOneByUserIdAndFrameNumber(userId, 7);
        assertTrue(optional7.isPresent());
        assertEquals(90, optional7.get().getFrameScore());
        Optional<Frame> optional8 = frameRepository.findOneByUserIdAndFrameNumber(userId, 8);
        assertTrue(optional8.isPresent());
        assertEquals(120, optional8.get().getFrameScore());
        Optional<Frame> optional9 = frameRepository.findOneByUserIdAndFrameNumber(userId, 9);
        assertTrue(optional9.isPresent());
        assertEquals(148, optional9.get().getFrameScore());
        Optional<Frame> optional10 = frameRepository.findOneByUserIdAndFrameNumber(userId, 10);
        assertTrue(optional10.isPresent());
        assertEquals(167, optional10.get().getFrameScore());

        assertEquals(167, gameService.getFinalResult(userId));
    }

    @Test
    @Transactional
    void shouldReturn300WhenPerfectScore() {
        //given
        long userId = 1L;
        Player player = new Player(1L, "Max", 0, false, List.of());
        playerRepository.save(player);

        //when
        gameService.saveRollResult(new NextFrameRecord(userId, 1, 1), 10);
        gameService.saveRollResult(new NextFrameRecord(userId, 2, 1), 10);
        gameService.saveRollResult(new NextFrameRecord(userId, 3, 1), 10);
        gameService.saveRollResult(new NextFrameRecord(userId, 4, 1), 10);
        gameService.saveRollResult(new NextFrameRecord(userId, 5, 1), 10);
        gameService.saveRollResult(new NextFrameRecord(userId, 6, 1), 10);
        gameService.saveRollResult(new NextFrameRecord(userId, 7, 1), 10);
        gameService.saveRollResult(new NextFrameRecord(userId, 8, 1), 10);
        gameService.saveRollResult(new NextFrameRecord(userId, 9, 1), 10);
        gameService.saveRollResult(new NextFrameRecord(userId, 10, 1), 10);
        gameService.saveRollResult(new NextFrameRecord(userId, 10, 2), 10);
        gameService.saveRollResult(new NextFrameRecord(userId, 10, 3), 10);

        //then
        Optional<Frame> optional1 = frameRepository.findOneByUserIdAndFrameNumber(userId, 1);
        assertTrue(optional1.isPresent());
        assertEquals(30, optional1.get().getFrameScore());
        Optional<Frame> optional2 = frameRepository.findOneByUserIdAndFrameNumber(userId, 2);
        assertTrue(optional2.isPresent());
        assertEquals(60, optional2.get().getFrameScore());
        Optional<Frame> optional3 = frameRepository.findOneByUserIdAndFrameNumber(userId, 3);
        assertTrue(optional3.isPresent());
        assertEquals(90, optional3.get().getFrameScore());
        Optional<Frame> optional4 = frameRepository.findOneByUserIdAndFrameNumber(userId, 4);
        assertTrue(optional4.isPresent());
        assertEquals(120, optional4.get().getFrameScore());
        Optional<Frame> optional5 = frameRepository.findOneByUserIdAndFrameNumber(userId, 5);
        assertTrue(optional5.isPresent());
        assertEquals(150, optional5.get().getFrameScore());
        Optional<Frame> optional6 = frameRepository.findOneByUserIdAndFrameNumber(userId, 6);
        assertTrue(optional6.isPresent());
        assertEquals(180, optional6.get().getFrameScore());
        Optional<Frame> optional7 = frameRepository.findOneByUserIdAndFrameNumber(userId, 7);
        assertTrue(optional7.isPresent());
        assertEquals(210, optional7.get().getFrameScore());
        Optional<Frame> optional8 = frameRepository.findOneByUserIdAndFrameNumber(userId, 8);
        assertTrue(optional8.isPresent());
        assertEquals(240, optional8.get().getFrameScore());
        Optional<Frame> optional9 = frameRepository.findOneByUserIdAndFrameNumber(userId, 9);
        assertTrue(optional9.isPresent());
        assertEquals(270, optional9.get().getFrameScore());
        Optional<Frame> optional10 = frameRepository.findOneByUserIdAndFrameNumber(userId, 10);
        assertTrue(optional10.isPresent());
        assertEquals(300, optional10.get().getFrameScore());

        assertEquals(300, gameService.getFinalResult(userId));
    }
}
