package com.ivpl.games.entity;

import com.ivpl.games.constants.GameStatus;
import com.ivpl.games.constants.GameType;
import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Table(name = "GAMES")
@Getter
public class Game {

    @Id
    @GeneratedValue
    private Long id;
    private Long userId;
    private Long secondUserId;
    private String type;
    private String status;
    private Timestamp startDate;

    public Game() {}

    public Game(Long userId, String type) {
        this.userId = userId;
        this.type = type;
        this.status = GameStatus.IN_PROGRESS.name();
        this.startDate = new Timestamp(new Date().getTime());
    }

    @Override
    public String toString() {
        return String.format("Game %s of Type %s in status %s of user %s", getId(), getType(), getStatus(), getUserId());
    }

    public boolean isNotFinished() {
        return !GameStatus.FINISHED.name().equals(status);
    }
}
