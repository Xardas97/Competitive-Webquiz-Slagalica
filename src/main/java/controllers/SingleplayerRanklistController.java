/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import entities.SingleplayerGame;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.hibernate.query.Query;
import util.SessionManager;
import util.Transaction;

/**
 *
 * @author Marko
 */

@ManagedBean
@ViewScoped
@Named(value="SingleplayerRanklistController")
public class SingleplayerRanklistController implements Serializable{
    private List<SingleplayerGame> games;
    private SingleplayerGame myGame;
    private int myPlacement = 0;
    
    @PostConstruct
    public void initGames(){
        String username = SessionManager.getUser().getUsername();
        Date today = new Date();

        Object myGameObject;
        List results;
        try(Transaction transaction = new Transaction()) {

            Query query = transaction.createQuery("FROM SingleplayerGame WHERE gameDate=? ORDER BY points DESC");
            results = query.setParameter(0, today).list();

            query = transaction.createQuery("FROM SingleplayerGame WHERE gameDate=? AND username=?");
            myGameObject = query.setParameter(0, today).setParameter(1, username).uniqueResult();
        }
        
        
        boolean iPlayed = false; //if the user has played today
        if(myGameObject!=null){
            iPlayed = true;
            myGame = (SingleplayerGame) myGameObject;
        }
        
        short gameIndex = 1;
        boolean foundMyself = false;
        boolean addMore = true;
        games = new LinkedList<>();
        for(Object result: results)
            if(result instanceof SingleplayerGame){
                SingleplayerGame game = (SingleplayerGame) result;
                if(iPlayed && game.getUsername().equals(username)) {
                    myGame = game;
                    myPlacement = gameIndex;
                    foundMyself = true;
                }
                if(gameIndex++>10) addMore = false;
                if(addMore) games.add(game);
                if(!addMore && (foundMyself || !iPlayed)) break;
            }
    }

    public SingleplayerGame getMyGame() {
        return myGame;
    }

    public int getMyPlacement() {
        return myPlacement;
    }

    public List<SingleplayerGame> getGames() {
        return games;
    }
    
    public boolean isPlacementBelowTen(){
        return myPlacement>10;
    }
    
    public boolean isMyRow(SingleplayerGame game){
        return myGame!=null && game.getUsername().equals( myGame.getUsername());
    }
}
