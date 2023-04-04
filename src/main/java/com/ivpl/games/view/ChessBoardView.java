package com.ivpl.games.view;

import com.ivpl.games.repository.GameRepository;
import com.ivpl.games.repository.StepRepository;
import com.ivpl.games.security.SecurityService;
import com.ivpl.games.services.ChessBoardServiceImpl;
import com.ivpl.games.services.GameService;
import com.ivpl.games.services.UIComponentsService;
import com.ivpl.games.services.broadcasting.BroadcasterService;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.security.PermitAll;


@Route("chess")
@PermitAll
public class ChessBoardView extends AbstractBoardView {

    @Autowired
    public ChessBoardView(UIComponentsService uiComponentsService,
                          BroadcasterService broadcasterService,
                          GameRepository gameRepository,
                          GameService gameService,
                          SecurityService securityService,
                          StepRepository stepRepository,
                          ChessBoardServiceImpl boardService) {
        super(uiComponentsService, broadcasterService, gameRepository, gameService, securityService, stepRepository, boardService);
    }

    @Override
    public void checkIsGameOver() {

    }
}
