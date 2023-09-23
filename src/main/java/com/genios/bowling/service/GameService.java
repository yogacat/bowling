package com.genios.bowling.service;

import com.genios.bowling.exception.FrameNotFoundException;
import com.genios.bowling.exception.GameAlreadyFinishedException;
import com.genios.bowling.exception.RollNotFoundException;
import com.genios.bowling.persistance.entity.Frame;
import com.genios.bowling.persistance.entity.Player;
import com.genios.bowling.persistance.entity.Roll;
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

    private final PlayerService playerService;
    private final FrameService frameService;
    private final RollService rollService;

    @Autowired
    public GameService(PlayerService playerService, FrameService frameService, RollService rollService) {
        this.rollService = rollService;
        this.playerService = playerService;
        this.frameService = frameService;
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
            return new NextFrameRecord(userId, 1, 1, true);
        }

        Optional<Frame> optionalLastFrame = getLastFrame(frames);
        if (optionalLastFrame.isEmpty()) {
            throw new FrameNotFoundException("Last frame not found for the user with id " + userId);
        }
        Frame lastFrame = optionalLastFrame.get();
        if (areRollsLeftInFrame(lastFrame)) {
            Optional<Roll> optionalLastRoll = getLastRoll(lastFrame.getRolls());
            if (optionalLastRoll.isEmpty()) {
                throw new RollNotFoundException(
                    "Last roll not found for the user with id " + userId + " within the frame number "
                        + lastFrame.getFrameNumber());
            }
            Roll lastRoll = optionalLastRoll.get();
            return new NextFrameRecord(userId, lastFrame.getFrameNumber(), lastRoll.getRollNumber() + 1, false);
        }

        return new NextFrameRecord(userId, lastFrame.getFrameNumber() + 1, 1, true);
    }


    /**
     * Will save the roll and a frame if there are no rolls on a frame left.
     *
     * @param nextFrameRecord {@link NextFrameRecord}info about the user and the frame and roll he currently used
     * @param pins Integer now many pins were knocked off during the roll
     */
    public void saveRollResult(NextFrameRecord nextFrameRecord, Integer pins) {
        Frame frame = frameService.getOrCreateFrame(nextFrameRecord.userId(), nextFrameRecord.frameNumber(),
            nextFrameRecord.isNewFrame());
        rollService.createRoll(frame, nextFrameRecord.rollNumber(), pins);
    }

    public List<FrameRecord> calculatePlayerScores(Long userId) {

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
