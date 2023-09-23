package com.genios.bowling.controller;

import com.genios.bowling.record.response.PlayerScore;
import com.genios.bowling.record.request.Roll;
import com.genios.bowling.record.response.GameOver;
import com.genios.bowling.record.response.NextFrameRecord;
import com.genios.bowling.record.request.Player;
import com.genios.bowling.record.response.PlayerCreated;
import com.genios.bowling.service.GameService;
import com.genios.bowling.service.PlayerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api")
public class BowlingController {

    private final PlayerService playerService;
    private final GameService gameService;

    @Autowired
    public BowlingController(PlayerService playerService, GameService gameService) {
        this.playerService = playerService;
        this.gameService = gameService;
    }

    @PostMapping(value = "/players", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<PlayerCreated> create(@RequestBody @Valid @NotNull Player player) {
        Long userId = playerService.createPlayer(player.name());
        PlayerCreated result = new PlayerCreated(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping(value = "/players/{id}/frames", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<NextFrameRecord> getNextFrame(@PathVariable @NotBlank Long id) {
        NextFrameRecord nextFrame = gameService.getNextFrame(id);
        return ResponseEntity.status(HttpStatus.OK).body(nextFrame);
    }

    @PostMapping(value = "/players/{id}/frames", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> saveRoll(@PathVariable @NotBlank Long id, @RequestBody @Valid Roll roll) {
        NextFrameRecord frameRecord = new NextFrameRecord(id, roll.frameNumber(), roll.rollNumber());
        int pins = roll.pins();
        gameService.saveRollResult(frameRecord, pins);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping(value = "/players/{id}/game", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> isGameOver(@PathVariable @NotBlank Long id) {
        boolean isGameOver = gameService.isGameOver(id);
        return ResponseEntity.status(HttpStatus.OK).body(new GameOver(isGameOver));
    }

    @GetMapping(value = "/players/{id}/scores", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PlayerScore> getPlayerScore(@PathVariable @NotBlank Long id) {
        PlayerScore playerScore = gameService.getPlayerScore(id);
        return ResponseEntity.status(HttpStatus.OK).body(playerScore);
    }

    @GetMapping(value = "/scores", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PlayerScore>> getTopScores() {
        List<PlayerScore> scores = playerService.getTopPlayerScores();
        return ResponseEntity.status(HttpStatus.OK).body(scores);
    }
}
