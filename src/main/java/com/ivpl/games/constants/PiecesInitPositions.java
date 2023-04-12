package com.ivpl.games.constants;

import java.util.AbstractMap;
import java.util.Map;

public class PiecesInitPositions {

    public static final Map<Long, Integer[]> checkersInitPositions = Map.ofEntries(
            new AbstractMap.SimpleEntry<>(1L, new Integer[]{2, 1}),
            new AbstractMap.SimpleEntry<>(2L, new Integer[]{4, 1}),
            new AbstractMap.SimpleEntry<>(3L, new Integer[]{6, 1}),
            new AbstractMap.SimpleEntry<>(4L, new Integer[]{8, 1}),
            new AbstractMap.SimpleEntry<>(5L, new Integer[]{1, 2}),
            new AbstractMap.SimpleEntry<>(6L, new Integer[]{3, 2}),
            new AbstractMap.SimpleEntry<>(7L, new Integer[]{5, 2}),
            new AbstractMap.SimpleEntry<>(8L, new Integer[]{7, 2}),
            new AbstractMap.SimpleEntry<>(9L, new Integer[]{2, 3}),
            new AbstractMap.SimpleEntry<>(10L, new Integer[]{4, 3}),
            new AbstractMap.SimpleEntry<>(11L, new Integer[]{6, 3}),
            new AbstractMap.SimpleEntry<>(12L, new Integer[]{8, 3}),
            new AbstractMap.SimpleEntry<>(13L, new Integer[]{1, 6}),
            new AbstractMap.SimpleEntry<>(14L, new Integer[]{3, 6}),
            new AbstractMap.SimpleEntry<>(15L, new Integer[]{5, 6}),
            new AbstractMap.SimpleEntry<>(16L, new Integer[]{7, 6}),
            new AbstractMap.SimpleEntry<>(17L, new Integer[]{2, 7}),
            new AbstractMap.SimpleEntry<>(18L, new Integer[]{4, 7}),
            new AbstractMap.SimpleEntry<>(19L, new Integer[]{6, 7}),
            new AbstractMap.SimpleEntry<>(20L, new Integer[]{8, 7}),
            new AbstractMap.SimpleEntry<>(21L, new Integer[]{1, 8}),
            new AbstractMap.SimpleEntry<>(22L, new Integer[]{3, 8}),
            new AbstractMap.SimpleEntry<>(23L, new Integer[]{5, 8}),
            new AbstractMap.SimpleEntry<>(24L, new Integer[]{7, 8})
    );

    public static final Map<Long, Integer[]> chessInitPositions = Map.ofEntries(
            new AbstractMap.SimpleEntry<>(1L, new Integer[]{1, 1}),
            new AbstractMap.SimpleEntry<>(2L, new Integer[]{2, 1}),
            new AbstractMap.SimpleEntry<>(3L, new Integer[]{3, 1}),
            new AbstractMap.SimpleEntry<>(4L, new Integer[]{4, 1}),
            new AbstractMap.SimpleEntry<>(5L, new Integer[]{5, 1}),
            new AbstractMap.SimpleEntry<>(6L, new Integer[]{6, 1}),
            new AbstractMap.SimpleEntry<>(7L, new Integer[]{7, 1}),
            new AbstractMap.SimpleEntry<>(8L, new Integer[]{8, 1}),
            new AbstractMap.SimpleEntry<>(9L, new Integer[]{1, 2}),
            new AbstractMap.SimpleEntry<>(10L, new Integer[]{2, 2}),
            new AbstractMap.SimpleEntry<>(11L, new Integer[]{3, 2}),
            new AbstractMap.SimpleEntry<>(12L, new Integer[]{4, 2}),
            new AbstractMap.SimpleEntry<>(13L, new Integer[]{5, 2}),
            new AbstractMap.SimpleEntry<>(14L, new Integer[]{6, 2}),
            new AbstractMap.SimpleEntry<>(15L, new Integer[]{7, 2}),
            new AbstractMap.SimpleEntry<>(16L, new Integer[]{8, 2}),
            new AbstractMap.SimpleEntry<>(17L, new Integer[]{1, 7}),
            new AbstractMap.SimpleEntry<>(18L, new Integer[]{2, 7}),
            new AbstractMap.SimpleEntry<>(19L, new Integer[]{3, 7}),
            new AbstractMap.SimpleEntry<>(20L, new Integer[]{4, 7}),
            new AbstractMap.SimpleEntry<>(21L, new Integer[]{5, 7}),
            new AbstractMap.SimpleEntry<>(22L, new Integer[]{6, 7}),
            new AbstractMap.SimpleEntry<>(23L, new Integer[]{7, 7}),
            new AbstractMap.SimpleEntry<>(24L, new Integer[]{8, 7}),
            new AbstractMap.SimpleEntry<>(25L, new Integer[]{1, 8}),
            new AbstractMap.SimpleEntry<>(26L, new Integer[]{2, 8}),
            new AbstractMap.SimpleEntry<>(27L, new Integer[]{3, 8}),
            new AbstractMap.SimpleEntry<>(28L, new Integer[]{4, 8}),
            new AbstractMap.SimpleEntry<>(29L, new Integer[]{5, 8}),
            new AbstractMap.SimpleEntry<>(30L, new Integer[]{6, 8}),
            new AbstractMap.SimpleEntry<>(31L, new Integer[]{7, 8}),
            new AbstractMap.SimpleEntry<>(32L, new Integer[]{8, 8})
    );

