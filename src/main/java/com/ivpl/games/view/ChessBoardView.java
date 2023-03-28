package com.ivpl.games.view;

import com.ivpl.games.constants.*;
import com.ivpl.games.entity.ChessBoardContainer;
import com.ivpl.games.entity.jpa.Game;
import com.ivpl.games.entity.jpa.Step;
import com.ivpl.games.entity.jpa.User;
import com.ivpl.games.entity.ui.*;
import com.ivpl.games.entity.ui.checkers.CheckerQueenView;
import com.ivpl.games.repository.GameRepository;
import com.ivpl.games.repository.StepRepository;
import com.ivpl.games.security.SecurityService;
import com.ivpl.games.services.BoardService;
import com.ivpl.games.services.BoardServiceImpl;
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
import lombok.Getter;
import org.apache.tomcat.websocket.AuthenticationException;

import javax.annotation.security.PermitAll;
import java.util.*;
import java.util.stream.Collectors;

import static com.ivpl.games.constants.Color.BLACK;
import static com.ivpl.games.constants.Color.WHITE;
import static com.ivpl.games.constants.Constants.*;
import static com.ivpl.games.constants.Styles.BLACK_CELL_COLOR;
import static com.ivpl.games.constants.Styles.WHITE_CELL_COLOR;

@CssImport("./styles/styles.css")

@Route("checkers")
@PermitAll
public class ChessBoardView extends VerticalLayout implements HasUrlParameter<String> {

    private final transient UIComponentsService uiComponentsService;
    private final transient BroadcasterService broadcasterService;
    private final transient GameRepository gameRepository;
    private final transient GameService gameService;
    private final transient SecurityService securityService;
    private final transient StepRepository stepRepository;
    private final transient BoardService boardService;
    Registration broadcasterRegistration;

    private transient Game game;
    private Color playerColor;
    @Getter
    private Color currentTurn;
    private Div turnIndicator;
    @Getter
    private AbstractPieceView selectedPiece;
    private final Map<CellKey, Cell> cells = new LinkedHashMap<>();
    private final List<AbstractPieceView> pieces = new ArrayList<>();
    private boolean isAnythingEaten;
    private VerticalLayout board;
    private transient LinkedList<Step> steps;

    public ChessBoardView(UIComponentsService uiComponentsService,
                          BroadcasterService broadcasterService,
                          GameRepository gameRepository,
                          GameService gameService,
                          SecurityService securityService,
                          StepRepository stepRepository,
                          BoardServiceImpl boardService) {
        this.uiComponentsService = uiComponentsService;
        this.broadcasterService = broadcasterService;
        this.gameRepository = gameRepository;
        this.gameService = gameService;
        this.securityService = securityService;
        this.stepRepository = stepRepository;
        this.boardService = boardService;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        if (game == null) return;
        UI ui = attachEvent.getUI();
        broadcasterRegistration = broadcasterService.registerBroadcasterListener(game.getId(), e -> {
            if (this.hashCode() != e)
                ui.access(() -> restoreGame(game.getId()));
        });
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        Optional.ofNullable(broadcasterRegistration).ifPresent(Registration::remove);
    }

    @Override
    public void setParameter(BeforeEvent event, String gameId) {
        restoreGame(Long.valueOf(gameId));
    }

    private void restoreGame(Long gameId) {
        gameRepository.findById(gameId)
                .filter(Game::isActive)
                .ifPresentOrElse(g -> {
                    try {
                        game = g;
                        steps = stepRepository.findAllByGameIdOrderByGameStepId(game.getId());
                        recognizeUser();
                        currentTurn = game.getTurn();
                        pieces.clear();
                        cells.clear();
                        drawNewBoard();
                        if (BLACK.equals(playerColor))
                            reverseBoard();
                    } catch (AuthenticationException e) {
                        e.printStackTrace();
                    }
                }, uiComponentsService::showGameNotFoundMessage);
        pieces.forEach(this::addPieceListener);
    }

    private void drawNewBoard() {
        ChessBoardContainer boardContainer = boardService.reloadBoardFromDB(game.getId(), playerColor);
        board = boardContainer.getBoardLayout();
        pieces.addAll(boardContainer.getPieces());
        cells.putAll(boardContainer.getCells());

        add(uiComponentsService.getHeaderWithGoToLobby());
        HorizontalLayout mainLayout = new HorizontalLayout(board, createRightSidebar());
        mainLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        add(mainLayout);
    }

    private void checkIsGameOver() {
        Map<Color, List<AbstractPieceView>> groupsByColor = pieces.stream()
                .filter(p -> p.getPosition() != null)
                .collect(Collectors.groupingBy(AbstractPieceView::getColor, Collectors.toList()));
        if (!groupsByColor.containsKey(WHITE) || !groupsByColor.containsKey(BLACK)) {
            gameService.finishGame(game, currentTurn);
            gameOver();
        }
    }

