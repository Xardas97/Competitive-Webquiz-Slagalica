package games;

import entities.ActiveGame;
import entities.GameOfTheDay;
import games.generators.SlagalicaGenerator;
import services.ActiveGameService;
import util.Transaction;

public class SlagalicaFactory {
    public static Slagalica create(GameOfTheDay game) {
        String[] letters = game.getLettersAsArray();

        return new Slagalica(letters);
    }

    public static Slagalica create(ActiveGame game, Transaction transaction) {
        String[] generatedLetters = SlagalicaGenerator.generate(transaction, game.getBlue(), game.getRed());

        return new Slagalica(generatedLetters);
    }

    public static Slagalica load(String username, Transaction transaction) {
        String[] generatedLetters = ActiveGameService.mySlagalicaVars(transaction, username).getLettersAsArray();

        return new Slagalica(generatedLetters);
    }
}
