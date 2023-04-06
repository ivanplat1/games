package com.ivpl.games.services;

import com.ivpl.games.entity.BoardContainer;
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
    public BoardContainer reloadBoard(Long gameId, AbstractBoardView boardView) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(GAME_NOT_FOUND_BY_ID, gameId)));
        Map<CellKey, Cell> cells = new LinkedHashMap<>();
        VerticalLayout board = uiComponentsService.getChessBoard(cells);
        List<AbstractPieceView> pieces = new ArrayList<>(gameService.loadPieceViewsForGame(game.getId(), cells));
        // place pieces
        pieces.stream().filter(p -> p.getPosition() != null)
                .forEach(p -> {
                    p.getPosition().setPiece(p);
                    p.getSteps().addAll(stepRepository.findAllByGameIdAndPieceIdOrderByGameStepId(game.getId(), p.getPieceId()));
                });
        // calculate possible steps
        if (game.getTurn().equals(boardView.getPlayerColor()))
            pieces.stream()
                    .filter(p -> p.getPosition() != null && game.getTurn().equals(p.getColor()))
                    .forEach(p -> {
                        p.calculatePossibleSteps(cells, true);
                        addPieceListener(game, cells, p, boardView, p);
        });
        return new BoardContainer(board, pieces, cells);
    }

    protected void addPieceListener(Game game, Map<CellKey, Cell> cells, AbstractPieceView p, AbstractBoardView boardView, AbstractPieceView piece) {
        p.setOnClickListener(p.addClickListener(e -> {
            AtomicReference<AbstractPieceView> selectedPieceFromBoard = boardView.getSelectedPiece();
            if (!p.equals(selectedPieceFromBoard.get())) {
                if (selectedPieceFromBoard.get() != null) {
                    selectedPieceFromBoard.get().getPossibleSteps().forEach(k -> cells.get(k).removeSelectedStyle());
                    selectedPieceFromBoard.get().unselectPiece();
                }

                p.getPossibleSteps().forEach(k -> {
                    Cell cell = cells.get(k);
                    cell.addSelectedStyle();
                    addCellListener(game, cells, cell, piece);
                });
                selectedPieceFromBoard.set(p);
            } else {
                p.getPossibleSteps().forEach(k -> cells.get(k).removeSelectedStyle());
                selectedPieceFromBoard.set(null);
            }
        }));
    }

    protected abstract void addCellListener(Game game, Map<CellKey, Cell> cells, Cell cell, AbstractPieceView selectedPiece);
}
