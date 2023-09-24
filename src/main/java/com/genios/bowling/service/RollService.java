package com.genios.bowling.service;

import com.genios.bowling.exception.roll.RollNotFoundException;
import com.genios.bowling.persistance.entity.Frame;
import com.genios.bowling.persistance.entity.Roll;
import com.genios.bowling.persistance.repository.RollRepository;
import com.genios.bowling.record.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * Contains operations on a roll within the frame.
 */

@Service
public class RollService {

    private final RollRepository rollRepository;

    @Autowired
    public RollService(RollRepository rollRepository) {
        this.rollRepository = rollRepository;
    }

    /**
     * Creates a new Roll and saves it.
     *
     * @param frame {@link Frame} current frame the roll belongs to
     * @param rollNumber Integer number of this roll
     * @param pins Integer how many pins were knocked off
     */
    void createRoll(Frame frame, Integer rollNumber, Integer pins) {
        Roll roll = new Roll(frame.getId(), rollNumber, pins);
        roll.setStatus(calculateStatusForRoll(frame, rollNumber, pins).getState());
        roll.setFrame(frame);
        rollRepository.save(roll);
    }

    private Status calculateStatusForRoll(Frame frame, Integer rollNumber, Integer pins) {
        boolean isStrike = pins == 10 && rollNumber == 1;
        boolean isMiss = pins == 0;
        boolean isSecondRollNotLastFrame = rollNumber == 2 && frame.getFrameNumber() != 10;

        if (isStrike) {
            return Status.STRIKE;
        } else if (isMiss) {
            return Status.MISS;
        } else if (isSecondRollNotLastFrame) {
            Roll firstRoll = getFirstRoll(frame.getRolls(), frame.getId());
            if (firstRoll.getPins() + pins == 10) {
                return Status.SPARE;
            }
        }

        return Status.NONE;
    }

    private Roll getFirstRoll(List<Roll> rolls, Long frameId) {
        Optional<Roll> optionalFirstRoll = rolls.stream()
            .filter(roll -> 1 == roll.getRollNumber())
            .findFirst();
        if (optionalFirstRoll.isEmpty()) {
            throw new RollNotFoundException("First roll for the frame id " + frameId + " not found");
        }
        return optionalFirstRoll.get();
    }

    /**
     * Return optional {@link Roll} if it was found
     *
     * @param frameId Long id of the {@link Frame}
     * @param rollNumber Integer number of the {@link Roll}
     */
    public Optional<Roll> getRoll(Long frameId, Integer rollNumber) {
        return rollRepository.findOneByFrameIdAndRollNumber(frameId, rollNumber);
    }
}