    public static final Map<Long, PieceType> idToTypeMapping = Map.ofEntries(
            new AbstractMap.SimpleEntry<>(1L, PieceType.ROOK),
            new AbstractMap.SimpleEntry<>(2L, PieceType.HORSE),
            new AbstractMap.SimpleEntry<>(3L, PieceType.BISHOP),
            new AbstractMap.SimpleEntry<>(4L, PieceType.QUEEN),
            new AbstractMap.SimpleEntry<>(5L, PieceType.KING),
            new AbstractMap.SimpleEntry<>(6L, PieceType.BISHOP),
            new AbstractMap.SimpleEntry<>(7L, PieceType.HORSE),
            new AbstractMap.SimpleEntry<>(8L, PieceType.ROOK),
            new AbstractMap.SimpleEntry<>(9L, PieceType.PAWN),
            new AbstractMap.SimpleEntry<>(10L, PieceType.PAWN),
            new AbstractMap.SimpleEntry<>(11L, PieceType.PAWN),
            new AbstractMap.SimpleEntry<>(12L, PieceType.PAWN),
            new AbstractMap.SimpleEntry<>(13L, PieceType.PAWN),
            new AbstractMap.SimpleEntry<>(14L, PieceType.PAWN),
            new AbstractMap.SimpleEntry<>(15L, PieceType.PAWN),
            new AbstractMap.SimpleEntry<>(16L, PieceType.PAWN),
            new AbstractMap.SimpleEntry<>(17L, PieceType.PAWN),
            new AbstractMap.SimpleEntry<>(18L, PieceType.PAWN),
            new AbstractMap.SimpleEntry<>(19L, PieceType.PAWN),
            new AbstractMap.SimpleEntry<>(20L, PieceType.PAWN),
            new AbstractMap.SimpleEntry<>(21L, PieceType.PAWN),
            new AbstractMap.SimpleEntry<>(22L, PieceType.PAWN),
            new AbstractMap.SimpleEntry<>(23L, PieceType.PAWN),
            new AbstractMap.SimpleEntry<>(24L, PieceType.PAWN),
            new AbstractMap.SimpleEntry<>(25L, PieceType.ROOK),
            new AbstractMap.SimpleEntry<>(26L, PieceType.HORSE),
            new AbstractMap.SimpleEntry<>(27L, PieceType.BISHOP),
            new AbstractMap.SimpleEntry<>(28L, PieceType.QUEEN),
            new AbstractMap.SimpleEntry<>(29L, PieceType.KING),
            new AbstractMap.SimpleEntry<>(30L, PieceType.BISHOP),
            new AbstractMap.SimpleEntry<>(31L, PieceType.HORSE),
            new AbstractMap.SimpleEntry<>(32L, PieceType.ROOK)
    );

    private PiecesInitPositions() {}
}
