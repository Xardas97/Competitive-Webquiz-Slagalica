package games;

import entities.ActiveGame;
import entities.GameOfTheDay;
import entities.SpojniceVariables;
import entities.WordPairs;
import games.generators.SpojniceGenerator;
import services.ActiveGameService;
import util.Transaction;

public class SpojniceFactory {
    public static Spojnice create(GameOfTheDay game) {
        WordPairs wordPairs = game.getPairs();

        String[][] pairs = wordPairs.getPairsAsArrays();
        int[] pairPosition = SpojniceGenerator.createRandomPositionsArray();
        String gameName = wordPairs.getText();

        return new Spojnice(pairs, pairPosition, gameName);
    }

    public static Spojnice create(ActiveGame game, Transaction transaction) {
        SpojniceVariables vars = SpojniceGenerator.generate(transaction, game.getBlue(), game.getRed());

        String[][] pairs = vars.getPairs().getPairsAsArrays();
        int[] pairPosition = vars.getPairPositionAsArray();
        String gameName = vars.getPairs().getText();

        return new Spojnice(pairs, pairPosition, gameName);
    }

    public static Spojnice load(String username, Transaction transaction) {
        SpojniceVariables vars = ActiveGameService.mySpojniceVars(transaction, username);

        String[][] pairs = vars.getPairs().getPairsAsArrays();
        int[] pairPosition = vars.getPairPositionAsArray();
        String gameName = vars.getPairs().getText();

        return new Spojnice(pairs, pairPosition, gameName);
    }
}
