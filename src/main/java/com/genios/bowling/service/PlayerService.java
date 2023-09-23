package com.genios.bowling.service;

import com.genios.bowling.configuration.BowlingConfiguration;
import com.genios.bowling.exception.NoFreeLinesException;
import com.genios.bowling.exception.PlayerNotFoundException;
import com.genios.bowling.persistance.entity.Player;
import com.genios.bowling.persistance.repository.PlayerRepository;
import com.genios.bowling.record.PlayerRecord;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Handles operations on a player.
 */
@Service
public class PlayerService {

    private final int maxLines;

    private final PlayerRepository playerRepository;

    @Autowired
    public PlayerService(BowlingConfiguration configuration, PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
        this.maxLines = configuration.getLines();
    }

    /**
     * Creates a player if there are free lines available
     *
     * @param name String name of the player
     */
    @Transactional
    public Long createPlayer(String name) {
        if (isFreeLines()) {
            Player player = new Player(name);
            playerRepository.save(player);

            return player.getId();
        } else {
            throw new NoFreeLinesException("No free lines are left, please try again later");
        }
    }

    /**
     * Sets the final score of the game, sets the game to finished.
     *
     * @param id Long id of the player
     * @param score Integer final score of the game
     */
    @Transactional
    public void setFinalScore(Long id, Integer score) {
        Optional<Player> optionalPlayer = playerRepository.findById(id);
        if (optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();
            player.setTotalScore(score);
            player.setFinished(true);
            playerRepository.save(player);
        } else {
            throw new PlayerNotFoundException("No player with the id " + id + " was found");
        }
    }

    /**
     * Returns a sorted list of names and the total score. Not limited.
     *
     * @return collection of the {@link PlayerRecord}
     */
    public List<PlayerRecord> getRating() {
        Player playerExample = new Player();
        playerExample.setFinished(true);

        Example<Player> example = Example.of(playerExample);
        List<Player> players = playerRepository.findAll(example);
        return players.stream()
            .map(p -> new PlayerRecord(p.getName(), p.getTotalScore()))
            .sorted(Comparator.comparingLong(PlayerRecord::totalScore).reversed())
            .toList();
    }

    /**
     * Returns {@link Player} entity if there is one with this id found.
     *
     * @param userId Long player id
     * @return {@link Player}
     */
    public Player getPlayer(Long userId) {
        Optional<Player> optionalPlayer = playerRepository.findById(userId);
        if (optionalPlayer.isPresent()) {
            return optionalPlayer.get();
        } else {
            throw new PlayerNotFoundException("No player with the id " + userId + " was found");
        }
    }

    /**
     * Saves the player's total score and marks the game as finished.
     *
     * @param player {@link Player}
     * @param lastFrameScore int final score
     */
    public void saveFinalScore(Player player, int lastFrameScore) {
        player.setFinished(true);
        player.setTotalScore(lastFrameScore);
        playerRepository.saveAndFlush(player);
    }

    private boolean isFreeLines() {
        int ongoingGames = playerRepository.countByIsFinishedFalse();
        return ongoingGames < maxLines;
    }
}
