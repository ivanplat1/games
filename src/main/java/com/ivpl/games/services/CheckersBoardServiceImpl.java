package com.ivpl.games.services;

import com.ivpl.games.constants.Color;
import com.ivpl.games.constants.PieceType;
import com.ivpl.games.entity.BoardContainer;
import com.ivpl.games.entity.jpa.Game;
import com.ivpl.games.entity.ui.AbstractPieceView;
import com.ivpl.games.entity.ui.Cell;
import com.ivpl.games.entity.ui.CellKey;
import com.ivpl.games.entity.ui.checkers.CheckerQueenView;
import com.ivpl.games.repository.GameRepository;
import com.ivpl.games.repository.StepRepository;
import com.ivpl.games.services.broadcasting.BroadcasterService;
import com.ivpl.games.utils.CommonUtils;
import com.ivpl.games.view.AbstractBoardView;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.ivpl.games.constants.Color.BLACK;
import static com.ivpl.games.constants.Color.WHITE;
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
    public BoardContainer reloadBoard(Long gameId, AbstractBoardView boardView) {
        BoardContainer boardContainer = super.reloadBoard(gameId, boardView);
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(GAME_NOT_FOUND_BY_ID, gameId)));
        List<AbstractPieceView> pieces = boardContainer.getPieces();

        boolean haveToEatAnything = pieces.stream()
                .filter(f -> game.getTurn().equals(f.getColor()))
                .anyMatch(f -> !f.getPiecesToBeEaten().isEmpty());

        if (haveToEatAnything)
            pieces.stream().filter(p -> p.getPiecesToBeEaten().isEmpty())
                    .forEach(p -> p.getPossibleSteps().clear());

        return boardContainer;
    }

    @Override
    protected void addCellListener(Game game, Map<CellKey, Cell> cells, Cell cell, AbstractPieceView selectedPiece) {
        cell.clearListener();
        cell.setOnClickListener(cell.addClickListener(event -> {
            AtomicBoolean isAnythingEaten = new AtomicBoolean(false);
            Optional.ofNullable(selectedPiece.getPiecesToBeEaten().get(cell.getKey()))
                    .ifPresent(f -> {
                        f.toDie();
                        gameService.killPiece(f.getDbId());
                        isAnythingEaten.set(true);
                    });
            selectedPiece.placeAt(cell);
            replaceWithQueenIfNeeded(cell, selectedPiece);

            if (isAnythingEaten.get()) {
                selectedPiece.calculatePossibleSteps(cells, true);
                // if still have anything to eat
                if (!selectedPiece.getPiecesToBeEaten().isEmpty()) {
                    gameService.saveStep(game.getId(), cell.getKey(),
                            selectedPiece, false);
                    broadcasterService.getBroadcaster(game.getId()).broadcast(this.hashCode());
                    return;
                }
                isAnythingEaten.set(false);
            }
            gameService.saveStep(game.getId(), cell.getKey(),
                    selectedPiece, true);
            checkIsGameOver(game, cells);
            broadcasterService.getBroadcaster(game.getId()).broadcast(this.hashCode());
        }));
    }

    private void replaceWithQueenIfNeeded(Cell cell, AbstractPieceView piece) {
        if (CommonUtils.isBorderCell(cell.getKey(), piece.getColor())) {
            AbstractPieceView checkerQueenView = new CheckerQueenView(piece.getPieceId(), piece.getDbId(), piece.getColor(), PieceType.CHECKER_QUEEN, cell);
            cell.remove(piece);
            cell.setPiece(checkerQueenView);
            piece = checkerQueenView;
            gameService.mutatePiece(piece.getDbId(), PieceType.CHECKER_QUEEN);
        }
    }

    protected void checkIsGameOver(Game game, Map<CellKey, Cell> cells) {
        List<AbstractPieceView> pieces = CommonUtils.getPiecesFromCells(cells);
        pieces.stream().filter(p -> !game.getTurn().equals(p.getColor()))
                .forEach(p -> p.calculatePossibleSteps(cells, false));

        Map<Color, List<AbstractPieceView>> groupsByColor = pieces.stream()
                .filter(p -> p.getPosition() != null)
                .collect(Collectors.groupingBy(AbstractPieceView::getColor, Collectors.toList()));
        if (!groupsByColor.containsKey(WHITE)) {
            gameService.finishGame(game, BLACK);
        } else if (!groupsByColor.containsKey(BLACK)) {
            gameService.finishGame(game, WHITE);
        }
    }
}
