package com.ivpl.games.entity.jpa;

import com.ivpl.games.constants.Color;
import com.ivpl.games.constants.PieceType;
import lombok.Getter;
import lombok.Setter;
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
    @Setter
    private PieceType type;
    private Color color;
    @Setter
    private boolean alive = true;
    @Column(columnDefinition = "int[]")
    @Type(type = "com.ivpl.games.converter.IntegerArrayType")
    @Setter
    private Integer[] position;
}
