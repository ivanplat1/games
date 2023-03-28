package com.ivpl.games.services;

import com.ivpl.games.constants.Color;
import com.ivpl.games.entity.ChessBoardContainer;
import com.ivpl.games.entity.ui.AbstractPieceView;
import com.ivpl.games.entity.ui.CellKey;

public interface BoardService {

    ChessBoardContainer reloadBoardFromDB(Long gameId, Color playerColor);

    void doStep(Long gameId, AbstractPieceView selectedPiece, CellKey cellKey);
}
