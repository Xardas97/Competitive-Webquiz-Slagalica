package services;

import org.hibernate.Session;

public class TransactionService {
    public static Session openTransaction(){
        Session session = database.HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        return session;
    }

    public static void closeTransaction(Session session) {
        session.getTransaction().commit();
        session.close();
    }
}
