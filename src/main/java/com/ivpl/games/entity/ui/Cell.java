package com.ivpl.games.entity.ui;

import com.ivpl.games.constants.Color;
import com.ivpl.games.constants.Styles;
import com.vaadin.flow.component.html.Div;

import com.vaadin.flow.shared.Registration;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.Optional;

import static com.ivpl.games.constants.Styles.BRIGHTNESS_DARK;


@Getter
public class Cell extends Div {

    private static final String WHITE_CELL_STATUS = "white-cell";
    private static final String BLACK_CELL_STATUS = "black-cell";

    private final CellKey key;
    private final Color color;
    private AbstractPieceView piece = null;
    @Setter
    private Registration onClickListener;

    @NonNull
    public Cell(int x, int y, Color color) {
        super();
        this.key = new CellKey(x, y);
        this.color = color;

        if (Color.WHITE.equals(color)) {
            addClassName(WHITE_CELL_STATUS);
        } else {
            addClassName(BLACK_CELL_STATUS);
        }
    }

    public void setPiece(AbstractPieceView piece) {
        this.piece = piece;
        add(piece);
    }

    public void removePiece() {
        remove(piece);
        this.piece = null;
    }

    public void addSelectedStyle() {
        getStyle().set(Styles.FILTER_PROP, BRIGHTNESS_DARK);
    }

    public void removeSelectedStyle() {
        getStyle().remove(Styles.FILTER_PROP);
        Optional.ofNullable(onClickListener).ifPresent(Registration::remove);
        onClickListener = null;
    }

    public boolean isOccupied() {
        return piece != null;
    }
}
