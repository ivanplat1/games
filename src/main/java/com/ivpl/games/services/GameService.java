package com.ivpl.games.services;

import com.ivpl.games.constants.Color;
import com.ivpl.games.constants.GameStatus;
import com.ivpl.games.constants.GameType;
import com.ivpl.games.entity.CellKey;
import com.ivpl.games.entity.jpa.Game;
import com.ivpl.games.entity.jpa.Step;
import com.ivpl.games.entity.jpa.User;
import com.ivpl.games.repository.GameRepository;
import com.ivpl.games.repository.StepRepository;
import com.ivpl.games.view.ChessBoard;
import com.vaadin.flow.component.UI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.ivpl.games.constants.Color.BLACK;
import static com.ivpl.games.constants.Color.WHITE;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final StepRepository stepRepository;

    @Autowired
    public GameService(GameRepository gameRepository, StepRepository stepRepository) {
        this.gameRepository = gameRepository;
        this.stepRepository = stepRepository;
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

    public void saveStep(Long gameId,
                         Integer gameStepId,
                         Color playerColor,
                         CellKey from,
                         CellKey to,
                         Long figureId) {
        Step step = new Step(gameId, gameStepId, playerColor, from, to, figureId);
        stepRepository.save(step);
    }
}
