package com.ivpl.games.repository;

import com.ivpl.games.entity.jpa.Piece;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PieceRepository extends JpaRepository<Piece, Long> {

}
