package services;

import entities.*;
import org.hibernate.query.Query;
import util.Transaction;

import java.util.Date;

public class ActiveGameService {
    public static ActiveGame myActiveGame(Transaction transaction, String username){
        Query query = transaction.createQuery("FROM ActiveGame WHERE blue=? OR red=?");
        return (ActiveGame) query
                .setParameter(0, username)
                .setParameter(1, username)
                .uniqueResult();
    }

    public static SlagalicaVariables mySlagalicaVars(Transaction transaction, String username){
        Query query = transaction.createQuery("FROM SlagalicaVariables WHERE blue=? OR red=?");
        return (SlagalicaVariables) query
                .setParameter(0, username)
                .setParameter(1, username)
                .uniqueResult();
    }

    public static MojBrojVariables myMojBrojVars(Transaction transaction, String username){
        Query query = transaction.createQuery("FROM MojBrojVariables WHERE blue=? OR red=?");
        return (MojBrojVariables) query
                .setParameter(0, username)
                .setParameter(1, username)
                .uniqueResult();
    }

    public static SkockoVariables mySkockoVars(Transaction transaction, String username){
        Query query = transaction.createQuery("FROM SkockoVariables WHERE blue=? OR red=?");
        return (SkockoVariables) query
                .setParameter(0, username)
                .setParameter(1, username)
                .uniqueResult();
    }

    public static SpojniceVariables mySpojniceVars(Transaction transaction, String username){
        Query query = transaction.createQuery("FROM SpojniceVariables WHERE blue=? OR red=?");
        return (SpojniceVariables) query
                .setParameter(0, username)
                .setParameter(1, username)
                .uniqueResult();
    }

    public static AsocijacijeVariables myAsocijacijeVars(Transaction transaction, String username){
        Query query = transaction.createQuery("FROM AsocijacijeVariables WHERE blue=? OR red=?");
        return (AsocijacijeVariables) query
                .setParameter(0, username)
                .setParameter(1, username)
                .uniqueResult();
    }

    public static GameOfTheDay currentGameOfTheDay(Transaction transaction){
        Query query = transaction.createQuery("FROM GameOfTheDay WHERE gameDate=?");
        return (GameOfTheDay) query.setParameter(0, new Date()).uniqueResult();
    }

    /**
     * Deletes database entries needed for game variables
     */
    public static void cleanUpDatabase(Transaction transaction, String username){
        transaction.createQuery("DELETE FROM SlagalicaVariables WHERE blue=?")
                .setParameter(0, username).executeUpdate();
        transaction.createQuery("DELETE FROM MojBrojVariables WHERE blue=?")
                .setParameter(0, username).executeUpdate();
        transaction.createQuery("DELETE FROM SkockoVariables WHERE blue=?")
                .setParameter(0, username).executeUpdate();
        transaction.createQuery("DELETE FROM SpojniceVariables WHERE blue=?")
                .setParameter(0, username).executeUpdate();
        transaction.createQuery("DELETE FROM AsocijacijeVariables WHERE blue=?")
                .setParameter(0, username).executeUpdate();
    }
}
