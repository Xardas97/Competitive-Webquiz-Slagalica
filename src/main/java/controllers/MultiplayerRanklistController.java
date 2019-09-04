/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import classes.MultiplayerScores;
import classes.MultiplayerScoresCounter;
import entities.FinishedGame;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.hibernate.query.Query;
import util.Transaction;

/**
 *
 * @author Marko
 */

@ManagedBean
@ViewScoped
@Named(value="MultiplayerRanklistController")
public class MultiplayerRanklistController implements Serializable{
    private short rankingPage = 0; //0-weekly, 1-monthly
    
    private List<MultiplayerScores> users;
    private List<MultiplayerScores> usersWeekly;
    private List<MultiplayerScores> usersMonthly;
    
    @PostConstruct
    public void init(){
        initWeekly();
        initMonthly();
        users = usersWeekly;
    }
    
    private void initWeekly(){
        LocalDate startDate = LocalDate.now().minusDays(7);
        usersWeekly = new LinkedList<>();
        initUsers(startDate, usersWeekly);
    }
    
    private void initMonthly(){
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        
        usersMonthly = new LinkedList<>();
        initUsers(startDate, usersMonthly);
    }

    private  void initUsers(LocalDate startDate, List<MultiplayerScores> users){
        List results = getGames(startDate);
        
        Map<String, MultiplayerScoresCounter> scoreCounters;
        scoreCounters = makeCountersAndInitUsers(results, users);
        
        countScores(users, scoreCounters);
        sort(users);
    }
    
    private Map<String, MultiplayerScoresCounter> makeCountersAndInitUsers(List games, List<MultiplayerScores> users) {
        Map<String, MultiplayerScoresCounter> scoreCounters = new HashMap<>();
        
        for (Object result : games) {
            if (result instanceof FinishedGame) {
                FinishedGame game = (FinishedGame) result;
                if (!scoreCounters.containsKey(game.getBlue())) {
                    users.add(new MultiplayerScores(game.getBlue()));
                    scoreCounters.put(game.getBlue(), new MultiplayerScoresCounter());
                }
                if (!scoreCounters.containsKey(game.getRed())) {
                    users.add(new MultiplayerScores(game.getRed()));
                    scoreCounters.put(game.getRed(), new MultiplayerScoresCounter());
                }
                switch (game.getGameResult()) {
                    case 1:
                        scoreCounters.get(game.getBlue()).incVictories();
                        scoreCounters.get(game.getRed()).incDefeats();
                        break;
                    case 0:
                        scoreCounters.get(game.getBlue()).incDraws();
                        scoreCounters.get(game.getRed()).incDraws();
                        break;
                    case -1:
                        scoreCounters.get(game.getBlue()).incDefeats();
                        scoreCounters.get(game.getRed()).incVictories();
                        break;
                }
            }
        }
        
        return scoreCounters;
    }
    
    private List getGames(LocalDate startDate){
        List results;
        try(Transaction transaction = new Transaction()) {
            Query query = transaction.createQuery("FROM FinishedGame WHERE gameDate>=?");
            results = query.setParameter(0, startDate).list();
        }
        
        return results;
    }
    
    private void countScores(List<MultiplayerScores> scores, Map<String,MultiplayerScoresCounter> scoreCounters) {
        for (MultiplayerScores user : scores) {
            MultiplayerScoresCounter myCounter = scoreCounters.get(user.getUsername());
            user.setScores(myCounter);
        }
    }
    
    private void sort(List<MultiplayerScores> scores) {
        scores.sort((a, b) -> Float.compare(b.getTotalScore(), a.getTotalScore()));
    }
    
    public short getRankingPage() {
        return rankingPage;
    }

    public void setRankingPage(short rankingPage) {
        if(rankingPage==0) users = usersWeekly;
        else users = usersMonthly;
        this.rankingPage = rankingPage;
    }
    
    public boolean isMyPage(int i){
        return rankingPage == i;
    }

    public List<MultiplayerScores> getUsers() {
        return users;
    }

}
