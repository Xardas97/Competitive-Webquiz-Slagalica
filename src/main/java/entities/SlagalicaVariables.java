/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author Marko
 */

@Entity
@Table(name="game_slagalica")
public class SlagalicaVariables implements GameVariables, Serializable{
    @Id
    private
    String red;
    @Id
    private
    String blue;
    private String letters;
    private int pointsBlue;
    private int pointsRed;

    public SlagalicaVariables(String blue, String red, String letters){
        this.blue = blue;
        this.red = red;
        this.letters = letters;
        pointsBlue = pointsRed = 0;
    }
    
    public SlagalicaVariables() {}
    
    public String getRed() {
        return red;
    }

    public void setRed(String red) {
        this.red = red;
    }

    public String getBlue() {
        return blue;
    }

    public void setBlue(String blue) {
        this.blue = blue;
    }

    public String getLetters() {
        return letters;
    }

    public void setLetters(String letters) {
        this.letters = letters;
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
    
}
