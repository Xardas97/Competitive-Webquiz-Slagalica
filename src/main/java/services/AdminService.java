package services;

import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class AdminService {
    public static List getWordPairs(Session session) {
        return session.createQuery("FROM WordPairs").list();
    }

    public static List getRegRequests(Session session) {
        return session.createQuery("FROM RegistrationRequest").list();
    }

    public static void deleteRegRequest(Session session, String username) {
        Query removeFromRequestsQuery = session.createQuery("DELETE FROM RegistrationRequest WHERE username=?");
        removeFromRequestsQuery.setParameter(0, username);
        removeFromRequestsQuery.executeUpdate();
    }
}
