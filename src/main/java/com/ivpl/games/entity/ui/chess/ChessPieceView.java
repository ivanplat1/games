package com.ivpl.games.entity.ui.chess;

import com.ivpl.games.constants.Color;
import com.ivpl.games.constants.PieceType;
import com.ivpl.games.entity.ui.AbstractPieceView;
import com.ivpl.games.entity.ui.Cell;
import com.ivpl.games.entity.ui.CellKey;
import com.vaadin.flow.component.html.Image;
import org.apache.logging.log4j.util.Strings;

import java.util.Map;

public abstract class ChessPieceView extends AbstractPieceView {

    protected ChessPieceView(Long pieceId, Long dbId, Color color, PieceType type, Cell position) {
        super(pieceId, dbId, color, type, position);
    }

    @Override
    protected Image getImage() {
        return new Image(Strings.concat("images/chess/", calculateImageName()).concat(".png"), "checkerImage");
    }

    @Override
    public void calculatePossibleSteps(Map<CellKey, Cell> cells) {
        return;
    }
}
