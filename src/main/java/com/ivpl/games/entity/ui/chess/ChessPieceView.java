package com.ivpl.games.entity.ui.chess;

import com.ivpl.games.constants.Color;
import com.ivpl.games.constants.PieceType;
import com.ivpl.games.entity.ui.AbstractPieceView;
import com.ivpl.games.entity.ui.Cell;
import com.ivpl.games.entity.ui.CellKey;
import com.vaadin.flow.component.html.Image;
import org.apache.logging.log4j.util.Strings;

import java.util.*;

import static com.ivpl.games.constants.Color.WHITE;

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
        possibleSteps.clear();
        piecesToBeEaten.clear();
        Set<CellKey> eatingCells = new HashSet<>();
        CellKey currentPosition = position.getKey();
        int currentX = currentPosition.getX();
        int currentY = currentPosition.getY();
        getDirections().values()
                .forEach(d -> {
                            shouldStopCalculationForDirection = false;
                            d.stream()
                                    .takeWhile(e -> !shouldStopCalculationForDirection)
                                    .map(dc -> new CellKey(currentX+dc[1], currentY+(WHITE.equals(color) ? dc[0]*-1 : dc[0])))
                                    .filter(c -> c.inRange(1, 8))
                                    .map(k -> Optional.ofNullable(cells.get(k)))
                                    .filter(Optional::isPresent).map(Optional::get)
                                    .takeWhile(c -> !c.isOccupied() || !getColor().equals(c.getPiece().getColor()))
                                    .forEach(targetCell -> {
                                        if (targetCell.isOccupied()) {
                                            piecesToBeEaten.put(targetCell.getKey(), targetCell.getPiece());
                                            eatingCells.add(targetCell.getKey());
                                            shouldStopCalculationForDirection = true;
                                        } else if ((WHITE.equals(color) || PieceType.CHECKER_QUEEN.equals(type)
                                                // filter out steps back
                                                ? currentPosition.getY() > targetCell.getKey().getY()
                                                : currentPosition.getY() < targetCell.getKey().getY()))
                                            possibleSteps.add(targetCell.getKey());
                                    });
                        }
                );
        if (!eatingCells.isEmpty()) {
            possibleSteps.clear();
            possibleSteps.addAll(eatingCells);
        }
    }
}
