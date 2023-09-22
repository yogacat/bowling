package com.genios.bowling.persistance.repository;

import com.genios.bowling.persistance.entity.Frame;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Olena Openko 22.09.2023
 */
public interface FrameRepository extends JpaRepository<Frame, Long> {

}
