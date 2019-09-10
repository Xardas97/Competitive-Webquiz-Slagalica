package games.generators;

import entities.SkockoVariables;
import games.Skocko;
import util.Transaction;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class SkockoGenerator {
    private static final String[] SKOCKO_SYMBOLS = Skocko.getSymbols();

    public static String[] generate(Transaction transaction, String blue, String red){
        String generatedCombo = generate();

        SkockoVariables skockoVars = transaction.createQuery("FROM SkockoVariables WHERE blue=?", SkockoVariables.class)
                .setParameter(0, blue)
                .uniqueResult();

        if (skockoVars == null) {
            skockoVars = new SkockoVariables(blue, red, generatedCombo);
            transaction.save(skockoVars);
        } else {
            skockoVars.prepareNewGame(generatedCombo, false);
        }
        return generatedCombo.split(" ");
    }

    public static String generate() {
        Random rnd = ThreadLocalRandom.current();
        StringBuilder builder = new StringBuilder();
        builder.append(SKOCKO_SYMBOLS[rnd.nextInt(6)]);
        for(int i=1; i<4; i++){
            builder.append(' ').append(SKOCKO_SYMBOLS[rnd.nextInt(6)]);
        }

        return builder.toString();
    }
}
