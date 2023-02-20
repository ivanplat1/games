package com.ivpl.games.entity;

import com.ivpl.games.constants.Color;
import com.ivpl.games.constants.FigureType;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.dom.Style;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.Range;
import org.springframework.data.annotation.Id;
import org.springframework.data.util.Pair;

import javax.persistence.GeneratedValue;
import java.util.*;

import static com.ivpl.games.constants.Constants.*;

@Getter
public abstract class Figure extends Div implements FigureTemp {

    private final String FILTER_PROP = "filter";

    @Id
    @GeneratedValue
    private Integer _id;
    private final Color color;
    private final FigureType type;
    @Getter
    private Cell position;
    private final Map<CellKey, Figure> figureToBeEaten = new HashMap<>();

    private final Set<CellKey> possibleSteps = new HashSet<>();
    private final List<Pair<Integer, Integer>> steps = new LinkedList<>();

    public Figure(Color color, FigureType type, Cell initPosition) {
        this.color = color;
        this.type = type;
        this.position = initPosition;
        add(getImage());
    }

    public void calculatePossibleSteps(Map<CellKey, Cell> cells) {
        possibleSteps.clear();
        figureToBeEaten.clear();
        Set<CellKey> eatingCells = new HashSet<>();
        Arrays.stream(getDirections()).
                forEach(a -> {
                    CellKey currentPosition = position.getKey();
                    int x = currentPosition.getX() + a[1];
                    int y = currentPosition.getY() + (Color.WHITE.equals(color) ? a[0]*-1 : a[0]);

                    if (Range.between(1, 8).contains(x) && Range.between(1, 8).contains(y)) {
                        CellKey targetKey = new CellKey(x, y);
                        Cell targetCell = cells.get(targetKey);
                        if (targetCell.getFigure() != null) {
                            if (!getColor().equals(targetCell.getFigure().getColor())) {
                                Optional<Cell> cellBehindTarget = getCellBehindTargetCell(currentPosition, targetCell.getKey(), cells);

                                if (cellBehindTarget.isPresent() && cellBehindTarget.get().getFigure() == null) {
                                    figureToBeEaten.put(cellBehindTarget.get().getKey(), targetCell.getFigure());
                                    eatingCells.add(cellBehindTarget.get().getKey());
                                }
                            }
                        } else if (Color.WHITE.equals(color)
                                // revert for blacks
                                ? currentPosition.getY() > targetKey.getY()
                                : currentPosition.getY() < targetKey.getY()) {
                            possibleSteps.add(targetKey);
                        }
                    }
                });
        if (!eatingCells.isEmpty()) {
            possibleSteps.clear();
            possibleSteps.addAll(eatingCells);
        }
    }

    public void doStepTo(Cell targetCellKey) {
        position.removeFigure();
        targetCellKey.setFigure(this);
        position = targetCellKey;
        selectUnselectFigure();
    }

    private Image getImage() {
        return new Image(Color.WHITE.equals(color) ? WHITE_CHECKER_IMG : BLACK_CHECKER_IMG, "checkerImage");
    }

    public void selectUnselectFigure() {
        Style style = getStyle();
        if (style.get(FILTER_PROP) == null) {
            style.set(FILTER_PROP, "brightness(0.80)");
        } else {
            style.remove(FILTER_PROP);
        }
    }

    protected abstract int[][] getDirections();

    @NonNull
    private Optional<Cell> getCellBehindTargetCell(CellKey sourceKey, CellKey targetKey, Map<CellKey, Cell> cells) {
        CellKey key = new CellKey(sourceKey.getX()+(targetKey.getX()-sourceKey.getX())*2,
                sourceKey.getY()+(targetKey.getY()- sourceKey.getY())*2);
        return Optional.ofNullable(cells.get(key));
    }

    public void toDie() {
        position.removeFigure();
        position = null;
    }
}
