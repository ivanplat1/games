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
import com.vaadin.flow.router.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

import static com.ivpl.games.constants.Color.*;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final StepRepository stepRepository;

    private static final Random rm = new Random();

    @Autowired
    public GameService(GameRepository gameRepository, StepRepository stepRepository) {
        this.gameRepository = gameRepository;
        this.stepRepository = stepRepository;
    }

    public void newGame(User user, Color selectedColor) {
        if (RANDOM.equals(selectedColor))
            selectedColor = Color.values()[rm.nextInt(2)];

        Game game = new Game(user.getId(), selectedColor, GameType.CHECKERS);
        game.setColorPlayer2(WHITE.equals(selectedColor) ? BLACK : WHITE);
        gameRepository.saveAndFlush(game);
        UI.getCurrent().navigate(ChessBoard.class, Long.toString(game.getId()));
    }

    public void joinGame(Game game, User user) {
        game.setPlayer2Id(user.getId());
        game.setStatus(GameStatus.IN_PROGRESS);
        gameRepository.saveAndFlush(game);
        UI.getCurrent().navigate(ChessBoard.class, Long.toString(game.getId()));
    }

    public void saveStep(Long gameId,
                         Color playerColor,
                         CellKey from,
                         CellKey to,
                         Integer figureId) {
        Step step = new Step(gameId, increaseStepCount(gameId), playerColor, from, to, figureId);
        stepRepository.saveAndFlush(step);
    }

    public Integer increaseStepCount(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() ->  new NotFoundException(String.format("Game with Id %s is was not found", gameId)));
        game.setStepCount(game.getStepCount()+1);
        gameRepository.saveAndFlush(game);
        return game.getStepCount();
    }
}
