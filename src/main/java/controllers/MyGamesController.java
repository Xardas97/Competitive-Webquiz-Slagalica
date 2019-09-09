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
import java.util.stream.Collectors;
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
        private String opponent;
        private String side;
        private int myPoints;
        private int opponentPoints;
        private String result;
        private Date date;

        MyGameElement(FinishedGame game, boolean playerIsBlue) {
            date = game.getGameDate();
            if(playerIsBlue) {
                initAsBlue(game);
            }
            else{
                initAsRed(game);
            }
        }

        private void initAsBlue(FinishedGame game) {
            side = "Blue";
            opponent = game.getRed();
            myPoints = game.getPointsBlue();
            opponentPoints = game.getPointsRed();
            switch(game.getGameResult()){
                case 1: result = "Victory"; break;
                case -1: result = "Defeat"; break;
                default: result = "Draw";
            }
        }

        private void initAsRed(FinishedGame game) {
            side = "Red";
            opponent = game.getBlue();
            myPoints = game.getPointsRed();
            opponentPoints = game.getPointsBlue();
            switch(game.getGameResult()){
                case 1: result = "Defeat"; break;
                case -1: result = "Victory"; break;
                default: result = "Draw";
            }
        }

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
        String myUsername = SessionManager.getUser().getUsername();

        List<FinishedGame> results;
        try(Transaction transaction = new Transaction()) {
            Query<FinishedGame> query =
                    transaction.createQuery("FROM FinishedGame WHERE blue=? OR red=?", FinishedGame.class);
            results = query.setParameter(0, myUsername).setParameter(1, myUsername).list();
        }

        myGames = new LinkedList<>();
        for (FinishedGame game : results) {
            boolean iWasBlue = game.getBlue().equals(myUsername);
            myGames.add(new MyGameElement(game, iWasBlue));
        }
    }

    public List<MyGameElement> getMyGames() {
        return myGames;
    }
    
}
