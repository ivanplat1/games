package com.ivpl.games.services;

import com.ivpl.games.constants.*;
import com.ivpl.games.converter.PieceToPieceViewConverter;
import com.ivpl.games.entity.ui.Cell;
import com.ivpl.games.entity.ui.CellKey;
import com.ivpl.games.entity.ui.chess.*;
import com.ivpl.games.security.SecurityService;
import com.ivpl.games.utils.CommonUtils;
import com.ivpl.games.view.MainPage;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apache.tomcat.websocket.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

import static com.ivpl.games.constants.Color.*;
import static com.ivpl.games.constants.Constants.*;
import static com.ivpl.games.constants.ExceptionMessages.COLOR_WAS_NOT_RECOGNIZED;
import static com.ivpl.games.constants.Styles.*;

@Component
public class UIComponentsService {

    Logger log = LoggerFactory.getLogger(UIComponentsService.class);

    SecurityService securityService;
    GameService gameService;

    @Autowired
    public UIComponentsService(SecurityService securityService, GameService gameService) {
        this.securityService = securityService;
        this.gameService = gameService;
    }

    public VerticalLayout getHeader() {
        VerticalLayout layout = new VerticalLayout(getLogoutButton());
        layout.setAlignItems(FlexComponent.Alignment.END);
        layout.setSpacing(false);
        layout.setPadding(false);
        return layout;
    }

    public VerticalLayout getHeaderWithGoToLobby() {
        HorizontalLayout hl = new HorizontalLayout(getGoToLobbyButton(), getLogoutButton());
        VerticalLayout layout = new VerticalLayout(hl);
        layout.setAlignItems(FlexComponent.Alignment.END);
        layout.setSpacing(false);
        layout.setPadding(false);
        return layout;
    }

    private Button getLogoutButton() {
        return new Button(LOGOUT_STR, new Icon(VaadinIcon.EXIT_O), click -> securityService.logout());
    }

    public Div getTurnIndicator(Color color) {
        Div indicator = getFixedBoxWithBorder();
        switch(color) {
            case BLACK -> indicator.getStyle().set(BACKGROUND, BLACK_CELL_COLOR);
            case WHITE -> indicator.getStyle().set(BACKGROUND, WHITE_CELL_COLOR);
            case RANDOM -> indicator.getStyle().set(BACKGROUND, RANDOM_SELECTOR_BACKGROUND);
            default -> throw new IllegalArgumentException(COLOR_WAS_NOT_RECOGNIZED);
        }
        indicator.getStyle().set(BORDER_STYLE, "solid");
        return indicator;
    }

