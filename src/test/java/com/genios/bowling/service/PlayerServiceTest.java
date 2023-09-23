package com.genios.bowling.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.genios.bowling.exception.NoFreeLinesException;
import com.genios.bowling.exception.PlayerNotFoundException;
import com.genios.bowling.persistance.entity.Player;
import com.genios.bowling.persistance.repository.PlayerRepository;
import com.genios.bowling.record.response.PlayerScore;
import org.junit.jupiter.api.Assertions;
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
class PlayerServiceTest {

    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private PlayerService playerService;

    @Test
    void shouldThrowExceptionWhenNoLanesAreAvailable() {
        //given
        Player player1 = new Player("Max");
        playerRepository.save(player1);

        //when
        NoFreeLinesException thrown = Assertions.assertThrows(NoFreeLinesException.class,
            () -> playerService.createPlayer("Monica"));

        //then
        assertEquals("No free lines are left, please try again later", thrown.getMessage());
    }

    @Test
    void shouldCreatePlayerWhenLanesAreAvailable() {
        //when
        Long id = playerService.createPlayer("Monica");

        //then
        assertNotNull(id);
    }

    @Test
    void shouldSetFinalScoreWhenPlayerIsPresent() {
        //given
        Player player1 = new Player("Max");
        playerRepository.save(player1);
        Long id = player1.getId();

        //when
        playerService.setFinalScore(id, 300);

        //then
        Optional<Player> optional = playerRepository.findById(id);
        assertTrue(optional.isPresent());
        Player savedPlayer = optional.get();
        assertEquals(300, savedPlayer.getTotalScore());
        assertTrue(savedPlayer.isFinished());
    }

    @Test
    void shouldThrowExceptionWhenPlayerIsNotPresent() {
        //when
        PlayerNotFoundException thrown = Assertions.assertThrows(PlayerNotFoundException.class,
            () -> playerService.setFinalScore(1L, 300));

        //then
        assertEquals("No player with the id 1 was found", thrown.getMessage());
    }

    @Test
    void shouldReturnRatingWhenScoreIsPresent() {
        //given
        Player player1 = new Player(1L, "Max", 100, true, List.of());
        Player player2 = new Player(2L, "Monica", null, false, List.of());
        Player player3 = new Player(3L, "Tomas", 130, true, List.of());
        Player player4 = new Player(4L, "Ralf", 39, true, List.of());
        playerRepository.saveAll(List.of(player1, player2, player3, player4));

        //when
        List<PlayerScore> rating = playerService.getTopPlayerScores();

        //then
        assertEquals(3, rating.size());
        assertEquals(130, rating.get(0).totalScore());
        assertEquals(100, rating.get(1).totalScore());
        assertEquals(39, rating.get(2).totalScore());
    }
}