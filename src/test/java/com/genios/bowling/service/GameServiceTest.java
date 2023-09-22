package com.genios.bowling.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.genios.bowling.persistance.entity.Frame;
import com.genios.bowling.persistance.entity.Player;
import com.genios.bowling.persistance.entity.Roll;
import com.genios.bowling.persistance.repository.FrameRepository;
import com.genios.bowling.persistance.repository.PlayerRepository;
import com.genios.bowling.persistance.repository.RollRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
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
}