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
@Table(name="asocijacije")
public class Asocijacija implements Serializable{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)        
    int idA;
    private String columns;
    private String resultA;
    private String resultB;
    private String resultC;
    private String resultD;
    private String resultEnd;

    public Asocijacija(String[][] columns, String[] results, String finalResult) {
        resultA = results[0];
        resultB = results[1];
        resultC = results[2];
        resultD = results[3];
        resultEnd = finalResult;
        
        StringBuilder builder = new StringBuilder();
        builder.append(columns[0][0]);
        for(int i=0; i<4; i++)
            for(int j=0; j<4; j++)
                if(i>0 || j>0) builder.append("-").append(columns[j][i]);
        this.columns = builder.toString();
    }
    
    public Asocijacija() {}

    public int getIdA() {
        return idA;
    }

    public void setIdA(int idA) {
        this.idA = idA;
    }

    public String getColumns() {
        return columns;
    }

    public void setColumns(String columns) {
        this.columns = columns;
    }

    public String getResultA() {
        return resultA;
    }

    public void setResultA(String resultA) {
        this.resultA = resultA;
    }

    public String getResultB() {
        return resultB;
    }

    public void setResultB(String resultB) {
        this.resultB = resultB;
    }

    public String getResultC() {
        return resultC;
    }

    public void setResultC(String resultC) {
        this.resultC = resultC;
    }

    public String getResultD() {
        return resultD;
    }

    public void setResultD(String resultD) {
        this.resultD = resultD;
    }

    public String getResultEnd() {
        return resultEnd;
    }

    public void setResultEnd(String resultEnd) {
        this.resultEnd = resultEnd;
    }
    
}
