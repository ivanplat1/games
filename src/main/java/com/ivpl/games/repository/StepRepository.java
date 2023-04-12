package com.ivpl.games.repository;

import com.ivpl.games.entity.jpa.Step;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.LinkedList;

@Repository
public interface StepRepository extends JpaRepository<Step, Long> {

    LinkedList<Step> findAllByGameIdOrderByGameStepId(Long gameId);

    LinkedList<Step> findAllByGameIdAndPieceIdOrderByGameStepId(Long gameId, Long pieceId);
}
