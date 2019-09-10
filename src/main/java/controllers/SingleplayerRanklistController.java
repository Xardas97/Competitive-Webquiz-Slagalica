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
import util.HttpSessionManager;
import util.Transaction;

/**
 *
 * @author Marko
 */

@ManagedBean
@ViewScoped
@Named(value="SingleplayerRanklistController")
public class SingleplayerRanklistController implements Serializable {
    private static final int NUM_PLAYERS_TO_SHOW = 10;
    private List<SingleplayerGame> topGames;
    private SingleplayerGame myGame;
    private int myPlacement = 0;
    
    @PostConstruct
    public void initGames(){
        String username = HttpSessionManager.getUser().getUsername();
        Date today = new Date();

        List<SingleplayerGame> allGamesFromToday;

        try(Transaction transaction = new Transaction()) {
            Query<SingleplayerGame> query =
                    transaction.createQuery("FROM SingleplayerGame WHERE gameDate=? ORDER BY points DESC",
                            SingleplayerGame.class);
            allGamesFromToday = query.setParameter(0, today).list();

            query = transaction
                    .createQuery("FROM SingleplayerGame WHERE gameDate=? AND username=?", SingleplayerGame.class);
            myGame = query.setParameter(0, today).setParameter(1, username).uniqueResult();
        }
        
        boolean iPlayed = (myGame != null);
        boolean foundMyself = false;
        boolean addMore = true;
        short currentPosition = 1;

        topGames = new LinkedList<>();
        for (SingleplayerGame game : allGamesFromToday) {
            if (iPlayed && game.getUsername().equals(username)) {
                myPlacement = currentPosition;
                foundMyself = true;
            }
            if (currentPosition++ > NUM_PLAYERS_TO_SHOW) {
                addMore = false;
            }
            if (addMore) {
                topGames.add(game);
            }
            if (!addMore && (foundMyself || !iPlayed)) {
                break;
            }
        }
    }

    public SingleplayerGame getMyGame() {
        return myGame;
    }

    public int getMyPlacement() {
        return myPlacement;
    }

    public List<SingleplayerGame> getTopGames() {
        return topGames;
    }
    
    public boolean isPlacementBelowTen(){
        return myPlacement>10;
    }
    
    public boolean isMyRow(SingleplayerGame game){
        return myGame!=null && game.getUsername().equals( myGame.getUsername());
    }
}
