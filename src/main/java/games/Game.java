package games;

import controllers.GameController.GameView;
import entities.GameVariables;
import util.Transaction;

public interface Game {
    int DEFAULT_GAME_LENGTH = 60;
    int getPoints();
    GameVariables getMyVars(Transaction t, String username);
    GameView getNextView();
    GameView getMyView();
    default int getGameLength() {
        return DEFAULT_GAME_LENGTH;
    }
}
