package com.ivpl.games.view;

import com.ivpl.games.constants.Color;
import com.ivpl.games.entity.ui.AbstractPieceView;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ivpl.games.constants.Color.BLACK;
import static com.ivpl.games.constants.Color.WHITE;

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

    @Override
    public void checkIsGameOver() {
        Map<Color, List<AbstractPieceView>> groupsByColor = pieces.stream()
                .filter(p -> p.getPosition() != null)
                .collect(Collectors.groupingBy(AbstractPieceView::getColor, Collectors.toList()));
        if (!groupsByColor.containsKey(WHITE) || !groupsByColor.containsKey(BLACK)) {
            gameService.finishGame(game, currentTurn);
            gameOver();
        }
    }
}