    public void showNewGameDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(CHOSE_YOUR_COLOR_STR);
        HorizontalLayout colorHL = getColorSelectorLayout();
        HorizontalLayout typeHL = getGameTypeSelectorLayout();
        VerticalLayout dialogLayout = new VerticalLayout(colorHL, typeHL);
        dialogLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        dialogLayout.setClassName("general-text");
        dialog.add(dialogLayout);
        Button newGameBtn = new Button(START_STR, newGameDialogListener(dialog, colorHL, typeHL));
        newGameBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(newGameBtn);
        UI.getCurrent().add(dialog);
        dialog.open();
    }

    private ComponentEventListener<ClickEvent<Button>> newGameDialogListener(Dialog dialog, HorizontalLayout colorHl, HorizontalLayout typeHl) {
        return e-> {
            try {
                Color color = colorHl.getChildren()
                        .filter(Div.class::isInstance).map(d -> (Div) d)
                        .filter(d -> d.getStyle().has(BORDER_COLOR))
                        .map(Div::getId).filter(Optional::isPresent)
                        .map(id -> Color.valueOf(id.get()))
                        .findFirst().orElse(RANDOM);
                GameType type = typeHl.getChildren()
                        .filter(Div.class::isInstance).map(d -> (Div) d)
                        .filter(d -> d.getStyle().has(BORDER_COLOR))
                        .map(Div::getId).filter(Optional::isPresent)
                        .map(id -> GameType.valueOf(id.get()))
                        .findFirst().orElse(GameType.CHESS);
                dialog.close();
                gameService.newGame(securityService.getAuthenticatedUser(), color, type);
            } catch (AuthenticationException authenticationException) {
                log.error(ExceptionMessages.AUTHORIZATION_ERROR);
            }
        };
    }

    private ComponentEventListener<ClickEvent<Div>> colorSelectorDialogListener(HorizontalLayout hl) {
        return e-> {
            hl.getChildren().filter(Div.class::isInstance)
                    .map(c -> (Div) c)
                    .forEach(div -> div.getStyle().remove(BORDER_COLOR));
            e.getSource().getStyle().set(BORDER_COLOR, GREEN);
        };
    }

    public void showGameNotFoundMessage() {
        Dialog dialog = new Dialog();
        Button mainPageBtn = getGoToLobbyButtonForDialog(dialog);
        dialog.getFooter().add(mainPageBtn);
        dialog.add(new Text(GAME_NOT_FOUND_LABEL_STR));
        UI.getCurrent().add(dialog);
        dialog.open();
    }

    public Button getGoToLobbyButtonForDialog(Dialog dialog) {
        return new Button(GO_TO_LOBBY_STR, e -> {
            UI.getCurrent().navigate(MainPage.class);
            dialog.close();
        });
    }

    public Button getGoToLobbyButton() {
        return new Button(GO_TO_LOBBY_STR, e -> UI.getCurrent().navigate(MainPage.class));
    }

    private HorizontalLayout getColorSelectorLayout() {
        HorizontalLayout layout = new HorizontalLayout();
        Div random = getTurnIndicator(RANDOM);
        random.setId(RANDOM.name());
        Icon ico = new Icon(VaadinIcon.QUESTION);
        random.add(ico);
        random.addClickListener(colorSelectorDialogListener(layout));
        Div black = getTurnIndicator(BLACK);
        black.setId(BLACK.name());
        black.addClickListener(colorSelectorDialogListener(layout));
        Div white = getTurnIndicator(WHITE);
        white.setId(WHITE.name());
        white.addClickListener(colorSelectorDialogListener(layout));
        layout.add(white, random, black);
        return layout;
    }

    private HorizontalLayout getGameTypeSelectorLayout() {
        HorizontalLayout layout = new HorizontalLayout();
        Div chess = getFixedBoxWithBorder();
        chess.add(new Image("images/chess/WHITEQueenView.png", "chessIco"));
        chess.setId(GameType.CHESS.name());
        chess.addClickListener(colorSelectorDialogListener(layout));
        Div checkers = getFixedBoxWithBorder();
        checkers.add(new Image("images/checkers/WHITECheckerView.png", "checkersIco"));
        checkers.setId(GameType.CHECKERS.name());
        checkers.addClickListener(colorSelectorDialogListener(layout));
        layout.add(chess, checkers);
        return layout;
    }

    private static Div getFixedBoxWithBorder() {
        Div box = new Div();
        box.addClassName("box-with-border");
        box.setHeight("50px");
        box.setWidth("50px");
        return box;
    }

    public VerticalLayout getChessBoard(Map<CellKey, Cell> cells) {
        cells.clear();
        VerticalLayout board = new VerticalLayout();
        board.setSpacing(false);
        board.addClassName(Styles.CHESS_BOARD_WHITES_STYLE);
        board.setPadding(false);
        HorizontalLayout line;

        for (int y = 1; y < 9; ++y) {
            line = new HorizontalLayout();
            line.setSpacing(false);

            for (int x = 1; x < 9; ++x) {
                Cell cell = new Cell(x, y, (x+y) % 2 == 0 ? WHITE : BLACK);
                line.add(cell);
                cells.put(cell.getKey(), cell);
            }
            board.add(line);
        }
        return board;
    }

    public ContextMenu getPieceSelectorContextMenu(Cell cell, Color color, ComponentEventListener<ClickEvent<MenuItem>> action) {
        ContextMenu menu = new ContextMenu();
        addMenuItemForPieceSelector(menu, color, QueenView.class, action);
        addMenuItemForPieceSelector(menu, color, HorseView.class, action);
        addMenuItemForPieceSelector(menu, color, RookView.class, action);
        addMenuItemForPieceSelector(menu, color, BishopView.class, action);
        menu.setOpenOnClick(true);
        menu.setTarget(cell);
        return menu;
    }

    public void addMenuItemForPieceSelector(ContextMenu menu, Color pieceColor, Class<? extends ChessPieceView> clazz, ComponentEventListener<ClickEvent<MenuItem>> action) {
        Div div = getTurnIndicator(CommonUtils.getOppositeColor(pieceColor));
        div.add(new Image(String.format(ChessPieceView.IMAGE_PATH_STR, CommonUtils.calculateImageName(pieceColor, clazz)), PIECE_IMAGE_ALT));
        menu.addItem(div, action).setId(PieceToPieceViewConverter.getTypeByClass(clazz).name());
    }
}
