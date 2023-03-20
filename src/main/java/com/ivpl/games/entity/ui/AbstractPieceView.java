package com.ivpl.games.entity.ui;

import com.ivpl.games.constants.Color;
import com.ivpl.games.constants.PieceType;
import com.ivpl.games.constants.Styles;
import com.ivpl.games.entity.jpa.Step;
import com.ivpl.games.utils.DirectionsForClassRepo;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.shared.Registration;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.logging.log4j.util.Strings;

import java.util.*;

import static com.ivpl.games.constants.Styles.BRIGHTNESS_DARK;

@Getter
public abstract class AbstractPieceView extends Div {

    private final Long pieceId;
    private final Long dbId;
    protected final Color color;
    protected final PieceType type;
    @Getter
    protected boolean isAlive;
    @Getter
    protected Cell position;
    @Setter
    private Registration onClickListener;
    protected final  transient Map<CellKey, AbstractPieceView> piecesToBeEaten = new HashMap<>();
    protected final transient Set<CellKey> possibleSteps = new HashSet<>();
    private final LinkedList<Step> steps = new LinkedList<>();
    protected boolean shouldStopCalculationForDirection;
    private static final String PAWN_FORWARD_DIRECTION_KEY = "[1,0]";

    protected AbstractPieceView(Long pieceId, Long dbId, Color color, PieceType type, Cell position) {
        this.pieceId = pieceId;
        this.dbId = dbId;
        this.color = color;
        this.type = type;
        this.position = position;
        add(getImage());
    }

    public abstract void calculatePossibleSteps(Map<CellKey, Cell> cells);

    @NonNull
    public void placeAt(Cell targetCell) {
        position.removePiece();
        targetCell.setPiece(this);
        position = targetCell;
        unselectPiece();
    }

    protected abstract Image getImage();

    public void selectUnselectPiece() {
        Style style = getStyle();
        if (style.get(Styles.FILTER_PROP) == null) {
            selectPiece();
        } else {
            unselectPiece();
        }
    }

    public void selectPiece() {
        getStyle().set(Styles.FILTER_PROP, BRIGHTNESS_DARK);
    }

    public void unselectPiece() {
        getStyle().remove(Styles.FILTER_PROP);
    }

    protected Map<String, LinkedList<int[]>> getDirections() {
        Map<String, LinkedList<int[]>> directions = DirectionsForClassRepo.getDirectionsForType(getType());
        if (PieceType.PAWN.equals(getType()) && steps.isEmpty()) {
            Map<String, LinkedList<int[]>> directionsNew = new HashMap<>(directions);
            LinkedList<int[]> newDir = new LinkedList<>(directions.get(PAWN_FORWARD_DIRECTION_KEY));
            newDir.add(new int[]{2,0});
            directionsNew.put(PAWN_FORWARD_DIRECTION_KEY, newDir);
            return directionsNew;
        }
        return directions;
    }

    public void toDie() {
        position.removePiece();
        position = null;
        isAlive = false;
    }

    protected String calculateImageName() {
        return Strings.concat(getColor().name(), getClass().getSimpleName());
    }
}
