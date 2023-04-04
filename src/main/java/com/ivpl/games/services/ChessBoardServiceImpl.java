package com.ivpl.games.services;

import com.ivpl.games.constants.PieceType;
import com.ivpl.games.entity.ChessBoardContainer;
import com.ivpl.games.entity.jpa.Game;
import com.ivpl.games.entity.ui.AbstractPieceView;
import com.ivpl.games.entity.ui.Cell;
import com.ivpl.games.entity.ui.CellKey;
import com.ivpl.games.entity.ui.chess.KingView;
import com.ivpl.games.entity.ui.chess.RookView;
import com.ivpl.games.repository.GameRepository;
import com.ivpl.games.repository.StepRepository;
import com.ivpl.games.services.broadcasting.BroadcasterService;
import com.ivpl.games.view.AbstractBoardView;
import org.apache.commons.lang3.Range;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.ivpl.games.constants.ExceptionMessages.GAME_NOT_FOUND_BY_ID;

@Service
public class ChessBoardServiceImpl extends AbstractBoardServiceImpl implements BoardService {

    public ChessBoardServiceImpl(UIComponentsService uiComponentsService,
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
                    .forEach(p -> {
                        p.calculatePossibleSteps(boardContainer.getCells(), true);
                        if (PieceType.KING.equals(p.getType())) calculateCastlingSteps((KingView) p, boardContainer.getCells());
                    });
        }
        return boardContainer;
    }

    @Override
    protected void addCellListener(Game game, Map<CellKey, Cell> cells, Cell cell, AbstractBoardView boardView) {
        cell.clearListener();
        AtomicReference<AbstractPieceView> selectedPiece = boardView.getSelectedPiece();
        cell.setOnClickListener(cell.addClickListener(event -> {
            AbstractPieceView piece = selectedPiece.get();
            if (PieceType.KING.equals(piece.getType())
                    && isPossibleStepCastlingStep(piece, cell)) {
                // move king
                gameService.saveStep(game.getId(), cell.getKey(),
                        piece, false);
                // move rook
                int kingX = piece.getPosition().getKey().getX();
                int kingY = piece.getPosition().getKey().getY();
                AbstractPieceView rook = cells.get(new CellKey(kingX - cell.getKey().getX() > 0 ? 1 : 8, kingY)).getPiece();
                int x = rook.getPosition().getKey().getX();
                gameService.saveStep(game.getId(), new CellKey(x == 1 ? 4 : 6, kingY),
                        rook, true);
            } else {
                Optional.ofNullable(selectedPiece.get().getPiecesToBeEaten().get(cell.getKey()))
                        .ifPresent(f -> {
                            f.toDie();
                            gameService.killPiece(f.getDbId());
                        });
                gameService.saveStep(game.getId(), cell.getKey(),
                        selectedPiece.get(), true);
            }
            broadcasterService.getBroadcaster(game.getId()).broadcast(this.hashCode());
        }));
    }

    private void calculateCastlingSteps(KingView kingPiece, Map<CellKey, Cell> cells) {
        if (kingPiece.getSteps().isEmpty()) {
            cells.values().stream().filter(Cell::isOccupied)
                    .map(Cell::getPiece)
                    .filter(p -> kingPiece.getColor().equals(p.getColor()) && PieceType.ROOK.equals(p.getType()))
                    .filter(rook -> rook.getSteps().isEmpty())
                    .map(p -> (RookView) p)
                    .filter(rook -> isRookCastlingPossible(cells, kingPiece, rook))
                    .forEach(rook -> {
                        int x = rook.getPosition().getKey().getX();
                        int y = rook.getPosition().getKey().getY();
                        Cell cell = cells.get(new CellKey(x == 1 ? 3 : 7, y));
                        kingPiece.getPossibleSteps().add(cell.getKey());
                    });
        }
    }

    private boolean isRookCastlingPossible(Map<CellKey, Cell> cells, KingView kingView, RookView rook) {
        int x = rook.getPosition().getKey().getX();
        int y = rook.getPosition().getKey().getY();
        return cells.entrySet().stream()
                .filter(e -> e.getKey().getY() == y &&
                        ((x == 1 && Range.between(2, 4).contains(e.getKey().getX())
                                && areCellsNotUnderAttack(cells, kingView,
                                new CellKey(2, y),
                                new CellKey(3, y),
                                new CellKey(4, y)))
                                || (x == 8 && Range.between(6, 7).contains(e.getKey().getX())
                                && areCellsNotUnderAttack(cells, kingView,
                                new CellKey(6, y),
                                new CellKey(7, y))))
                )
                .map(Map.Entry::getValue)
                .anyMatch(c -> !c.isOccupied());
    }

    private boolean areCellsNotUnderAttack(Map<CellKey, Cell> cells, KingView kingView, CellKey ... keys) {
        return Arrays.stream(keys).noneMatch(k -> kingView.isStepLeadsToCheck(cells, cells.get(k)));
    }

    private boolean isPossibleStepCastlingStep(AbstractPieceView kingView, Cell cell) {
        int direction = kingView.getPosition().getKey().getX() - cell.getKey().getX();
        return direction == 2 || direction == -2;
    }
}
