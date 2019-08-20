/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classes;

/**
 *
 * @author Marko
 */
public class MultiplayerScores {
    String username;
    float totalScore;
    float averageScore;
    int victories;
    int defeats;
    int draws;

    public MultiplayerScores(String username){
        this.username = username;
        totalScore = averageScore = victories = defeats = draws = 0;
    }
    
    public void setScores(MultiplayerScoresCounter counter){
        victories=counter.getVictories();
        draws = counter.getDraws();
        defeats = counter.getDefeats();
        
        averageScore = counter.CountAverageScore();
        totalScore = counter.countTotalScore();
    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getVictories() {
        return victories;
    }

    public void setVictories(int vicoties) {
        this.victories = vicoties;
    }

    public int getDefeats() {
        return defeats;
    }

    public void setDefeats(int defeats) {
        this.defeats = defeats;
    }

    public int getDraws() {
        return draws;
    }

    public void setDraws(int draws) {
        this.draws = draws;
    }

    public float getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(float totalScore) {
        this.totalScore = totalScore;
    }

    public float getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(float averageScore) {
        this.averageScore = averageScore;
    }
    
}
