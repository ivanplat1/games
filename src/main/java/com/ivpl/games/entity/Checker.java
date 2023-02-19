package com.ivpl.games.entity;

import com.ivpl.games.constants.Color;
import com.ivpl.games.constants.FigureType;

public class Checker extends Figure {

    public Checker(Color color, Cell initPosition) {
        super(color, FigureType.CHECKER, initPosition);
    }

    @Override
    protected int[][] getDirections() {
        return new int[][] {{1, -1}, {1, 1}};
    }
}
