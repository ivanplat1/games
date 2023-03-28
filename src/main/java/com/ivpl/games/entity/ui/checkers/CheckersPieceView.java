package com.ivpl.games.entity.ui.checkers;

import com.ivpl.games.constants.Color;
import com.ivpl.games.constants.PieceType;
import com.ivpl.games.entity.ui.AbstractPieceView;
import com.ivpl.games.entity.ui.Cell;
import com.ivpl.games.entity.ui.CellKey;
import com.vaadin.flow.component.html.Image;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.ivpl.games.constants.Color.WHITE;
import static com.ivpl.games.constants.Constants.PIECE_IMAGE_ALT;

public abstract class CheckersPieceView extends AbstractPieceView {

    private static final String IMAGE_PATH_STR = "images/checkers/%s.png";

    protected CheckersPieceView(Long pieceId, Long dbId, Color color, PieceType type, Cell position) {
        super(pieceId, dbId, color, type, position);
    }

    public void calculatePossibleSteps(Map<CellKey, Cell> cells, boolean checkValidationNeeded) {
        possibleSteps.clear();
        piecesToBeEaten.clear();
        Set<CellKey> eatingCells = new HashSet<>();
        CellKey currentPosition = position.getKey();
        int currentX = currentPosition.getX();
        int currentY = currentPosition.getY();
        getDirections().values()
                .forEach(d -> {
                            AtomicBoolean shouldStopCalculationForDirection = new AtomicBoolean(false);
                            d.stream()
                                    .takeWhile(e -> !shouldStopCalculationForDirection.get())
                                    .map(dc -> new CellKey(currentX+dc[1], currentY+(WHITE.equals(color) ? dc[0]*-1 : dc[0])))
                                    .filter(c -> c.inRange(1, 8))
                                    .map(k -> Optional.ofNullable(cells.get(k)))
                                    .filter(Optional::isPresent).map(Optional::get)
                                    .takeWhile(c -> !c.isOccupied() || !getColor().equals(c.getPiece().getColor()))
                                    .forEach(targetCell -> {
                                        if (targetCell.isOccupied()) {
                                            LinkedList<Cell> cellsBehindTarget = getCellsBehindTargetCell(currentPosition, targetCell.getKey(), cells);
                                            if (cellsBehindTarget.isEmpty() || cellsBehindTarget.getFirst().isOccupied()) {
                                                shouldStopCalculationForDirection.set(true);
                                            } else {
                                                cellsBehindTarget.forEach(c -> {
                                                    piecesToBeEaten.put(c.getKey(), targetCell.getPiece());
                                                    eatingCells.add(c.getKey());
                                                    shouldStopCalculationForDirection.set(true);
                                                });
                                            }
                                        } else if ((WHITE.equals(color)
                                                // filter out steps back
                                                ? currentPosition.getY() > targetCell.getKey().getY()
                                                : currentPosition.getY() < targetCell.getKey().getY())
                                                || PieceType.CHECKER_QUEEN.equals(type))
                                            possibleSteps.add(targetCell.getKey());
                                    });
                        }
                );
        if (!eatingCells.isEmpty()) {
            possibleSteps.clear();
            possibleSteps.addAll(eatingCells);
        }
    }

    @Override
    protected Image getImage() {
        return new Image(String.format(IMAGE_PATH_STR, calculateImageName()), PIECE_IMAGE_ALT);
    }

    protected abstract LinkedList<Cell> getCellsBehindTargetCell(CellKey sourceKey, CellKey targetKey, Map<CellKey, Cell> cells);
}
