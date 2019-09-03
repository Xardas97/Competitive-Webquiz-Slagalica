/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 *
 * @author Marko
 */

@Entity
@Table(name="singleplayergame")
public class SingleplayerGame implements Serializable{
    @Id
    private String username;
    private int points;
    @Id
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date gameDate;

    public SingleplayerGame(){}
    public SingleplayerGame(String username, int points){
        this.username = username;
        this.points = points;
        gameDate = new Date();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public Date getGameDate() {
        return gameDate;
    }

    public void setGameDate(Date gameDate) {
        this.gameDate = gameDate;
    }
    
}
