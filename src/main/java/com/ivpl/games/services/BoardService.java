package com.ivpl.games.services;

import com.ivpl.games.entity.BoardContainer;
import com.ivpl.games.view.AbstractBoardView;

public interface BoardService {

    BoardContainer reloadBoard(Long gameId, AbstractBoardView boardView);
}
