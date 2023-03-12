package com.ivpl.games.entity.ui;

import com.ivpl.games.constants.Color;
import com.vaadin.flow.component.html.Image;

import java.util.*;

import static com.ivpl.games.constants.Constants.BLACK_CHECKER_IMG;
import static com.ivpl.games.constants.Constants.WHITE_CHECKER_IMG;

public class CheckerView extends PieceView {

    public CheckerView(Long id, Long dbId, Color color, Cell initPosition) {
        super(id, dbId, color, initPosition);
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
        Optional.ofNullable(cells.get(key)).filter(c -> !c.isOccupied()).ifPresent(list::add);
        return list;
    }
}
