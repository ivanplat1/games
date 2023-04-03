package com.ivpl.games.entity.ui.chess;

import com.ivpl.games.constants.Color;
import com.ivpl.games.constants.PieceType;
import com.ivpl.games.entity.ui.Cell;

public class KingView extends ChessPieceView {

    public KingView(Long pieceId, Long dbId, Color color, PieceType type, Cell position) {
        super(pieceId, dbId, color, type, position);
    }
}
