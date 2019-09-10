package games;

import entities.ActiveGame;
import entities.GameOfTheDay;
import games.generators.SkockoGenerator;
import services.ActiveGameService;
import util.Transaction;

public class SkockoFactory {
    public static Skocko create(GameOfTheDay game) {
        String[] combo = game.getSecretComboAsArray();

        return new Skocko(combo);
    }

    public static Skocko create(ActiveGame game, Transaction transaction) {
        String[] generatedCombo = SkockoGenerator.generate(transaction, game.getBlue(), game.getRed());

        return new Skocko(generatedCombo);
    }

    public static Skocko load(String username, Transaction transaction) {
        String[] generatedCombo = ActiveGameService.mySkockoVars(transaction, username).getSecretComboAsArray();

        return new Skocko(generatedCombo);
    }
}
