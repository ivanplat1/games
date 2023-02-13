package com.ivpl.games.entity;

import com.ivpl.games.constants.Constants;
import com.vaadin.flow.component.html.Image;
import lombok.Getter;

public class Checker extends Image {

    @Getter
    public Constants.CheckerColor color;

    public Checker(Constants.CheckerColor color) {
        super(Constants.CheckerColor.WHITE.equals(color) ? Constants.WHITE_CHECKER_IMG : Constants.BLACK_CHECKER_IMG, Constants.CheckerColor.WHITE.equals(color) ? "whiteChecker" : "blackChecker");
        this.color = color;
    }
}
