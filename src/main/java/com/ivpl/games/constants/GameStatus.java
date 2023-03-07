package com.ivpl.games.constants;

import lombok.Getter;

public enum GameStatus {
    SELECTING_COLOR("Player Is Selecting Color"),
    WAITING_FOR_OPPONENT("Waiting For Opponent"),
    IN_PROGRESS("In Progress"),
    FINISHED("Finished");


    @Getter
    String label;

    GameStatus(String label) {
        this.label=label;
    }
}
