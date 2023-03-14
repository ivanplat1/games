package com.ivpl.games.converter;

import com.ivpl.games.constants.Color;
import com.ivpl.games.constants.PieceType;
import com.ivpl.games.entity.jpa.Piece;
import com.ivpl.games.entity.ui.*;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.Map;

import static com.ivpl.games.constants.PieceType.*;

@Service
public class PieceToPieceViewConverter {

    private static final Map<PieceType, Class> typeToViewClassMapping = Map.ofEntries(
            new AbstractMap.SimpleEntry<>(CHECKER, CheckerView.class),
            new AbstractMap.SimpleEntry<>(CHECKER_QUEEN, CheckerQueenView.class),
            new AbstractMap.SimpleEntry<>(PAWN, PawnView.class),
            new AbstractMap.SimpleEntry<>(ROOK, RookView.class),
            new AbstractMap.SimpleEntry<>(HORSE, HorseView.class),
            new AbstractMap.SimpleEntry<>(BISHOP, BishopView.class),
            new AbstractMap.SimpleEntry<>(QUEEN, QueenView.class),
            new AbstractMap.SimpleEntry<>(KING, KingView.class)
    );

    public PieceView convert(Piece source, Map<CellKey, Cell> cells) {
        try {
            return (PieceView) typeToViewClassMapping.get(source.getType())
                    .getDeclaredConstructor(Long.class, Long.class, Color.class, PieceType.class, Cell.class)
            .newInstance(source.getGamePieceId(), source.getId(),
                    source.getColor(),
                    source.getType(),
                    source.getPosition() != null
                            ? cells.get(new CellKey(source.getPosition())) : null);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}
