package com.ivpl.games.services;

import com.ivpl.games.constants.PieceType;
import com.ivpl.games.entity.ChessBoardContainer;
import com.ivpl.games.entity.jpa.Game;
import com.ivpl.games.entity.ui.AbstractPieceView;
import com.ivpl.games.entity.ui.Cell;
import com.ivpl.games.entity.ui.CellKey;
import com.ivpl.games.entity.ui.checkers.CheckerQueenView;
import com.ivpl.games.repository.GameRepository;
import com.ivpl.games.repository.StepRepository;
import com.ivpl.games.services.broadcasting.BroadcasterService;
import com.ivpl.games.view.AbstractBoardView;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.ivpl.games.constants.ExceptionMessages.GAME_NOT_FOUND_BY_ID;

@Service
public class CheckersBoardServiceImpl extends AbstractBoardServiceImpl implements BoardService {

    public CheckersBoardServiceImpl(UIComponentsService uiComponentsService,
                                 GameRepository gameRepository,
                                 GameService gameService,
                                 StepRepository stepRepository, BroadcasterService broadcasterService) {
        super(uiComponentsService, gameRepository, gameService, stepRepository, broadcasterService);
    }

    @Override
    public ChessBoardContainer reloadBoard(Long gameId, AbstractBoardView boardView) {
        ChessBoardContainer boardContainer = super.reloadBoard(gameId, boardView);
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(GAME_NOT_FOUND_BY_ID, gameId)));
        List<AbstractPieceView> pieces = boardContainer.getPieces();
        // calculate possible steps for pieces
        if (game.getTurn().equals(boardView.getPlayerColor())) {
            pieces.stream()
                    .filter(p -> p.getPosition() != null && game.getTurn().equals(p.getColor()))
                    .forEach(p -> p.calculatePossibleSteps(boardContainer.getCells(), true));

            boolean haveToEatAnything = pieces.stream()
                    .filter(f -> game.getTurn().equals(f.getColor()))
                    .anyMatch(f -> !f.getPiecesToBeEaten().isEmpty());

            if (haveToEatAnything)
                pieces.stream().filter(p -> p.getPiecesToBeEaten().isEmpty())
                        .forEach(p -> p.getPossibleSteps().clear());
        }
        return boardContainer;
    }

    @Override
    protected void addCellListener(Game game, Map<CellKey, Cell> cells, Cell cell, AbstractBoardView boardView) {
        cell.clearListener();
        AtomicReference<AbstractPieceView> selectedPiece = boardView.getSelectedPiece();
        cell.setOnClickListener(cell.addClickListener(event -> {
            AtomicBoolean isAnythingEaten = new AtomicBoolean(false);
            Optional.ofNullable(selectedPiece.get().getPiecesToBeEaten().get(cell.getKey()))
                    .ifPresent(f -> {
                        f.toDie();
                        gameService.killPiece(f.getDbId());
                        isAnythingEaten.set(true);
                    });
            selectedPiece.get().placeAt(cell);
            replaceWithQueenIfNeeded(cell, selectedPiece);

            if (isAnythingEaten.get()) {
                selectedPiece.get().calculatePossibleSteps(cells, true);
                // if still have anything to eat
                if (!selectedPiece.get().getPiecesToBeEaten().isEmpty()) {
                    gameService.saveStep(game.getId(), cell.getKey(),
                            selectedPiece.get(), false);
                    broadcasterService.getBroadcaster(game.getId()).broadcast(this.hashCode());
                    return;
                }
                isAnythingEaten.set(false);
            }
            gameService.saveStep(game.getId(), cell.getKey(),
                    selectedPiece.get(), true);
            broadcasterService.getBroadcaster(game.getId()).broadcast(this.hashCode());
        }));
    }

    private void replaceWithQueenIfNeeded(Cell cell, AtomicReference<AbstractPieceView> piece) {
        if (isBorderCell(cell.getKey(), piece.get().getColor())) {
            AbstractPieceView checkerQueenView = new CheckerQueenView(piece.get().getPieceId(), piece.get().getDbId(), piece.get().getColor(), PieceType.CHECKER_QUEEN, cell);
            cell.remove(piece.get());
            cell.setPiece(checkerQueenView);
            piece.set(checkerQueenView);
            gameService.mutatePiece(piece.get().getDbId(), PieceType.CHECKER_QUEEN);
        }
    }
}
