package com.ivpl.games.services;

import com.ivpl.games.constants.Color;
import com.ivpl.games.constants.GameStatus;
import com.ivpl.games.constants.GameType;
import com.ivpl.games.entity.Game;
import com.ivpl.games.entity.User;
import com.ivpl.games.repository.GameRepository;
import com.ivpl.games.view.ChessBoard;
import com.vaadin.flow.component.UI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.ivpl.games.constants.Color.BLACK;
import static com.ivpl.games.constants.Color.WHITE;

@Service
public class GameService {

    private final GameRepository gameRepository;

    @Autowired
    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public void newGame(User user) {
        Game game = new Game(user.getId(), GameType.CHECKERS);
        gameRepository.saveAndFlush(game);
        UI.getCurrent().navigate(ChessBoard.class, Long.toString(game.getId()));
    }

    public void joinGame(Game game, User user) {
        Color color = WHITE.equals(game.getColorPlayer1()) ? BLACK : WHITE;
        game.setPlayer2Id(user.getId());
        game.setColorPlayer2(color);
        game.setStatus(GameStatus.IN_PROGRESS);
        gameRepository.saveAndFlush(game);
        UI.getCurrent().navigate(ChessBoard.class, Long.toString(game.getId()));
    }
}
