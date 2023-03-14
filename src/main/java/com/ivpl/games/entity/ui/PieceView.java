package com.ivpl.games.entity.ui;

import com.ivpl.games.constants.Color;
import com.ivpl.games.constants.PieceType;
import com.ivpl.games.utils.DirectionsForClassRepo;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.shared.Registration;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.logging.log4j.util.Strings;

import java.util.*;

import static com.ivpl.games.constants.Color.WHITE;
import static com.ivpl.games.constants.Constants.*;

@Getter
public abstract class PieceView extends Div {

    private final Long pieceId;
    private final Long dbId;
    private final Color color;
    private final PieceType type;
    @Getter
    private Cell position;
    @Setter
    private Registration onClickListener;
    private final Map<CellKey, PieceView> piecesToBeEaten = new HashMap<>();
    private final Set<CellKey> possibleSteps = new HashSet<>();
    private final LinkedList<CellKey> steps = new LinkedList<>();
    private boolean shouldStopCalculationForDirection;

    protected PieceView(Long pieceId, Long dbId, Color color, PieceType type, Cell position) {
        this.pieceId = pieceId;
        this.dbId = dbId;
        this.color = color;
        this.type = type;
        this.position = position;
        add(getImage());
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
                            .takeWhile(c -> !c.isOccupied() || !getColor().equals(c.getPiece().getColor()))
                            .forEach(targetCell -> {
                                if (targetCell.isOccupied()) {
                                    LinkedList<Cell> cellsBehindTarget = getCellsBehindTargetCell(currentPosition, targetCell.getKey(), cells);
                                    if (cellsBehindTarget.isEmpty() || cellsBehindTarget.getFirst().isOccupied()) {
                                        shouldStopCalculationForDirection = true;
                                    } else {
                                        cellsBehindTarget.forEach(c -> {
                                            piecesToBeEaten.put(c.getKey(), targetCell.getPiece());
                                            eatingCells.add(c.getKey());
                                            shouldStopCalculationForDirection = true;
                                        });
                                    }
                                } else if (getClass() == CheckerQueenView.class || (WHITE.equals(color)
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
    public void placeAt(Cell targetCell) {
        position.removePiece();
        targetCell.setPiece(this);
        position = targetCell;
        unselectPiece();
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

    protected abstract @NonNull LinkedList<Cell> getCellsBehindTargetCell(CellKey sourceKey, CellKey targetKey, Map<CellKey, Cell> cells);

    public void toDie() {
        position.removePiece();
        position = null;
    }

    protected String calculateImageName() {
        return Strings.concat(getColor().name(), getClass().getSimpleName());
    }
}
