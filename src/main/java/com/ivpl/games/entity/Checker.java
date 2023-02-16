package com.ivpl.games.entity;

import com.ivpl.games.constants.Color;
import com.ivpl.games.constants.FigureType;

import java.util.Map;


public class Checker extends Figure {

    public Checker(Color color, CellKey initPosition) {
        super(color, FigureType.CHECKER, initPosition);
    }

    @Override
    protected int[][] getDirections() {
        return new int[][] {{1, -1}, {1, 1}};
    }
}
