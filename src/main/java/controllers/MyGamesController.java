/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import entities.FinishedGame;
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
@Named(value="MyGamesController")
public class MyGamesController implements Serializable{
    static class MyGameElement{
        String opponent;
        String side;
        int myPoints;
        int opponentPoints;
        String result;
        Date date;

        public String getOpponent() {
            return opponent;
        }

        public String getSide() {
            return side;
        }

        public int getMyPoints() {
            return myPoints;
        }

        public int getOpponentPoints() {
            return opponentPoints;
        }

        public String getResult() {
            return result;
        }

        public Date getDate() {
            return date;
        }
        
    }
    private List<MyGameElement> myGames;
    
    @PostConstruct
    public void initGames(){
        String username = SessionManager.getUser().getUsername();

        List results;
        try(Transaction transaction = new Transaction()) {
            Query query = transaction.createQuery("FROM FinishedGame WHERE blue=? OR red=?");
             results = query.setParameter(0, username).setParameter(1, username).list();
        }

        
        myGames = new LinkedList<>();
        for(Object result: results)
            if(result instanceof FinishedGame){
                FinishedGame game = (FinishedGame) result;
                MyGameElement gameElement = new MyGameElement(); 
                gameElement.date = game.getGameDate();
                if(game.getBlue().equals(username)) {
                    initGameElementAsBlue(game, gameElement);
                }
                else{
                    initGameElementAsRed(game, gameElement);
                }
                myGames.add(gameElement);
            }
    }

    private void initGameElementAsRed(FinishedGame game, MyGameElement gameElement) {
        gameElement.side = "Red";
        gameElement.opponent = game.getBlue();
        gameElement.myPoints = game.getPointsRed();
        gameElement.opponentPoints = game.getPointsBlue();
        switch(game.getGameResult()){
            case 1: gameElement.result = "Defeat"; break;
            case -1: gameElement.result = "Victory"; break;
            default: gameElement.result = "Draw";
        }
    }

    private void initGameElementAsBlue(FinishedGame game, MyGameElement gameElement) {
        gameElement.side = "Blue";
        gameElement.opponent = game.getRed();
        gameElement.myPoints = game.getPointsBlue();
        gameElement.opponentPoints = game.getPointsRed();
        switch(game.getGameResult()){
            case 1: gameElement.result = "Victory"; break;
            case -1: gameElement.result = "Defeat"; break;
            default: gameElement.result = "Draw";
        }
    }

    public List<MyGameElement> getMyGames() {
        return myGames;
    }
    
}
