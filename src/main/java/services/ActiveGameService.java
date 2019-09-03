package services;

import entities.*;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.Date;

public class ActiveGameService {
    public static ActiveGame myActiveGame(Session session, String username){
        Query query = session.createQuery("FROM ActiveGame WHERE blue=? OR red=?");
        return (ActiveGame) query
                .setParameter(0, username)
                .setParameter(1, username)
                .uniqueResult();
    }

    public static SlagalicaVariables mySlagalicaVars(Session session, String username){
        Query query = session.createQuery("FROM SlagalicaVariables WHERE blue=? OR red=?");
        return (SlagalicaVariables) query
                .setParameter(0, username)
                .setParameter(1, username)
                .uniqueResult();
    }

    public static MojBrojVariables myMojBrojVars(Session session, String username){
        Query query = session.createQuery("FROM MojBrojVariables WHERE blue=? OR red=?");
        return (MojBrojVariables) query
                .setParameter(0, username)
                .setParameter(1, username)
                .uniqueResult();
    }

    public static SkockoVariables mySkockoVars(Session session, String username){
        Query query = session.createQuery("FROM SkockoVariables WHERE blue=? OR red=?");
        return (SkockoVariables) query
                .setParameter(0, username)
                .setParameter(1, username)
                .uniqueResult();
    }

    public static SpojniceVariables mySpojniceVars(Session session, String username){
        Query query = session.createQuery("FROM SpojniceVariables WHERE blue=? OR red=?");
        return (SpojniceVariables) query
                .setParameter(0, username)
                .setParameter(1, username)
                .uniqueResult();
    }

    public static AsocijacijeVariables myAsocijacijeVars(Session session, String username){
        Query query = session.createQuery("FROM AsocijacijeVariables WHERE blue=? OR red=?");
        return (AsocijacijeVariables) query
                .setParameter(0, username)
                .setParameter(1, username)
                .uniqueResult();
    }

    public static GameOfTheDay currentGameOfTheDay(Session session){
        Query query = session.createQuery("FROM GameOfTheDay WHERE gameDate=?");
        return (GameOfTheDay) query.setParameter(0, new Date()).uniqueResult();
    }

    /**
     * Deletes database entries needed for game variables
     */
    public static void cleanUpDatabase(Session session, String username){
        session.createQuery("DELETE FROM SlagalicaVariables WHERE blue=?")
                .setParameter(0, username).executeUpdate();
        session.createQuery("DELETE FROM MojBrojVariables WHERE blue=?")
                .setParameter(0, username).executeUpdate();
        session.createQuery("DELETE FROM SkockoVariables WHERE blue=?")
                .setParameter(0, username).executeUpdate();
        session.createQuery("DELETE FROM SpojniceVariables WHERE blue=?")
                .setParameter(0, username).executeUpdate();
        session.createQuery("DELETE FROM AsocijacijeVariables WHERE blue=?")
                .setParameter(0, username).executeUpdate();
    }
}
