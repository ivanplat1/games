package com.ivpl.games.repository;

import com.ivpl.games.constants.GameStatus;
import com.ivpl.games.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    List<Game> findAllByStatusIn(Set<GameStatus> statuses);
}
