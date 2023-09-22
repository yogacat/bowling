package com.genios.bowling.persistance.repository;

import com.genios.bowling.persistance.entity.Roll;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RollRepository extends JpaRepository<Roll, Long> {

}
