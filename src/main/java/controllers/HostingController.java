/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import static services.GameQueueService.*;
import entities.ActiveGame;
import java.io.Serializable;
import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import services.ActiveGameService;
import util.HttpSessionManager;
import util.Transaction;

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

        try(Transaction transaction = new Transaction()){
            ActiveGame game = ActiveGameService.myActiveGame(transaction, username);
            if(game!=null) {
                returnString="game?faces-redirect=true";
                message="";
                HttpSessionManager.setGameMode("multiplayer");
                HttpSessionManager.setPlayerSide("blue");
            }
        }
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
        username = HttpSessionManager.getUser().getUsername();
    }
    
    public String getMessage() {
        return message;
    }
    
}
