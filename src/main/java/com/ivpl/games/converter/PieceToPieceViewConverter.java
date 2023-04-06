package com.ivpl.games.converter;

import com.ivpl.games.constants.Color;
import com.ivpl.games.constants.PieceType;
import com.ivpl.games.entity.jpa.Piece;
import com.ivpl.games.entity.ui.*;
import com.ivpl.games.entity.ui.checkers.CheckerQueenView;
import com.ivpl.games.entity.ui.checkers.CheckerView;
import com.ivpl.games.entity.ui.chess.*;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;

import static com.ivpl.games.constants.ExceptionMessages.*;
import static com.ivpl.games.constants.PieceType.*;

@Service
public class PieceToPieceViewConverter {

    private static final Map<PieceType, Class<? extends AbstractPieceView>> typeToViewClassMapping = Map.ofEntries(
            new AbstractMap.SimpleEntry<>(CHECKER, CheckerView.class),
            new AbstractMap.SimpleEntry<>(CHECKER_QUEEN, CheckerQueenView.class),
            new AbstractMap.SimpleEntry<>(PAWN, PawnView.class),
            new AbstractMap.SimpleEntry<>(ROOK, RookView.class),
            new AbstractMap.SimpleEntry<>(HORSE, HorseView.class),
            new AbstractMap.SimpleEntry<>(BISHOP, BishopView.class),
            new AbstractMap.SimpleEntry<>(QUEEN, QueenView.class),
            new AbstractMap.SimpleEntry<>(KING, KingView.class)
    );

    public AbstractPieceView convert(Piece source, Map<CellKey, Cell> cells) {
        try {
            Cell position = source.getPosition() != null
                    ? Optional.ofNullable(cells.get(new CellKey(source.getPosition())))
                    .orElseThrow(() -> new IllegalArgumentException(String.format(POSITION_NOT_FOUND_FOR_PIECE, source.getId())))
                    : null;
            return typeToViewClassMapping.get(source.getType())
                    .getDeclaredConstructor(Long.class, Long.class, Color.class, PieceType.class, Cell.class)
            .newInstance(source.getGamePieceId(), source.getId(), source.getColor(), source.getType(),
                    position);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(String.format(CANNOT_FIND_VIEW_FOR_PIECE, source.getType()));
        }
    }

    public static PieceType getTypeByClass(Class<? extends AbstractPieceView> pieceViewClass) {
        return typeToViewClassMapping.entrySet().stream()
                .filter(e -> e.getValue().equals(pieceViewClass))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format(CANNOT_FIND_PIECE_TYPE_FOR_FOR_CLASS, pieceViewClass)));
    }
}
