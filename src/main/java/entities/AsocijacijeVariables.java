/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 *
 * @author Marko
 */

@Entity
@Table(name="game_asocijacije")
public class AsocijacijeVariables implements GameVariables, Serializable{
    @Id
    private
    String blue;
    @Id
    private String red;
    private int pointsBlue;
    private int pointsRed;
    private boolean bluePlaying;
    private String revealedByBlue;
    private String revealedByRed;
    private String opened;
    @OneToOne
    @JoinColumn(name="idA")
    private
    Asocijacija asocijacija;

    public AsocijacijeVariables(String blue, String red, Asocijacija asocijacija){
        this.blue = blue;
        this.red = red;
        this.asocijacija = asocijacija;
        pointsBlue = pointsRed = 0;
        revealedByBlue=revealedByRed="0 0 0 0 0";
        opened = "0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0";
        bluePlaying = true;
    }
    
    public AsocijacijeVariables(){}

    public void updateVariables(boolean[] opened, boolean[] revealedArray, boolean nextPlayer){
        StringBuilder builder = new StringBuilder();
        if(opened[0]) builder.append("1");
        else builder.append("0");
        for(int i=1; i<21; i++) 
            if(opened[i]) builder.append(" ").append("1");
            else builder.append(" ").append("0");
        this.opened = builder.toString();
        
        builder = new StringBuilder();
        if (revealedArray[0]) builder.append("1");
        else builder.append("0"); 
        for (int i = 1; i < 5; i++)
            if (revealedArray[i]) builder.append(" ").append("1");
            else builder.append(" ").append("0");
            
        if(bluePlaying) this.revealedByBlue = builder.toString();
        else this.revealedByRed = builder.toString();
        
        bluePlaying = nextPlayer;
    }
    
    public String getBlue() {
        return blue;
    }

    public void setBlue(String blue) {
        this.blue = blue;
    }

    public String getRed() {
        return red;
    }

    public void setRed(String red) {
        this.red = red;
    }

    public int getPointsBlue() {
        return pointsBlue;
    }

    public void setPointsBlue(int pointsBlue) {
        this.pointsBlue = pointsBlue;
    }

    public int getPointsRed() {
        return pointsRed;
    }

    public void setPointsRed(int pointsRed) {
        this.pointsRed = pointsRed;
    }

    public boolean isBluePlaying() {
        return bluePlaying;
    }

    public void setBluePlaying(boolean bluePlaying) {
        this.bluePlaying = bluePlaying;
    }

    public String getRevealedByBlue() {
        return revealedByBlue;
    }

    public void setRevealedByBlue(String revealedByBlue) {
        this.revealedByBlue = revealedByBlue;
    }

    public String getRevealedByRed() {
        return revealedByRed;
    }

    public void setRevealedByRed(String revealedByRed) {
        this.revealedByRed = revealedByRed;
    }

    public String getOpened() {
        return opened;
    }

    public void setOpened(String opened) {
        this.opened = opened;
    }

    public Asocijacija getAsocijacija() {
        return asocijacija;
    }

    public void setAsocijacija(Asocijacija asocijacija) {
        this.asocijacija = asocijacija;
    }
    
}
