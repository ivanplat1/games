package com.ivpl.games.services;

import com.ivpl.games.constants.Color;
import com.ivpl.games.constants.PieceType;
import com.ivpl.games.entity.BoardContainer;
import com.ivpl.games.entity.jpa.Game;
import com.ivpl.games.entity.ui.AbstractPieceView;
import com.ivpl.games.entity.ui.Cell;
import com.ivpl.games.entity.ui.CellKey;
import com.ivpl.games.entity.ui.chess.KingView;
import com.ivpl.games.repository.GameRepository;
import com.ivpl.games.repository.StepRepository;
import com.ivpl.games.services.broadcasting.BroadcasterService;
import com.ivpl.games.utils.CommonUtils;
import com.ivpl.games.view.AbstractBoardView;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.ivpl.games.constants.ExceptionMessages.GAME_NOT_FOUND_BY_ID;
import static com.ivpl.games.constants.ExceptionMessages.KING_NOT_FOUND;

@Service
public class ChessBoardServiceImpl extends AbstractBoardServiceImpl implements BoardService {

    public ChessBoardServiceImpl(UIComponentsService uiComponentsService,
                                    GameRepository gameRepository,
                                    GameService gameService,
                                    StepRepository stepRepository, BroadcasterService broadcasterService) {
        super(uiComponentsService, gameRepository, gameService, stepRepository, broadcasterService);
    }

    @Override
    public BoardContainer reloadBoard(Long gameId, AbstractBoardView boardView) {
        BoardContainer boardContainer = super.reloadBoard(gameId, boardView);
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(GAME_NOT_FOUND_BY_ID, gameId)));
        getKingOfCertainColor(game.getTurn(), boardContainer.getPieces()).calculateCastlingSteps(boardContainer.getCells());
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
                piece.placeAt(cell);
                gameService.saveStep(game.getId(), cell.getKey(),
                        selectedPiece.get(), true);
            }
            checkIsGameOver(game, cells);
            broadcasterService.getBroadcaster(game.getId()).broadcast(this.hashCode());
        }));
    }

    private boolean isPossibleStepCastlingStep(AbstractPieceView kingView, Cell cell) {
        int direction = kingView.getPosition().getKey().getX() - cell.getKey().getX();
        return direction == 2 || direction == -2;
    }

    private void checkIsGameOver(Game game, Map<CellKey, Cell> cells) {
        List<AbstractPieceView> pieces = CommonUtils.getPiecesFromCells(cells).stream()
                .filter(p -> !game.getTurn().equals(p.getColor()))
                .collect(Collectors.toList());

        pieces.forEach(p -> p.calculatePossibleSteps(cells, true));

        if (pieces.stream().allMatch(p -> p.getPossibleSteps().isEmpty())) {
            gameService.finishGame(game, isKingUnderAttack(CommonUtils.getOppositeColor(game.getTurn()), cells)
                            ? game.getTurn()
                            : Color.NOBODY);
        }
    }

    private boolean isKingUnderAttack(Color currentTurn, Map<CellKey, Cell> cells) {
        AbstractPieceView kingPiece = getKingOfCertainColor(currentTurn, CommonUtils.getPiecesFromCells(cells));
        return CommonUtils.isAnyCellUnderAttack(currentTurn, cells, kingPiece.getPosition().getKey());
    }

    private KingView getKingOfCertainColor(Color color, List<AbstractPieceView> pieces) {
        return (KingView) pieces.stream()
                .filter(p -> PieceType.KING.equals(p.getType()) && color.equals(p.getColor()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(KING_NOT_FOUND));
    }
}
