package com.ivpl.games.entity.ui;

import com.ivpl.games.constants.Color;
import com.ivpl.games.constants.PieceType;
import com.ivpl.games.utils.DirectionsForClassRepo;
import com.vaadin.flow.component.html.Image;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.ivpl.games.constants.Color.WHITE;

public class CheckerQueenView extends PieceView {

    public CheckerQueenView(Long id, Long dbId, Color color, PieceType type, Cell initPosition) {
        super(id, dbId, color, type, initPosition);
    }

    @Override
    protected Image getImage() {
        return new Image(Color.WHITE.equals(getColor()) ? "images/checkers/WhiteQueen.png" : "images/checkers/BlackQueen.png", "queenImage");
    }

    @Override
    protected @NonNull LinkedList<Cell> getCellsBehindTargetCell(CellKey sourceKey, CellKey targetKey, Map<CellKey, Cell> cells) {
        return DirectionsForClassRepo
                .getCertainDirectionForClass(getClass(), calculateDirectionKey(sourceKey, targetKey))
                .stream()
                .map(dc -> new CellKey(
                        targetKey.getX()+dc[0],
                        targetKey.getY()+(WHITE.equals(cells.get(targetKey).getColor()) ? dc[1]*-1 : dc[1])))
                .filter(c -> c.inRange(1, 8))
                .map(k -> Optional.ofNullable(cells.get(k)))
                .filter(Optional::isPresent).map(Optional::get)
                .takeWhile(c -> !c.isOccupied())
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private String calculateDirectionKey(CellKey sourceKey, CellKey targetKey) {
        return Arrays.toString(new int[] {(targetKey.getX()-sourceKey.getX()) < 0 ? -1 : 1,
                (targetKey.getY()-sourceKey.getY()) < 0 ? -1 : 1});
    }

}
