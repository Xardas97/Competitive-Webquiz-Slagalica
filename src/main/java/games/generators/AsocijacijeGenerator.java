package games.generators;

import entities.Asocijacija;
import entities.AsocijacijeVariables;
import util.Transaction;

public class AsocijacijeGenerator {
    public static Asocijacija generate(Transaction transaction, String blue, String red){
        Asocijacija asocijacija = (Asocijacija) transaction
                .createQuery("FROM Asocijacija ORDER BY rand(5)")
                .setMaxResults(1).uniqueResult();

        transaction.save(new AsocijacijeVariables(blue, red, asocijacija));

        return asocijacija;
    }
}
