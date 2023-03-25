package com.ivpl.games.services;

import com.ivpl.games.constants.Color;
import com.ivpl.games.constants.GameType;
import com.ivpl.games.constants.PieceType;
import com.ivpl.games.entity.jpa.Game;
import com.ivpl.games.entity.ui.AbstractPieceView;
import com.ivpl.games.entity.ui.Cell;
import com.ivpl.games.entity.ui.CellKey;
import com.ivpl.games.entity.ui.checkers.CheckerQueenView;
import com.ivpl.games.repository.GameRepository;
import com.ivpl.games.repository.StepRepository;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.ivpl.games.constants.Color.WHITE;
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
    public VerticalLayout reloadBoardFromDB(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(GAME_NOT_FOUND_BY_ID, gameId)));
        Map<CellKey, Cell> cells = new HashMap<>();
        List<AbstractPieceView> pieces = new ArrayList<>(gameService.loadPieceViewsForGame(game.getId(), cells));
        pieces.stream().filter(p -> p.getPosition() != null)
                .forEach(p -> {
                    p.getPosition().setPiece(p);
                    //addPieceListener(game, p, selectedPiece, cells);
                    p.getSteps().clear();
                    p.getSteps().addAll(stepRepository.findAllByGameIdAndPieceIdOrderByGameStepId(game.getId(), p.getPieceId()));
                });
        return uiComponentsService.getChessBoard(cells);
    }

/*    private void addPieceListener(Game game, AbstractPieceView p, AbstractPieceView selectedPiece, Map<CellKey, Cell> cells) {
        p.setOnClickListener(p.addClickListener(e -> {
            if (p.getPossibleSteps().isEmpty() || !game.getTurn().equals(p.getColor())) return;
            p.selectUnselectPiece();
            if (!p.equals(selectedPiece)) {
                p.getPossibleSteps().forEach(k -> {
                    Cell cell = cells.get(k);
                    cell.addSelectedStyle();
                    //addCellListener(cell);
                });
            } else {
                p.getPossibleSteps().forEach(k -> cells.get(k).removeSelectedStyle());
            }
        }));
    }*/

    @Override
    public void doStep(Long gameId, AbstractPieceView selectedPiece, CellKey cellKey) {
        gameRepository.findById(gameId).ifPresent(game -> {

        });

        /*gameRepository.findById(gameId).ifPresent(game -> {
            Boolean isAnythingEaten = false;
            Optional.ofNullable(selectedPiece.getPiecesToBeEaten().get(cell.getKey()))
                    .ifPresent(f -> {
                        f.toDie();
                        gameService.killPiece(f.getDbId());
                        isAnythingEaten = true;
                    });
            selectedPiece.placeAt(cell);
            replaceWithQueenIfNeeded(game, cell, selectedPiece);

            if (GameType.CHECKERS.equals(game.getType()) && isAnythingEaten) {
                selectedPiece.calculatePossibleSteps(cells);
                // if still have anything to eat
                if (!selectedPiece.getPiecesToBeEaten().isEmpty()) {
                    gameService.saveStep(game.getId(),
                            selectedPiece.getColor(), cell.getKey(),
                            selectedPiece.getPieceId(), selectedPiece.getDbId(), false);
                    return;
                }
            }
            gameService.saveStep(game.getId(),
                    selectedPiece.getColor(), cell.getKey(),
                    selectedPiece.getPieceId(), selectedPiece.getDbId(), true);
        });*/
    }

    private void replaceWithQueenIfNeeded(Game game, Cell cell, AbstractPieceView piece) {
        if (GameType.CHECKERS.equals(game.getType()) && isBorderCell(cell.getKey(), piece.getColor())) {
            CheckerQueenView checkerQueenView = new CheckerQueenView(piece.getPieceId(), piece.getDbId(), piece.getColor(), PieceType.CHECKER_QUEEN, cell);
            cell.remove(piece);
            cell.setPiece(checkerQueenView);
            gameService.mutatePiece(piece.getDbId(), PieceType.CHECKER_QUEEN);
        }
    }

    private boolean isBorderCell(CellKey cellKey, Color pieceColor) {
        return WHITE.equals(pieceColor) ? cellKey.getY() == 1 : cellKey.getY() == 8;
    }
}
