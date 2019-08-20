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
public class MultiplayerScoresCounter {
    int victories=0;
    int defeats=0;
    int draws=0;
    
    public float countTotalScore(){
        int victoryPoints = 2*victories+defeats;
        int defeatPoints = 2*defeats;
        if(defeats==0) defeatPoints = 1;
        return (float)victoryPoints/defeatPoints + (float)victoryPoints/5;
    }
    
    public float CountAverageScore() {
        int victoryPoints = 2 * victories + defeats;
        int defeatPoints = 2 * defeats;
        if(defeats==0) defeatPoints = 1;
        return (float) victoryPoints / defeatPoints ;
    }
    
    public void incDefeats(){
        defeats++;
    }
    
    public void incDraws(){
        draws++;
    }
    
    public void incVictories(){
        victories++;
    }
    
    public int getVictories() {
        return victories;
    }

    public void setVictories(int victories) {
        this.victories = victories;
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
    
}
