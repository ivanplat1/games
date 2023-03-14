package com.ivpl.games.converter;

import com.ivpl.games.entity.jpa.Piece;
import com.ivpl.games.entity.ui.*;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.ivpl.games.constants.PieceType.CHECKER;
import static com.ivpl.games.constants.PieceType.CHECKER_QUEEN;

@Service
public class PieceToPieceViewConverter {

    public PieceView convert(Piece source, Map<CellKey, Cell> cells) {
        if (CHECKER.equals(source.getType())) {
            return new CheckerView(source.getGamePieceId(), source.getId(),
                    source.getColor(),
                    source.getPosition() != null
                            ? cells.get(new CellKey(source.getPosition())) : null);
        } else if (CHECKER_QUEEN.equals(source.getType())) {
            return new QueenView(source.getGamePieceId(), source.getId(), source.getColor(),
                    source.getPosition() != null
                            ? cells.get(new CellKey(source.getPosition())) : null);
        }
        throw new IllegalArgumentException(String.format("Piece type %s is not exists!", source.getType()));
    }
}
