package com.genios.bowling.service;

import com.genios.bowling.exception.FrameNotFoundException;
import com.genios.bowling.persistance.entity.Frame;
import com.genios.bowling.persistance.entity.Player;
import com.genios.bowling.persistance.entity.Roll;
import com.genios.bowling.persistance.repository.FrameRepository;
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

    private final FrameRepository frameRepository;
    private final PlayerService playerService;

    @Autowired
    public FrameService(FrameRepository frameRepository, PlayerService playerService) {
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
}
