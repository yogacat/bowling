package com.genios.bowling.service;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

import com.genios.bowling.exception.NoFreeLinesException;
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
        Assertions.assertEquals("No free lines are left, please try again later", thrown.getMessage());
    }

    @Test
    void shouldCreatePlayerWhenLanesAreAvailable() {
        //when
        Long id = playerService.createPlayer("Monica");

        //then
        assertNotNull(id);
    }
}