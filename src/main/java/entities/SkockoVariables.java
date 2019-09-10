/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import games.SidePlayerGame;
import games.Skocko;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author Marko
 */

@Entity
@Table(name="game_skocko")
public class SkockoVariables implements SidePlayerGameVariables, Serializable{
    @Id
    private
    String red;
    @Id
    private
    String blue;
    private String secretCombo;
    private int pointsBlue;
    private int pointsRed;
    private boolean bluePlaying;
    private boolean sidePlayerDone;
    private String inputCombos;
    private String outputCombos;

    public SkockoVariables(String blue, String red, String secretCombo){
        this.blue = blue;
        this.red = red;
        pointsBlue = pointsRed = 0;
        prepareNewGame(secretCombo, true);
    }
    
    public SkockoVariables() {}
    
    final public void prepareNewGame(String secretCombo, boolean bluePlaying){
        this.secretCombo = secretCombo;
        this.bluePlaying = bluePlaying;
        sidePlayerDone = false;
        inputCombos = "x x x x-x x x x-x x x x-x x x x-x x x x-x x x x-x x x x";
        outputCombos = "0 0-0 0-0 0-0 0-0 0-0 0-0 0";
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

    public String getSecretCombo() {
        return secretCombo;
    }

    public String[] getSecretComboAsArray() {
        return secretCombo.split(" ");
    }

    public void setSecretCombo(String letters) {
        this.secretCombo = letters;
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

    @Override
    public boolean isBluePlaying() {
        return bluePlaying;
    }

    @Override
    public void updateVariables(SidePlayerGame game, boolean forBlue) {
        if(game instanceof Skocko){
            setInputCombos(((Skocko) game).getInputAsString());
            setOutputCombos(((Skocko) game).getFeedbackAsString());
        }
    }

    @Override
    public void setBluePlaying(boolean bluePlaying) {
        this.bluePlaying = bluePlaying;
    }

    public String getInputCombos() {
        return inputCombos;
    }

    private void setInputCombos(String inputCombos) {
        this.inputCombos = inputCombos;
    }

    public String getOutputCombos() {
        return outputCombos;
    }

    private void setOutputCombos(String outputCombos) {
        this.outputCombos = outputCombos;
    }

    @Override
    public boolean isSidePlayerDone() {
        return sidePlayerDone;
    }

    @Override
    public void setSidePlayerDone(boolean sidePlayerDone) {
        this.sidePlayerDone = sidePlayerDone;
    }
    
}
