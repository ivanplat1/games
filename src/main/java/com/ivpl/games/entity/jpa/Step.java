package com.ivpl.games.entity.jpa;

import com.ivpl.games.constants.Color;
import com.ivpl.games.entity.ui.CellKey;
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
    @Type(type = "com.ivpl.games.converter.IntegerArrayType")
    private Integer[] stepFrom;
    @Column(columnDefinition = "int[]")
    @Type(type = "com.ivpl.games.converter.IntegerArrayType")
    private Integer[] stepTo;
    private Integer pieceId;

    public Step(Long gameId, Integer gameStepId, Color playerColor, CellKey stepFrom, CellKey stepTo, Integer pieceId) {
        this.gameId = gameId;
        this.gameStepId = gameStepId;
        this.playerColor = playerColor;
        this.stepFrom = stepFrom.getAsArray();
        this.stepTo = stepTo.getAsArray();
        this.pieceId = pieceId;
    }

    protected Step() {}
}
