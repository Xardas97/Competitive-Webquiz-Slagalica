package games;

import entities.ActiveGame;
import entities.Asocijacija;
import entities.GameOfTheDay;
import games.generators.AsocijacijeGenerator;
import services.ActiveGameService;
import util.Transaction;

public class AsocijacijeFactory {
    public static Asocijacije create(GameOfTheDay game) {
        Asocijacija asocijacija = game.getAsocijacija();

        return new Asocijacije(asocijacija);
    }

    public static Asocijacije create(ActiveGame game, Transaction transaction) {
        Asocijacija asocijacija = AsocijacijeGenerator.generate(transaction, game.getBlue(), game.getRed());

        return new Asocijacije(asocijacija);
    }

    public static Asocijacije load(String username, Transaction transaction) {
        Asocijacija asocijacija = ActiveGameService.myAsocijacijeVars(transaction, username).getAsocijacija();

        return new Asocijacije(asocijacija);
    }
}
