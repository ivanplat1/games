package com.ivpl.games.entity.jpa;

import com.ivpl.games.constants.Color;
import com.ivpl.games.constants.GameStatus;
import com.ivpl.games.constants.GameType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

import static com.ivpl.games.constants.GameStatus.IN_PROGRESS;
import static com.ivpl.games.constants.GameStatus.WAITING_FOR_OPPONENT;

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
    private Color turn;
    private Color winner;

    protected Game() {}

    public Game(Long player1Id, Color color, GameType type) {
        this.player1Id = player1Id;
        this.colorPlayer1 = color;
        this.type = type;
        this.status = WAITING_FOR_OPPONENT;
        this.startDate = new Timestamp(new Date().getTime());
        this.stepCount = 0;
        this.turn = Color.WHITE;
    }

    public boolean isActive() {
        return WAITING_FOR_OPPONENT.equals(status) || IN_PROGRESS.equals(status);
    }
}
