package com.ivpl.games.entity.jpa;

import com.ivpl.games.constants.Color;
import com.ivpl.games.constants.GameType;
import com.ivpl.games.constants.PieceType;
import lombok.Getter;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Entity
@Table(name = "PIECES")
@Getter
public class Piece {

    public Piece() {}

    public Piece(Long gameId, Long gamePieceId, PieceType type, Color color, Integer[] position) {
        this.gameId = gameId;
        this.gamePieceId = gamePieceId;
        this.type = type;
        this.color = color;
        this.position = position;
    }

    @Id
    @GeneratedValue
    private Long id;
    private Long gameId;
    private Long gamePieceId;
    private PieceType type;
    private Color color;
    private boolean isAlive = true;
    @Column(columnDefinition = "int[]")
    @Type(type = "com.ivpl.games.converter.IntegerArrayType")
    private Integer[] position;
}
