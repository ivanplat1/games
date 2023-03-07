package com.ivpl.games.view;

import com.ivpl.games.constants.Color;
import com.ivpl.games.entity.*;
import com.ivpl.games.entity.jpa.Game;
import com.ivpl.games.repository.GameRepository;
import com.ivpl.games.security.SecurityService;
import com.ivpl.games.services.UIComponentsService;
import com.ivpl.games.services.broadcasting.BroadcasterService;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.shared.Registration;
import lombok.SneakyThrows;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.security.PermitAll;
import java.util.*;
import java.util.stream.Collectors;

import static com.ivpl.games.constants.Color.BLACK;
import static com.ivpl.games.constants.Color.WHITE;
import static com.ivpl.games.constants.Constants.*;
import static com.ivpl.games.constants.GameStatus.WAITING_FOR_OPPONENT;

@CssImport("./styles/styles.css")

@Route("checkers")
@PermitAll
public class ChessBoard extends VerticalLayout implements HasUrlParameter<String> {

    private static final String CHESS_BOARD_WHITES_STYLE = "chess-board-whites";
    private static final String CHESS_BOARD_BLACKS_STYLE = "chess-board-blacks";
    private static final String BOARD_LINE_WHITES_STYLE = "board-line-whites";
    private static final String BOARD_LINE_BLACKS_STYLE = "board-line-blacks";

    private final transient UIComponentsService uiComponentsService;
    private final transient BroadcasterService broadcasterService;
    private final transient GameRepository gameRepository;
    private final transient SecurityService securityService;
    Registration broadcasterRegistration;

    private transient Game game;
    private Color playerColor;
    private Long playerId;
    private Color currentTurn = WHITE;
    private Div turnIndicator;
    private Figure selectedFigure;
    private final Map<CellKey, Cell> cells = new LinkedHashMap<>();
    private final List<Figure> figures = new ArrayList<>();
    private boolean isAnythingEaten;
    private VerticalLayout board;

    public ChessBoard(UIComponentsService uiComponentsService,
                      BroadcasterService broadcasterService,
                      GameRepository gameRepository, SecurityService securityService) {
        this.uiComponentsService = uiComponentsService;
        this.broadcasterService = broadcasterService;
        this.gameRepository = gameRepository;
        this.securityService = securityService;
    }

    private void placeFigures() {
        figures.addAll(cells.entrySet().stream()
                .filter(e -> BLACK.equals(e.getValue().getColor()))
                .filter(e -> Range.between(1, 3).contains(e.getKey().getY()) || Range.between(6, 8).contains(e.getKey().getY()))
                .map(this::addFigure).collect(Collectors.toList()));
    }

    private VerticalLayout printBoard(Color color) {
        board = new VerticalLayout();
        board.setSpacing(false);
        board.addClassName(CHESS_BOARD_WHITES_STYLE);
        board.setPadding(false);
        HorizontalLayout line;

        for (int y = 1; y < 9; ++y) {
            line = new HorizontalLayout();
            line.setSpacing(false);

            for (int x = 1; x < 9; ++x) {
                Cell cell = new Cell(x, y, (x+y) % 2 == 0 ? WHITE : BLACK);
                line.add(cell);
                cells.put(cell.getKey(), cell);
            }
            board.add(line);
        }

        if (BLACK.equals(color))
            reverseBoard();
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
                broadcasterService.getBroadcaster(game.getId()).broadcast(Pair.of(this.hashCode(), f.getPosition().getKey()));
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
                broadcasterService.getBroadcaster(game.getId()).broadcast(Pair.of(UI.getCurrent().getUIId(), cell.getKey()));
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
        turnIndicator = UIComponentsService.getTurnIndicator(WHITE);
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
        Button inverseBtn = new Button(new Icon(VaadinIcon.REFRESH), e -> reverseBoard());
        HorizontalLayout indicatorLayout = new HorizontalLayout(inverseBtn, addTurnIndicator());
        indicatorLayout.setAlignItems(Alignment.CENTER);
        HorizontalLayout menuLayout = new HorizontalLayout();
        menuLayout.setPadding(false);
        menuLayout.setSpacing(false);
        return new VerticalLayout(indicatorLayout, menuLayout);
    }

    private Button createNewGameButton() {
        return new Button(NEW_GAME_STR, e -> showSelectColorDialogForNewGame());
    }

    private void newGame(Color color) {
        removeAll();
        cleanUpVariables();
        add(uiComponentsService.getHeader());
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
        broadcasterRegistration = broadcasterService.registerBroadcasterListener(game.getId(), e -> {
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

    private void showSelectColorDialogForNewGame() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(COOSE_YOUR_COLOR_STR);
        Div black = UIComponentsService.getTurnIndicator(BLACK);
        black.addClickListener(e -> {
            newGame(BLACK);
            game.setColorPlayer1(BLACK);
            game.setStatus(WAITING_FOR_OPPONENT);
            gameRepository.saveAndFlush(game);
            dialog.close();
        });
        Div white = UIComponentsService.getTurnIndicator(WHITE);
        white.addClickListener(e -> {
            newGame(WHITE);
            game.setColorPlayer1(WHITE);
            game.setStatus(WAITING_FOR_OPPONENT);
            gameRepository.saveAndFlush(game);
            dialog.close();
        });
        HorizontalLayout hLayout = new HorizontalLayout(white, black);
        VerticalLayout dialogLayout = new VerticalLayout(hLayout);
        dialogLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        dialogLayout.setClassName("general-text");
        dialog.add(dialogLayout);
        add(dialog);
        dialog.open();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        broadcasterRegistration.remove();
    }

    private void reverseBoard() {
        if (CHESS_BOARD_WHITES_STYLE.equals(board.getClassName())) {
            board.setClassName(CHESS_BOARD_BLACKS_STYLE);
            board.getChildren().forEach(c -> ((HorizontalLayout) c).setClassName(BOARD_LINE_BLACKS_STYLE));
        } else {
            board.setClassName(CHESS_BOARD_WHITES_STYLE);
            board.getChildren().forEach(c -> ((HorizontalLayout) c).setClassName(BOARD_LINE_WHITES_STYLE));
        }
    }

    @SneakyThrows
    @Override
    public void setParameter(BeforeEvent event, String gameId) {
        game = gameRepository.findById(Long.valueOf(gameId))
                .orElseThrow(() ->  new NotFoundException(String.format("Game with Id %s is was not found", gameId)));
        if (game.getColorPlayer1() == null) {
            showSelectColorDialogForNewGame();
        } else if (game.getPlayer2Id() != null) {
            newGame(game.getColorPlayer2());
        }
    }
}
