package com.ivpl.games.entity.jpa;

import com.ivpl.games.constants.Color;
import com.ivpl.games.entity.CellKey;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Arrays;

@Entity
@Table(name = "STEPS")
public class Step {

    @Id
    @GeneratedValue
    private Long id;
    private Integer gameStepId;
    private Long gameId;
    private Color playerColor;
    private String stepFrom;
    private String stepTo;
    private Integer figureId;

    public Step(Long gameId, Integer gameStepId, Color playerColor, CellKey stepFrom, CellKey stepTo, Integer figureId) {
        this.gameId = gameId;
        this.gameStepId = gameStepId;
        this.playerColor = playerColor;
        this.stepFrom = Arrays.toString(stepFrom.getAsArray());
        this.stepTo = Arrays.toString(stepTo.getAsArray());
        this.figureId = figureId;
    }

    protected Step() {}
}
