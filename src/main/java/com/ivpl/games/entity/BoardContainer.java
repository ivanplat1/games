package com.ivpl.games.entity;

import com.ivpl.games.entity.ui.AbstractPieceView;
import com.ivpl.games.entity.ui.Cell;
import com.ivpl.games.entity.ui.CellKey;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class BoardContainer {

    private final VerticalLayout boardLayout;
    private final List<AbstractPieceView> pieces;
    private final Map<CellKey, Cell> cells;


    public BoardContainer(VerticalLayout boardLayout, List<AbstractPieceView> pieces, Map<CellKey, Cell> cells) {
        this.boardLayout = boardLayout;
        this.pieces = pieces;
        this.cells = cells;
    }
}
