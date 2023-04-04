package com.ivpl.games.view;

import com.ivpl.games.repository.GameRepository;
import com.ivpl.games.repository.StepRepository;
import com.ivpl.games.security.SecurityService;
import com.ivpl.games.services.CheckersBoardServiceImpl;
import com.ivpl.games.services.GameService;
import com.ivpl.games.services.UIComponentsService;
import com.ivpl.games.services.broadcasting.BroadcasterService;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.security.PermitAll;

@Route("checkers")
@PermitAll
public class CheckersBoardView extends AbstractBoardView {

    @Autowired
    public CheckersBoardView(UIComponentsService uiComponentsService,
                             BroadcasterService broadcasterService,
                             GameRepository gameRepository,
                             GameService gameService,
                             SecurityService securityService,
                             StepRepository stepRepository,
                             CheckersBoardServiceImpl boardService) {
        super(uiComponentsService, broadcasterService, gameRepository, gameService, securityService, stepRepository, boardService);
    }
}
