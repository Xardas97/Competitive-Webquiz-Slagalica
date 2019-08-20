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
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import util.SessionManager;

/**
 *
 * @author Marko
 */

@ManagedBean
@SessionScoped
@Named(value="MainMenuController")
public class MainMenuController implements Serializable{
    private final String redirect="?faces-redirect=true";
    String errorMessage = "";
    
    public String startGameOfTheDay(){
        String username = SessionManager.getUser().getUsername();
        Date currentDate = new Date();
        
        Session session = database.HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        
        Object gameOfTheDay = session.createCriteria(GameOfTheDay.class)
                .add(Restrictions.eq("gameDate", currentDate)).uniqueResult();
        
        Object playedToday = session.createCriteria(SingleplayerGame.class)
                .add(Restrictions.eq("username", username)).add(Restrictions.eq("gameDate", currentDate)).uniqueResult();
  
        session.getTransaction().commit();
        session.close();
        
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
        return SessionManager.getUser()==null;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
    
}
