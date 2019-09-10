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

import games.generators.MojBrojGenerator;
import games.generators.SkockoGenerator;
import games.generators.SlagalicaGenerator;

/**
 *
 * @author Marko
 */

@Entity
@Table(name="game_of_the_day")
public class GameOfTheDay implements Serializable{
    @Id
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date date;

    private String letters;
    private String numbers;
    private String secretCombo;
    @OneToOne
    @JoinColumn(name="idWP")
    private WordPairs pairs;
    @OneToOne
    @JoinColumn(name="idA")
    private Asocijacija asocijacija;

    private boolean played;
    
    public GameOfTheDay(Date date, WordPairs pairs, Asocijacija asocijacija) {
        this.date = date;
        this.pairs = pairs;
        this.asocijacija = asocijacija;
        letters = SlagalicaGenerator.generate();
        numbers = MojBrojGenerator.generate();
        secretCombo = SkockoGenerator.generate();
    }
    
    public GameOfTheDay() {}

    public Asocijacija getAsocijacija() {
        return asocijacija;
    }

    public void setAsocijacija(Asocijacija asocijacija) {
        this.asocijacija = asocijacija;
    }
    
    public Date getDate() {
        return date;
    }

    public void setDate(Date gameDate) {
        this.date = gameDate;
    }

    public String getLetters() {
        return letters;
    }

    public String[] getLettersAsArray() {
        return letters.split(" ");
    }

    public void setLetters(String letters) {
        this.letters = letters;
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

    public String getSecretCombo() {
        return secretCombo;
    }

    public String[] getSecretComboAsArray() {
        return secretCombo.split(" ");
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
