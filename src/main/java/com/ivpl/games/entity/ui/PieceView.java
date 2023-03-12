package com.ivpl.games.entity.ui;

import com.ivpl.games.constants.Color;
import com.ivpl.games.utils.DirectionsForClassRepo;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.shared.Registration;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.*;

import static com.ivpl.games.constants.Color.WHITE;
import static com.ivpl.games.constants.Constants.*;

@Getter
public abstract class PieceView extends Div {

    private final Integer pieceId;
    private final Color color;
    @Getter
    private Cell position;
    @Setter
    private Registration onClickListener;
    private final Map<CellKey, PieceView> piecesToBeEaten = new HashMap<>();
    private final Set<CellKey> possibleSteps = new HashSet<>();
    private final LinkedList<CellKey> steps = new LinkedList<>();
    private boolean shouldStopCalculationForDirection;

    protected PieceView(Integer pieceId, Color color, Cell initPosition) {
        this.pieceId = pieceId;
        this.color = color;
        this.position = initPosition;
        add(getImage());
        steps.addFirst(initPosition.getKey());
    }

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
                            .takeWhile(c -> !c.isOccupied() || !getColor().equals(c.getPieceView().getColor()))
                            .forEach(targetCell -> {
                                if (targetCell.isOccupied()) {
                                    List<Cell> cellsBehindTarget = getCellsBehindTargetCell(currentPosition, targetCell.getKey(), cells);
                                    cellsBehindTarget.forEach(c -> {
                                        piecesToBeEaten.put(c.getKey(), targetCell.getPieceView());
                                        eatingCells.add(c.getKey());
                                        shouldStopCalculationForDirection = true;
                                    });
                                } else if (getClass() == QueenView.class || (WHITE.equals(color)
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

    @NonNull
    public void moveTo(Cell targetCell) {
        position.removePiece();
        targetCell.setPieceView(this);
        position = targetCell;
        unselectPiece();
        steps.add(targetCell.getKey());
    }

    protected abstract Image getImage();

    public void selectUnselectPiece() {
        Style style = getStyle();
        if (style.get(FILTER_PROP) == null) {
            selectPiece();
        } else {
            unselectPiece();
        }
    }

    public void selectPiece() {
        getStyle().set(FILTER_PROP, "brightness(0.80)");
    }

    public void unselectPiece() {
        getStyle().remove(FILTER_PROP);
    }

    private Map<String, List<int[]>> getDirections() {
        return DirectionsForClassRepo.getDirectionsForClass(getClass());
    }

    @NonNull
    protected abstract List<Cell> getCellsBehindTargetCell(CellKey sourceKey, CellKey targetKey, Map<CellKey, Cell> cells);

    public void toDie() {
        position.removePiece();
        position = null;
    }
}
