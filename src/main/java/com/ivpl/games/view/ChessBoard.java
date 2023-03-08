package com.ivpl.games.view;

import com.ivpl.games.constants.Color;
import com.ivpl.games.entity.*;
import com.ivpl.games.entity.jpa.Game;
import com.ivpl.games.entity.jpa.User;
import com.ivpl.games.repository.GameRepository;
import com.ivpl.games.security.SecurityService;
import com.ivpl.games.services.GameService;
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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.shared.Registration;
import lombok.SneakyThrows;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tomcat.websocket.AuthenticationException;

import javax.annotation.security.PermitAll;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.ivpl.games.constants.Color.BLACK;
import static com.ivpl.games.constants.Color.WHITE;
import static com.ivpl.games.constants.Constants.*;

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
    private final transient GameService gameService;
    private final transient SecurityService securityService;
    Registration broadcasterRegistration;

    private transient Game game;
    private Color playerColor;
    private User player;
    private Color currentTurn = WHITE;
    private Div turnIndicator;
    private Figure selectedFigure;
    private final Map<CellKey, Cell> cells = new LinkedHashMap<>();
    private final List<Figure> figures = new ArrayList<>();
    private boolean isAnythingEaten;
    private VerticalLayout board;

    public ChessBoard(UIComponentsService uiComponentsService,
                      BroadcasterService broadcasterService,
                      GameRepository gameRepository, GameService gameService, SecurityService securityService) {
        this.uiComponentsService = uiComponentsService;
        this.broadcasterService = broadcasterService;
        this.gameRepository = gameRepository;
        this.gameService = gameService;
        this.securityService = securityService;
    }

    private void placeFigures() {
        AtomicInteger i = new AtomicInteger();
        figures.addAll(cells.entrySet().stream()
                .filter(e -> BLACK.equals(e.getValue().getColor()))
                .filter(e -> Range.between(1, 3).contains(e.getKey().getY()) || Range.between(6, 8).contains(e.getKey().getY()))
                .map(e -> addFigure(i.addAndGet(1),e)).collect(Collectors.toList()));
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

    private Figure addFigure(int id, Map.Entry<CellKey, Cell> cellEntry) {
        Figure f = new Checker(id, cellEntry.getKey().getY() < 5 ? BLACK : WHITE, cellEntry.getValue());
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
                    selectedFigure.unselectFigure();
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
            if (cell.equals(event.getSource())) {
                broadcasterService.getBroadcaster(game.getId()).broadcast(Pair.of(UI.getCurrent().getUIId(), cell.getKey()));
                gameService.saveStep(game.getId(),
                        playerColor, selectedFigure.getPosition().getKey(), cell.getKey(), selectedFigure.getFigureId());
            }
            selectedFigure.doStepTo(cell);
            Optional.ofNullable(selectedFigure.getFiguresToBeEaten().get(cell.getKey()))
                    .ifPresent(f -> {
                        f.toDie();
                        figures.remove(f);
                        isAnythingEaten = true;
            });

            replaceWithQueenIfNeeded(cell, selectedFigure);

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

    private void replaceWithQueenIfNeeded(Cell cell, Figure figure) {
        if (isBorderCell(cell.getKey(), figure.getColor())) {
            Queen queen = new Queen(figure.getFigureId(), figure.getColor(), cell);
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
        return new Button(NEW_GAME_STR, e -> uiComponentsService.showNewGameDialog());
    }

    private void drawNewGame() {
        removeAll();
        cleanUpVariables();
        add(uiComponentsService.getHeader());
        HorizontalLayout mainLayout = new HorizontalLayout(printBoard(playerColor), createRightSidebar());
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

    private void restoreGame(User user) {
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        if (game == null) return;
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
        Optional<Game> g = gameRepository.findById(Long.valueOf(gameId));
        if (g.isPresent()) {
            game = g.get();
            recognizeUser();
            drawNewGame();
        } else {
            UI.getCurrent().navigate(MainPage.class);
        }
    }

    private void recognizeUser() throws AuthenticationException {
        User user = securityService.getAuthenticatedUser();
        if (user.getId().equals(game.getPlayer1Id())) {
            player = user;
            playerColor = game.getColorPlayer1();
        } else if (user.getId().equals(game.getPlayer2Id())) {
            player = user;
            playerColor = game.getColorPlayer2();
        }
    }
}
