package com.ivpl.games.services;

import com.ivpl.games.constants.Color;
import com.ivpl.games.security.SecurityService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.ivpl.games.constants.Color.WHITE;
import static com.ivpl.games.constants.Constants.*;

@Component
public class UIComponentsService {

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
        indicator.getStyle().set(BACKGROUND, WHITE.equals(color) ? WHITE_CELL_COLOR : BLACK_CELL_COLOR);
        indicator.getStyle().set(BORDER_STYLE, "solid");
        return indicator;
    }
}
