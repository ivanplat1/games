package com.ivpl.games.constants;

public final class ExceptionMessages {

    private ExceptionMessages() {}

    public static final String AUTHORIZATION_ERROR_ERROR = "Authorization error.";
    public static final String PASSWORD_VALIDATION_ERROR = "Password validation error. {}";
    public static final String PIECE_NOT_FOUND_BY_ID = "Piece with Id %s is not exists.";
    public static final String GAME_NOT_FOUND_BY_ID = "Game with Id %s is was not found";
    public static final String CANNOT_FIND_VIEW_FOR_PIECE = "Cannot find view for piece type %s";
    public static final String DIRECTIONS_ARE_NOT_IMPLEMENTED = "Directions are not implemented for Item child class %s";
}
