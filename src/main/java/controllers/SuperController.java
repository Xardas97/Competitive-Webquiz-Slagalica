/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import static services.TransactionService.*;
import entities.AcceptableWord;
import entities.Asocijacija;
import entities.WordPairs;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import javax.annotation.ManagedBean;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import org.hibernate.Session;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

/**
 *
 * @author Marko
 */

@ManagedBean
@SessionScoped
@Named(value="SuperController")
public class SuperController implements Serializable{
    private short pageTab  = 0; //0-slagalica, 1-spojnice , 2-asocijacije
    private String fileContent;
    private String fileName;
    
    private String text;
    private String[][] pairs = new String[10][2];

    private String[][] columns = {{"A1", "B1", "C1", "D1"},{"A2", "B2", "C2", "D2"},{"A3", "B3", "C3", "D3"},{"A4", "B4", "C4", "D4"}};
    private String[] result = {"A","B","C","D"};
    private String resultEnd = "Final";
    
    public void submitAsocijacije() {
        Asocijacija asocijacija = new Asocijacija(columns, result, resultEnd);
        
        Session session = openTransaction();
        session.save(asocijacija);
        closeTransaction(session);
        
        columns[0][0] = "A1"; columns[0][1] = "B1"; columns[0][2] = "C1"; columns[0][3] = "D1";
        columns[1][0] = "A2"; columns[1][1] = "B2"; columns[1][2] = "C2"; columns[1][3] = "D2"; 
        columns[2][0] = "A3"; columns[2][1] = "B3"; columns[2][2] = "C3"; columns[2][3] = "D3"; 
        columns[3][0] = "A4"; columns[3][1] = "B4"; columns[3][2] = "C4"; columns[3][3] = "D4"; 
        result[0] = "A"; result[1] = "B"; result[2] = "C"; result[3] = "D";
        resultEnd = "Final";
    }
    
    public void submitSlagalica() {
        Session session = openTransaction();
        
        for(String word: fileContent.split("\n")){
            if(session.createQuery("FROM AcceptableWord WHERE word=?")
                    .setParameter(0, word)
                    .uniqueResult()==null)
                session.save(new AcceptableWord(word));
        }

        closeTransaction(session);
    }
    
    public void handleUpload(FileUploadEvent event) {
        UploadedFile file = event.getFile();
        fileName = file.getFileName();
        fileContent = new String(file.getContents(), StandardCharsets.UTF_8);
    }
    
    public void submitSpojnice(){
        WordPairs wordPairs = new WordPairs(text, pairs);
        
        Session session = openTransaction();
        session.save(wordPairs);
        closeTransaction(session);
        
        pairs = new String[10][2];
        text = "";
    }
    
    public String[] getResult() {
        return result;
    }

    public void setResult(String[] result) {
        this.result = result;
    }
    
    public String getFileContent() {
        return fileContent;
    }

    public String getFileName() {
        return fileName;
    }
    
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String[][] getPairs() {
        return pairs;
    }

    public void setPairs(String[][] pairs) {
        this.pairs = pairs;
    }
    
    public boolean isMyPage(short i){
        return i==pageTab;
    }

    public short getPageTab() {
        return pageTab;
    }

    public void setPageTab(short pageTab) {
        this.pageTab = pageTab;
    }

    public String[][] getColumns() {
        return columns;
    }

    public void setColumns(String[][] columns) {
        this.columns = columns;
    }
    
    public String getResultEnd() {
        return resultEnd;
    }

    public void setResultEnd(String resultEnd) {
        this.resultEnd = resultEnd;
    }
    
}
