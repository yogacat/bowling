package com.genios.bowling.persistance.repository;

import com.genios.bowling.persistance.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    int countByIsFinishedFalse();
}
