package com.ivpl.games.entity;

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
    private Figure figure = null;
    @Setter
    private Registration onClickListener;

    @NonNull
    public Cell(int x, int y, Color color) {
        super();
        this.key = new CellKey(x, y);
        this.color = color;

        if (Color.WHITE.equals(color)) {
            getStyle().set(BACKGROUND, WHITE_CELL_COLOR);
        } else {
            getStyle().set(BACKGROUND, BLACK_CELL_COLOR);
        }

        setHeight("77px");
        setWidth("77px");
    }

    public void setFigure(Figure figure) {
        this.figure = figure;
        add(figure);
    }

    public void removeFigure() {
        remove(figure);
        this.figure = null;
    }

    public void addSelectedStyle() {
        getStyle().set("filter", "brightness(0.50)");
    }

    public void removeSelectedStyle() {
        getStyle().remove("filter");
        Optional.ofNullable(onClickListener).ifPresent(Registration::remove);
    }

}
