package games;

import entities.ActiveGame;
import entities.GameOfTheDay;
import util.PreparationManager;
import util.Transaction;

public class SlagalicaFactory {
    public static Slagalica create(GameOfTheDay game) {
        return new Slagalica(game.getLetters());
    }

    public static Slagalica create(ActiveGame game, Transaction t) {
        return new Slagalica(PreparationManager
                .generateSlagalica(t, game.getBlue(), game.getRed(), true));
    }

    public static Slagalica load(String username, Transaction t) {
        return new Slagalica(PreparationManager.loadSlagalica(t, username));
    }
}
