package com.ivpl.games.services;

import com.ivpl.games.constants.Color;
import com.ivpl.games.constants.PieceType;
import com.ivpl.games.entity.BoardContainer;
import com.ivpl.games.entity.jpa.Game;
import com.ivpl.games.entity.ui.AbstractPieceView;
import com.ivpl.games.entity.ui.Cell;
import com.ivpl.games.entity.ui.CellKey;
import com.ivpl.games.entity.ui.chess.*;
import com.ivpl.games.repository.GameRepository;
import com.ivpl.games.repository.StepRepository;
import com.ivpl.games.services.broadcasting.BroadcasterService;
import com.ivpl.games.utils.CommonUtils;
import com.ivpl.games.view.AbstractBoardView;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.ivpl.games.constants.ExceptionMessages.*;

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
    protected void addCellListener(Game game, Map<CellKey, Cell> cells, Cell cell, AbstractPieceView selectedPiece) {
        cell.clearListener();
        if (PieceType.PAWN.equals(selectedPiece.getType())
                && CommonUtils.isBorderCell(cell.getKey(), selectedPiece.getColor())) {
            addCellListenerWithPieceSelector(game, cells, cell, selectedPiece);
        } else {
            cell.setOnClickListener(cell.addClickListener(event -> {
                if (PieceType.KING.equals(selectedPiece.getType())
                        && isPossibleStepCastlingStep(selectedPiece, cell)) {
                    // move king
                    gameService.saveStep(game.getId(), cell.getKey(),
                            selectedPiece, false);
                    // move rook
                    int kingX = selectedPiece.getPosition().getKey().getX();
                    int kingY = selectedPiece.getPosition().getKey().getY();
                    AbstractPieceView rook = cells.get(new CellKey(kingX - cell.getKey().getX() > 0 ? 1 : 8, kingY)).getPiece();
                    int x = rook.getPosition().getKey().getX();
                    gameService.saveStep(game.getId(), new CellKey(x == 1 ? 4 : 6, kingY),
                            rook, true);
                } else {
                    executeDefaultStepLogic(game, cell, selectedPiece);
                }
                checkIsGameOver(game, cells);
                broadcasterService.getBroadcaster(game.getId()).broadcast(this.hashCode());
            }));
        }
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

    private void addCellListenerWithPieceSelector(Game game, Map<CellKey, Cell> cells, Cell cell, AbstractPieceView selectedPiece) {
        cell.setContextMenu(uiComponentsService.getPieceSelectorContextMenu(cell, selectedPiece.getColor(),
                e -> {
            executeDefaultStepLogic(game, cell, selectedPiece);
            String miId = e.getSource().getId().orElseThrow(() -> new IllegalArgumentException(ID_OF_MENU_ITEM_IS_NULL));
            gameService.mutatePiece(selectedPiece.getDbId(), PieceType.valueOf(miId));
            checkIsGameOver(game, cells); //TODO not working
            broadcasterService.getBroadcaster(game.getId()).broadcast(this.hashCode());
        }));
    }

    private void executeDefaultStepLogic(Game game, Cell cell,  AbstractPieceView selectedPiece) {
        Optional.ofNullable(selectedPiece.getPiecesToBeEaten().get(cell.getKey()))
                .ifPresent(f -> {
                    f.toDie();
                    gameService.killPiece(f.getDbId());
                });
        selectedPiece.placeAt(cell);
        gameService.saveStep(game.getId(), cell.getKey(),
                selectedPiece, true);
    }
}
