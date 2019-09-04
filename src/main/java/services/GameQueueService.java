package services;

import org.hibernate.query.NativeQuery;
import util.Transaction;

public class GameQueueService {
    public static void insertIntoGameQueue(String username) {
        try(Transaction trans = new Transaction()) {
            NativeQuery testQuery = trans.createSQLQuery("SELECT * FROM gamequeue WHERE blue=?");
            testQuery.setParameter(0, username);
            if(testQuery.uniqueResult()==null){
                NativeQuery query = trans.createSQLQuery("INSERT INTO gamequeue VALUES (?)");
                query.setParameter(0, username);
                query.executeUpdate();
            }
        }
    }

    public static void deleteFromGameQueue(String username){
        try(Transaction trans = new Transaction()){
            NativeQuery query = trans.createSQLQuery("DELETE FROM gamequeue WHERE blue=?");
            query.setParameter(0, username);
            query.executeUpdate();
        }
    }
}
