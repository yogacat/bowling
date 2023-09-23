package com.genios.bowling.service;

import com.genios.bowling.exception.FrameNotFoundException;
import com.genios.bowling.persistance.entity.Frame;
import com.genios.bowling.persistance.entity.Player;
import com.genios.bowling.persistance.entity.Roll;
import com.genios.bowling.persistance.repository.FrameRepository;
import com.genios.bowling.record.CacheRecord;
import com.genios.bowling.record.Status;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Contains methods related to the frame.
 */
@Service
public class FrameService {

    private final EntityManager entityManager;

    private final FrameRepository frameRepository;
    private final PlayerService playerService;

    //todo recalculate on service restart
    private final Map<Long, LinkedList<CacheRecord>> cache = new HashMap<>();
    private final Map<Long, Integer> cacheLastFinalScore = new HashMap<>();

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
     * @param frame {@link Frame}
     */
    public void updateFrameScore(Frame frame, Roll currentRoll) {
        entityManager.flush();
        entityManager.refresh(frame);

        Status status = Status.fromString(currentRoll.getStatus());
        if (Status.STRIKE.equals(status) || Status.SPARE.equals(status) || !cache.isEmpty()) {
            this.addScoreToCache(frame.getUserId(), frame.getFrameNumber(), currentRoll.getRollNumber(),
                currentRoll.getPins(),
                status.getBonus());
        }
        int pins = frame.getRolls().stream()
            .map(Roll::getPins)
            .mapToInt(Integer::intValue)
            .sum();
        frame.setFrameScore(pins);
        frameRepository.save(frame);

        this.recalculateCache(frame.getUserId());
        System.out.println("X");

        //todo save both
    }

    private void addScoreToCache(Long userId, int frameNumber, int rollNumber, int pins, int bonus) {
        CacheRecord cacheRecord = new CacheRecord(frameNumber, rollNumber, pins, bonus);
        LinkedList<CacheRecord> cacheRecords = cache.getOrDefault(userId, new LinkedList<>());
        cacheRecords.addLast(cacheRecord);
        cache.put(userId, cacheRecords);
    }

    private void recalculateCache(Long userId) {
        LinkedList<CacheRecord> cacheRecords = cache.getOrDefault(userId, new LinkedList<>());
        if (cacheRecords.size() > 1) {
            if (cacheRecords.peekFirst().bonus() == 2 && cacheRecords.size() >= 3) {
                //strike
                CacheRecord headRecord = cacheRecords.pollFirst();
                CacheRecord secondRecord = cacheRecords.peekFirst();
                CacheRecord thirdRecord = cacheRecords.get(1);

                int bonusPoints = headRecord.frameNumber() != 10 ? secondRecord.pins() + thirdRecord.pins() : 0;
                this.updateFrameScore(userId, headRecord.frameNumber(), bonusPoints);
                cache.put(userId, cacheRecords);

                this.cleanupCacheTopAndFinalizeFrames(userId);
            } else if (cacheRecords.peekFirst().bonus() == 1) {
                //spare
                CacheRecord headRecord = cacheRecords.pollFirst();
                CacheRecord secondRecord = cacheRecords.peekFirst();

                int bonusPoints = secondRecord.pins();
                this.updateFrameScore(userId, headRecord.frameNumber(), bonusPoints);
                cache.put(userId, cacheRecords);

                this.cleanupCacheTopAndFinalizeFrames(userId);
            }
        } else if (cacheRecords.peekFirst().bonus() == 0) {
            //remove anything with no bonus from the top
            while (!cacheRecords.isEmpty() && cacheRecords.peekFirst().bonus() == 0) {
                CacheRecord headRecord = cacheRecords.pollFirst();
                Optional<Frame> optionalFrame = frameRepository.findOneByUserIdAndFrameNumber(userId,
                    headRecord.frameNumber());
                if (optionalFrame.isEmpty()) {
                    throw new FrameNotFoundException(
                        "Frame with number " + headRecord.frameNumber() + " not found for the user with id "
                            + userId);
                }
                Frame frame = optionalFrame.get();
                updateFrameScore(frame.getUserId(), frame.getFrameNumber(), 0);
            }
            cache.put(userId, cacheRecords);

            this.cleanupCacheTopAndFinalizeFrames(userId);
        }
    }

    private void cleanupCacheTopAndFinalizeFrames(Long userId) {
        LinkedList<CacheRecord> cacheRecords = cache.getOrDefault(userId, new LinkedList<>());
        if (cacheRecords.isEmpty() || cacheRecords.peekFirst().bonus() != 0) {
            return;
        }

        CacheRecord firstRecord = cacheRecords.pollFirst();
        if (firstRecord.rollNumber() == 2 && firstRecord.frameNumber() != 10) {
            this.updateFrameScore(userId, firstRecord.frameNumber(), firstRecord.bonus());
        } else if (!cacheRecords.isEmpty() && cacheRecords.peekFirst().bonus() == 0 && cacheRecords.peekFirst().frameNumber() != 10) {
            CacheRecord secondRecord = cacheRecords.pollFirst();
            this.updateFrameScore(userId, secondRecord.frameNumber(), secondRecord.bonus());
        } else if (firstRecord.frameNumber() == 10) {
            while (!cacheRecords.isEmpty()) {
                cacheRecords.pollFirst();
            }
        }
    }

    private void updateFrameScore(Long userId, int frameNumber, int bonusPoints) {
        Optional<Frame> optionalFrame = frameRepository.findOneByUserIdAndFrameNumber(userId, frameNumber);
        if (optionalFrame.isEmpty()) {
            throw new FrameNotFoundException(
                "Frame with number " + frameNumber + " not found for the user with id " + userId);
        }

        int lastFinalScore = cacheLastFinalScore.get(userId) != null ? cacheLastFinalScore.get(userId) : 0;

        Frame frame = optionalFrame.get();
        int frameScore = lastFinalScore + frame.getFrameScore() + bonusPoints;
        frame.setFrameScore(frameScore);
        frame.setFinalScore(true);
        frameRepository.save(frame);

        cacheLastFinalScore.put(userId, frameScore);
    }

    private boolean isLastRollInFrame(Frame frame, Roll currentRoll) {
        if (frame.getFrameNumber() != 10 && currentRoll.getRollNumber() == 2) {
            return true;
        } else if (frame.getFrameNumber() != 10 && currentRoll.getPins() == 10) {
            return true;
        } else if (frame.getFrameNumber() == 10 && currentRoll.getRollNumber() == 3) {
            return true;
        } else if (frame.getFrameNumber() == 10 && currentRoll.getRollNumber() == 2) {
            Roll firstRoll =
                frame.getRolls().stream().min(Comparator.comparing(Roll::getRollNumber))
                    .orElseThrow();
            return firstRoll.getPins() == 10;
        }
        return false;
    }
}
