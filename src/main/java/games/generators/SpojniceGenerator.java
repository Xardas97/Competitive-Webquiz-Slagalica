package games.generators;

import entities.SpojniceVariables;
import entities.WordPairs;
import util.Transaction;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class SpojniceGenerator {
    public static SpojniceVariables generate(Transaction transaction, String blue, String red){
        WordPairs wordPairs = transaction
                .createQuery("FROM WordPairs ORDER BY rand(5)", WordPairs.class)
                .setMaxResults(1).uniqueResult();
        int[] randomPositions = createRandomPositionsArray();

        SpojniceVariables spojniceVars = transaction
                .createQuery("FROM SpojniceVariables WHERE blue=?", SpojniceVariables.class)
                .setParameter(0, blue)
                .uniqueResult();

        if (spojniceVars == null) {
            spojniceVars = new SpojniceVariables(blue, red, randomPositions, wordPairs);
            transaction.save(spojniceVars);
        }
        else {
            spojniceVars.prepareNewGame(randomPositions, wordPairs, false);
        }
        return spojniceVars;
    }

    public static int[] createRandomPositionsArray(){
        int[] randomPositions = new int[10];
        for(int i=0; i<10; i++) {
            randomPositions[i] = i;
        }

        Random rnd = ThreadLocalRandom.current();
        for (int i = 9; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            int a = randomPositions[index];
            randomPositions[index] = randomPositions[i];
            randomPositions[i] = a;
        }

        return randomPositions;
    }
}
