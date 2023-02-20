package com.ivpl.games.view;

import com.ivpl.games.constants.Color;
import com.ivpl.games.entity.Cell;
import com.ivpl.games.entity.CellKey;
import com.ivpl.games.entity.Checker;
import com.ivpl.games.entity.Figure;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.apache.commons.lang3.Range;

import java.util.*;
import java.util.stream.Collectors;

import static com.ivpl.games.constants.Color.BLACK;
import static com.ivpl.games.constants.Color.WHITE;
import static com.ivpl.games.constants.Constants.*;


@Route
public class ChessBoard extends VerticalLayout {

    private Color currentTurn = WHITE;
    private Div turnIndicator;
    private Figure selectedFigure;
    private final Map<CellKey, Cell> cells = new LinkedHashMap<>();
    private final List<Figure> figures = new LinkedList<>();
    private boolean isAnythingEaten;

    public ChessBoard() {

        HorizontalLayout mainLayout = new HorizontalLayout(printBoard(), new VerticalLayout( new Label("Turn: "), addTurnIndicator()));
        mainLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        add(mainLayout);
        placeFigures();
        recalculatePossibleSteps();
    }

    private void placeFigures() {
        figures.addAll(cells.entrySet().stream()
                .filter(e -> BLACK.equals(e.getValue().getColor()))
                .filter(e -> Range.between(1, 3).contains(e.getKey().getY()) || Range.between(6, 8).contains(e.getKey().getY()))
                .map(this::addFigure).collect(Collectors.toList()));
    }

    private VerticalLayout printBoard() {
        VerticalLayout board = new VerticalLayout();
        board.setSpacing(false);
        board.getStyle().set(BORDER_WIDTH, "6px");
        board.getStyle().set(BORDER_STYLE, "solid");
        board.setPadding(false);
        HorizontalLayout line;

        for (int y = 1; y < 9; ++y) {
            line = new HorizontalLayout();
            line.setSpacing(false);

            for(int x = 1; x < 9; ++x) {
                Cell cell = new Cell(x, y, (x+y) % 2 == 0 ? WHITE : BLACK);
                line.add(cell);
                cells.put(cell.getKey(), cell);
            }
            board.add(line);
        }
        return board;
    }

    private void recalculatePossibleSteps() {
        cleanupCells();
        figures.stream().filter(f -> currentTurn.equals(f.getColor()))
                .forEach(f -> f.calculatePossibleSteps(cells));

        boolean haveToEatAnything = figures.stream()
                .filter(f -> currentTurn.equals(f.getColor()))
                .anyMatch(f -> !f.getFigureToBeEaten().isEmpty());

        if (haveToEatAnything)
            figures.stream().filter(f -> f.getFigureToBeEaten().isEmpty())
                    .forEach(f -> f.getPossibleSteps().clear());
    }

    private Figure addFigure(Map.Entry<CellKey, Cell> cellEntry) {
        Figure f = new Checker(cellEntry.getKey().getY() < 5 ? BLACK : WHITE, cellEntry.getValue());
        cellEntry.getValue().setFigure(f);
        addFigureListener(f);
         return f;
    }

    private void addFigureListener(Figure f) {
        f.addClickListener(e -> {
            if (f.getPossibleSteps().isEmpty() || !currentTurn.equals(f.getColor())) return;
            f.selectUnselectFigure();
            if (!f.equals(selectedFigure)) {
                if (selectedFigure != null) {
                    selectedFigure.getPossibleSteps().forEach(k -> cells.get(k).removeSelectedStyle());
                    selectedFigure.selectUnselectFigure();
                }

                f.getPossibleSteps().forEach(k -> {
                    Cell cell = cells.get(k);
                    cell.addSelectedStyle();
                    addCellListener(cell);
                });
                selectedFigure = f;
            } else {
                f.getPossibleSteps().forEach(k -> cells.get(k).removeSelectedStyle());
                selectedFigure = null;
            }
        });
    }

    private void addCellListener(Cell cell) {

        cell.setOnClickListener(cell.addClickListener(event -> {
            selectedFigure.doStepTo(cell);
            Optional.ofNullable(selectedFigure.getFigureToBeEaten().get(cell.getKey()))
                    .ifPresent(f -> {
                        f.toDie();
                        figures.remove(f);
                        isAnythingEaten = true;
            });

            if (isAnythingEaten) {
                cleanupCells();
                selectedFigure.calculatePossibleSteps(cells);
                // if still have anything to eat
                if (!selectedFigure.getFigureToBeEaten().isEmpty()) {
                    selectedFigure = null;
                    return;
                }
                isAnythingEaten = false;
            }

            revertTurn();
            recalculatePossibleSteps();
            selectedFigure = null;
            checkIsGameOver();
        }));
    }

    private void revertTurn() {
        if (WHITE.equals(currentTurn)) {
            currentTurn = BLACK;
            turnIndicator.getStyle().set(BACKGROUND, BLACK_CELL_COLOR);
        } else {
            currentTurn = WHITE;
            turnIndicator.getStyle().set(BACKGROUND, WHITE_CELL_COLOR);
        }
    }


    private Div addTurnIndicator() {
        turnIndicator = new Div();
        turnIndicator.setHeight("40px");
        turnIndicator.setWidth("40px");
        turnIndicator.getStyle().set(BACKGROUND, WHITE_CELL_COLOR);
        turnIndicator.getStyle().set(BORDER_STYLE, "solid");
        return turnIndicator;
    }

    private void checkIsGameOver() {
        Map<Color, List<Figure>> groupsByColor = figures.stream().collect(Collectors.groupingBy(Figure::getColor, Collectors.toList()));
        if (!groupsByColor.containsKey(WHITE) || !groupsByColor.containsKey(BLACK))
            gameOver();
    }

    private void gameOver() {

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Game Over");
        dialog.add(new Label(currentTurn.toString() + " wins!"));

        Button okButton = new Button("OK", e -> dialog.close());
        dialog.getFooter().add(okButton);
        add(dialog, okButton);
    }

    private void cleanupCells() {
        cells.values().forEach(Cell::removeSelectedStyle);
    }
}
