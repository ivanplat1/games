package com.ivpl.games.entity;

import com.ivpl.games.constants.Color;
import com.ivpl.games.utils.DirectionsForClassRepo;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.dom.Style;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.util.Pair;

import javax.persistence.GeneratedValue;
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
        getDirections().forEach(d -> d.forEach(dc -> {
                CellKey currentPosition = position.getKey();
                int x = currentPosition.getX() + dc[1];
                int y = currentPosition.getY() + (WHITE.equals(color) ? dc[0]*-1 : dc[0]);

                if (Range.between(1, 8).contains(x) && Range.between(1, 8).contains(y)) {
                    CellKey targetKey = new CellKey(x, y);
                    Cell targetCell = cells.get(targetKey);
                    if (targetCell.getFigure() != null) {
                        if (!getColor().equals(targetCell.getFigure().getColor())) {
                            List<Cell> cellsBehindTarget = getCellsBehindTargetCell(currentPosition, targetCell.getKey(), cells);

                            cellsBehindTarget.forEach(c -> {
                                figuresToBeEaten.put(c.getKey(), targetCell.getFigure());
                                eatingCells.add(c.getKey());
                            });
                        }
                    } else if (getClass() == Queen.class || (WHITE.equals(color)
                            // revert for blacks
                            ? currentPosition.getY() > targetKey.getY()
                            : currentPosition.getY() < targetKey.getY()))
                        possibleSteps.add(targetKey);
                }
            }));
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

    private List<List<int[]>> getDirections() {
        return DirectionsForClassRepo.getDirectionsForClass(getClass());
    }

    @NonNull
    protected abstract List<Cell> getCellsBehindTargetCell(CellKey sourceKey, CellKey targetKey, Map<CellKey, Cell> cells);

    public void toDie() {
        position.removeFigure();
        position = null;
    }
}
