package games.generators;

import entities.SlagalicaVariables;
import util.Transaction;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class SlagalicaGenerator {
    private static final String[] LETTERS = {"A","B","V","G","D","Đ","E","Ž","Z","I","J","K","L","LJ","M",
            "N","NJ","O", "P","R","S","T","Ć","U","F","H","C","Č","DŽ","Š"};

    public static String[] generate(Transaction transaction, String blue, String red){
        String generatedLetters = generate();
        transaction.save(new SlagalicaVariables(blue, red, generatedLetters));
        return generatedLetters.split(" ");
    }

    public static String generate() {
        Random rnd = ThreadLocalRandom.current();
        StringBuilder builder = new StringBuilder();
        builder.append(LETTERS[rnd.nextInt(30)]);
        for(int i=1; i<12; i++){
            builder.append(' ').append(LETTERS[rnd.nextInt(30)]);
        }

        return builder.toString();
    }
}
