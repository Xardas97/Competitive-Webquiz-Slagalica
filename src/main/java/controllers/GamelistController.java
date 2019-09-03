/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import static services.TransactionService.*;
import entities.ActiveGame;
import entities.User;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import util.SessionManager;

/**
 *
 * @author Marko
 */

@ManagedBean
@ViewScoped
@Named(value="GamelistController")
public class GamelistController implements Serializable{
    private List<String> games;
    
    @PostConstruct
    public void initGames(){
        Session session = openTransaction();
        
        NativeQuery query = session.createSQLQuery("SELECT * FROM gamequeue");
        List results = query.list();
        
        closeTransaction(session);

        games = new LinkedList<>();
        for(Object result: results)
            if(result instanceof String)
                games.add((String)result);
    }
    
    public String join(String bluePlayer){
        User redPlayer = SessionManager.getUser();
        ActiveGame game = new ActiveGame(bluePlayer, redPlayer.getUsername());
        
        Session session = openTransaction();
        
        NativeQuery query = session.createSQLQuery("DELETE FROM gamequeue WHERE blue=?");
        query.setParameter(0, bluePlayer);
        query.executeUpdate();
        
        session.save(game);

        closeTransaction(session);
        
        SessionManager.setPlayerSide("red");
        SessionManager.setGameMode("multiplayer");
        
        return "game?faces-redirect=true";
    }

    public List<String> getGames() {
        return games;
    }
    
}
