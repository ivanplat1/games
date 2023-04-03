package com.ivpl.games.entity.ui.chess;

import com.ivpl.games.constants.Color;
import com.ivpl.games.constants.PieceType;
import com.ivpl.games.entity.ui.Cell;
import com.ivpl.games.entity.ui.CellKey;
import org.apache.commons.lang3.Range;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KingView extends ChessPieceView {

    public KingView(Long pieceId, Long dbId, Color color, PieceType type, Cell position) {
        super(pieceId, dbId, color, type, position);
    }

    public void calculatePossibleSteps(Map<CellKey, Cell> cells, boolean checkValidationNeeded) {
        super.calculatePossibleSteps(cells, checkValidationNeeded);
        calculateCastlingSteps(cells);
    }

    private void calculateCastlingSteps(Map<CellKey, Cell> cells) {
        if (steps.isEmpty()) {
            List<RookView> rooks = cells.values().stream().filter(Cell::isOccupied)
                    .map(Cell::getPiece)
                    .filter(p -> color.equals(p.getColor()) && PieceType.ROOK.equals(p.getType()))
                    .filter(rook -> rook.getSteps().isEmpty())
                    .map(p -> (RookView) p)
                    .collect(Collectors.toList());
            rooks.stream().filter(rook -> isRookCastlingPossible(cells, rook))
                    .forEach(rook -> {
                        int x = rook.getPosition().getKey().getX();
                        int y = rook.getPosition().getKey().getY();
                        if (x == 1) {
                            Cell cell = cells.get(new CellKey(3, y));
                            cell.setOnClickListener(cell.addClickListener(event -> {
                                placeAt(cell);
                                rook.placeAt(cells.get(new CellKey(4, y)));
                            }));
                            possibleSteps.add(cell.getKey());
                        } else if (x == 8) {
                            Cell cell = cells.get(new CellKey(7, y));
                            cell.setOnClickListener(cell.addClickListener(event -> {
                                placeAt(cell);
                                rook.placeAt(cells.get(new CellKey(6, y)));
                            }));
                            possibleSteps.add(cell.getKey());
                        }
                    });
        }
    }

    private boolean isRookCastlingPossible(Map<CellKey, Cell> cells, RookView rook) {
        int x = rook.getPosition().getKey().getX();
        int y = rook.getPosition().getKey().getY();
        return cells.entrySet().stream()
                .filter(e -> e.getKey().getY() == y &&
                        ((x == 1 && Range.between(2, 4).contains(e.getKey().getX()))
                                || (x == 8 && Range.between(6, 7).contains(e.getKey().getX())))
                )
                .map(Map.Entry::getValue)
                .noneMatch(Cell::isOccupied);
    }
}
