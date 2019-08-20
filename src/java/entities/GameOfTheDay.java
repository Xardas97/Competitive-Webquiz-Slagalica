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
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import util.PreparationManager;

/**
 *
 * @author Marko
 */

@Entity
@Table(name="game_of_the_day")
public class GameOfTheDay implements Serializable{
    @Id
    @Temporal(javax.persistence.TemporalType.DATE)
    Date gameDate;
    String letters;
    String numbers;
    String secretCombo;
    @OneToOne
    @JoinColumn(name="idWP")
    WordPairs pairs;
    @OneToOne
    @JoinColumn(name="idA")
    Asocijacija asocijacija;
    boolean played;
    
    public GameOfTheDay(Date gameDate, WordPairs pairs, Asocijacija asocijacija) {
        this.gameDate = gameDate;
        this.pairs = pairs;
        this.asocijacija = asocijacija;
        letters = PreparationManager.generateSlagalica();
        numbers = PreparationManager.generateMojBroj();
        secretCombo = PreparationManager.generateSkocko();
    }
    
    public GameOfTheDay() {}

    public Asocijacija getAsocijacija() {
        return asocijacija;
    }

    public void setAsocijacija(Asocijacija asocijacija) {
        this.asocijacija = asocijacija;
    }
    
    public Date getGameDate() {
        return gameDate;
    }

    public void setGameDate(Date gameDate) {
        this.gameDate = gameDate;
    }

    public String getLetters() {
        return letters;
    }

    public void setLetters(String letters) {
        this.letters = letters;
    }

    public String getNumbers() {
        return numbers;
    }

    public void setNumbers(String numbers) {
        this.numbers = numbers;
    }

    public String getSecretCombo() {
        return secretCombo;
    }

    public void setSecretCombo(String secretCombo) {
        this.secretCombo = secretCombo;
    }

    public WordPairs getPairs() {
        return pairs;
    }

    public void setPairs(WordPairs pairs) {
        this.pairs = pairs;
    }

    public boolean isPlayed() {
        return played;
    }

    public void setPlayed(boolean played) {
        this.played = played;
    }
    
}
