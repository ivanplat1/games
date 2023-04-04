package com.ivpl.games.services;

import com.ivpl.games.constants.Color;
import com.ivpl.games.entity.ChessBoardContainer;
import com.ivpl.games.entity.jpa.Game;
import com.ivpl.games.entity.ui.AbstractPieceView;
import com.ivpl.games.entity.ui.Cell;
import com.ivpl.games.entity.ui.CellKey;
import com.ivpl.games.repository.GameRepository;
import com.ivpl.games.repository.StepRepository;
import com.ivpl.games.services.broadcasting.BroadcasterService;
import com.ivpl.games.view.AbstractBoardView;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.ivpl.games.constants.Color.WHITE;
import static com.ivpl.games.constants.ExceptionMessages.GAME_NOT_FOUND_BY_ID;

@Service
public abstract class AbstractBoardServiceImpl implements BoardService {

    protected final UIComponentsService uiComponentsService;
    protected final GameRepository gameRepository;
    protected final GameService gameService;
    protected final StepRepository stepRepository;
    protected final BroadcasterService broadcasterService;

    protected AbstractBoardServiceImpl(UIComponentsService uiComponentsService,
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
                    if (game.getTurn().equals(p.getColor())) addPieceListener(game, cells, p, boardView);
                });
        return new ChessBoardContainer(board, pieces, cells);
    }

    protected void addPieceListener(Game game, Map<CellKey, Cell> cells, AbstractPieceView p, AbstractBoardView boardView) {
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

    protected abstract void addCellListener(Game game, Map<CellKey, Cell> cells, Cell cell, AbstractBoardView boardView);

    protected boolean isBorderCell(CellKey cellKey, Color pieceColor) {
        return WHITE.equals(pieceColor) ? cellKey.getY() == 1 : cellKey.getY() == 8;
    }
}
