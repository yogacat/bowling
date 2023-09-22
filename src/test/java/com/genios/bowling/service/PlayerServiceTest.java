package com.genios.bowling.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.genios.bowling.exception.NoFreeLinesException;
import com.genios.bowling.exception.PlayerNotFoundException;
import com.genios.bowling.persistance.entity.Player;
import com.genios.bowling.persistance.repository.PlayerRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
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
}