package com.genios.bowling.persistance.repository;

import com.genios.bowling.persistance.entity.Frame;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FrameRepository extends JpaRepository<Frame, Long> {

    Optional<Frame> findOneByUserIdAndFrameNumber(Long userId, Integer frameNumber);
}
