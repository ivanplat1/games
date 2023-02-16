package com.ivpl.games.entity;

import com.ivpl.games.constants.Color;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import lombok.Getter;
import lombok.NonNull;


@Getter
public class Cell extends Div {

    private final CellKey key;
    private final Color color;
    private Figure figure = null;

    @NonNull
    public Cell(int x, int y, Color color) {
        super();
        this.key = new CellKey(x, y);
        this.color = color;

        if (Color.WHITE.equals(color)) {
            getStyle().set("background", "#f5f5f5");
        } else {
            getStyle().set("background", "#3f4d62");
        }

        setHeight("77px");
        setWidth("77px");

        Label number = new Label(key.getY() + "" + key.getX());
        number.getStyle().set("position", "absolute").set("color", "red");
        add(number);
    }

    public void setFigure(Figure figure) {
        this.figure = figure;
        add(figure);
    }

    public void removeFigure() {
        remove(figure);
        this.figure = null;
    }
}
