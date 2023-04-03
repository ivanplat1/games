package com.ivpl.games.services;

import com.ivpl.games.constants.Color;
import com.ivpl.games.constants.GameType;
import com.ivpl.games.constants.PieceType;
import com.ivpl.games.entity.ChessBoardContainer;
import com.ivpl.games.entity.jpa.Game;
import com.ivpl.games.entity.ui.AbstractPieceView;
import com.ivpl.games.entity.ui.Cell;
import com.ivpl.games.entity.ui.CellKey;
import com.ivpl.games.entity.ui.checkers.CheckerQueenView;
import com.ivpl.games.entity.ui.chess.RookView;
import com.ivpl.games.repository.GameRepository;
import com.ivpl.games.repository.StepRepository;
import com.ivpl.games.services.broadcasting.BroadcasterService;
import com.ivpl.games.view.AbstractBoardView;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apache.commons.lang3.Range;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.ivpl.games.constants.Color.WHITE;
import static com.ivpl.games.constants.ExceptionMessages.GAME_NOT_FOUND_BY_ID;

@Service
public class BoardServiceImpl implements BoardService {

    private final UIComponentsService uiComponentsService;
    private final GameRepository gameRepository;
    private final GameService gameService;
    private final StepRepository stepRepository;
    private final BroadcasterService broadcasterService;

    public BoardServiceImpl(UIComponentsService uiComponentsService,
                            GameRepository gameRepository,
                            GameService gameService,
                            StepRepository stepRepository, BroadcasterService broadcasterService) {
        this.uiComponentsService = uiComponentsService;
        this.gameRepository = gameRepository;
        this.gameService = gameService;
        this.stepRepository = stepRepository;
        this.broadcasterService = broadcasterService;
    }

    @Override
    public ChessBoardContainer reloadBoard(Long gameId, AbstractBoardView boardView) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(GAME_NOT_FOUND_BY_ID, gameId)));
        Map<CellKey, Cell> cells = new LinkedHashMap<>();
        VerticalLayout board = uiComponentsService.getChessBoard(cells);
        List<AbstractPieceView> pieces = new ArrayList<>(gameService.loadPieceViewsForGame(game.getId(), cells));
        pieces.stream().filter(p -> p.getPosition() != null)
                .forEach(p -> {
                    p.getPosition().setPiece(p);
                    p.getSteps().clear();
                    p.getSteps().addAll(stepRepository.findAllByGameIdAndPieceIdOrderByGameStepId(game.getId(), p.getPieceId()));
                    addPieceListener(game, cells, p, boardView);
                });

        // calculate possible steps for pieces
        if (game.getTurn().equals(boardView.getPlayerColor())) {
            pieces.stream()
                    .filter(p -> p.getPosition() != null && game.getTurn().equals(p.getColor()))
                    .forEach(p -> {
                        p.calculatePossibleSteps(cells, true);
                        if (PieceType.KING.equals(p.getType())) {
                            calculateCastlingSteps(game, p, cells);
                        }
                    });

            boolean haveToEatAnything = GameType.CHECKERS.equals(game.getType()) && pieces.stream()
                    .filter(f -> game.getTurn().equals(f.getColor()))
                    .anyMatch(f -> !f.getPiecesToBeEaten().isEmpty());

            if (haveToEatAnything)
                pieces.stream().filter(f -> f.getPiecesToBeEaten().isEmpty())
                        .forEach(f -> f.getPossibleSteps().clear());
        }
        return new ChessBoardContainer(board, pieces, cells);
    }

    private void addPieceListener(Game game, Map<CellKey, Cell> cells, AbstractPieceView p, AbstractBoardView boardView) {
        p.setOnClickListener(p.addClickListener(e -> {
            AtomicReference<AbstractPieceView> selectedPiece = boardView.getSelectedPiece();
            if (!p.equals(boardView.getSelectedPiece().get())) {
                if (selectedPiece.get() != null) {
                    selectedPiece.get().getPossibleSteps().forEach(k -> cells.get(k).removeSelectedStyle());
                    selectedPiece.get().unselectPiece();
                }

                p.getPossibleSteps().forEach(k -> {
                    Cell cell = cells.get(k);
                    cell.addSelectedStyle();
                    addCellListener(game, cells, cell, boardView);
                });
                selectedPiece.set(p);
            } else {
                p.getPossibleSteps().forEach(k -> cells.get(k).removeSelectedStyle());
                selectedPiece.set(null);
            }
        }));
    }

    private void addCellListener(Game game, Map<CellKey, Cell> cells, Cell cell, AbstractBoardView boardView) {
        if (cell.getOnClickListener() != null) return;
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
            if (GameType.CHECKERS.equals(game.getType())) replaceWithQueenIfNeeded(cell, selectedPiece);

            if (GameType.CHECKERS.equals(game.getType()) && isAnythingEaten.get()) {
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

    private boolean isBorderCell(CellKey cellKey, Color pieceColor) {
        return WHITE.equals(pieceColor) ? cellKey.getY() == 1 : cellKey.getY() == 8;
    }

    private void calculateCastlingSteps(Game game, AbstractPieceView kingPiece, Map<CellKey, Cell> cells) {
        if (kingPiece.getSteps().isEmpty()) {
            cells.values().stream().filter(Cell::isOccupied)
                    .map(Cell::getPiece)
                    .filter(p -> kingPiece.getColor().equals(p.getColor()) && PieceType.ROOK.equals(p.getType()))
                    .filter(rook -> rook.getSteps().isEmpty())
                    .map(p -> (RookView) p)
                    .filter(rook -> isRookCastlingPossible(cells, rook))
                    .forEach(rook -> {
                        int x = rook.getPosition().getKey().getX();
                        int y = rook.getPosition().getKey().getY();
                        Cell cell = cells.get(new CellKey(x == 1 ? 3 : 7, y));
                        cell.setOnClickListener(cell.addClickListener(event -> {
                            // move king
                            gameService.saveStep(game.getId(), cell.getKey(),
                                    kingPiece, false);
                            kingPiece.getPossibleSteps().add(cell.getKey());
                            // move rook
                            gameService.saveStep(game.getId(), new CellKey(x == 1 ? 4 : 6, y),
                                    rook, true);
                            broadcasterService.getBroadcaster(game.getId()).broadcast(this.hashCode());
                        }));
                        kingPiece.getPossibleSteps().add(cell.getKey());
                    });
        }
    }

    private boolean isRookCastlingPossible(Map<CellKey, Cell> cells, RookView rook) {
        int x = rook.getPosition().getKey().getX();
        int y = rook.getPosition().getKey().getY();
        return cells.entrySet().stream()
                .filter(e -> e.getKey().getY() == y &&
                        ((x == 1 && Range.between(2, 4).contains(e.getKey().getX()))
                                || (x == 8 && Range.between(6, 7).contains(e.getKey().getX())))
                )
                .map(Map.Entry::getValue)
                .noneMatch(Cell::isOccupied);
    }
}
