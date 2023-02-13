package com.ivpl.games.view;

import com.ivpl.games.components.BoardCell;
import com.ivpl.games.constants.Constants;
import com.ivpl.games.entity.Checker;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Route
public class ChessBoard extends VerticalLayout {

    private BoardCell selectedCell = null;
    private final BoardCell[][] cells = new BoardCell[8][8];

    public ChessBoard() {

        getStyle().set("background", "");

        VerticalLayout lines = new VerticalLayout();
        lines.setSpacing(false);
        HorizontalLayout line;

        int count=0;

        for(int x = 0; x < 8; ++x) {
            line = new HorizontalLayout();
            line.setSpacing(false);
            for(int y = 0; y < 8; ++y) {

                BoardCell newCell = new BoardCell(count, x, y);
                newCell.addClickListener(e -> {

                    //select cell
                    if (selectedCell == null && !newCell.isEmpty()) {
                        selectCell(newCell);
                    //unselect
                    } else if (selectedCell != null) {
                        if (selectedCell.equals(newCell)) {
                            unselectCell(newCell);
                        } else if (getPossibleSteps(selectedCell).contains(newCell)) {
                            doStep(newCell);
                        }
                    }
                });

                cells[x][y] = newCell;
                line.add(newCell);
                count++;
            }
            lines.add(line);
        }

        add(lines);
        placeCheckers();
    }

    private void placeCheckers() {

        for (int i = 5; i < 8; ++i) {
            for (BoardCell c : cells[i])
            {
                if (!c.isWhite()) {
                    c.createChecker(Constants.CheckerColor.WHITE);
                }
            }
        }

        for (int i = 0; i < 3; ++i) {
            for (BoardCell c : cells[i])
            {
                if (!c.isWhite()) {
                    c.createChecker(Constants.CheckerColor.BLACK);
                }
            }
        }
    }

    private void highlightPossibleSteps(BoardCell selectedCell) {
        getPossibleSteps(selectedCell).forEach(BoardCell::highlight);
    }

    private void unhighlightPossibleSteps(BoardCell selectedCell) {
        getPossibleSteps(selectedCell).forEach(BoardCell::unhighlight);
    }

    private List<BoardCell> getPossibleSteps(BoardCell selectedCell) {
        List<BoardCell> result = new ArrayList<>();
        Optional<Checker> checker = Optional.ofNullable(selectedCell.getChecker());

        if (checker.isPresent() && Constants.CheckerColor.BLACK.equals(checker.get().getColor())) {
            if (selectedCell.getX()+1 > 7) return result;

            for (BoardCell cell : cells[selectedCell.getX()+1]) {
                if (cell.isEmpty() && (selectedCell.getY() == cell.getY()-1 || selectedCell.getY() == cell.getY()+1)) {
                    result.add(cell);
                }
            }
        } else {
            if (selectedCell.getX()-1 < 0) return result;

            for (BoardCell cell : cells[selectedCell.getX()-1]) {
                if (cell.isEmpty() && (selectedCell.getY() == cell.getY()-1 || selectedCell.getY() == cell.getY()+1)) {
                    result.add(cell);
                }
            }
        }
        return result;
    }

    private void selectCell(BoardCell cell) {
        cell.highlight();
        selectedCell = cell;
        highlightPossibleSteps(cell);
    }

    private void unselectCell(BoardCell cell) {
        cell.getStyle().remove("filter");
        selectedCell = null;
        unhighlightPossibleSteps(cell);
    }

    private void doStep(BoardCell cell) {
        selectedCell.getChildren().findAny().ifPresent(cell::replaceChecker);
        cell.unhighlight();
        selectedCell.removeChecker();
        selectedCell.unhighlight();
        unhighlightPossibleSteps(selectedCell);
        selectedCell = null;
    }
}
