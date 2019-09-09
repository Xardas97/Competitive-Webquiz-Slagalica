package services;

import entities.Asocijacija;
import entities.RegistrationRequest;
import entities.WordPairs;
import org.hibernate.query.Query;
import util.Transaction;

import java.util.List;

public class AdminService {
    public static List<WordPairs> getWordPairs() {
        try(Transaction transaction = new Transaction()) {
            return transaction.createQuery("FROM WordPairs", WordPairs.class).list();
        }
    }

    public static List<RegistrationRequest> getRegRequests() {
        try(Transaction transaction = new Transaction()) {
            return transaction.createQuery("FROM RegistrationRequest", RegistrationRequest.class).list();
        }
    }

    public static void deleteRegRequest(Transaction transaction, String username) {
        Query removeFromRequestsQuery = transaction.createQuery("DELETE FROM RegistrationRequest WHERE username=?");
        removeFromRequestsQuery.setParameter(0, username);
        removeFromRequestsQuery.executeUpdate();
    }

    public static List<Asocijacija> getAsocijacije() {
        try(Transaction transaction = new Transaction()){
            return transaction.createQuery("FROM Asocijacija", Asocijacija.class).list();
        }
    }
}
