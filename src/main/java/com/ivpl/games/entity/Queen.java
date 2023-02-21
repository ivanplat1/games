package com.ivpl.games.entity;

import com.ivpl.games.constants.Color;
import com.vaadin.flow.component.html.Image;
import lombok.NonNull;

import java.util.*;

import static com.ivpl.games.constants.Constants.*;

public class Queen extends Figure {

    public Queen(Color color, Cell initPosition) {
        super(color, initPosition);
    }

    @Override
    protected Image getImage() {
        return new Image(Color.WHITE.equals(getColor()) ? WHITE_QUEEN_IMG : BLACK_QUEEN_IMG, "queenImage");
    }

    @Override
    protected @NonNull List<Cell> getCellsBehindTargetCell(CellKey sourceKey, CellKey targetKey, Map<CellKey, Cell> cells) {
        List<Cell> list = new ArrayList<>();
        return list;
    }
}
