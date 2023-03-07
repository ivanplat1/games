package com.ivpl.games.view;

import com.ivpl.games.entity.jpa.Game;
import com.ivpl.games.entity.jpa.User;
import com.ivpl.games.repository.GameRepository;
import com.ivpl.games.repository.UserRepository;
import com.ivpl.games.security.SecurityService;
import com.ivpl.games.services.GameService;
import com.ivpl.games.services.UIComponentsService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.router.Route;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.websocket.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

import static com.ivpl.games.constants.Constants.*;
import static com.ivpl.games.constants.GameStatus.*;

@Route("")
public class MainPage extends VerticalLayout {

    private final transient GameRepository gameRepository;
    private final transient UserRepository userRepository;
    private final transient GameService gameService;
    private final transient SecurityService securityService;
    private final transient UIComponentsService uiComponentsService;
    private static final Logger log = LoggerFactory.getLogger(MainPage.class);

    public MainPage(GameRepository gameRepository,
                    UserRepository userRepository,
                    GameService gameService,
                    UIComponentsService uiComponentsService,
                    SecurityService securityService) {
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.gameService = gameService;
        this.securityService = securityService;
        this.uiComponentsService = uiComponentsService;
        add(uiComponentsService.getHeader());
        add(getLobbyScreen());
    }

    private VerticalLayout getLobbyScreen() {
        VerticalLayout vl = new VerticalLayout();
        vl.add(getActiveGamesGrid(), getMenu());
        vl.setPadding(false);
        vl.setSpacing(false);
        return vl;
    }

    private VerticalLayout getMenu() {
        Button newGameBtn = new Button(NEW_GAME_STR, e -> {
            try {
                gameService.newGame(getCurrentUser());
            } catch (AuthenticationException ex) {
                log.error(AUTHORIZATION_ERROR_EXCEPTION_MESSAGE, ex);
            }
        });
        newGameBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        newGameBtn.setClassName("button");
        VerticalLayout vl = new VerticalLayout(newGameBtn);
        vl.setAlignItems(Alignment.END);
        return vl;
    }

    private VerticalLayout getActiveGamesGrid() {
        Grid<Game> activeGames = new Grid<>(Game.class, false);
        activeGames.setClassName("active-games");
        activeGames.addColumn(createIdRenderer())
                .setHeader(ACTIVE_GAMES_HEADER_ID_STR)
                .setSortable(true).setWidth("10px")
                .setComparator(Comparator.comparing(Game::getId));
        activeGames.addColumn(createUsersRenderer())
                .setHeader(ACTIVE_GAMES_HEADER_PLAYERS_STR);
        activeGames.addColumn(createStatusComponentRenderer())
                .setHeader(ACTIVE_GAMES_HEADER_GAME_STATUS_STR)
                .setSortable(true)
                .setComparator(Comparator.comparing(Game::getStatus));
        activeGames.addColumn(createActionComponentRenderer());
        refreshActiveGamesGrid(activeGames);
        Label label = new Label(JOIN_GAME_OR_START_NEW_STR);
        label.setClassName("label-text");
        Button refreshBtn = new Button(new Icon(VaadinIcon.REFRESH), e -> refreshActiveGamesGrid(activeGames));
        HorizontalLayout hl = new HorizontalLayout(label, refreshBtn);
        hl.setAlignItems(Alignment.END);
        return new VerticalLayout(hl, activeGames);
    }

    private void refreshActiveGamesGrid(Grid<Game> grid) {
        grid.setItems(gameRepository.findAllByStatusIn(Set.of(SELECTING_COLOR, IN_PROGRESS, WAITING_FOR_OPPONENT)));
    }

    private static ComponentRenderer<Span, Game> createStatusComponentRenderer() {
        return new ComponentRenderer<>(Span::new, statusComponentUpdater);
    }

    private static final SerializableBiConsumer<Span, Game> statusComponentUpdater = (
            span, game) -> {
        if (IN_PROGRESS.equals(game.getStatus())) {
            span.setClassName("badge-orange");
            span.setText(IN_PROGRESS.getLabel());
        } else if (WAITING_FOR_OPPONENT.equals(game.getStatus())) {
            span.setClassName("badge-green");
            span.setText(WAITING_FOR_OPPONENT.getLabel());
        } else if (SELECTING_COLOR.equals(game.getStatus())) {
            span.setClassName("badge-rose");
            span.setText(SELECTING_COLOR.getLabel());
        }
    };

    private User getCurrentUser() throws AuthenticationException {
        return securityService.getAuthenticatedUser();
    }

    private Renderer<Game> createUsersRenderer() {
        return LitRenderer.<Game> of(PLAYERS_CELL_HTML)
                .withProperty("user", game -> Optional.ofNullable(game.getPlayer1Id())
                        .map(userRepository::findById).map(user -> user.map(User::getUsername).orElse(StringUtils.EMPTY)).orElse(StringUtils.EMPTY))
                .withProperty("nick", game -> Optional.ofNullable(game.getPlayer1Id())
                        .map(userRepository::findById).map(user -> user.map(User::getNick).orElse(StringUtils.EMPTY)).orElse(StringUtils.EMPTY))
                .withProperty("secondUser", game -> Optional.ofNullable(game.getPlayer2Id())
                .map(userRepository::findById).map(user -> user.map(User::getUsername).orElse(StringUtils.EMPTY)).orElse(StringUtils.EMPTY))
                .withProperty("nick2", game -> Optional.ofNullable(game.getPlayer2Id())
                .map(userRepository::findById).map(user -> user.map(User::getNick).orElse(StringUtils.EMPTY)).orElse(StringUtils.EMPTY));
    }

    private static Renderer<Game> createIdRenderer() {
        return LitRenderer.<Game> of(GAME_ID_CELL_HTML)
                .withProperty("gameId", Game::getId)
                .withProperty("startDate", game -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(game.getStartDate()));
    }

    private final SerializableBiConsumer<Button, Game> actionComponentUpdater = (
            button, game) -> {
        if (IN_PROGRESS.equals(game.getStatus())) {
            button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            button.setIcon(new Icon(VaadinIcon.EYE));
            button.setTooltipText("Spectate");
        } else if (WAITING_FOR_OPPONENT.equals(game.getStatus())) {
            button.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            button.setIcon(new Icon(VaadinIcon.SWORD));
            button.setTooltipText("Fight");
            button.addClickListener(e -> {
                try {
                    getGameService().joinGame(game, getCurrentUser());
                } catch (AuthenticationException ex) {
                    log.error(AUTHORIZATION_ERROR_EXCEPTION_MESSAGE, ex);
                }
            });
        } else {
            button.setVisible(false);
        }
    };

    private ComponentRenderer<Button, Game> createActionComponentRenderer() {
        return new ComponentRenderer<>(Button::new, actionComponentUpdater);
    }

    private GameService getGameService() {
        return gameService;
    }
}
