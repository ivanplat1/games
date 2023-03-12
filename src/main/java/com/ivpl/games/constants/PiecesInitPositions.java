package com.ivpl.games.constants;

import java.util.HashMap;
import java.util.Map;

public class PiecesInitPositions {

    private PiecesInitPositions() {}

    public static final Map<Long, Integer[]> checkersInitPositions = new HashMap<>();

    static {
        checkersInitPositions.put(1L, new Integer[]{2, 1});
        checkersInitPositions.put(2L, new Integer[]{4, 1});
        checkersInitPositions.put(3L, new Integer[]{6, 1});
        checkersInitPositions.put(4L, new Integer[]{8, 1});
        checkersInitPositions.put(5L, new Integer[]{1, 2});
        checkersInitPositions.put(6L, new Integer[]{3, 2});
        checkersInitPositions.put(7L, new Integer[]{5, 2});
        checkersInitPositions.put(8L, new Integer[]{7, 2});
        checkersInitPositions.put(9L, new Integer[]{2, 3});
        checkersInitPositions.put(10L, new Integer[]{4, 3});
        checkersInitPositions.put(11L, new Integer[]{6, 3});
        checkersInitPositions.put(12L, new Integer[]{8, 3});
        checkersInitPositions.put(13L, new Integer[]{1, 6});
        checkersInitPositions.put(14L, new Integer[]{3, 6});
        checkersInitPositions.put(15L, new Integer[]{5, 6});
        checkersInitPositions.put(16L, new Integer[]{7, 6});
        checkersInitPositions.put(17L, new Integer[]{2, 7});
        checkersInitPositions.put(18L, new Integer[]{4, 7});
        checkersInitPositions.put(19L, new Integer[]{6, 7});
        checkersInitPositions.put(20L, new Integer[]{8, 7});
        checkersInitPositions.put(21L, new Integer[]{1, 8});
        checkersInitPositions.put(22L, new Integer[]{3, 8});
        checkersInitPositions.put(23L, new Integer[]{5, 8});
        checkersInitPositions.put(24L, new Integer[]{7, 8});
    }
}
