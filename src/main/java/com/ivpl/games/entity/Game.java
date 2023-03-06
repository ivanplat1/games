package com.ivpl.games.entity;

import com.ivpl.games.constants.Color;
import com.ivpl.games.constants.GameStatus;
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
    private Long user1Id;
    private Color colorUser1;
    private Long user2Id;
    private Color colorUser2;
    private String type;
    private String status;
    private Timestamp startDate;

    public Game() {}

    public Game(Long user1Id, String type) {
        this.user1Id = user1Id;
        this.type = type;
        this.status = GameStatus.WAITING_FOR_OPPONENT.name();
        this.startDate = new Timestamp(new Date().getTime());
    }
}
