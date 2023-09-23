package com.genios.bowling.service;

import com.genios.bowling.exception.RollNotFoundException;
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
    Roll createRoll(Frame frame, Integer rollNumber, Integer pins) {
        Roll roll = new Roll(frame.getId(), rollNumber, pins);
        roll.setStatus(calculateStatusForRoll(frame, rollNumber, pins).getState());
        roll.setFrame(frame);
        rollRepository.save(roll);

        return roll;
    }

    private Status calculateStatusForRoll(Frame frame, Integer rollNumber, Integer pins) {
        if (pins == 10 && rollNumber == 1) {
            return Status.STRIKE;
        } else if (pins == 0) {
            return Status.MISS;
        } else if (rollNumber == 2) {
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
}
