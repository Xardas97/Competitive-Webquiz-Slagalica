/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import entities.GameOfTheDay;
import entities.SingleplayerGame;
import java.io.Serializable;
import java.util.Date;
import javax.annotation.ManagedBean;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import util.SessionManager;
import util.Transaction;

/**
 *
 * @author Marko
 */

@ManagedBean
@SessionScoped
@Named(value="MainMenuController")
public class MainMenuController implements Serializable{
    private final String redirect="?faces-redirect=true";
    private String errorMessage = "";
    
    public String startGameOfTheDay(){
        String username = SessionManager.getUser().getUsername();
        Date currentDate = new Date();

        GameOfTheDay gameOfTheDay;
        SingleplayerGame playedToday;
        try(Transaction transaction = new Transaction()) {

            gameOfTheDay = transaction.get(GameOfTheDay.class, currentDate);

            CriteriaBuilder builder = transaction.getCriteriaBuilder();
            CriteriaQuery<SingleplayerGame> criteria = builder.createQuery(SingleplayerGame.class);
            Root<SingleplayerGame> game = criteria.from(SingleplayerGame.class);
            criteria.select(game)
                    .where(builder.equal(game.get("username"), username))
                    .where(builder.equal(game.get("gameDate"), currentDate));
            playedToday = transaction.createQuery(criteria).uniqueResult();
        }
        
        //if admin hasn't set up the game for today
        if(gameOfTheDay==null) {
            errorMessage = "Game of the Day not ready yet!";
            return "menu";
        }
        // if the player has already played the game today
        else if(playedToday!=null){
            errorMessage = "Game of the Day already played!";
            return "menu";
        }
        
        errorMessage = "";
        SessionManager.setGameMode("singleplayer");
        return "game"+redirect;
    }
    
    public String returnToMenu(){
        errorMessage = "";
        return "menu"+redirect;
    }
    
    public String browseMultiplayerRanklist(){
        errorMessage = "";
        return "multiplayer_games"+redirect;
    }
    
    public String browseSingleplayerRanklist(){
        errorMessage = "";
        return "singleplayer_games"+redirect;
    }
    
    public String browseGames(){
        errorMessage = "";
        return "gamelist"+redirect;
    }
    
    public String browseMyGames(){
        errorMessage = "";
        return "mygames"+redirect;
    }
    
    public String hostGame(){
        errorMessage = "";
        return "hosting"+redirect;
    }
    
    public boolean isGuest(){
        try{
            SessionManager.getUser();
            return false;
        }
        catch(NullPointerException e) {
            return true;
        }
    }

    public String getErrorMessage() {
        return errorMessage;
    }
    
}
