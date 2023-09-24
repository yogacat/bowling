package com.genios.bowling.service;

import com.genios.bowling.exception.frame.FrameNotFoundException;
import com.genios.bowling.persistance.entity.Frame;
import com.genios.bowling.persistance.entity.Player;
import com.genios.bowling.persistance.entity.Roll;
import com.genios.bowling.persistance.repository.FrameRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Contains methods related to the frame.
 */
@Service
public class FrameService {

    private final EntityManager entityManager;

    private final FrameRepository frameRepository;
    private final PlayerService playerService;

    @Autowired
    public FrameService(EntityManager entityManager, FrameRepository frameRepository, PlayerService playerService) {
        this.entityManager = entityManager;
        this.frameRepository = frameRepository;
        this.playerService = playerService;
    }

    /**
     * Creates new frame or gets existing one.
     *
     * @param userId Long id of the user
     * @param frameNumber Integer current frame number
     * @return {@link Frame}
     */
    Frame getOrCreateFrame(Long userId, Integer frameNumber, Integer nextRollNumber) {
        Frame frame;
        if (this.isNewFrame(userId, frameNumber, nextRollNumber)) {
            frame = this.createFrame(userId, frameNumber);
        } else {
            Optional<Frame> optional = frameRepository
                .findOneByUserIdAndFrameNumber(userId, frameNumber);
            if (optional.isEmpty()) {
                throw new FrameNotFoundException("Frame number " + frameNumber
                    + " is not found for the player with the id " + userId);
            }
            frame = optional.get();
        }
        return frame;
    }

    private Frame createFrame(Long userId, Integer frameNumber) {
        Player player = playerService.getPlayer(userId);

        Frame frame = new Frame();
        frame.setUserId(userId);
        frame.setFrameNumber(frameNumber);
        frame.setPlayer(player);
        frame.setRolls(List.of());
        frameRepository.save(frame);
        return frame;
    }

    Optional<Frame> getLastFrame(List<Frame> frames) {
        return frames.stream().max(Comparator.comparing(Frame::getFrameNumber));
    }

