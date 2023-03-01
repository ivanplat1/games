package com.ivpl.games.view;

import com.ivpl.games.constants.GameType;
import com.ivpl.games.entity.Game;
import com.ivpl.games.entity.User;
import com.ivpl.games.repository.GameRepository;
import com.ivpl.games.repository.UserRepository;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.router.Route;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ivpl.games.constants.GameStatus.IN_PROGRESS;
import static com.ivpl.games.constants.GameStatus.WAITING_FOR_OPPONENT;

@Route("")
public class MainPage extends VerticalLayout {

    private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final transient GameRepository gameRepository;
    private final transient UserRepository userRepository;
    private final Dialog dialog;
    private final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    public MainPage(GameRepository gameRepository, UserRepository userRepository) {
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;

        add(getLobbyScreen());

        dialog = new Dialog();
        VerticalLayout vl = new VerticalLayout();
        Button newGameBtn = new Button("New Game", e -> newGame());
        newGameBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button loadGameBtn = new Button("Load Game", e -> loadGameDialog());
        newGameBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        vl.add(newGameBtn, loadGameBtn);
        dialog.add(vl);
        dialog.open();
    }

    private VerticalLayout getLobbyScreen() {
        VerticalLayout vl = new VerticalLayout();
        vl.add(getActiveGamesGrid());
        return vl;
    }

    private Grid<Game> getActiveGamesGrid() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Grid<Game> activeGames = new Grid<>(Game.class, false);
        activeGames.setClassName("active-games");
        activeGames.addColumn(createIdRenderer()).setHeader("ID").setSortable(true).setWidth("10px");
        activeGames.addColumn(createUsersRenderer()).setHeader("Players");
        activeGames.addColumn(createStatusComponentRenderer()).setHeader("Game Status").setSortable(true);
        activeGames.addColumn(createActionComponentRenderer());

        activeGames.setItems(gameRepository.findAllByStatusIn(Set.of(IN_PROGRESS.name(), WAITING_FOR_OPPONENT.name())));
        activeGames.addItemDoubleClickListener(e -> e.getItem().getId());
        return activeGames;
    }

    private static ComponentRenderer<Span, Game> createStatusComponentRenderer() {
        return new ComponentRenderer<>(Span::new, statusComponentUpdater);
    }

    private static final SerializableBiConsumer<Span, Game> statusComponentUpdater = (
            span, game) -> {
        if (IN_PROGRESS.name().equals(game.getStatus())) {
            span.setClassName("orangeBadge");
            span.setText(IN_PROGRESS.getLabel());
        } else {
            span.setClassName("greenBadge");
            span.setText(WAITING_FOR_OPPONENT.getLabel());
        }
    };

    private void newGame() {
        Game game = new Game(getCurrentUser().getId(), GameType.CHECKERS.toString());
        gameRepository.save(game);
        UI.getCurrent().navigate(ChessBoard.class);
    }

    private void loadGameDialog() {
        Dialog loadDialog = new Dialog();
        loadDialog.setHeaderTitle("You have unfinished games. Would you like to continue?");
        Grid<Game> grid = new Grid<>(Game.class);
        grid.setItems(getActiveGamesForCurrentUser());
        loadDialog.add(grid);
        dialog.close();
        loadDialog.open();
    }

    private List<Game> getActiveGamesForCurrentUser() {
        return gameRepository.findAllByUserId(getCurrentUser().getId())
                .stream().filter(Game::isNotFinished).collect(Collectors.toList());
    }

    private User getCurrentUser() {
        return (User) authentication.getPrincipal();
    }

    private Renderer<Game> createUsersRenderer() {
        return LitRenderer.<Game> of(
                "<vaadin-horizontal-layout style=\"align-items: center;\" theme=\"spacing\">"
                        + "<vaadin-vertical-layout style=\"line-height: var(--lumo-line-height-m);\">"
                        + "  <span> ${item.nick} </span>"
                        + "    <span style=\"font-size: var(--lumo-font-size-s); color: var(--lumo-secondary-text-color);\">"
                        + "      ${item.user}" + "    </span>"
                        + "  </vaadin-vertical-layout>"
                        + "<vaadin-vertical-layout style=\"line-height: var(--lumo-line-height-m);\">"
                        + "  <span> ${item.nick2} </span>"
                        + "    <span style=\"font-size: var(--lumo-font-size-s); color: var(--lumo-secondary-text-color);\">"
                        + "      ${item.secondUser}" + "    </span>"
                        + "  </vaadin-vertical-layout>"
                        + "</vaadin-horizontal-layout>")
                .withProperty("user", game -> Optional.ofNullable(game.getUserId())
                        .map(userRepository::findById).map(user -> user.map(User::getUsername).orElse(StringUtils.EMPTY)).orElse(StringUtils.EMPTY))
                .withProperty("nick", game -> Optional.ofNullable(game.getUserId())
                        .map(userRepository::findById).map(user -> user.map(User::getNick).orElse(StringUtils.EMPTY)).orElse(StringUtils.EMPTY))
                .withProperty("secondUser", game -> Optional.ofNullable(game.getSecondUserId())
                .map(userRepository::findById).map(user -> user.map(User::getUsername).orElse(StringUtils.EMPTY)).orElse(StringUtils.EMPTY))
                .withProperty("nick2", game -> Optional.ofNullable(game.getSecondUserId())
                .map(userRepository::findById).map(user -> user.map(User::getNick).orElse(StringUtils.EMPTY)).orElse(StringUtils.EMPTY));
    }

    private static Renderer<Game> createIdRenderer() {
        return LitRenderer.<Game> of(
                "<vaadin-horizontal-layout style=\"align-items: center;\" theme=\"spacing\">"
                        + "<vaadin-vertical-layout style=\"line-height: var(--lumo-line-height-m);\">"
                        + "  <span> Game ${item.gameId} </span>"
                        + "    <span style=\"font-size: var(--lumo-font-size-s); color: var(--lumo-secondary-text-color);\">"
                        + "      ${item.startDate}" + "    </span>"
                        + "  </vaadin-vertical-layout>"
                        + "</vaadin-horizontal-layout>")
                .withProperty("gameId", Game::getId)
                .withProperty("startDate", game -> df.format(game.getStartDate()));
    }

    private static final SerializableBiConsumer<Button, Game> actionComponentUpdater = (
            button, game) -> {
        if (IN_PROGRESS.name().equals(game.getStatus())) {
            button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            button.setIcon(new Icon(VaadinIcon.EYE));
        } else {
            button.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            button.setIcon(new Icon(VaadinIcon.SWORD));
        }
    };

    private static ComponentRenderer<Button, Game> createActionComponentRenderer() {
        return new ComponentRenderer<>(Button::new, actionComponentUpdater);
    }
}
