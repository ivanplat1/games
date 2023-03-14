package com.ivpl.games.constants;


public final class Constants {

    private Constants() {
    }

    public static final String WHITE_CELL_COLOR = "rgb(226 213 202)";
    public static final String BLACK_CELL_COLOR = "rgb(50, 49, 49)";
    public static final String RANDOM_SELECTOR_BACKGROUND = "-webkit-linear-gradient(-30deg, rgb(226, 213, 202) 50%, rgb(50, 49, 49) 50%)";

    public static final String BACKGROUND = "background";
    public static final String BORDER_STYLE = "border-style";
    public static final String FILTER_PROP = "filter";
    public static final String LOGOUT_STR = "Logout";
    public static final String JOIN_GAME_OR_START_NEW_STR = "Choose a game to fight or start a new one";
    public static final String NEW_GAME_STR = "New Game";
    public static final String COOSE_YOUR_COLOR_STR = "Chose Your Color";
    public static final String GAME_NOT_FOUND_LABEL_STR = "Game not found or finished.";
    public static final String GO_TO_LOBBY_STR = "Go To Lobby";

    public static final String ACTIVE_GAMES_HEADER_ID_STR = "ID";
    public static final String ACTIVE_GAMES_HEADER_PLAYERS_STR = "Players";
    public static final String ACTIVE_GAMES_HEADER_GAME_STATUS_STR = "Game Status";

    public static final String PLAYERS_CELL_HTML =
            "<vaadin-horizontal-layout style=\"align-items: center;\" theme=\"spacing\">"
            + "<vaadin-vertical-layout style=\"line-height: var(--lumo-line-height-m);\">"
            + "  <span> ${item.nick} </span>"
            + "    <span style=\"font-size: var(--lumo-font-size-s); color: var(--lumo-secondary-text-color);\">"
            + "      ${item.user}" + "    </span>"
            + "  </vaadin-vertical-layout>"
            + "<vaadin-vertical-layout style=\"line-height: var(--lumo-line-height-m);\">"
            + "  <span> ${item.nick2} </span>"
            + "    <span style=\"font-size: var(--lumo-font-size-s); color: var(--lumo-secondary-text-color);\">"
            + "      ${item.secondUser}" + "    </span>"
            + "  </vaadin-vertical-layout>"
            + "</vaadin-horizontal-layout>";

    public static final String GAME_ID_CELL_HTML =
            "<vaadin-horizontal-layout style=\"align-items: center;\" theme=\"spacing\">"
                    + "<vaadin-vertical-layout style=\"line-height: var(--lumo-line-height-m);\">"
                    + "  <span> Game ${item.gameId} </span>"
                    + "    <span style=\"font-size: var(--lumo-font-size-s); color: var(--lumo-secondary-text-color);\">"
                    + "      ${item.startDate}" + "    </span>"
                    + "  </vaadin-vertical-layout>"
                    + "</vaadin-horizontal-layout>";
}
