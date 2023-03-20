package com.ivpl.games.services;

import com.ivpl.games.constants.*;
import com.ivpl.games.converter.PieceToPieceViewConverter;
import com.ivpl.games.entity.ui.Cell;
import com.ivpl.games.entity.ui.CellKey;
import com.ivpl.games.entity.jpa.Game;
import com.ivpl.games.entity.jpa.Piece;
import com.ivpl.games.entity.jpa.Step;
import com.ivpl.games.entity.jpa.User;
import com.ivpl.games.entity.ui.AbstractPieceView;
import com.ivpl.games.repository.GameRepository;
import com.ivpl.games.repository.PieceRepository;
import com.ivpl.games.repository.StepRepository;
import com.ivpl.games.utils.CommonUtils;
import com.ivpl.games.view.ChessBoard;
import com.vaadin.flow.component.UI;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.ivpl.games.constants.Color.*;
import static com.ivpl.games.constants.ExceptionMessages.GAME_NOT_FOUND_BY_ID;
import static com.ivpl.games.constants.ExceptionMessages.PIECE_NOT_FOUND_BY_ID;
import static com.ivpl.games.constants.GameType.CHECKERS;
import static com.ivpl.games.constants.GameType.CHESS;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final StepRepository stepRepository;
    private final PieceRepository pieceRepository;
    private final PieceToPieceViewConverter pieceToPieceViewConverter;

    private static final Random rm = new Random();

    @Autowired
    public GameService(GameRepository gameRepository, StepRepository stepRepository, PieceRepository pieceRepository, PieceToPieceViewConverter pieceToPieceViewConverter) {
        this.gameRepository = gameRepository;
        this.stepRepository = stepRepository;
        this.pieceRepository = pieceRepository;
        this.pieceToPieceViewConverter = pieceToPieceViewConverter;
    }

    public void newGame(User user, Color selectedColor, GameType gameType) {
        if (RANDOM.equals(selectedColor))
            selectedColor = Color.values()[rm.nextInt(2)];

        Game game = new Game(user.getId(), selectedColor, gameType);
        game.setColorPlayer2(CommonUtils.getOppositeColor(selectedColor));
        gameRepository.saveAndFlush(game);
        createPieces(game.getId(), gameType);
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
                         CellKey to,
                         Long pieceId,
                         Long pieceDBId,
                         boolean changeColor) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(GAME_NOT_FOUND_BY_ID, gameId)));
        Piece piece = pieceRepository.findPieceById(pieceDBId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(PIECE_NOT_FOUND_BY_ID, pieceDBId)));
        Step step = new Step(gameId, increaseStepCount(game), playerColor, new CellKey(piece.getPosition()), to, pieceId);
        stepRepository.saveAndFlush(step);
        piece.setPosition(to.getAsArray());
        pieceRepository.saveAndFlush(piece);
        if (changeColor) game.setTurn(CommonUtils.getOppositeColor(game.getTurn()));
        gameRepository.saveAndFlush(game);
    }

    public List<AbstractPieceView> loadPieceViewsForGame(Long gameId, Map<CellKey, Cell> cells) {
        return pieceRepository.findAllByGameId(gameId).stream()
                .map(e -> pieceToPieceViewConverter.convert(e, cells))
                .collect(Collectors.toList());
    }

    private Integer increaseStepCount(Game game) {
        game.setStepCount(game.getStepCount()+1);
        gameRepository.saveAndFlush(game);
        return game.getStepCount();
    }

    private void createPieces(Long gameId, GameType gameType) {
        Map<Long, Integer[]> positions = new HashMap<>(
                CHECKERS.equals(gameType)
                        ? PiecesInitPositions.checkersInitPositions
                        : PiecesInitPositions.chessInitPositions);

        if (CHECKERS.equals(gameType)) {
            positions.forEach((k, v) -> pieceRepository.save(
                    new Piece(gameId, k, PieceType.CHECKER, v[1] > 4 ? WHITE : BLACK, v)));
        } else if (CHESS.equals(gameType)) {
            positions.forEach((k, v) -> pieceRepository.save(
                    new Piece(gameId, k, PiecesInitPositions.idToTypeMapping.get(k), v[1] > 4 ? WHITE : BLACK, v)));
        }

        pieceRepository.flush();
    }

    @NonNull
    public void killPiece(Long pieceId) {
        Piece piece = pieceRepository.findPieceById(pieceId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(PIECE_NOT_FOUND_BY_ID, pieceId)));
        piece.setPosition(null);
        piece.setAlive(false);
        pieceRepository.saveAndFlush(piece);
    }

    @NonNull
    public void mutatePiece(Long pieceId, PieceType newType) {
        Piece piece = pieceRepository.findPieceById(pieceId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(PIECE_NOT_FOUND_BY_ID, pieceId)));
        piece.setType(newType);
        pieceRepository.saveAndFlush(piece);
    }

    public void finishGame(Game game, Color winner) {
        game.setWinner(winner);
        game.setStatus(GameStatus.FINISHED);
        gameRepository.saveAndFlush(game);
    }
}
