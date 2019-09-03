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
import org.hibernate.Session;
import util.SessionManager;

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
        Session session = database.HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        
        Query query = session.createQuery("FROM FinishedGame WHERE blue=? OR red=?");
        List results = query.setParameter(0, username).setParameter(1, username).list();
        
        session.getTransaction().commit();
        session.close();
        
        myGames = new LinkedList<>();
        for(Object result: results)
            if(result instanceof FinishedGame){
                FinishedGame game = (FinishedGame) result;
                MyGameElement gameElement = new MyGameElement(); 
                gameElement.date = game.getGameDate();
                if(((FinishedGame) result).getBlue().equals(username)){
                    //We are blue
                    gameElement.side = "Blue";
                    gameElement.opponent = ((FinishedGame) result).getRed();
                    gameElement.myPoints = game.getPointsBlue();
                    gameElement.opponentPoints = game.getPointsRed();
                    switch(game.getGameResult()){
                        case 1: gameElement.result = "Victory"; break; 
                        case -1: gameElement.result = "Defeat"; break;
                        default: gameElement.result = "Draw";
                    }
                }
                else{
                    //We are red
                    gameElement.side = "Red";
                    gameElement.opponent = ((FinishedGame) result).getBlue();
                    gameElement.myPoints = game.getPointsRed();
                    gameElement.opponentPoints = game.getPointsBlue();
                    switch(game.getGameResult()){
                        case 1: gameElement.result = "Defeat"; break;
                        case -1: gameElement.result = "Victory"; break;
                        default: gameElement.result = "Draw";
                    }
                }
                myGames.add(gameElement);
            }
    }

    public List<MyGameElement> getMyGames() {
        return myGames;
    }
    
}
