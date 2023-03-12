package com.ivpl.games.view;

import com.ivpl.games.constants.Color;
import com.ivpl.games.entity.jpa.Game;
import com.ivpl.games.entity.jpa.Step;
import com.ivpl.games.entity.jpa.User;
import com.ivpl.games.entity.ui.*;
import com.ivpl.games.repository.GameRepository;
import com.ivpl.games.repository.StepRepository;
import com.ivpl.games.security.SecurityService;
import com.ivpl.games.services.GameService;
import com.ivpl.games.services.UIComponentsService;
import com.ivpl.games.services.broadcasting.BroadcasterService;
import com.ivpl.games.utils.CommonUtils;
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
    private final transient StepRepository stepRepository;
    Registration broadcasterRegistration;

    private transient Game game;
    private Color playerColor;
    private User player;
    private Color currentTurn;
    private Div turnIndicator;
    private PieceView selectedPiece;
    private final Map<CellKey, Cell> cells = new LinkedHashMap<>();
    private final List<PieceView> pieces = new ArrayList<>();
    private boolean isAnythingEaten;
    private VerticalLayout board;
    private transient LinkedList<Step> steps;

    public ChessBoard(UIComponentsService uiComponentsService,
                      BroadcasterService broadcasterService,
                      GameRepository gameRepository, GameService gameService, SecurityService securityService, StepRepository stepRepository) {
        this.uiComponentsService = uiComponentsService;
        this.broadcasterService = broadcasterService;
        this.gameRepository = gameRepository;
        this.gameService = gameService;
        this.securityService = securityService;
        this.stepRepository = stepRepository;
    }

    private void initiatePieces() {
        AtomicInteger i = new AtomicInteger();
        pieces.addAll(cells.entrySet().stream()
                .filter(e -> BLACK.equals(e.getValue().getColor()))
                .filter(e -> Range.between(1, 3).contains(e.getKey().getY()) || Range.between(6, 8).contains(e.getKey().getY()))
                .map(e -> addPieces(i.addAndGet(1),e)).collect(Collectors.toList()));
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
        pieces.stream().filter(f -> currentTurn.equals(f.getColor()) && currentTurn.equals(playerColor))
                .forEach(f -> f.calculatePossibleSteps(cells));

        boolean haveToEatAnything = pieces.stream()
                .filter(f -> currentTurn.equals(f.getColor()))
                .anyMatch(f -> !f.getPiecesToBeEaten().isEmpty());

        if (haveToEatAnything)
            pieces.stream().filter(f -> f.getPiecesToBeEaten().isEmpty())
                    .forEach(f -> f.getPossibleSteps().clear());
    }

    private PieceView addPieces(int id, Map.Entry<CellKey, Cell> cellEntry) {
        PieceView f = new CheckerView(id, cellEntry.getKey().getY() < 5 ? BLACK : WHITE, cellEntry.getValue());
        cellEntry.getValue().setPieceView(f);
        addPieceListener(f);
         return f;
    }

    private void addPieceListener(PieceView f) {
        f.setOnClickListener(f.addClickListener(e -> {
            if (f.getPossibleSteps().isEmpty() || !currentTurn.equals(f.getColor())) return;
            f.selectUnselectPiece();
            if (!f.equals(selectedPiece)) {
                if (selectedPiece != null) {
                    selectedPiece.getPossibleSteps().forEach(k -> cells.get(k).removeSelectedStyle());
                    selectedPiece.unselectPiece();
                }

                f.getPossibleSteps().forEach(k -> {
                    Cell cell = cells.get(k);
                    cell.addSelectedStyle();
                    addCellListener(cell);
                });
                selectedPiece = f;
            } else {
                f.getPossibleSteps().forEach(k -> cells.get(k).removeSelectedStyle());
                selectedPiece = null;
            }
        }));
    }

    private void addCellListener(Cell cell) {
        cell.setOnClickListener(cell.addClickListener(event -> {
            Optional.ofNullable(selectedPiece.getPiecesToBeEaten().get(cell.getKey()))
                    .ifPresent(f -> {
                        f.toDie();
                        pieces.remove(f);
                        isAnythingEaten = true;
            });
            gameService.saveStep(game.getId(),
                    playerColor, selectedPiece.getPosition().getKey(),
                    cell.getKey(), selectedPiece.getPieceId());

            selectedPiece.moveTo(cell);
            replaceWithQueenIfNeeded(cell, selectedPiece);

            if (isAnythingEaten) {
                cleanupCells();
                selectedPiece.calculatePossibleSteps(cells);
                // if still have anything to eat
                if (!selectedPiece.getPiecesToBeEaten().isEmpty()) {
                    selectedPiece = null;
                    return;
                }
                isAnythingEaten = false;
            }
            checkIsGameOver();
            currentTurn = CommonUtils.getOppositeColor(currentTurn);
            revertTurn();
            recalculatePossibleSteps();
            selectedPiece = null;

            if (cell.equals(event.getSource())) {
                broadcasterService.getBroadcaster(game.getId()).broadcast(UI.getCurrent().getUIId());
            }
        }));
    }

    private void revertTurn() {
        if (WHITE.equals(currentTurn)) {
            turnIndicator.getStyle().set(BACKGROUND, WHITE_CELL_COLOR);
        } else {
            turnIndicator.getStyle().set(BACKGROUND, BLACK_CELL_COLOR);
        }
    }

    private Div addTurnIndicator() {
        turnIndicator = UIComponentsService.getTurnIndicator(currentTurn);
        return turnIndicator;
    }

    private void checkIsGameOver() {
        Map<Color, List<PieceView>> groupsByColor = pieces.stream().collect(Collectors.groupingBy(PieceView::getColor, Collectors.toList()));
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

    private boolean isBorderCell(CellKey cellKey, Color pieceColor) {
        return WHITE.equals(pieceColor) ? cellKey.getY() == 1 : cellKey.getY() == 8;
    }

    private void replaceWithQueenIfNeeded(Cell cell, PieceView pieceView) {
        if (isBorderCell(cell.getKey(), pieceView.getColor())) {
            QueenView queenView = new QueenView(pieceView.getPieceId(), pieceView.getColor(), cell);
            addPieceListener(queenView);
            cell.remove(selectedPiece);
            cell.setPieceView(queenView);
            pieces.remove(selectedPiece);
            pieces.add(queenView);
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
        add(uiComponentsService.getHeader());
        HorizontalLayout mainLayout = new HorizontalLayout(printBoard(playerColor), createRightSidebar());
        mainLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        add(mainLayout);
        //initiatePieces();
    }

    private void newGame() {
        cleanUpAll();
        drawNewGame();
        gameRepository.saveAndFlush(game);
        recalculatePossibleSteps();
    }

    private void cleanUpAll() {
        selectedPiece = null;
        currentTurn = WHITE;
        cells.clear();
        pieces.clear();
        isAnythingEaten = false;
        steps = null;
        removeAll();
    }

    private void placePieces() {

/*        pieces.stream().map(
                f -> Pair.of(f, steps.stream()
                        .filter(s -> s.getPieceId().equals(f.getPieceId()))
                        .max(Comparator.comparing(Step::getGameStepId))))
                .filter(p -> p.getRight().isPresent())
                .forEach(p -> p.getLeft().moveTo(cells.get(new CellKey(p.getRight().get().getStepTo()))));
        recalculatePossibleSteps();*/
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        if (game == null) return;
        UI ui = attachEvent.getUI();
        broadcasterRegistration = broadcasterService.registerBroadcasterListener(game.getId(), e -> {
            if (this.hashCode() != e)
                ui.access(this::refreshBoard);
        });
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        Optional.ofNullable(broadcasterRegistration).ifPresent(Registration::remove);
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

/*    @SneakyThrows
    @Override
    public void setParameter(BeforeEvent event, String gameId) {
        Optional<Game> g = gameRepository.findById(Long.valueOf(gameId));
        if (g.isPresent()) {
            game = g.get();
            steps = stepRepository.findAllByGameIdOrderByGameStepId(game.getId());
            recognizeUser();
            if (!steps.isEmpty()) {
                currentTurn = CommonUtils.getOppositeColor(steps.getLast().getPlayerColor());
                drawNewGame();
                replacePieces();
            } else
                newGame();
        } else {
            uiComponentsService.showGameNotFoundMessage();
        }
    }*/


    @Override
    public void setParameter(BeforeEvent event, String gameId) {
        restoreGame(gameId);
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

    private void refreshBoard() {
        Optional<Game> g = gameRepository.findById(game.getId());
        if (g.isPresent()) {
            game = g.get();
            steps = stepRepository.findAllByGameIdOrderByGameStepId(game.getId());
            currentTurn = CommonUtils.getOppositeColor(steps.getLast().getPlayerColor());
            placePieces();
            revertTurn();
        }
    }

    private void restoreGame(String gameId) {
        gameRepository.findById(Long.valueOf(gameId))
                .ifPresentOrElse(g -> {
                    try {
                        game = g;
                        steps = stepRepository.findAllByGameIdOrderByGameStepId(game.getId());
                        recognizeUser();
                        currentTurn = steps.isEmpty() ? WHITE : CommonUtils.getOppositeColor(steps.getLast().getPlayerColor());
                        drawNewGame();
                        placePieces();
                    } catch (AuthenticationException e) {
                        e.printStackTrace();
                    }
                }, uiComponentsService::showGameNotFoundMessage);
    }

    private void reloadPieces() {

    }
}
