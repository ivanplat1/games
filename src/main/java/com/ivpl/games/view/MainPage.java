package com.ivpl.games.view;

import com.ivpl.games.constants.GameType;
import com.ivpl.games.entity.Game;
import com.ivpl.games.entity.User;
import com.ivpl.games.repository.GameRepository;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.stream.Collectors;

@Route("")
public class MainPage extends VerticalLayout {

    private final transient GameRepository gameRepository;
    private final Dialog dialog;
    private final Authentication authentication;

    public MainPage(GameRepository gameRepository) {
        this.gameRepository = gameRepository;

        authentication = SecurityContextHolder.getContext().getAuthentication();

        dialog = new Dialog();
        VerticalLayout vl = new VerticalLayout();
        Button newGameBtn = new Button("New Game", e -> newGame());
        newGameBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button loadGameBtn = new Button("Load Game", e -> loadGameDialog());
        newGameBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        vl.add(newGameBtn, loadGameBtn);
        dialog.add(vl);
        add(dialog);
        dialog.open();
    }

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
}
