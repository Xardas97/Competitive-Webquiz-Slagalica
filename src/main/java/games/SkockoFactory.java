package games;

import entities.ActiveGame;
import entities.GameOfTheDay;
import util.PreparationManager;
import util.Transaction;

public class SkockoFactory {
    public static Skocko create(GameOfTheDay game) {
        return new Skocko(game.getSecretCombo());
    }

    public static Skocko create(ActiveGame game, Transaction t) {
        return new Skocko(PreparationManager
                .generateSkocko(t, game.getBlue(), game.getRed(), true));
    }

    public static Skocko load(String username, Transaction t) {
        return new Skocko(PreparationManager.loadSkocko(t, username));
    }
}
