package com.genios.bowling.service;

import com.genios.bowling.exception.GameAlreadyFinishedException;
import com.genios.bowling.persistance.entity.Frame;
import com.genios.bowling.persistance.entity.Player;
import com.genios.bowling.persistance.entity.Roll;
import com.genios.bowling.persistance.repository.FrameRepository;
import com.genios.bowling.persistance.repository.PlayerRepository;
import com.genios.bowling.persistance.repository.RollRepository;
import com.genios.bowling.record.FrameRecord;
import com.genios.bowling.record.NextFrameRecord;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Counts the score of the player, saves frames and rolls, marks strike/spare or miss in the roll.
 */
@Service
public class GameService {

    private final PlayerRepository playerRepository;
    private final FrameRepository frameRepository;
    private final RollRepository rollRepository;

    private final PlayerService playerService;

    @Autowired
    public GameService(PlayerRepository playerRepository, FrameRepository frameRepository,
        RollRepository rollRepository,
        PlayerService playerService) {
        this.playerRepository = playerRepository;
        this.frameRepository = frameRepository;
        this.rollRepository = rollRepository;
        this.playerService = playerService;
    }

    /**
     * Checks if the game is finished and no more frames and rolls are left.
     *
     * @param userId Long id of the {@link  com.genios.bowling.persistance.entity.Player}
     * @return true if there are no frames and rolls available.
     */
    @Transactional
    public boolean isGameOver(Long userId) {
        Player player = playerService.getPlayer(userId);
        if (player.isFinished()) {
            return true;
        }

        List<Frame> frames = player.getFrames();
        if (frames.isEmpty()) {
            return false;
        }

        Optional<Frame> optionalLastFrame = getLastFrame(frames);
        if (optionalLastFrame.isEmpty() || optionalLastFrame.get().getFrameNumber() != 10) {
            return false;
        }
        return !areRollsLeftInFrame(optionalLastFrame.get());
    }

    /**
     * Returns the number of the next frame and a roll
     *
     * @param userId Long id of the {@link  Player}
     * @return {@link NextFrameRecord}
     */
    @Transactional
    public NextFrameRecord getNextFrame(Long userId) {
        Player player = playerService.getPlayer(userId);
        if (player.isFinished() || isGameOver(userId)) {
            throw new GameAlreadyFinishedException(
                "No frames left for the current game for the user with id " + userId);
        }

        List<Frame> frames = player.getFrames();
        if (frames.isEmpty()) {
            return new NextFrameRecord(userId, 1, 1);
        }

        Optional<Frame> optionalLastFrame = getLastFrame(frames);
        Frame lastFrame = optionalLastFrame.get();
        if (areRollsLeftInFrame(lastFrame)) {
            Optional<Roll> optionalLastRoll = getLastRoll(lastFrame.getRolls());
            Roll lastRoll = optionalLastRoll.get();
            return new NextFrameRecord(userId, lastFrame.getFrameNumber(), lastRoll.getRollNumber() + 1);
        }

        return new NextFrameRecord(userId, lastFrame.getFrameNumber() + 1, 1);
    }

    public void saveRollResult(Long userId, Long frameId, Long rollId, Integer pins) {

    }

    public List<FrameRecord> getPlayerScores(Long userId) {

        return List.of();
    }

    private Optional<Frame> getLastFrame(List<Frame> frames) {
        return frames.stream().max(Comparator.comparing(Frame::getFrameNumber));
    }

    private boolean areRollsLeftInFrame(Frame frame) {
        List<Roll> rolls = frame.getRolls().stream()
            .sorted(Comparator.comparing(Roll::getRollNumber))
            .toList();
        if (rolls.isEmpty() || rolls.size() == 1) {
            return true;
        }

        if (frame.getFrameNumber() != 10 && rolls.size() == 2) {
            return false;
        }

        if (rolls.size() == 3) {
            return false;
        }

        return rolls.get(0).getPins() == 10;
    }

    private Optional<Roll> getLastRoll(List<Roll> rolls) {
        return rolls.stream().max(Comparator.comparing(Roll::getRollNumber));
    }
}
