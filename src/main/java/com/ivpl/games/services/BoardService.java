package com.ivpl.games.services;

import com.ivpl.games.entity.ui.AbstractPieceView;
import com.ivpl.games.entity.ui.CellKey;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public interface BoardService {

    VerticalLayout reloadBoardFromDB(Long gameId);

    void doStep(Long gameId, AbstractPieceView selectedPiece, CellKey cellKey);
}
