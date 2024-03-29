package com.genios.bowling.service;

import com.genios.bowling.exception.GameAlreadyFinishedException;
import com.genios.bowling.exception.GameNotFinishedException;
import com.genios.bowling.exception.frame.FrameNotFoundException;
import com.genios.bowling.exception.roll.InvalidRollException;
import com.genios.bowling.exception.roll.RollAlreadyExistsException;
import com.genios.bowling.exception.roll.RollNotFoundException;
import com.genios.bowling.persistance.entity.Frame;
import com.genios.bowling.persistance.entity.Player;
import com.genios.bowling.persistance.entity.Roll;
import com.genios.bowling.record.response.IntermediateScore;
import com.genios.bowling.record.response.NextFrameRecord;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
    public GameService(PlayerService playerService, FrameService frameService,
        RollService rollService) {
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

        Optional<Frame> optionalLastFrame = frameService.getLastFrame(frames);
        if (optionalLastFrame.isEmpty() || optionalLastFrame.get().getFrameNumber() != 10) {
            return false;
        }
        return !frameService.areRollsLeftInFrame(optionalLastFrame.get());
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

        Optional<Frame> optionalLastFrame = frameService.getLastFrame(frames);
        if (optionalLastFrame.isEmpty()) {
            throw new FrameNotFoundException("Last frame not found for the user with id " + userId);
        }
        Frame lastFrame = optionalLastFrame.get();
        if (frameService.areRollsLeftInFrame(lastFrame)) {
            Optional<Roll> optionalLastRoll = frameService.getLastRoll(lastFrame.getRolls());
            if (optionalLastRoll.isEmpty()) {
                throw new RollNotFoundException(
                    "Last roll not found for the user with id " + userId + " within the frame number "
                        + lastFrame.getFrameNumber());
            }
            Roll lastRoll = optionalLastRoll.get();
            if (lastFrame.getFrameNumber() != 10 && lastRoll.getPins() == 10) {
                return new NextFrameRecord(userId, lastFrame.getFrameNumber() + 1, 1);
            }
            return new NextFrameRecord(userId, lastFrame.getFrameNumber(), lastRoll.getRollNumber() + 1);
        }

        return new NextFrameRecord(userId, lastFrame.getFrameNumber() + 1, 1);
    }


    /**
     * Will save the roll and a frame if there are no rolls on a frame left.
     *
     * @param nextFrameRecord {@link NextFrameRecord}info about the user and the frame and roll he currently used
     * @param pins Integer now many pins were knocked off during the roll
     */
    @Transactional
    public void saveRollResult(NextFrameRecord nextFrameRecord, Integer pins) {
        Frame frame = frameService.getOrCreateFrame(nextFrameRecord.userId(), nextFrameRecord.frameNumber(),
            nextFrameRecord.rollNumber());
        Optional<Roll> rollOptional = rollService.getRoll(frame.getId(), nextFrameRecord.rollNumber());
        if (rollOptional.isPresent()) {
            throw new RollAlreadyExistsException(
                "For the frame " + nextFrameRecord.frameNumber() + " roll " + nextFrameRecord.rollNumber()
                    + " was already saved.");
        }

        int availablePins = 10 - frame.getRolls().stream()
            .filter(r -> !"X".equals(r.getStatus()))
            .filter(r -> pins != 10)
            .map(Roll::getPins)
            .mapToInt(Integer::intValue)
            .sum();
        if (availablePins < pins) {
            throw new InvalidRollException(
                "Received the number of pins higher than the number of available pins on a frame");
        }
        rollService.createRoll(frame, nextFrameRecord.rollNumber(), pins);
        frameService.updateFrameScore(frame);

        frameService.updateFinishedFrames(frame.getUserId());
    }


    /**
     * Returns the final result of the game if it was finished.
     *
     * @param userId Long id of the {@link  Player}
     * @return int score
     */
    public int getFinalResult(long userId) {
        Player player = playerService.getPlayer(userId);
        if (player.isFinished()) {
            return player.getTotalScore();
        }

        throw new GameNotFinishedException("Game is not marked as finished for the user id" + userId);
    }

    /**
     * Converts player information into a record, suitable for the frontend.
     *
     * @param id Long userId
     * @return {@link IntermediateScore}
     */
    @Transactional
    public IntermediateScore getIntermediateScore(Long id) {
        Player player = playerService.getPlayer(id);
        return player.getIntermediateScore();
    }
}
