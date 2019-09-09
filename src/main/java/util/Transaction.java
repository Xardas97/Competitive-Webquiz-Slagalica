package util;

import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.io.Serializable;

public class Transaction implements AutoCloseable {
    private final Session session;

    public Transaction() {
        session = database.HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
    }

    @Override
    public void close() {
        session.getTransaction().commit();
        session.close();
    }

    public <T> NativeQuery<T> createNativeQuery(String query, Class<T> type){
        return session.createNativeQuery(query, type);
    }

    public NativeQuery createSQLQuery(String s) {
        return session.createSQLQuery(s);
    }

    public <T> T get(Class<T> var1, Serializable var2) {
        return session.get(var1, var2);
    }

    public void save(Object o) {
        session.save(o);
    }

    public <T> Query<T> createQuery(CriteriaQuery<T> var1){
        return session.createQuery(var1);
    }

    public Query createQuery(String var1){
        return session.createQuery(var1);
    }

    public <T> Query<T> createQuery(String var1, Class<T> type){
        return session.createQuery(var1, type);
    }

    public CriteriaBuilder getCriteriaBuilder() {
        return session.getCriteriaBuilder();
    }

    public void delete(Object o) {
        session.delete(o);
    }
}
