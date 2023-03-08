package com.ivpl.games.entity.jpa;

import com.ivpl.games.constants.Color;
import com.ivpl.games.entity.CellKey;
import lombok.Getter;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Entity
@Getter
@Table(name = "STEPS")
public class Step {

    @Id
    @GeneratedValue
    private Long id;
    private Integer gameStepId;
    private Long gameId;
    private Color playerColor;
    @Column(columnDefinition = "int[]")
    @Type(type = "com.ivpl.games.entity.IntegerArrayType")
    private Integer[] stepFrom;
    @Column(columnDefinition = "int[]")
    @Type(type = "com.ivpl.games.entity.IntegerArrayType")
    private Integer[] stepTo;
    private Integer figureId;

    public Step(Long gameId, Integer gameStepId, Color playerColor, CellKey stepFrom, CellKey stepTo, Integer figureId) {
        this.gameId = gameId;
        this.gameStepId = gameStepId;
        this.playerColor = playerColor;
        this.stepFrom = stepFrom.getAsArray();
        this.stepTo = stepTo.getAsArray();
        this.figureId = figureId;
    }

    protected Step() {}
}
