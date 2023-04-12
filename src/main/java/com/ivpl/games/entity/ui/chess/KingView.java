package com.ivpl.games.entity.ui.chess;

import com.ivpl.games.constants.Color;
import com.ivpl.games.constants.PieceType;
import com.ivpl.games.entity.ui.Cell;
import com.ivpl.games.entity.ui.CellKey;
import com.ivpl.games.utils.CommonUtils;
import org.apache.commons.lang3.Range;

import java.util.Map;

public class KingView extends ChessPieceView {

    public KingView(Long pieceId, Long dbId, Color color, PieceType type, Cell position) {
        super(pieceId, dbId, color, type, position);
    }

    public void calculateCastlingSteps(Map<CellKey, Cell> cells) {
        if (steps.isEmpty()
                // check is King already under attack
                && !CommonUtils.isAnyCellUnderAttack(color, cells, position.getKey())) {
            cells.values().stream().filter(Cell::isOccupied)
                    .map(Cell::getPiece)
                    .filter(p -> color.equals(p.getColor()) && PieceType.ROOK.equals(p.getType()))
                    .filter(rook -> rook.getSteps().isEmpty())
                    .map(p -> (RookView) p)
                    .filter(rook -> isRookCastlingPossible(cells, rook))
                    .forEach(rook -> {
                        int x = rook.getPosition().getKey().getX();
                        int y = rook.getPosition().getKey().getY();
                        Cell cell = cells.get(new CellKey(x == 1 ? 3 : 7, y));
                        possibleSteps.add(cell.getKey());
                    });
        }
    }

    private boolean isRookCastlingPossible(Map<CellKey, Cell> cells, RookView rook) {
        int x = rook.getPosition().getKey().getX();
        int y = rook.getPosition().getKey().getY();
        return cells.entrySet().stream()
                .filter(e -> e.getKey().getY() == y &&
                        ((x == 1 && Range.between(2, 4).contains(e.getKey().getX())
                                && cellsSuitForCastling(color, cells,
                                new CellKey(2, y),
                                new CellKey(3, y),
                                new CellKey(4, y)))
                                || (x == 8 && Range.between(6, 7).contains(e.getKey().getX())
                                && cellsSuitForCastling(color, cells,
                                new CellKey(6, y),
                                new CellKey(7, y))))
                )
                .map(Map.Entry::getValue)
                .anyMatch(c -> !c.isOccupied());
    }

    private boolean cellsSuitForCastling(Color playerColor, Map<CellKey, Cell> cells, CellKey ... keys) {
        return !CommonUtils.isAnyCellUnderAttack(playerColor, cells, keys)
                && CommonUtils.cellsAreNotOccupied(cells, keys);
    }
}
