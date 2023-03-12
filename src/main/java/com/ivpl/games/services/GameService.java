package com.ivpl.games.services;

import com.ivpl.games.constants.*;
import com.ivpl.games.entity.ui.CellKey;
import com.ivpl.games.entity.jpa.Game;
import com.ivpl.games.entity.jpa.Piece;
import com.ivpl.games.entity.jpa.Step;
import com.ivpl.games.entity.jpa.User;
import com.ivpl.games.repository.GameRepository;
import com.ivpl.games.repository.PieceRepository;
import com.ivpl.games.repository.StepRepository;
import com.ivpl.games.utils.CommonUtils;
import com.ivpl.games.view.ChessBoard;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.ivpl.games.constants.Color.*;
import static com.ivpl.games.constants.GameType.CHECKERS;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final StepRepository stepRepository;
    private final PieceRepository pieceRepository;

    private static final Random rm = new Random();

    @Autowired
    public GameService(GameRepository gameRepository, StepRepository stepRepository, PieceRepository pieceRepository) {
        this.gameRepository = gameRepository;
        this.stepRepository = stepRepository;
        this.pieceRepository = pieceRepository;
    }

    public void newGame(User user, Color selectedColor, GameType gameType) {
        if (RANDOM.equals(selectedColor))
            selectedColor = Color.values()[rm.nextInt(2)];

        Game game = new Game(user.getId(), selectedColor, gameType);
        game.setColorPlayer2(CommonUtils.getOppositeColor(selectedColor));
        gameRepository.saveAndFlush(game);
        UI.getCurrent().navigate(ChessBoard.class, Long.toString(game.getId()));
        createPieces(game.getId(), gameType);
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
                         Integer pieceId) {
        Step step = new Step(gameId, increaseStepCount(gameId), playerColor, from, to, pieceId);
        stepRepository.saveAndFlush(step);
    }

    public Integer increaseStepCount(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() ->  new NotFoundException(String.format("Game with Id %s is was not found", gameId)));
        game.setStepCount(game.getStepCount()+1);
        gameRepository.saveAndFlush(game);
        return game.getStepCount();
    }

    private void createPieces(Long gameId, GameType gameType) {
        Map<Long, Integer[]> positions = new HashMap<>(
                CHECKERS.equals(gameType)
                        ? PiecesInitPositions.checkersInitPositions
                        : Collections.emptyMap());
        positions.forEach((k, v) -> pieceRepository.save(
                new Piece(gameId, k, PieceType.CHECKER, k > 12 ? WHITE : BLACK, v)));
        pieceRepository.flush();
    }
}
