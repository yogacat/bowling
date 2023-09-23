package com.genios.bowling.service;

import com.genios.bowling.exception.FrameNotFoundException;
import com.genios.bowling.exception.GameAlreadyFinishedException;
import com.genios.bowling.exception.GameNotFinishedException;
import com.genios.bowling.exception.RollNotFoundException;
import com.genios.bowling.persistance.entity.Frame;
import com.genios.bowling.persistance.entity.Player;
import com.genios.bowling.persistance.entity.Roll;
import com.genios.bowling.record.response.NextFrameRecord;
import com.genios.bowling.record.response.PlayerScore;
import jakarta.persistence.EntityManager;
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

    private final EntityManager entityManager;

    private final PlayerService playerService;
    private final FrameService frameService;
    private final RollService rollService;

    @Autowired
    public GameService(EntityManager entityManager, PlayerService playerService, FrameService frameService,
        RollService rollService) {
        this.entityManager = entityManager;
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
        Roll currentRoll = rollService.createRoll(frame, nextFrameRecord.rollNumber(), pins);

        frameService.updateFrameScore(frame, currentRoll);

        if (!frameService.areRollsLeftInFrame(frame) && frame.getFrameNumber() == 10) {
            Player player = playerService.getPlayer(frame.getUserId());
            entityManager.flush();
            entityManager.refresh(player);
            playerService.saveFinalScore(player, getLastFrameScore(player.getFrames()));
        }
    }

    private int getLastFrameScore(List<Frame> frames) {
        Optional<Frame> optionalFrame = frameService.getLastFrame(frames);

        if (optionalFrame.isPresent() && optionalFrame.get().getFrameNumber() == 10 && optionalFrame.get()
            .isFinalScore()) {
            return optionalFrame.get().getFrameScore();
        }
        throw new GameNotFinishedException(
            "No last frame received, frame is not the last one or frame calculation is not finished yet");
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

    @Transactional
    public PlayerScore getPlayerScore(Long id) {
        Player player = playerService.getPlayer(id);
        return new PlayerScore(player.getName(), this.getFinalResult(id));
    }
}
