package com.ivpl.games.services;

import com.ivpl.games.constants.Color;
import com.ivpl.games.constants.GameType;
import com.ivpl.games.entity.ChessBoardContainer;
import com.ivpl.games.entity.jpa.Game;
import com.ivpl.games.entity.ui.AbstractPieceView;
import com.ivpl.games.entity.ui.Cell;
import com.ivpl.games.entity.ui.CellKey;
import com.ivpl.games.repository.GameRepository;
import com.ivpl.games.repository.StepRepository;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.ivpl.games.constants.ExceptionMessages.GAME_NOT_FOUND_BY_ID;

@Service
public class BoardServiceImpl implements BoardService {

    private final UIComponentsService uiComponentsService;
    private final GameRepository gameRepository;
    private final GameService gameService;
    private final StepRepository stepRepository;

    public BoardServiceImpl(UIComponentsService uiComponentsService,
                            GameRepository gameRepository,
                            GameService gameService,
                            StepRepository stepRepository) {
        this.uiComponentsService = uiComponentsService;
        this.gameRepository = gameRepository;
        this.gameService = gameService;
        this.stepRepository = stepRepository;
    }

    @Override
    public ChessBoardContainer reloadBoardFromDB(Long gameId, Color playerColor) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(GAME_NOT_FOUND_BY_ID, gameId)));
        Map<CellKey, Cell> cells = new LinkedHashMap<>();
        VerticalLayout board = uiComponentsService.getChessBoard(cells);
        List<AbstractPieceView> pieces = new ArrayList<>(gameService.loadPieceViewsForGame(game.getId(), cells));
        pieces.stream().filter(p -> p.getPosition() != null)
                .forEach(p -> {
                    p.getPosition().setPiece(p);
                    //addPieceListener(game, p, selectedPiece, cells);
                    p.getSteps().clear();
                    p.getSteps().addAll(stepRepository.findAllByGameIdAndPieceIdOrderByGameStepId(game.getId(), p.getPieceId()));
                });

        // calculate possible steps for pieces
        if (game.getTurn().equals(playerColor)) {
            pieces.stream()
                    .filter(p -> p.getPosition() != null)
                    .forEach(p -> p.calculatePossibleSteps(cells));

            boolean haveToEatAnything = GameType.CHECKERS.equals(game.getType()) && pieces.stream()
                    .filter(f -> game.getTurn().equals(f.getColor()))
                    .anyMatch(f -> !f.getPiecesToBeEaten().isEmpty());

            if (haveToEatAnything)
                pieces.stream().filter(f -> f.getPiecesToBeEaten().isEmpty())
                        .forEach(f -> f.getPossibleSteps().clear());
        }
        return new ChessBoardContainer(board, pieces, cells);
    }

    @Override
    public void doStep(Long gameId, AbstractPieceView selectedPiece, CellKey cellKey) {
        gameRepository.findById(gameId).ifPresent(game -> {

        });
    }
}
