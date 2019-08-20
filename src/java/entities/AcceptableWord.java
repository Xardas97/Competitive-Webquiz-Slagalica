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
@Table(name="acceptable_words")
public class AcceptableWord implements Serializable{
    @Id
    String word;

    public AcceptableWord(String word){
        this.word = word;
    }
    
    public AcceptableWord(){}
    
    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
    
}
