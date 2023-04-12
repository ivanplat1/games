package com.ivpl.games.entity.ui.chess;

import com.ivpl.games.constants.Color;
import com.ivpl.games.constants.PieceType;
import com.ivpl.games.entity.ui.Cell;
import com.ivpl.games.utils.DirectionsForClassRepo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class PawnView extends ChessPieceView {

    private static final String PAWN_FORWARD_DIRECTION_KEY = "[1,0]";

    public PawnView(Long pieceId, Long dbId, Color color, PieceType type, Cell position) {
        super(pieceId, dbId, color, type, position);
    }

    @Override
    protected Map<String, LinkedList<int[]>> getDirections() {
        Map<String, LinkedList<int[]>> directions = DirectionsForClassRepo.getDirectionsForType(getType());
        if (steps.isEmpty()) {
            Map<String, LinkedList<int[]>> directionsNew = new HashMap<>(directions);
            LinkedList<int[]> newDir = new LinkedList<>(directions.get(PAWN_FORWARD_DIRECTION_KEY));
            newDir.add(new int[]{2,0});
            directionsNew.put(PAWN_FORWARD_DIRECTION_KEY, newDir);
            directions = directionsNew;
        }
        return directions;
    }
}
