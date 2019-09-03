/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import entities.Asocijacija;
import entities.AsocijacijeVariables;
import entities.MojBrojVariables;
import entities.SkockoVariables;
import entities.SlagalicaVariables;
import entities.SpojniceVariables;
import entities.WordPairs;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import games.Skocko;
import games.Spojnice;
import org.hibernate.query.Query;
import org.hibernate.Session;

/**
 *
 * @author Marko
 */
public class PreparationManager {
    private static final String[] LETTERS = {"A","B","V","G","D","Đ","E","Ž","Z","I","J","K","L","LJ","M","N","NJ","O","P","R","S","T","Ć","U","F","H","C","Č","DŽ","Š"};
    private static final String[] FIRST_NUMBER_POOL = {"10", "15", "20"};
    private static final String[] SECOND_NUMBER_POOL = {"25", "50", "75", "100"};
    private static final String[] SKOCKO_SYMBOLS = Skocko.getSymbols();
    
    public static String generateSlagalica(Session session, String blue, String red, boolean multiplayer){
        Random rnd = ThreadLocalRandom.current();
        StringBuilder builder = new StringBuilder();
        builder.append(LETTERS[rnd.nextInt(30)]);
        for(int i=1; i<12; i++){
            builder.append(' ').append(LETTERS[rnd.nextInt(30)]);
        }
        
        String generatedLetters = builder.toString();
        if(multiplayer)  session.save(new SlagalicaVariables(blue, red, generatedLetters));
        return generatedLetters;
    }
    
    public static String generateMojBroj(Session session, String blue, String red, boolean multiplayer){
        Random rnd = ThreadLocalRandom.current();
        StringBuilder builder = new StringBuilder();
        builder.append(rnd.nextInt(999)+1);
        for(int i=1; i<5; i++){
            builder.append(' ').append(rnd.nextInt(9)+1);
        }
        builder.append(' ').append(FIRST_NUMBER_POOL[rnd.nextInt(3)]);
        builder.append(' ').append(SECOND_NUMBER_POOL[rnd.nextInt(4)]);
        
        String generatedNumbers = builder.toString();
        if(multiplayer) session.save(new MojBrojVariables(blue, red, generatedNumbers));
        return generatedNumbers;
    }
    
    public static String generateSkocko(Session session, String blue, String red, boolean multiplayer, boolean newEntry){
        Random rnd = ThreadLocalRandom.current();
        StringBuilder builder = new StringBuilder();
        builder.append(SKOCKO_SYMBOLS[rnd.nextInt(6)]);
        for(int i=1; i<4; i++){
            builder.append(' ').append(SKOCKO_SYMBOLS[rnd.nextInt(6)]);
        }
        
        String generatedCombo = builder.toString();
        if(multiplayer) 
            if(newEntry) session.save(new SkockoVariables(blue, red, generatedCombo));
            else{
                Query query = session.createQuery("FROM SkockoVariables WHERE blue=?");
                SkockoVariables skockoVars = (SkockoVariables) query
                        .setParameter(0, blue)
                        .uniqueResult();
                skockoVars.prepareNewGame(generatedCombo, false);
            }
        return generatedCombo;
    }
    
    public static void generateSpojnice(Session session, String blue, String red, Spojnice spojnice, boolean newEntry){
        WordPairs wordPairs = (WordPairs) session
                .createQuery("FROM WordPairs ORDER BY rand(5)")
                .setMaxResults(1).uniqueResult();
        
        String gameText = wordPairs.getText();
        createSpojniceWordAndPositionArrays(wordPairs.getPairs().split("-"), spojnice);
        
        if (newEntry) session.save(new SpojniceVariables(blue, red, spojnice.getPairPosition(), wordPairs));
        else {
            Query query = session.createQuery("FROM SpojniceVariables WHERE blue=?");
            SpojniceVariables spojniceVars = (SpojniceVariables) query.setParameter(0, blue).uniqueResult();
            spojniceVars.prepareNewGame(spojnice.getPairPosition(), wordPairs, false);
        }

        spojnice.setGameName(gameText);
    }
    
    public static Asocijacija generateAsocijacije(Session session, String blue, String red){
        Asocijacija asocijacija = (Asocijacija) session
                .createQuery("FROM Asocijacija ORDER BY rand(5)")
                .setMaxResults(1).uniqueResult();
        
        session.save(new AsocijacijeVariables(blue, red, asocijacija));
        
        return asocijacija;
    }
    
    public static String[] loadSlagalica(Session session, String red) {
        return ((SlagalicaVariables) session.createQuery("FROM SlagalicaVariables WHERE red=?").
                setParameter(0, red).uniqueResult()).getLetters().split(" ");
    }
    
    public static String[] loadMojBroj(Session session, String red){
        return ((MojBrojVariables) session.createQuery("FROM MojBrojVariables WHERE red=?").
                setParameter(0, red).uniqueResult()).getNumbers().split(" ");
    }
    
    public static String[] loadSkocko(Session session, String red){
        return ((SkockoVariables) session.createQuery("FROM SkockoVariables WHERE red=?").
                setParameter(0, red).uniqueResult()).getSecretCombo().split(" ");
    }
    
    public static void loadSpojnice(Session session, String red, Spojnice spojnice){
        SpojniceVariables spojniceVars = (SpojniceVariables) session.createQuery("FROM SpojniceVariables WHERE red=?").
                setParameter(0, red).uniqueResult();
        
        String[] pairPositionStrings = spojniceVars.getPairPosition().split(" ");
        for(int i=0; i<10; i++) spojnice.getPairPosition()[i] = Integer.parseInt(pairPositionStrings[i]);
        
        String[] pairs = spojniceVars.getPairs().getPairs().split("-");
        String[] pair;
        for(int i=0; i<10; i++){
            pair = pairs[i].split("/");
            spojnice.getWords()[i][0] = pair[0];
            spojnice.getWords()[i][1] = pair[1];
        }

        spojnice.setGameName(spojniceVars.getPairs().getText());
    }
    
    public static Asocijacija loadAsocijacije(Session session, String red){
        return ((AsocijacijeVariables) session.
                createQuery("FROM AsocijacijeVariables WHERE red=?").
                setParameter(0, red).uniqueResult()).getAsocijacija();
    }
        
    public static String generateSlagalica(){
        return PreparationManager.generateSlagalica(null, null, null, false);
    }
    
    public static String generateMojBroj(){
        return PreparationManager.generateMojBroj(null, null, null, false);
    }
    
    public static String generateSkocko(){
        return PreparationManager.generateSkocko(null, null, null, false, true);
    }
        
    public static void createSpojniceWordAndPositionArrays(String[] pairs, Spojnice spojnice){
        String[][] words = spojnice.getWords();
        int[] pairPosition = spojnice.getPairPosition();

        String[] pair;
        for(int i=0; i<10; i++){
            pair = pairs[i].split("/");
            words[i][0] = pair[0];
            words[i][1] = pair[1];
        }
        
        for(int i=0; i<10; i++) pairPosition[i]=i;
        Random rnd = ThreadLocalRandom.current();
        for (int i = 9; i > 0; i--) {
          int index = rnd.nextInt(i + 1);
          int a = pairPosition[index];
          pairPosition[index] = pairPosition[i];
          pairPosition[i] = a;
        }
    }
}
