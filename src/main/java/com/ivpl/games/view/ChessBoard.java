package com.ivpl.games.view;

import com.ivpl.games.constants.Color;
import com.ivpl.games.entity.*;
import com.ivpl.games.security.SecurityService;
import com.ivpl.games.services.Broadcaster;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.security.PermitAll;
import java.util.*;
import java.util.stream.Collectors;

import static com.ivpl.games.constants.Color.BLACK;
import static com.ivpl.games.constants.Color.WHITE;
import static com.ivpl.games.constants.Constants.*;

@CssImport("./styles/styles.css")

@Route("checkers")
@PermitAll
public class ChessBoard extends VerticalLayout {

    private final transient SecurityService securityService;
    Registration broadcasterRegistration;

    private Color currentTurn = WHITE;
    private Div turnIndicator;
    private Figure selectedFigure;
    private final Map<CellKey, Cell> cells = new LinkedHashMap<>();
    private final List<Figure> figures = new ArrayList<>();
    private boolean isAnythingEaten;

    @Autowired
    public ChessBoard(SecurityService securityService) {
        this.securityService = securityService;
        showColorSelector();
    }

    private void placeFigures() {
        figures.addAll(cells.entrySet().stream()
                .filter(e -> BLACK.equals(e.getValue().getColor()))
                .filter(e -> Range.between(1, 3).contains(e.getKey().getY()) || Range.between(6, 8).contains(e.getKey().getY()))
                .map(this::addFigure).collect(Collectors.toList()));
    }

    private VerticalLayout printBoard(Color color) {
        VerticalLayout board = new VerticalLayout();
        board.setSpacing(false);
        board.addClassName("chess-board");
        board.setPadding(false);
        HorizontalLayout line;

        if (WHITE.equals(color)) {
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
        } else {
            for (int y = 8; y > 0; --y) {
                line = new HorizontalLayout();
                line.setSpacing(false);

                for(int x = 8; x > 0; --x) {
                    Cell cell = new Cell(x, y, (x+y) % 2 == 0 ? WHITE : BLACK);
                    line.add(cell);
                    cells.put(cell.getKey(), cell);
                }
                board.add(line);
            }
        }
        return board;
    }

    private void recalculatePossibleSteps() {
        cleanupCells();
        figures.stream().filter(f -> currentTurn.equals(f.getColor()))
                .forEach(f -> f.calculatePossibleSteps(cells));

        boolean haveToEatAnything = figures.stream()
                .filter(f -> currentTurn.equals(f.getColor()))
                .anyMatch(f -> !f.getFiguresToBeEaten().isEmpty());

        if (haveToEatAnything)
            figures.stream().filter(f -> f.getFiguresToBeEaten().isEmpty())
                    .forEach(f -> f.getPossibleSteps().clear());
    }

    private Figure addFigure(Map.Entry<CellKey, Cell> cellEntry) {
        Figure f = new Checker(cellEntry.getKey().getY() < 5 ? BLACK : WHITE, cellEntry.getValue());
        cellEntry.getValue().setFigure(f);
        addFigureListener(f);
         return f;
    }

    private void addFigureListener(Figure f) {
        f.setOnClickListener(f.addClickListener(e -> {
            if (f.equals(e.getSource()))
                Broadcaster.broadcast(Pair.of(this.hashCode(), f.getPosition().getKey()));
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
        }));
    }

