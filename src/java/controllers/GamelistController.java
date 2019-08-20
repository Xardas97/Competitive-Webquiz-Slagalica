/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import entities.ActiveGame;
import entities.User;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import util.SessionManager;

/**
 *
 * @author Marko
 */

@ManagedBean
@ViewScoped
@Named(value="GamelistController")
public class GamelistController implements Serializable{
    List<String> games;
    
    @PostConstruct
    public void initGames(){
        Session session = database.HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        
        SQLQuery query = session.createSQLQuery("SELECT * FROM gamequeue");
        List results = query.list();
        
        session.getTransaction().commit();
        session.close();
        
        games = new LinkedList<>();
        for(Object result: results)
            if(result instanceof String)
                games.add((String)result);
    }
    
    public String join(String bluePlayer){
        User redPlayer = SessionManager.getUser();
        ActiveGame game = new ActiveGame(bluePlayer, redPlayer.getUsername());
        
        Session session = database.HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        
        SQLQuery query = session.createSQLQuery("DELETE FROM gamequeue WHERE blue=:username");
        query.setString("username", bluePlayer);
        query.executeUpdate();
        
        session.save(game);
        
        session.getTransaction().commit();
        session.close();
        
        SessionManager.setPlayerSide("red");
        SessionManager.setGameMode("multiplayer");
        
        return "game?faces-redirect=true";
    }

    public List<String> getGames() {
        return games;
    }
    
}
