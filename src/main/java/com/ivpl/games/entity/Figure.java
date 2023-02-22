package com.ivpl.games.entity;

import com.ivpl.games.constants.Color;
import com.ivpl.games.utils.DirectionsForClassRepo;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.dom.Style;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;

import java.util.*;

import static com.ivpl.games.constants.Color.WHITE;
import static com.ivpl.games.constants.Constants.*;

@Getter
public abstract class Figure extends Div {

    private final Color color;
    @Getter
    private Cell position;
    private final Map<CellKey, Figure> figuresToBeEaten = new HashMap<>();
    private final Set<CellKey> possibleSteps = new HashSet<>();
    private final List<Pair<Integer, Integer>> steps = new LinkedList<>();

    @Autowired
    protected Figure(Color color, Cell initPosition) {
        this.color = color;
        this.position = initPosition;
        add(getImage());
    }

    public void calculatePossibleSteps(Map<CellKey, Cell> cells) {
        possibleSteps.clear();
        figuresToBeEaten.clear();
        Set<CellKey> eatingCells = new HashSet<>();
        CellKey currentPosition = position.getKey();
        int currentX = currentPosition.getX();
        int currentY = currentPosition.getY();
        getDirections().values()
                .forEach(d -> d.stream()
                        .map(dc -> new CellKey(currentX+dc[1], currentY+(WHITE.equals(color) ? dc[0]*-1 : dc[0])))
                        .filter(c -> c.inRange(1, 8))
                        .map(k -> Optional.ofNullable(cells.get(k)))
                        .filter(Optional::isPresent).map(Optional::get)
                        .takeWhile(c -> !c.hasFigure() || !getColor().equals(c.getFigure().getColor()))
                        .forEach(targetCell -> {
                            if (targetCell.hasFigure()) {
                                List<Cell> cellsBehindTarget = getCellsBehindTargetCell(currentPosition, targetCell.getKey(), cells);
                                cellsBehindTarget.forEach(c -> {
                                    figuresToBeEaten.put(c.getKey(), targetCell.getFigure());
                                    eatingCells.add(c.getKey());
                                });
                            } else if (getClass() == Queen.class || (WHITE.equals(color)
                                    // filter out steps back
                                    ? currentPosition.getY() > targetCell.getKey().getY()
                                    : currentPosition.getY() < targetCell.getKey().getY()))
                                possibleSteps.add(targetCell.getKey());
                        })
        );
        if (!eatingCells.isEmpty()) {
            possibleSteps.clear();
            possibleSteps.addAll(eatingCells);
        }
    }

    @NonNull
    public void doStepTo(Cell targetCellKey) {
        position.removeFigure();
        targetCellKey.setFigure(this);
        position = targetCellKey;
        selectUnselectFigure();
    }

    protected abstract Image getImage();

    public void selectUnselectFigure() {
        Style style = getStyle();
        if (style.get(FILTER_PROP) == null) {
            style.set(FILTER_PROP, "brightness(0.80)");
        } else {
            style.remove(FILTER_PROP);
        }
    }

    private Map<String, List<int[]>> getDirections() {
        return DirectionsForClassRepo.getDirectionsForClass(getClass());
    }

    @NonNull
    protected abstract List<Cell> getCellsBehindTargetCell(CellKey sourceKey, CellKey targetKey, Map<CellKey, Cell> cells);

    public void toDie() {
        position.removeFigure();
        position = null;
    }
}
