package com.ivpl.games.repository;

import com.ivpl.games.entity.jpa.Piece;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PieceRepository extends JpaRepository<Piece, Long> {

    List<Piece> findAllByGameId(Long gameId);

    Optional<Piece> findPieceById(Long id);
}
