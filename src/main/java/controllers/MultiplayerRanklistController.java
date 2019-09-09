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
        List<FinishedGame> results = getGames(startDate);
        
        Map<String, MultiplayerScoresCounter> scoreCounters;
        scoreCounters = makeCountersAndInitUsers(results, users);
        
        countScores(users, scoreCounters);
        sort(users);
    }

    private List<FinishedGame> getGames(LocalDate startDate){
        List<FinishedGame> results;
        try(Transaction transaction = new Transaction()) {
            Query<FinishedGame> query = transaction.createQuery("FROM FinishedGame WHERE gameDate>=?", FinishedGame.class);
            results = query.setParameter(0, startDate).list();
        }

        return results;
    }
    
    private Map<String, MultiplayerScoresCounter> makeCountersAndInitUsers(List<FinishedGame> games,
                                                                           List<MultiplayerScores> users) {
        Map<String, MultiplayerScoresCounter> scoreCounters = new HashMap<>();

        for (FinishedGame game : games) {
            String bluePlayer = game.getBlue();
            String redPlayer = game.getRed();

            if (!scoreCounters.containsKey(bluePlayer)) {
                users.add(new MultiplayerScores(bluePlayer));
                scoreCounters.put(bluePlayer, new MultiplayerScoresCounter());
            }
            if (!scoreCounters.containsKey(redPlayer)) {
                users.add(new MultiplayerScores(redPlayer));
                scoreCounters.put(redPlayer, new MultiplayerScoresCounter());
            }

            switch (game.getGameResult()) {
                case 1:
                    scoreCounters.get(bluePlayer).incVictories();
                    scoreCounters.get(redPlayer).incDefeats();
                    break;
                case 0:
                    scoreCounters.get(bluePlayer).incDraws();
                    scoreCounters.get(redPlayer).incDraws();
                    break;
                case -1:
                    scoreCounters.get(bluePlayer).incDefeats();
                    scoreCounters.get(redPlayer).incVictories();
                    break;
            }
        }
        
        return scoreCounters;
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
