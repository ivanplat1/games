package com.ivpl.games.utils;

import com.ivpl.games.constants.Color;

import static com.ivpl.games.constants.Color.BLACK;
import static com.ivpl.games.constants.Color.WHITE;

public class CommonUtils {

    private CommonUtils() {}

    public static Color getOppositeColor(Color color) {
        return WHITE.equals(color) ? BLACK : WHITE;
    }
}
