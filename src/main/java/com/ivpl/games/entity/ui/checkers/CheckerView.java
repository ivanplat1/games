package com.ivpl.games.entity.ui.checkers;

import com.ivpl.games.constants.Color;
import com.ivpl.games.constants.PieceType;
import com.ivpl.games.entity.ui.Cell;
import com.ivpl.games.entity.ui.CellKey;

import java.util.*;

public class CheckerView extends CheckersPieceView {

    public CheckerView(Long id, Long dbId, Color color, PieceType type, Cell initPosition) {
        super(id, dbId, color, type, initPosition);
    }

    @Override
    protected @lombok.NonNull LinkedList<Cell> getCellsBehindTargetCell(CellKey sourceKey, CellKey targetKey, Map<CellKey, Cell> cells) {
        @lombok.NonNull LinkedList<Cell> list = new LinkedList<>();
        CellKey key = new CellKey(sourceKey.getX()+(targetKey.getX()-sourceKey.getX())*2,
                sourceKey.getY()+(targetKey.getY()- sourceKey.getY())*2);
        Optional.ofNullable(cells.get(key)).filter(c -> !c.isOccupied()).ifPresent(list::add);
        return list;
    }
}
