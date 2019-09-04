package services;

import org.hibernate.query.Query;
import util.Transaction;

import java.util.List;

public class AdminService {
    public static List getWordPairs() {
        try(Transaction transaction = new Transaction()) {
            return transaction.createQuery("FROM WordPairs").list();
        }
    }

    public static List getRegRequests() {
        try(Transaction transaction = new Transaction()) {
            return transaction.createQuery("FROM RegistrationRequest").list();
        }
    }

    public static void deleteRegRequest(Transaction transaction, String username) {
        Query removeFromRequestsQuery = transaction.createQuery("DELETE FROM RegistrationRequest WHERE username=?");
        removeFromRequestsQuery.setParameter(0, username);
        removeFromRequestsQuery.executeUpdate();
    }

    public static List getAsocijacije() {
        try(Transaction transaction = new Transaction()){
            return transaction.createQuery("FROM Asocijacija").list();
        }
    }
}
