package com.genios.bowling.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.genios.bowling.exception.NoFreeLinesException;
import com.genios.bowling.persistance.repository.PlayerRepository;
import com.genios.bowling.record.request.Player;
import com.genios.bowling.record.response.PlayerCreated;
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

    //todo olo
}