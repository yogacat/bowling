package com.genios.bowling.persistance.repository;

import com.genios.bowling.persistance.entity.Frame;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FrameRepository extends JpaRepository<Frame, Long> {

}
