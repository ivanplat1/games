package com.ivpl.games.entity.jpa;

import com.ivpl.games.constants.Color;
import com.ivpl.games.entity.CellKey;
import lombok.AllArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Step {

    @Id
    @GeneratedValue
    private Long id;
    private Integer gameStepId;
    private Long gameId;
    private Color playerColor;
    private CellKey from;
    private CellKey to;
    private Long figureId;

    public Step(Long gameId, Integer gameStepId, Color playerColor, CellKey from, CellKey to, Long figureId) {
        this.gameId = gameId;
        this.gameStepId = gameStepId;
        this.playerColor = playerColor;
        this.from = from;
        this.to = to;
        this.figureId = figureId;
    }

    protected Step() {}
}
