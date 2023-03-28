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
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.shared.Registration;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.tomcat.websocket.AuthenticationException;

import java.util.*;

import static com.ivpl.games.constants.Color.BLACK;
import static com.ivpl.games.constants.Color.WHITE;
import static com.ivpl.games.constants.Constants.*;

@CssImport("./styles/styles.css")

public abstract class AbstractBoardView extends VerticalLayout implements HasUrlParameter<String> {

    private final transient UIComponentsService uiComponentsService;
    private final transient BroadcasterService broadcasterService;
    private final transient GameRepository gameRepository;
    protected final transient GameService gameService;
    private final transient SecurityService securityService;
    private final transient StepRepository stepRepository;
    private final transient BoardService boardService;
    Registration broadcasterRegistration;

    protected transient Game game;
    private Color playerColor;
    @Getter
    protected Color currentTurn;
    @Getter
    private AbstractPieceView selectedPiece;
    private final Map<CellKey, Cell> cells = new LinkedHashMap<>();
    protected final transient List<AbstractPieceView> pieces = new ArrayList<>();
    private boolean isAnythingEaten;
    private VerticalLayout board;
    private transient LinkedList<Step> steps;

    protected AbstractBoardView(UIComponentsService uiComponentsService,
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
        broadcasterRegistration = broadcasterService.registerBroadcasterListener(game.getId(), e ->
                ui.access(() -> {
                    cleanupVariables();
                    reloadGameFromRepository(game.getId());
                    restoreGame();
            }));
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        Optional.ofNullable(broadcasterRegistration).ifPresent(Registration::remove);
    }

    @SneakyThrows
    @Override
    public void setParameter(BeforeEvent event, String gameId) {
        reloadGameFromRepository(Long.valueOf(gameId));
        recognizeUser();
        restoreGame();
    }

    private void restoreGame() {
        steps = stepRepository.findAllByGameIdOrderByGameStepId(game.getId());
        currentTurn = game.getTurn();
        drawNewBoard();
        if (BLACK.equals(playerColor))
            reverseBoard();
        pieces.forEach(this::addPieceListener);
    }

    private void drawNewBoard() {
        removeAll();
        ChessBoardContainer boardContainer = boardService.reloadBoard(game.getId(), playerColor);
        board = boardContainer.getBoardLayout();
        pieces.addAll(boardContainer.getPieces());
        cells.putAll(boardContainer.getCells());

        add(uiComponentsService.getHeaderWithGoToLobby());
        HorizontalLayout mainLayout = new HorizontalLayout(board, createRightSidebar());
        mainLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        add(mainLayout);
    }

    protected abstract void checkIsGameOver();

    protected void gameOver() {
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
        HorizontalLayout indicatorLayout = new HorizontalLayout(inverseBtn, uiComponentsService.getTurnIndicator(currentTurn));
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
                selectedPiece.calculatePossibleSteps(cells, true);
                // if still have anything to eat
                if (!selectedPiece.getPiecesToBeEaten().isEmpty()) {
                    gameService.saveStep(game.getId(),
                            playerColor, cell.getKey(),
                            selectedPiece.getPieceId(), selectedPiece.getDbId(), false);
                    broadcasterService.getBroadcaster(game.getId()).broadcast(this.hashCode());
                    return;
                }
                isAnythingEaten = false;
            }
            gameService.saveStep(game.getId(),
                    playerColor, cell.getKey(),
                    selectedPiece.getPieceId(), selectedPiece.getDbId(), true);
            checkIsGameOver();
            broadcasterService.getBroadcaster(game.getId()).broadcast(this.hashCode());
        }));
    }

    private boolean gameTypeIsCheckers() {
        return GameType.CHECKERS.equals(game.getType());
    }

    private void cleanupVariables() {
        selectedPiece = null;
        pieces.clear();
        cells.clear();
    }

    private void reloadGameFromRepository(Long gameId) {
        gameRepository.findById(gameId).ifPresentOrElse(g -> game = g, uiComponentsService::showGameNotFoundMessage);
    }
}
