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
import org.apache.commons.lang3.Range;

import java.util.*;
import java.util.stream.Collectors;


@Route
public class ChessBoard extends VerticalLayout {

    private Figure selectedFigure;
    private Color currentTurn = Color.WHITE;
    private final Map<CellKey, Cell> cells = new LinkedHashMap<>();
    private final List<Figure> figures = new LinkedList<>();

    public ChessBoard() {

        add(printBoard());
        placeFigures();
        recalculatePossibleSteps();
    }

    private void placeFigures() {
        figures.addAll(cells.entrySet().stream()
                .filter(e -> Color.BLACK.equals(e.getValue().getColor()))
                .filter(e -> Range.between(1, 3).contains(e.getKey().getY()) || Range.between(6, 8).contains(e.getKey().getY()))
                .map(this::addFigure).collect(Collectors.toList()));
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

    private Figure addFigure(Map.Entry<CellKey, Cell> cellEntry) {
        Figure f = new Checker(cellEntry.getKey().getY() < 5 ? Color.BLACK : Color.WHITE, cellEntry.getValue());
        cellEntry.getValue().setFigure(f);

        f.addClickListener(e -> {
            if (f.getPossibleSteps().isEmpty()) return;
            f.selectUnselectAction(f.equals(selectedFigure));
            if (!f.equals(selectedFigure)) {
                if (selectedFigure != null) {
                    selectedFigure.getPossibleSteps().forEach(k -> cells.get(k).getStyle().remove("filter"));
                    selectedFigure.selectUnselectAction(true);
                }
                f.getPossibleSteps().forEach(k -> {
                    Cell cell = cells.get(k);
                    cell.getStyle().set("filter", "brightness(0.80)");
                    cell.addClickListener(event -> selectedFigure.doStepTo(cell));
                });
                selectedFigure = f;
            } else {
                f.getPossibleSteps().forEach(k -> cells.get(k).getStyle().remove("filter"));
                selectedFigure = null;
            }
        });
         return f;
    }
}
