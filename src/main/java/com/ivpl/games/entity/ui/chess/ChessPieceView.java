package com.ivpl.games.entity.ui.chess;

import com.ivpl.games.constants.Color;
import com.ivpl.games.constants.PieceType;
import com.ivpl.games.entity.ui.AbstractPieceView;
import com.ivpl.games.entity.ui.Cell;
import com.ivpl.games.entity.ui.CellKey;
import com.vaadin.flow.component.html.Image;

import java.util.*;

import static com.ivpl.games.constants.Color.WHITE;
import static com.ivpl.games.constants.Constants.PIECE_IMAGE_ALT;

public abstract class ChessPieceView extends AbstractPieceView {

    private static final String IMAGE_PATH_STR = "images/chess/%s.png";
    private boolean kingIsUnderAttack;

    protected ChessPieceView(Long pieceId, Long dbId, Color color, PieceType type, Cell position) {
        super(pieceId, dbId, color, type, position);
    }

    @Override
    protected Image getImage() {
        return new Image(String.format(IMAGE_PATH_STR, calculateImageName()), PIECE_IMAGE_ALT);
    }

    @Override
    public void calculatePossibleSteps(Map<CellKey, Cell> cells, boolean checkValidationNeeded) {
        possibleSteps.clear();
        piecesToBeEaten.clear();
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
                                    .takeWhile(c ->
                                            PieceType.PAWN.equals(getType())
                                                    ? (c.getKey().getX() == currentX && !c.isOccupied())
                                                        || (c.getKey().getX() != currentX && c.isOccupied() && !getColor().equals(c.getPiece().getColor()))
                                                    : !c.isOccupied() || !getColor().equals(c.getPiece().getColor()))
                                    .forEach(targetCell -> {
                                        if (targetCell.isOccupied()) {
                                            shouldStopCalculationForDirection = true;
                                            piecesToBeEaten.put(targetCell.getKey(), targetCell.getPiece());
                                        }
                                        if (!checkValidationNeeded || !isStepLeadsToCheck(cells, targetCell))
                                            possibleSteps.add(targetCell.getKey());
                                    });
                        }
                );
    }

    private boolean isStepLeadsToCheck(Map<CellKey, Cell> cells, Cell targetCell) {
        Cell currentPosition = getPosition();
        AbstractPieceView pieceOnTargetCell = targetCell.getPiece();
        placeAt(targetCell);
        kingIsUnderAttack = false;
        cells.values().stream()
                .takeWhile(e -> !kingIsUnderAttack)
                .filter(Cell::isOccupied)
                .map(Cell::getPiece)
                .filter(p -> !color.equals(p.getColor()))
                .forEach(p -> {
                    p.calculatePossibleSteps(cells, false);
                    if (p.getPiecesToBeEaten().values().stream()
                            .anyMatch(piece -> PieceType.KING.equals(piece.getType())))
                        kingIsUnderAttack = true;
                });
        placeAt(currentPosition);
        Optional.ofNullable(pieceOnTargetCell).ifPresent(p -> targetCell.setPiece(pieceOnTargetCell));
        return kingIsUnderAttack;
    }
}
