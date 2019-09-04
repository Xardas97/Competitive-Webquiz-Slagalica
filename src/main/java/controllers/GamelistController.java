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
import org.hibernate.query.NativeQuery;
import util.SessionManager;
import util.Transaction;

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
        List results;
        try(Transaction transaction = new Transaction()) {
            NativeQuery query = transaction.createSQLQuery("SELECT * FROM gamequeue");
            results = query.list();
        }

        games = new LinkedList<>();
        for(Object result: results)
            if(result instanceof String)
                games.add((String)result);
    }
    
    public String join(String bluePlayer){
        User redPlayer = SessionManager.getUser();
        ActiveGame game = new ActiveGame(bluePlayer, redPlayer.getUsername());

        try(Transaction transaction = new Transaction()) {
            NativeQuery query = transaction.createSQLQuery("DELETE FROM gamequeue WHERE blue=?");
            query.setParameter(0, bluePlayer);
            query.executeUpdate();

            transaction.save(game);
        }
        
        SessionManager.setPlayerSide("red");
        SessionManager.setGameMode("multiplayer");
        
        return "game?faces-redirect=true";
    }

    public List<String> getGames() {
        return games;
    }
    
}
