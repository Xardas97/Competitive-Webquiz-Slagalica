package games;

import entities.ActiveGame;
import entities.GameOfTheDay;
import util.PreparationManager;
import util.Transaction;

public class MojBrojFactory {
    public static MojBroj create(GameOfTheDay game) {
        return new MojBroj(game.getNumbers());
    }

    public static MojBroj create(ActiveGame game, Transaction t) {
        return new MojBroj(PreparationManager
                .generateMojBroj(t, game.getBlue(), game.getRed(), true));
    }

    public static MojBroj load(String username, Transaction t) {
        return new MojBroj(PreparationManager.loadMojBroj(t, username));
    }
}
