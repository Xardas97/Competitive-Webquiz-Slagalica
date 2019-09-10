package games;

import entities.ActiveGame;
import entities.GameOfTheDay;
import games.generators.MojBrojGenerator;
import services.ActiveGameService;
import util.Transaction;

public class MojBrojFactory {
    public static MojBroj create(GameOfTheDay game) {
        String[] numbers = game.getNumbersAsArray();

        return new MojBroj(numbers);
    }

    public static MojBroj create(ActiveGame game, Transaction transaction) {
        String[] generatedNumbers = MojBrojGenerator.generate(transaction, game.getBlue(), game.getRed());

        return new MojBroj(generatedNumbers);
    }

    public static MojBroj load(String username, Transaction transaction) {
        String[] generatedNumbers = ActiveGameService.myMojBrojVars(transaction, username).getNumbersAsArray();

        return new MojBroj(generatedNumbers);
    }
}
