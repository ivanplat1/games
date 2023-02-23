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
            addClassName("white-cell");
        } else {
            addClassName("black-cell");
        }

        setHeight("75px");
        setWidth("75px");
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
        getStyle().set(FILTER_PROP, "brightness(0.50)");
    }

    public void removeSelectedStyle() {
        getStyle().remove(FILTER_PROP);
        Optional.ofNullable(onClickListener).ifPresent(Registration::remove);
    }

    public boolean hasFigure() {
        return figure != null;
    }
}
