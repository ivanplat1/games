package com.ivpl.games.components;

import com.ivpl.games.constants.Constants;
import com.ivpl.games.entity.Checker;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import lombok.Getter;

import java.util.Optional;

public class BoardCell extends Div {

    private final int id;
    @Getter
    private final int x;
    @Getter
    private final int y;
    @Getter
    private final boolean isWhite;

    public BoardCell(int id, int x, int y) {
        super();
        this.id = id;
        this.x = x;
        this.y = y;
        this.isWhite = (x + y) % 2 == 0;

        setHeight("77px");
        setWidth("77px");

        if (isWhite) {
            getStyle().set("background", "#f5f5f5");
        } else {
            getStyle().set("background", "#3f4d62");
        }
    }

    public interface OnClickHandler {
        void onClick();
    }

    public boolean isEmpty() {
        return getComponentCount() == 0;
    }

    public Checker getChecker() {
        return (Checker) getChildren().findAny().orElse(null);
    }

    public void replaceChecker(Component checker) {
        removeChecker();
        add(checker);
    }

    public void createChecker(Constants.CheckerColor color) {
        add(new Checker(color));
    }

    public void removeChecker() {
        removeAll();
    }

    public void highlight() {
        getStyle().set("filter", "brightness(0.80)");
    }

    public void unhighlight() {
        getStyle().remove("filter");
    }
}
