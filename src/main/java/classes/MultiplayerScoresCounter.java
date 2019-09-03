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
    private int victories=0;
    private int defeats=0;
    private int draws=0;
    
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

    public int getDefeats() {
        return defeats;
    }

    public int getDraws() {
        return draws;
    }

}
