package com.genios.bowling.persistance.repository;

import com.genios.bowling.persistance.entity.Roll;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RollRepository extends JpaRepository<Roll, Long> {

    Optional<Roll> findOneByFrameIdAndRollNumber(Long frameId, Integer rollNumber);
}
