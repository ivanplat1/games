package com.ivpl.games.view;

import com.ivpl.games.constants.Color;
import com.ivpl.games.entity.Cell;
import com.ivpl.games.entity.CellKey;
import com.ivpl.games.entity.Checker;
import com.ivpl.games.entity.Figure;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import lombok.NonNull;
import org.apache.commons.lang3.Range;

import java.util.*;
import java.util.stream.Collectors;


@Route
public class ChessBoard extends VerticalLayout {

    private Color currentTurn = Color.WHITE;
    private final Map<CellKey, Cell> cells = new LinkedHashMap<>();
    private final List<Figure> figures = new LinkedList<>();

    public ChessBoard() {

        add(printBoard());
        placeFigures();
        recalculatePossibleSteps();

        figures.forEach(e -> add(new Label(
                        e.getPossibleSteps()
                        .stream()
                        .map(k ->(k.getY() + " " + k.getX() + " ")).collect(Collectors.joining()))));
    }

    private void placeFigures() {
        figures.addAll(cells.entrySet().stream()
                .filter(e -> Color.BLACK.equals(e.getValue().getColor()))
                .filter(e -> Range.between(1, 3).contains(e.getKey().getY()) || Range.between(6, 8).contains(e.getKey().getY()))
                .map(e -> {
                    Figure f = new Checker(e.getKey().getY() < 5 ? Color.BLACK : Color.WHITE, e.getKey());
                    e.getValue().setFigure(f);
                    return f;
                }).collect(Collectors.toList()));
    }

    private VerticalLayout printBoard() {
        VerticalLayout board = new VerticalLayout();
        board.setSpacing(false);
        HorizontalLayout line;

        for (int y = 1; y < 9; ++y) {
            line = new HorizontalLayout();
            line.setSpacing(false);

            for(int x = 1; x < 9; ++x) {
                Cell cell = new Cell(x, y, (x+y) % 2 == 0 ? Color.WHITE : Color.BLACK);
                line.add(cell);
                cells.put(cell.getKey(), cell);
            }
            board.add(line);
        }
        return board;
    }

    private void recalculatePossibleSteps() {
        figures.forEach(f -> f.calculatePossibleSteps(cells));
    }
}
