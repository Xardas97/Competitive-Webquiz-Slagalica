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
@Table(name="activegame")
public class ActiveGame implements Serializable {
    @Id
    private String blue;

    @Id
    private String red;

    private boolean blueReady;
    private boolean redReady;

    public ActiveGame() {}
    public ActiveGame(String blue, String red){
        this.blue = blue;
        this.red = red;
        blueReady=redReady=false;
    }

    public boolean isBlueReady() {
        return blueReady;
    }

    public void setBlueReady(boolean blueReady) {
        this.blueReady = blueReady;
    }

    public boolean isRedReady() {
        return redReady;
    }

    public void setRedReady(boolean redReady) {
        this.redReady = redReady;
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
    
}
