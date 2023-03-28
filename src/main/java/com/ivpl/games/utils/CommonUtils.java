package com.ivpl.games.utils;

import com.ivpl.games.constants.Color;
import com.ivpl.games.constants.GameType;
import com.ivpl.games.view.AbstractBoardView;
import com.ivpl.games.view.CheckersBoardView;
import com.ivpl.games.view.ChessBoardView;

import static com.ivpl.games.constants.Color.BLACK;
import static com.ivpl.games.constants.Color.WHITE;
import static com.ivpl.games.constants.ExceptionMessages.POSITION_NOT_FOUND_FOR_PIECE;

public class CommonUtils {

    private CommonUtils() {}

    public static Color getOppositeColor(Color color) {
        return WHITE.equals(color) ? BLACK : WHITE;
    }

    public static Class<? extends AbstractBoardView> getViewForGameType(GameType gameType) {
        return switch (gameType.ordinal()) {
            case 0 -> CheckersBoardView.class;
            case 1 -> ChessBoardView.class;
            default -> throw new IllegalArgumentException(String.format(POSITION_NOT_FOUND_FOR_PIECE, gameType.name()));
        };
    }
}