    boolean areRollsLeftInFrame(Frame frame) {
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

    Optional<Roll> getLastRoll(List<Roll> rolls) {
        return rolls.stream().max(Comparator.comparing(Roll::getRollNumber));
    }

    private boolean isNewFrame(Long userId, Integer frameNumber, Integer nextRollNumber) {
        Optional<Frame> optionalFrame = frameRepository.findOneByUserIdAndFrameNumber(userId, frameNumber);
        if (nextRollNumber != 1 && optionalFrame.isEmpty()) {
            throw new FrameNotFoundException(
                "Frame with number " + frameNumber + " not found. For the roll that's higher than one it must exist");
        }
        return frameRepository.findOneByUserIdAndFrameNumber(userId, frameNumber).isEmpty();
    }

    /**
     * Updates the score of the frame, does not finalise the frame. Score will contains total pins hit in the frame.
     *
     * @param frame {@link Frame}
     */
    public void updateFrameScore(Frame frame) {
        entityManager.flush();
        entityManager.refresh(frame);
        int pins = this.getFrameTotalPins(frame);
        frame.setFrameScore(pins);
        frameRepository.save(frame);
    }

    public void updateFinishedFrames(Long userId) {
        Player player = playerService.getPlayer(userId);
        entityManager.flush();
        entityManager.refresh(player);

        int lastTotalScore = this.getLastTotal(player.getFrames());
        List<Frame> openedFrames = getOpenedFrames(player.getFrames());

        for (int i = 0; i < openedFrames.size(); i++) {
            Frame currentFrame = openedFrames.get(i);
            if (isRegularFrame(currentFrame)) {
                lastTotalScore = finalizeFrame(currentFrame, lastTotalScore);
            } else if (isStrikeFrame(currentFrame) && isNextFrameExist(i + 1, openedFrames)) {
                Frame nextFrame = openedFrames.get(i + 1);
                boolean isNextFrameTwoRolls = nextFrame.getRolls().size() == 2;
                if (isNextFrameTwoRolls) {
                    int pins = this.getFrameTotalPins(nextFrame);
                    lastTotalScore = finalizeFrame(currentFrame, lastTotalScore + pins);
                } else if (isNextFrameExist(i + 2, openedFrames)) {
                    Frame nextNextFrame = openedFrames.get(i + 2);
                    if (nextNextFrame.getRolls().size() == 1) {
                        int pins = this.getFrameTotalPins(nextFrame) + this.getFrameTotalPins(nextNextFrame);
                        lastTotalScore = finalizeFrame(currentFrame, lastTotalScore + pins);
                    }
                }
            } else if (isSpareFrame(currentFrame) && isNextFrameExist(i + 1, openedFrames)) {
                Frame nextFrame = openedFrames.get(i + 1);
                if (nextFrame.getRolls().size() == 1) {
                    int pins = this.getFrameTotalPins(nextFrame);
                    lastTotalScore = finalizeFrame(currentFrame, lastTotalScore + pins);
                }
            }
        }

        this.updateGameIfFinished(player);
    }

    private int getFrameTotalPins(Frame nextFrame) {
        return nextFrame.getRolls().stream()
            .map(Roll::getPins)
            .mapToInt(Integer::intValue)
            .sum();
    }

    private int getLastTotal(List<Frame> frames) {
        return frames.stream()
            .filter(Frame::isFinalScore)
            .max(Comparator.comparing(Frame::getFrameNumber))
            .map(Frame::getFrameScore)
            .orElse(0);
    }

    private List<Frame> getOpenedFrames(List<Frame> frames) {
        return frames.stream()
            .filter(f -> !f.isFinalScore())
            .sorted(Comparator.comparing(Frame::getFrameNumber))
            .toList();
    }

    private boolean isRegularFrame(Frame currentFrame) {
        boolean isLastFrame = currentFrame.getFrameNumber() == 10;
        boolean isTwoRolls = currentFrame.getRolls().size() == 2;
        boolean isThreeRolls = currentFrame.getRolls().size() == 3;
        boolean hasNoStrikeOrSpare = currentFrame.getRolls().stream()
            .filter(r -> !"X".equals(r.getStatus()))
            .filter(r -> !"/".equals(r.getStatus()))
            .toList().size() == 2;
        return hasNoStrikeOrSpare && isTwoRolls || isLastFrame && isThreeRolls;
    }

    private boolean isStrikeFrame(Frame currentFrame) {
        return currentFrame.getRolls().stream()
            .anyMatch(r -> "X".equals(r.getStatus()));
    }

    private boolean isSpareFrame(Frame currentFrame) {
        return currentFrame.getRolls().stream()
            .anyMatch(r -> "/".equals(r.getStatus()));
    }

    private int finalizeFrame(Frame currentFrame, int lastTotalScore) {
        int pins = currentFrame.getFrameScore();
        int finalScore = pins + lastTotalScore;
        currentFrame.setFrameScore(finalScore);
        currentFrame.setFinalScore(true);
        frameRepository.save(currentFrame);
        return finalScore;
    }

    private boolean isNextFrameExist(int i, List<Frame> openedFrames) {
        return openedFrames.size() > i;
    }

    private void updateGameIfFinished(Player player) {
        if (isAllFramesFinished(player.getFrames())) {
            int finalScore = player.getFrames().stream()
                .filter(f -> f.getFrameNumber() == 10)
                .map(Frame::getFrameScore)
                .findFirst()
                .orElse(0);
            playerService.setFinalScore(player.getId(), finalScore);
        }
    }

    private boolean isAllFramesFinished(List<Frame> frames) {
        return frames.stream()
            .filter(Frame::isFinalScore)
            .count() == 10;
    }
}
