package com.ivpl.games.services;

import com.ivpl.games.constants.Color;
import com.ivpl.games.constants.ExceptionMessages;
import com.ivpl.games.constants.GameType;
import com.ivpl.games.security.SecurityService;
import com.ivpl.games.view.MainPage;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
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

import static com.ivpl.games.constants.Color.*;
import static com.ivpl.games.constants.Constants.*;

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
        VerticalLayout layout = new VerticalLayout(
                new Button(LOGOUT_STR, click -> securityService.logout()));
        layout.setAlignItems(FlexComponent.Alignment.END);
        layout.setSpacing(false);
        layout.setPadding(false);
        return layout;
    }

    public static Div getTurnIndicator(Color color) {
        Div indicator = new Div();
        indicator.addClassName("turnIndicator");
        indicator.setHeight("50px");
        indicator.setWidth("50px");
        switch(color) {
            case BLACK -> indicator.getStyle().set(BACKGROUND, BLACK_CELL_COLOR);
            case WHITE -> indicator.getStyle().set(BACKGROUND, WHITE_CELL_COLOR);
            case RANDOM -> indicator.getStyle().set(BACKGROUND, RANDOM_SELECTOR_BACKGROUND);
        }
        indicator.getStyle().set(BORDER_STYLE, "solid");
        return indicator;
    }

    public void showNewGameDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(COOSE_YOUR_COLOR_STR);
        Div random = UIComponentsService.getTurnIndicator(RANDOM);
        Icon ico = new Icon(VaadinIcon.QUESTION);
        random.add(ico);
        random.addClickListener(newGameDialogListener(dialog, RANDOM));
        Div black = UIComponentsService.getTurnIndicator(BLACK);
        black.addClickListener(newGameDialogListener(dialog, BLACK));
        Div white = UIComponentsService.getTurnIndicator(WHITE);
        white.addClickListener(newGameDialogListener(dialog, WHITE));
        HorizontalLayout hLayout = new HorizontalLayout(white, random, black);
        VerticalLayout dialogLayout = new VerticalLayout(hLayout);
        dialogLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        dialogLayout.setClassName("general-text");
        dialog.add(dialogLayout);
        UI.getCurrent().add(dialog);
        dialog.open();
    }

    private ComponentEventListener<ClickEvent<Div>> newGameDialogListener(Dialog dialog, Color color) {
        return e-> {
            try {
                dialog.close();
                gameService.newGame(securityService.getAuthenticatedUser(), color, GameType.CHECKERS);
            } catch (AuthenticationException authenticationException) {
                log.error(ExceptionMessages.AUTHORIZATION_ERROR_ERROR);
            }
        };
    }

    public void showGameNotFoundMessage() {
        Dialog dialog = new Dialog();
        Button mainPageBtn = new Button("Go To Lobby", e -> {
            UI.getCurrent().navigate(MainPage.class);
            dialog.close();
        });
        dialog.getFooter().add(mainPageBtn);
        dialog.add(new Text(GAME_NOT_FOUND_LABEL_STR));
        UI.getCurrent().add(dialog);
        dialog.open();
    }
}
