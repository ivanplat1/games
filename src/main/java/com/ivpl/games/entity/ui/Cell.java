package com.ivpl.games.entity.ui;

import com.ivpl.games.constants.Color;
import com.vaadin.flow.component.html.Div;

import com.vaadin.flow.shared.Registration;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.Optional;

import static com.ivpl.games.constants.Constants.*;


@Getter
public class Cell extends Div {

    private final CellKey key;
    private final Color color;
    private PieceView piece = null;
    @Setter
    private Registration onClickListener;

    @NonNull
    public Cell(int x, int y, Color color) {
        super();
        this.key = new CellKey(x, y);
        this.color = color;

        if (Color.WHITE.equals(color)) {
            addClassName("white-cell");
        } else {
            addClassName("black-cell");
        }
    }

    public void setPiece(PieceView piece) {
        this.piece = piece;
        add(piece);
    }

    public void removePiece() {
        remove(piece);
        this.piece = null;
    }

    public void addSelectedStyle() {
        getStyle().set(FILTER_PROP, "brightness(0.50)");
    }

    public void removeSelectedStyle() {
        getStyle().remove(FILTER_PROP);
        Optional.ofNullable(onClickListener).ifPresent(Registration::remove);
    }

    public boolean isOccupied() {
        return piece != null;
    }
}
