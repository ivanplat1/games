package com.ivpl.games.services;

import com.ivpl.games.entity.ChessBoardContainer;
import com.ivpl.games.view.AbstractBoardView;

public interface BoardService {

    ChessBoardContainer reloadBoard(Long gameId, AbstractBoardView boardView);
}
