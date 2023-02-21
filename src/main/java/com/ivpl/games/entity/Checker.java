package com.ivpl.games.entity;

import com.ivpl.games.constants.Color;
import com.vaadin.flow.component.html.Image;

import java.util.*;

import static com.ivpl.games.constants.Constants.BLACK_CHECKER_IMG;
import static com.ivpl.games.constants.Constants.WHITE_CHECKER_IMG;

public class Checker extends Figure {

    public Checker(Color color, Cell initPosition) {
        super(color, initPosition);
    }

    @Override
    protected Image getImage() {
        return new Image(Color.WHITE.equals(getColor()) ? WHITE_CHECKER_IMG : BLACK_CHECKER_IMG, "checkerImage");
    }

    @Override
    protected List<Cell> getCellsBehindTargetCell(CellKey sourceKey, CellKey targetKey, Map<CellKey, Cell> cells) {
        List<Cell> list = new ArrayList<>();
        CellKey key = new CellKey(sourceKey.getX()+(targetKey.getX()-sourceKey.getX())*2,
                sourceKey.getY()+(targetKey.getY()- sourceKey.getY())*2);
        Optional.ofNullable(cells.get(key)).filter(c -> c.getFigure() == null).ifPresent(list::add);
        return list;
    }
}