    private void gameOver() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(GAME_OVER_STR);
        VerticalLayout dialogLayout = new VerticalLayout(new Label(currentTurn.toString() + " wins!"));
        dialogLayout.setAlignItems(Alignment.CENTER);
        Button okButton = uiComponentsService.getGoToLobbyButtonForDialog(dialog);
        dialog.getFooter().add(okButton);
        dialog.add(dialogLayout);
        add(dialog);
        dialog.open();
    }

    private void replaceWithQueenIfNeeded(Cell cell, AbstractPieceView piece) {
        if (gameTypeIsCheckers() && isBorderCell(cell.getKey(), piece.getColor())) {
            CheckerQueenView checkerQueenView = new CheckerQueenView(piece.getPieceId(), piece.getDbId(), piece.getColor(), PieceType.CHECKER_QUEEN, cell);
            addPieceListener(checkerQueenView);
            cell.remove(selectedPiece);
            cell.setPiece(checkerQueenView);
            selectedPiece = checkerQueenView;
            gameService.mutatePiece(piece.getDbId(), PieceType.CHECKER_QUEEN);
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

    private void reverseBoard() {
        if (Styles.CHESS_BOARD_WHITES_STYLE.equals(board.getClassName())) {
            board.setClassName(Styles.CHESS_BOARD_BLACKS_STYLE);
            board.getChildren().forEach(c -> ((HorizontalLayout) c).setClassName(Styles.BOARD_LINE_BLACKS_STYLE));
        } else {
            board.setClassName(Styles.CHESS_BOARD_WHITES_STYLE);
            board.getChildren().forEach(c -> ((HorizontalLayout) c).setClassName(Styles.BOARD_LINE_WHITES_STYLE));
        }
    }

    private void recognizeUser() throws AuthenticationException {
        User user = securityService.getAuthenticatedUser();
        if (user.getId().equals(game.getPlayer1Id())) {
            playerColor = game.getColorPlayer1();
        } else if (user.getId().equals(game.getPlayer2Id())) {
            playerColor = game.getColorPlayer2();
        }
    }

    private boolean isBorderCell(CellKey cellKey, Color pieceColor) {
        return WHITE.equals(pieceColor) ? cellKey.getY() == 1 : cellKey.getY() == 8;
    }

    private void removeCellsHighlights() {
        cells.values().forEach(Cell::removeSelectedStyle);
    }

    private void revertTurn() {
        if (WHITE.equals(currentTurn)) {
            turnIndicator.getStyle().set(Styles.BACKGROUND, WHITE_CELL_COLOR);
        } else {
            turnIndicator.getStyle().set(Styles.BACKGROUND, BLACK_CELL_COLOR);
        }
    }

    private Div addTurnIndicator() {
        turnIndicator = UIComponentsService.getTurnIndicator(currentTurn);
        return turnIndicator;
    }

    private void addPieceListener(AbstractPieceView p) {
        p.setOnClickListener(p.addClickListener(e -> {
            if (p.getPossibleSteps().isEmpty() || !currentTurn.equals(p.getColor())) return;
            p.selectUnselectPiece();
            if (!p.equals(selectedPiece)) {
                if (selectedPiece != null) {
                    selectedPiece.getPossibleSteps().forEach(k -> cells.get(k).removeSelectedStyle());
                    selectedPiece.unselectPiece();
                }

                p.getPossibleSteps().forEach(k -> {
                    Cell cell = cells.get(k);
                    cell.addSelectedStyle();
                    addCellListener(cell);
                });
                selectedPiece = p;
            } else {
                p.getPossibleSteps().forEach(k -> cells.get(k).removeSelectedStyle());
                selectedPiece = null;
            }
        }));
    }

    private void addCellListener(Cell cell) {
        cell.setOnClickListener(cell.addClickListener(event -> {
            Optional.ofNullable(selectedPiece.getPiecesToBeEaten().get(cell.getKey()))
                    .ifPresent(f -> {
                        f.toDie();
                        gameService.killPiece(f.getDbId());
                        isAnythingEaten = true;
                    });
            selectedPiece.placeAt(cell);
            replaceWithQueenIfNeeded(cell, selectedPiece);

            if (gameTypeIsCheckers() && isAnythingEaten) {
                removeCellsHighlights();
                selectedPiece.calculatePossibleSteps(cells);
                // if still have anything to eat
                if (!selectedPiece.getPiecesToBeEaten().isEmpty()) {
                    gameService.saveStep(game.getId(),
                            playerColor, cell.getKey(),
                            selectedPiece.getPieceId(), selectedPiece.getDbId(), false);
                    selectedPiece = null;
                    if (cell.equals(event.getSource())) {
                        broadcasterService.getBroadcaster(game.getId()).broadcast(this.hashCode());
                    }
                    return;
                }
                isAnythingEaten = false;
            }
            gameService.saveStep(game.getId(),
                    playerColor, cell.getKey(),
                    selectedPiece.getPieceId(), selectedPiece.getDbId(), true);
            checkIsGameOver();
            currentTurn = CommonUtils.getOppositeColor(currentTurn);
            revertTurn();
            //recalculatePossibleSteps();
            selectedPiece = null;

            if (cell.equals(event.getSource())) {
                broadcasterService.getBroadcaster(game.getId()).broadcast(this.hashCode());
            }
        }));
    }

    private boolean gameTypeIsCheckers() {
        return GameType.CHECKERS.equals(game.getType());
    }
}
