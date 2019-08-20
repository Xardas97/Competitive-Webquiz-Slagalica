/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author Marko
 */

@Entity
@Table(name="wordpairs")
public class WordPairs implements Serializable{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    int idWP;
    String text;
    String pairs;

    public WordPairs(){}
    
    public WordPairs(String text, String[][] pairs){
        this.text = text;
        StringBuilder builder = new StringBuilder();
        builder.append(pairs[0][0]).append("/").append(pairs[0][1]);
        for(int i=1; i<10; i++) builder.append("-").append(pairs[i][0]).append("/").append(pairs[i][1]);
        this.pairs = builder.toString();
    }
    
    public int getIdWP() {
        return idWP;
    }

    public void setIdWP(int idWP) {
        this.idWP = idWP;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPairs() {
        return pairs;
    }

    public void setPairs(String pairs) {
        this.pairs = pairs;
    }
    
    @Override
    public String toString(){
        return text;
    }
}
