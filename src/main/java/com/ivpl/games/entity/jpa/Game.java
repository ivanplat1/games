package com.ivpl.games.entity.jpa;

import com.ivpl.games.constants.Color;
import com.ivpl.games.constants.GameStatus;
import com.ivpl.games.constants.GameType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Table(name = "GAMES")
@Getter
@Setter
public class Game {

    @Id
    @GeneratedValue
    private Long id;
    private Long player1Id;
    private Color colorPlayer1;
    private Long player2Id;
    private Color colorPlayer2;
    private GameType type;
    private GameStatus status;
    private Timestamp startDate;
    private Integer stepCount;

    protected Game() {}

    public Game(Long player1Id, GameType type) {
        this.player1Id = player1Id;
        this.type = type;
        this.status = GameStatus.SELECTING_COLOR;
        this.startDate = new Timestamp(new Date().getTime());
    }
}
