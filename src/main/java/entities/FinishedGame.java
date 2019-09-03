/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 *
 * @author Marko
 */

@Entity
@Table(name="finishedgame")
public class FinishedGame implements Serializable {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    int gid; //game ID
    private String blue;
    private String red;
    private int pointsBlue;
    private int pointsRed;
    private short gameResult;
    @Temporal(javax.persistence.TemporalType.DATE)
    private
    Date gameDate;

    public FinishedGame(){}
    public FinishedGame(ActiveGame activeGame, int totalBlue, int totalRed){
        blue = activeGame.getBlue();
        red = activeGame.getRed();
        pointsBlue = totalBlue;
        pointsRed = totalRed;
        gameDate = new Date();
    }
    
    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
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

    public short getGameResult() {
        return gameResult;
    }

    public void setGameResult(short gameResult) {
        this.gameResult = gameResult;
    }

    public Date getGameDate() {
        return gameDate;
    }

    public void setGameDate(Date gameDate) {
        this.gameDate = gameDate;
    }
    
}
