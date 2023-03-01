package com.ivpl.games.constants;

import lombok.Getter;

public enum GameStatus {
    FINISHED("Finished"),
    IN_PROGRESS("In Progress"),
    WAITING_FOR_OPPONENT("Waiting For Opponent");

    @Getter
    String label;

    GameStatus(String label) {
        this.label=label;
    }
}
