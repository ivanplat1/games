package com.ivpl.games.entity;

import com.ivpl.games.constants.Color;
import com.ivpl.games.constants.FigureType;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import lombok.Getter;
import org.apache.commons.lang3.Range;
import org.springframework.data.annotation.Id;
import org.springframework.data.util.Pair;

import javax.persistence.GeneratedValue;
import java.util.*;

import static com.ivpl.games.constants.Constants.BLACK_CHECKER_IMG;
import static com.ivpl.games.constants.Constants.WHITE_CHECKER_IMG;

@Getter
public abstract class Figure extends Div implements FigureTemp {

    @Id
    @GeneratedValue
    private Integer _id;
    private final Color color;
    private final FigureType type;
    private CellKey position;

    private Set<CellKey> possibleSteps = new HashSet<>();
    private List<Pair<Integer, Integer>> steps = new LinkedList<>();

    public Figure(Color color, FigureType type, CellKey initPosition) {
        this.color = color;
        this.type = type;
        this.position = initPosition;
        add(getImage());
    }

    public  void calculatePossibleSteps(Map<CellKey, Cell> cells) {
        possibleSteps.clear();
        Arrays.stream(getDirections()).
                forEach(a -> {
                    int x = position.getX() + a[1];
                    int y = position.getY() + (Color.WHITE.equals(color) ? a[0]*-1 : a[0]);

                    if (Range.between(1, 8).contains(x) && Range.between(1, 8).contains(y)) {
                        CellKey key = new CellKey(x, y);
                        if (cells.get(key).getFigure() == null)
                            possibleSteps.add(key);
                    }
                });
    }

    public void doStepTo(Integer targetCellId) {

    }

    private Image getImage() {
        return new Image(Color.WHITE.equals(color) ? WHITE_CHECKER_IMG : BLACK_CHECKER_IMG, "checkerImage");
    }

    protected abstract int[][] getDirections();
}
