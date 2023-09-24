package com.genios.bowling.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.genios.bowling.exception.NoFreeLinesException;
import com.genios.bowling.exception.player.PlayerNotFoundException;
import com.genios.bowling.persistance.repository.PlayerRepository;
import com.genios.bowling.record.request.Player;
import com.genios.bowling.record.response.GameOver;
import com.genios.bowling.record.response.NextFrameRecord;
import com.genios.bowling.record.response.PlayerCreated;
import com.genios.bowling.record.response.PlayerScore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BowlingControllerTest {

    @Autowired
    private BowlingController controller;
    @Autowired
    private PlayerRepository playerRepository;

    @Test
    void shouldCreatePlayerWhenAvailableLines() {
        //given
        Player player = new Player("Mark");

        //when
        ResponseEntity<PlayerCreated> entity = controller.create(player);

        //then
        assertNotNull(entity);
        assertEquals(HttpStatus.CREATED, entity.getStatusCode());
        assertNotNull(entity.getBody());
        Long id = entity.getBody().id();
        assertEquals(1L, id);
    }

    @Test
    void shouldThrowAnExceptionPlayerWhenOngoingGames() {
        //given
        com.genios.bowling.persistance.entity.Player player0 =
            new com.genios.bowling.persistance.entity.Player(1L, "Max", 0, false, List.of());
        playerRepository.save(player0);
        Player player = new Player("Mark");

        //when
        NoFreeLinesException thrown = Assertions.assertThrows(NoFreeLinesException.class,
            () -> controller.create(player));

        //then
        assertEquals("No free lines are left, please try again later", thrown.getMessage());
    }

    @Test
    void shouldGetTheNextFrameForExistingPlayer() {
        //given
        com.genios.bowling.persistance.entity.Player player0 =
            new com.genios.bowling.persistance.entity.Player(1L, "Max", 0, false, List.of());
        playerRepository.save(player0);

        //when
        ResponseEntity<NextFrameRecord> entity = controller.getNextFrame(1L);

        //then
        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertNotNull(entity.getBody());
        assertEquals(1, entity.getBody().frameNumber());
        assertEquals(1, entity.getBody().rollNumber());
    }

    @Test
    void shouldThrowAnExceptionPlayerNotFound() {
        //when
        PlayerNotFoundException thrown = Assertions.assertThrows(PlayerNotFoundException.class,
            () -> controller.getNextFrame(1L));

        //then
        assertEquals("No player with the id 1 was found", thrown.getMessage());
    }

    @Test
    void shouldReturnTrueWhenGameIsOver() {
        //given
        com.genios.bowling.persistance.entity.Player player0 =
            new com.genios.bowling.persistance.entity.Player(1L, "Max", 150, true, List.of());
        playerRepository.save(player0);

        //when
        ResponseEntity<GameOver> entity = controller.isGameOver(1L);

        //then
        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertNotNull(entity.getBody());
        assertTrue(entity.getBody().isGameOver());
    }

    @Test
    void shouldReturnFalseWhenGameIsOngoing() {
        //given
        com.genios.bowling.persistance.entity.Player player0 =
            new com.genios.bowling.persistance.entity.Player(1L, "Max", 0, false, List.of());
        playerRepository.save(player0);

        //when
        ResponseEntity<GameOver> entity = controller.isGameOver(1L);

        //then
        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertNotNull(entity.getBody());
        assertFalse(entity.getBody().isGameOver());
    }

    @Test
    void shouldThrowAnExceptionPlayerNotFoundInGameOver() {
        //when
        PlayerNotFoundException thrown = Assertions.assertThrows(PlayerNotFoundException.class,
            () -> controller.isGameOver(1L));

        //then
        assertEquals("No player with the id 1 was found", thrown.getMessage());
    }

    @Test
    void shouldReturnTopScoresWhenExist() {
        //given
        playerRepository.save(new com.genios.bowling.persistance.entity.Player(1L, "Max", 130, true, List.of()));
        playerRepository.save(new com.genios.bowling.persistance.entity.Player(2L, "Mary", 168, true, List.of()));

        //when
        ResponseEntity<List<PlayerScore>> entity = controller.getTopScores();

        //then
        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertNotNull(entity.getBody());
        List<PlayerScore> scores = entity.getBody();
        assertEquals(new PlayerScore("Mary", 168), scores.get(0));
        assertEquals(new PlayerScore("Max", 130), scores.get(1));
    }

    @Test
    void shouldNotReturnTopScoresWhenGameIsNotOver() {
        //given
        playerRepository.save(new com.genios.bowling.persistance.entity.Player(1L, "Max", 0, false, List.of()));
        playerRepository.save(new com.genios.bowling.persistance.entity.Player(2L, "Mary", 0, false, List.of()));

        //when
        ResponseEntity<List<PlayerScore>> entity = controller.getTopScores();

        //then
        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertNotNull(entity.getBody());
        assertEquals(List.of(), entity.getBody());
    }

    //there should be more tests for saveRoll and getIntermediateScore
}