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
import java.sql.Date;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 *
 * @author Marko
 */

@ManagedBean
@ViewScoped
@Named(value="MultiplayerRanklistController")
public class MultiplayerRanklistController implements Serializable{
    short rankingPage = 0; //0-weekly, 1-monthly
    
    List<MultiplayerScores> users;
    List<MultiplayerScores> usersWeekly;
    List<MultiplayerScores> usersMonthly;
    
    Map<String, MultiplayerScoresCounter> weeklyScoreCounters;
    
    @PostConstruct
    public void init(){
        initWeekly();
        initMonthly();
        users = usersWeekly;
    }
    
    public void initWeekly(){
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH);
        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        Date currentDate = new Date(year-1900, month, day);
        Date startDate = new Date(currentDate.getTime()-1000*60*60*24*6);
        
        usersWeekly = new LinkedList<>();
        initUsers(startDate, usersWeekly);
    }
    
    public void initMonthly(){
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH);
        Date startDate = new Date(year-1900, month, 1);
        
        usersMonthly = new LinkedList<>();
        initUsers(startDate, usersMonthly);
    }

    private  void initUsers(Date startDate, List<MultiplayerScores> users){
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
    
    private List getGames(Date startDate){
        Session session = database.HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        
        Query query = session.createQuery("FROM FinishedGame WHERE gameDate>=:startDate");
        List results = query.setDate("startDate", startDate).list();
        
        session.getTransaction().commit();
        session.close();
        
        return results;
    }
    
    private void countScores(List<MultiplayerScores> scores, Map<String,MultiplayerScoresCounter> scoreCounters) {
        for (MultiplayerScores user : scores) {
            MultiplayerScoresCounter myCounter = scoreCounters.get(user.getUsername());
            user.setScores(myCounter);
        }
    }
    
    private void sort(List scores) {
        Collections.sort(scores, (MultiplayerScores a, MultiplayerScores b) -> {
            if (a.getTotalScore() > b.getTotalScore()) return -1;
            if (a.getTotalScore() < b.getTotalScore()) return 1;
            return 0;
        });
    }
    
    public short getRankingPage() {
        return rankingPage;
    }

    public void setRankingPage(short rankingPage) {
        if(rankingPage==0) users = usersWeekly;
        else users = usersMonthly;
        this.rankingPage = rankingPage;
    }

    public Map<String, MultiplayerScoresCounter> getWeeklyScoreCounters() {
        return weeklyScoreCounters;
    }
    
    public boolean isMyPage(int i){
        return rankingPage == i;
    }

    public List<MultiplayerScores> getUsers() {
        return users;
    }

}
