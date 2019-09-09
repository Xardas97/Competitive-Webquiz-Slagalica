package games;

import entities.ActiveGame;
import entities.GameOfTheDay;
import util.PreparationManager;
import util.Transaction;

public class AsocijacijeFactory {
    public static Asocijacije create(GameOfTheDay game) {
        return new Asocijacije(game.getAsocijacija());
    }

    public static Asocijacije create(ActiveGame game, Transaction t) {
        return new Asocijacije(PreparationManager
                .generateAsocijacije(t, game.getBlue(), game.getRed()));
    }

    public static Asocijacije load(String username, Transaction t) {
        return new Asocijacije(PreparationManager.loadAsocijacije(t, username));
    }
}
