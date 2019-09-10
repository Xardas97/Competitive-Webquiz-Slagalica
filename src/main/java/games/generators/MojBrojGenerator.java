package games.generators;

import entities.MojBrojVariables;
import util.Transaction;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MojBrojGenerator {
    private static final String[] FIRST_NUMBER_POOL = {"10", "15", "20"};
    private static final String[] SECOND_NUMBER_POOL = {"25", "50", "75", "100"};

    public static String[] generate(Transaction transaction, String blue, String red){
        String generatedNumbers = generate();
        transaction.save(new MojBrojVariables(blue, red, generatedNumbers));
        return generatedNumbers.split(" ");
    }

    public static String generate() {
        Random rnd = ThreadLocalRandom.current();
        StringBuilder builder = new StringBuilder();
        builder.append(rnd.nextInt(999)+1);
        for(int i=1; i<5; i++){
            builder.append(' ').append(rnd.nextInt(9)+1);
        }
        builder.append(' ').append(FIRST_NUMBER_POOL[rnd.nextInt(3)]);
        builder.append(' ').append(SECOND_NUMBER_POOL[rnd.nextInt(4)]);

        return builder.toString();
    }
}
