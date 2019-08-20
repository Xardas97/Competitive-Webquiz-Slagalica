/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import entities.ActiveGame;
import java.io.Serializable;
import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import util.SessionManager;

/**
 *
 * @author Marko
 */

@ManagedBean
@Named(value="HostingController")
@ViewScoped
public class HostingController implements Serializable{
    String message = "";
    boolean hosting=false;
    String username = null;
    
    public String checkIfAccepted(){
        if(!hosting) return null;
        String returnString = null;
        Session session = database.HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        
        Criteria query = session.createCriteria(ActiveGame.class).add(Restrictions.eq("blue", username));;
        ActiveGame game = (ActiveGame) query.uniqueResult();
        if(game!=null) { 
            returnString="game?faces-redirect=true"; 
            message=""; 
            SessionManager.setGameMode("multiplayer");
            SessionManager.setPlayerSide("blue");
        }
        
        session.getTransaction().commit();
        session.close();
        return returnString;
    }
    
    public void startHosting(){
        if(hosting) return;
        
        Session session = database.HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        
        SQLQuery testQuery = session.createSQLQuery("SELECT * FROM gamequeue WHERE blue=:username");
        testQuery.setString("username", username);
        if(testQuery.uniqueResult()==null){
            SQLQuery query = session.createSQLQuery("INSERT INTO gamequeue VALUES (:username)");
            query.setString("username", username);
            query.executeUpdate();
        }
        
        session.getTransaction().commit();
        session.close();
        hosting = true;
        message = "Waiting for someone to join";
    }
    
    public void stopHosting(){
        hosting = false;
        message = "";
        
        Session session = database.HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        
        SQLQuery query = session.createSQLQuery("DELETE FROM gamequeue WHERE blue=:username");
        query.setString("username", username);
        query.executeUpdate();
        
        session.getTransaction().commit();
        session.close();
    }
    
    @PreDestroy
    public void removeFromDatabase(){
        Session session = database.HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        
        SQLQuery query = session.createSQLQuery("DELETE FROM gamequeue WHERE blue=:username");
        query.setString("username", username);
        query.executeUpdate();
        
        session.getTransaction().commit();
        session.close();
    }
    
    @PostConstruct
    public void initUsername(){
        username = SessionManager.getUser().getUsername();
    }
    
    public String getMessage() {
        return message;
    }
    
}
