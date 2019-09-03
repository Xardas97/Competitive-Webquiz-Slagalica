package services;

import static services.TransactionService.*;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;

public class GameQueueService {
    public static void insertIntoGameQueue(String username) {
        Session session = openTransaction();

        NativeQuery testQuery = session.createSQLQuery("SELECT * FROM gamequeue WHERE blue=?");
        testQuery.setParameter(0, username);
        if(testQuery.uniqueResult()==null){
            NativeQuery query = session.createSQLQuery("INSERT INTO gamequeue VALUES (?)");
            query.setParameter(0, username);
            query.executeUpdate();
        }

        closeTransaction(session);
    }

    public static void deleteFromGameQueue(String username){
        Session session = openTransaction();

        NativeQuery query = session.createSQLQuery("DELETE FROM gamequeue WHERE blue=?");
        query.setParameter(0, username);
        query.executeUpdate();

        closeTransaction(session);
    }
}