    private void addCellListener(Cell cell) {
        cell.setOnClickListener(cell.addClickListener(event -> {
            if (cell.equals(event.getSource()))
                Broadcaster.broadcast(Pair.of(UI.getCurrent().getUIId(), cell.getKey()));
            selectedFigure.doStepTo(cell);
            Optional.ofNullable(selectedFigure.getFiguresToBeEaten().get(cell.getKey()))
                    .ifPresent(f -> {
                        f.toDie();
                        figures.remove(f);
                        isAnythingEaten = true;
            });

            replaceWithQueenIfNeeded(cell, selectedFigure.getColor());

            if (isAnythingEaten) {
                cleanupCells();
                selectedFigure.calculatePossibleSteps(cells);
                // if still have anything to eat
                if (!selectedFigure.getFiguresToBeEaten().isEmpty()) {
                    selectedFigure = null;
                    return;
                }
                isAnythingEaten = false;
            }
            checkIsGameOver();
            revertTurn();
            recalculatePossibleSteps();
            selectedFigure = null;
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
        turnIndicator = createTurnIndicator(WHITE);
        return turnIndicator;
    }

    private Div createTurnIndicator(Color color) {
        Div indicator = new Div();
        indicator.addClassName("turnIndicator");
        indicator.setHeight("50px");
        indicator.setWidth("50px");
        indicator.getStyle().set(BACKGROUND, WHITE.equals(color) ? WHITE_CELL_COLOR : BLACK_CELL_COLOR);
        indicator.getStyle().set(BORDER_STYLE, "solid");
        return indicator;
    }

    private void checkIsGameOver() {
        Map<Color, List<Figure>> groupsByColor = figures.stream().collect(Collectors.groupingBy(Figure::getColor, Collectors.toList()));
        if (!groupsByColor.containsKey(WHITE) || !groupsByColor.containsKey(BLACK))
            gameOver();
    }

    private void gameOver() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Game Over");
        VerticalLayout dialogLayout = new VerticalLayout(new Label(currentTurn.toString() + " wins!"));
        dialogLayout.setAlignItems(Alignment.CENTER);
        dialogLayout.setClassName("general-text");
        Button okButton = new Button("OK", e -> dialog.close());
        dialog.getFooter().add(createNewGameButton(), okButton);
        dialog.add(dialogLayout);
        add(dialog);
        dialog.open();
    }

    private void cleanupCells() {
        cells.values().forEach(Cell::removeSelectedStyle);
    }

    private boolean isBorderCell(CellKey cellKey, Color figureColor) {
        return WHITE.equals(figureColor) ? cellKey.getY() == 1 : cellKey.getY() == 8;
    }

    private void replaceWithQueenIfNeeded(Cell cell, Color figureColor) {
        if (isBorderCell(cell.getKey(), figureColor)) {
            Queen queen = new Queen(figureColor, cell);
            addFigureListener(queen);
            cell.remove(selectedFigure);
            cell.setFigure(queen);
            figures.remove(selectedFigure);
            figures.add(queen);

        }
    }

    private VerticalLayout createRightSidebar() {
        Label label = new Label("TURN");
        label.addClassName("general-text");
        HorizontalLayout indicatorLayout = new HorizontalLayout(label, addTurnIndicator());
        indicatorLayout.setAlignItems(Alignment.CENTER);
        HorizontalLayout menuLayout = new HorizontalLayout(createNewGameButton());
        menuLayout.setPadding(false);
        menuLayout.setSpacing(false);
        return new VerticalLayout(indicatorLayout, menuLayout,
                new Button("Logout", click -> securityService.logout()));
    }

    private Button createNewGameButton() {
        return new Button("New Game", e -> {
            removeAll();
            showColorSelector();
        });
    }

    private void newGame(Color color) {
        removeAll();
        cleanUpVariables();
        HorizontalLayout mainLayout = new HorizontalLayout(printBoard(color), createRightSidebar());
        mainLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        add(mainLayout);
        placeFigures();
        recalculatePossibleSteps();
    }

    private void cleanUpVariables() {
        selectedFigure = null;
        currentTurn = WHITE;
        cells.clear();
        figures.clear();
        isAnythingEaten = false;
        removeAll();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        UI ui = attachEvent.getUI();
        broadcasterRegistration = Broadcaster.register(e -> {
            if (this.hashCode() != e.getLeft()) {
                Optional.ofNullable(cells.get(e.getRight())).ifPresent(c -> {
                    if (c.hasFigure()) {
                        ui.access(() -> ComponentUtil.fireEvent(c.getFigure(), new ClickEvent<>(attachEvent.getSource())));
                    } else {
                        ui.access(() -> ComponentUtil.fireEvent(c, new ClickEvent<>(attachEvent.getSource())));
                    }
                });
            }
        });
    }

    private void showColorSelector() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Chose Your Color");
        Div black = createTurnIndicator(BLACK);
        black.addClickListener(e -> {
            newGame(BLACK);
            dialog.close();
        });
        Div white = createTurnIndicator(WHITE);
        white.addClickListener(e -> {
            newGame(WHITE);
            dialog.close();
        });
        HorizontalLayout hLayout = new HorizontalLayout(white, black);
        VerticalLayout dialogLayout = new VerticalLayout(hLayout);
        dialogLayout.setAlignItems(Alignment.CENTER);
        dialogLayout.setClassName("general-text");
        dialog.add(dialogLayout);
        add(dialog);
        dialog.open();
    }
}
