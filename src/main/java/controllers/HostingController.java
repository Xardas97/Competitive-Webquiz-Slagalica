/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import static services.TransactionService.*;
import static services.GameQueueService.*;
import entities.ActiveGame;
import java.io.Serializable;
import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.hibernate.Session;
import services.ActiveGameService;
import util.SessionManager;

/**
 *
 * @author Marko
 */

@ManagedBean
@Named(value="HostingController")
@ViewScoped
public class HostingController implements Serializable{
    private String message = "";
    private boolean hosting=false;
    private String username = null;
    
    public String checkIfAccepted(){
        if(!hosting) return null;
        String returnString = null;
        Session session = openTransaction();

        ActiveGame game = ActiveGameService.myActiveGame(session, username);
        if(game!=null) { 
            returnString="game?faces-redirect=true"; 
            message=""; 
            SessionManager.setGameMode("multiplayer");
            SessionManager.setPlayerSide("blue");
        }
        
        closeTransaction(session);
        return returnString;
    }
    
    public void startHosting(){
        if(hosting) return;

        insertIntoGameQueue(username);
        hosting = true;
        message = "Waiting for someone to join";
    }
    
    public void stopHosting(){
        hosting = false;
        message = "";
        removeFromDatabase();
    }
    
    @PreDestroy
    public void removeFromDatabase(){
        deleteFromGameQueue(username);
    }
    
    @PostConstruct
    public void initUsername(){
        username = SessionManager.getUser().getUsername();
    }
    
    public String getMessage() {
        return message;
    }
    
}
