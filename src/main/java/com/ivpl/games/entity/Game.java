package com.ivpl.games.entity;

import com.ivpl.games.constants.Color;
import com.ivpl.games.entity.jpa.Step;
import com.ivpl.games.entity.ui.AbstractPieceView;
import com.ivpl.games.entity.ui.Cell;
import com.ivpl.games.entity.ui.CellKey;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.Getter;

import java.util.*;

public class Game {

    private com.ivpl.games.entity.jpa.Game game;
    private Color playerColor;
    @Getter
    private Color currentTurn;
    @Getter
    private final Map<CellKey, Cell> cells = new LinkedHashMap<>();
    private final List<AbstractPieceView> pieces = new ArrayList<>();
    private boolean isAnythingEaten;
    private VerticalLayout board;
    private LinkedList<Step> steps;
}
