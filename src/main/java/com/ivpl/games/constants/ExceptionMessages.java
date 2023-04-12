package com.ivpl.games.constants;

public final class ExceptionMessages {

    private ExceptionMessages() {}

    public static final String AUTHORIZATION_ERROR = "Authorization error.";
    public static final String PASSWORD_VALIDATION_ERROR = "Password validation error. {}";
    public static final String PIECE_NOT_FOUND_BY_ID = "Piece with Id %s is not exists.";
    public static final String GAME_NOT_FOUND_BY_ID = "Game with Id %s is was not found";
    public static final String POSITION_NOT_FOUND_FOR_PIECE = "Cannot resolve position for piece with ID %s";
    public static final String CANNOT_FIND_VIEW_FOR_PIECE = "Cannot find view for piece type %s";
    public static final String CANNOT_FIND_PIECE_TYPE_FOR_FOR_CLASS = "Cannot find piece type for class %s";
    public static final String DIRECTIONS_ARE_NOT_IMPLEMENTED = "Directions are not implemented for Item child class %s";
    public static final String PAGE_SHOULD_NOT_BE_AVAILABLE = "Page should not be available for unauthorized users.";
    public static final String VIEW_BOARD_IS_NOT_FOUND_FOR_GAME_TYPE = "View is not found for GameType %s.";
    public static final String KING_NOT_FOUND = "How have you eaten King???";
    public static final String COLOR_WAS_NOT_RECOGNIZED = "Color was not recognized";
    public static final String ID_OF_MENU_ITEM_IS_NULL = "Id of Menu Item is null";
}
