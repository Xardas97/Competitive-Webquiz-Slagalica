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
@Table(name="game_mojbroj")
public class MojBrojVariables implements GameVariables, Serializable {
    @Id
    private
    String red;
    @Id
    private
    String blue;
    private String numbers;
    private int pointsBlue;
    private int pointsRed;
    private int differenceBlue;
    private int differenceRed;

    public MojBrojVariables(String blue, String red, String numbers){
        this.blue = blue;
        this.red = red;
        this.numbers = numbers;
        pointsBlue = pointsRed = differenceBlue = differenceRed = 0;
    }
    
    public MojBrojVariables() {}

    @Override
    public void fixPoints() {
        if(pointsBlue == 10 && pointsRed== 10) {
            if(differenceBlue > differenceRed) {
                pointsBlue = 0;
            }
            else if(differenceBlue < differenceRed) {
                pointsRed = 0;
            }
            else {
                pointsBlue = pointsRed = 5;
            }
        }
    }

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

    public String getNumbers() {
        return numbers;
    }

    public String[] getNumbersAsArray() {
        return numbers.split(" ");
    }

    public void setNumbers(String numbers) {
        this.numbers = numbers;
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

    public int getDifferenceBlue() {
        return differenceBlue;
    }

    public void setDifferenceBlue(int differenceBlue) {
        this.differenceBlue = differenceBlue;
    }

    public void setDifference(int difference, boolean forBlue){
        if(forBlue) {
            setDifferenceBlue(difference);
        }
        else {
            setDifferenceRed(difference);
        }
    }

    public int getDifferenceRed() {
        return differenceRed;
    }

    public void setDifferenceRed(int differenceRed) {
        this.differenceRed = differenceRed;
    }
    
}
