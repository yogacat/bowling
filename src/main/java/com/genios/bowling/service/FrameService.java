package com.genios.bowling.service;

import com.genios.bowling.exception.FrameNotFoundException;
import com.genios.bowling.persistance.entity.Frame;
import com.genios.bowling.persistance.entity.Player;
import com.genios.bowling.persistance.repository.FrameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
     * @param isNewFrame true if the frame needs to be created, otherwise it already exists
     * @return {@link Frame}
     */
    public Frame getOrCreateFrame(Long userId, Integer frameNumber, boolean isNewFrame) {
        Frame frame;
        if (isNewFrame) {
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
}
