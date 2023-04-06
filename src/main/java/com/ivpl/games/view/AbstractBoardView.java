package com.ivpl.games.view;

import com.ivpl.games.constants.*;
import com.ivpl.games.entity.BoardContainer;
import com.ivpl.games.entity.jpa.Game;
import com.ivpl.games.entity.jpa.Step;
import com.ivpl.games.entity.jpa.User;
import com.ivpl.games.entity.ui.*;
import com.ivpl.games.repository.GameRepository;
import com.ivpl.games.repository.StepRepository;
import com.ivpl.games.security.SecurityService;
import com.ivpl.games.services.BoardService;
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
import java.util.concurrent.atomic.AtomicReference;

import static com.ivpl.games.constants.Color.BLACK;
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
    @Getter
    private Color playerColor;
    @Getter
    protected Color currentTurn;
    @Getter
    private final AtomicReference<AbstractPieceView> selectedPiece = new AtomicReference<>();
    protected final transient List<AbstractPieceView> pieces = new ArrayList<>();
    private VerticalLayout board;
    protected transient LinkedList<Step> steps;

    protected AbstractBoardView(UIComponentsService uiComponentsService,
                                BroadcasterService broadcasterService,
                                GameRepository gameRepository,
                                GameService gameService,
                                SecurityService securityService,
                                StepRepository stepRepository,
                                BoardService boardService) {
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
    }

    protected void drawNewBoard() {
        removeAll();
        BoardContainer boardContainer = boardService.reloadBoard(game.getId(), this);
        board = boardContainer.getBoardLayout();
        pieces.addAll(boardContainer.getPieces());

        add(uiComponentsService.getHeaderWithGoToLobby());
        HorizontalLayout mainLayout = new HorizontalLayout(board, createRightSidebar());
        mainLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        add(mainLayout);
        Optional.ofNullable(game.getWinner()).ifPresent(this::gameOver);
    }

    protected void gameOver(Color winner) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(GAME_OVER_STR);
        VerticalLayout dialogLayout = new VerticalLayout(new Label(winner.toString() + " wins!"));
        dialogLayout.setAlignItems(Alignment.CENTER);
        Button okButton = uiComponentsService.getGoToLobbyButtonForDialog(dialog);
        dialog.getFooter().add(okButton);
        dialog.add(dialogLayout);
        add(dialog);
        dialog.open();
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

    private void cleanupVariables() {
        selectedPiece.set(null);
        pieces.clear();
    }

    private void reloadGameFromRepository(Long gameId) {
        gameRepository.findById(gameId).ifPresentOrElse(g -> game = g, uiComponentsService::showGameNotFoundMessage);
    }
}
