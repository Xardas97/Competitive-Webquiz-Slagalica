package games;

import entities.ActiveGame;
import entities.GameOfTheDay;
import util.PreparationManager;
import util.Transaction;

public class SpojniceFactory {
    public static Spojnice create(GameOfTheDay game) {
        Spojnice spojnice = new Spojnice(game.getPairs());
        PreparationManager.createSpojniceWordAndPositionArrays(game.getPairs().getPairs().split("-"), spojnice);
        return spojnice;
    }

    public static Spojnice create(ActiveGame game, Transaction t) {
        Spojnice spojnice = new Spojnice();
        PreparationManager.generateSpojnice(t, game.getBlue(), game.getRed(), spojnice);
        return spojnice;
    }

    public static Spojnice load(String username, Transaction t) {
        Spojnice spojnice = new Spojnice();
        PreparationManager.loadSpojnice(t, username, spojnice);
        return spojnice;
    }
}
