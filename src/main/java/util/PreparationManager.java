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

/**
 *
 * @author Marko
 */
public class PreparationManager {
    private static final String[] LETTERS = {"A","B","V","G","D","Đ","E","Ž","Z","I","J","K","L","LJ","M","N","NJ","O","P","R","S","T","Ć","U","F","H","C","Č","DŽ","Š"};
    private static final String[] FIRST_NUMBER_POOL = {"10", "15", "20"};
    private static final String[] SECOND_NUMBER_POOL = {"25", "50", "75", "100"};
    private static final String[] SKOCKO_SYMBOLS = Skocko.getSymbols();
    
    public static String generateSlagalica(Transaction transaction, String blue, String red, boolean multiplayer){
        Random rnd = ThreadLocalRandom.current();
        StringBuilder builder = new StringBuilder();
        builder.append(LETTERS[rnd.nextInt(30)]);
        for(int i=1; i<12; i++){
            builder.append(' ').append(LETTERS[rnd.nextInt(30)]);
        }
        
        String generatedLetters = builder.toString();
        if(multiplayer)  transaction.save(new SlagalicaVariables(blue, red, generatedLetters));
        return generatedLetters;
    }
    
    public static String generateMojBroj(Transaction transaction, String blue, String red, boolean multiplayer){
        Random rnd = ThreadLocalRandom.current();
        StringBuilder builder = new StringBuilder();
        builder.append(rnd.nextInt(999)+1);
        for(int i=1; i<5; i++){
            builder.append(' ').append(rnd.nextInt(9)+1);
        }
        builder.append(' ').append(FIRST_NUMBER_POOL[rnd.nextInt(3)]);
        builder.append(' ').append(SECOND_NUMBER_POOL[rnd.nextInt(4)]);
        
        String generatedNumbers = builder.toString();
        if(multiplayer) transaction.save(new MojBrojVariables(blue, red, generatedNumbers));
        return generatedNumbers;
    }
    
    public static String generateSkocko(Transaction transaction, String blue, String red, boolean multiplayer){
        Random rnd = ThreadLocalRandom.current();
        StringBuilder builder = new StringBuilder();
        builder.append(SKOCKO_SYMBOLS[rnd.nextInt(6)]);
        for(int i=1; i<4; i++){
            builder.append(' ').append(SKOCKO_SYMBOLS[rnd.nextInt(6)]);
        }
        
        String generatedCombo = builder.toString();
        if(multiplayer) {
            Query query = transaction.createQuery("FROM SkockoVariables WHERE blue=?");
            SkockoVariables skockoVars = (SkockoVariables) query
                    .setParameter(0, blue)
                    .uniqueResult();

            if (skockoVars == null) {
                skockoVars = new SkockoVariables(blue, red, generatedCombo);
                transaction.save(skockoVars);
            }
            else {
                skockoVars.prepareNewGame(generatedCombo, false);
            }
        }
        return generatedCombo;
    }
    
    public static void generateSpojnice(Transaction transaction, String blue, String red, Spojnice spojnice){
        WordPairs wordPairs = (WordPairs) transaction
                .createQuery("FROM WordPairs ORDER BY rand(5)")
                .setMaxResults(1).uniqueResult();
        
        String gameText = wordPairs.getText();
        createSpojniceWordAndPositionArrays(wordPairs.getPairs().split("-"), spojnice);

        Query query = transaction.createQuery("FROM SpojniceVariables WHERE blue=?");
        SpojniceVariables spojniceVars = (SpojniceVariables) query.setParameter(0, blue).uniqueResult();

        if (spojniceVars == null) {
            spojniceVars = new SpojniceVariables(blue, red, spojnice.getPairPosition(), wordPairs);
            transaction.save(spojniceVars);
        }
        else {
            spojniceVars.prepareNewGame(spojnice.getPairPosition(), wordPairs, false);
        }

        spojnice.setGameName(gameText);
    }
    
    public static Asocijacija generateAsocijacije(Transaction transaction, String blue, String red){
        Asocijacija asocijacija = (Asocijacija) transaction
                .createQuery("FROM Asocijacija ORDER BY rand(5)")
                .setMaxResults(1).uniqueResult();
        
        transaction.save(new AsocijacijeVariables(blue, red, asocijacija));
        
        return asocijacija;
    }
    
    public static String[] loadSlagalica(Transaction transaction, String red) {
        return ((SlagalicaVariables) transaction.createQuery("FROM SlagalicaVariables WHERE red=?").
                setParameter(0, red).uniqueResult()).getLetters().split(" ");
    }
    
    public static String[] loadMojBroj(Transaction transaction, String red){
        return ((MojBrojVariables) transaction.createQuery("FROM MojBrojVariables WHERE red=?").
                setParameter(0, red).uniqueResult()).getNumbers().split(" ");
    }
    
    public static String[] loadSkocko(Transaction transaction, String red){
        return ((SkockoVariables) transaction.createQuery("FROM SkockoVariables WHERE red=?").
                setParameter(0, red).uniqueResult()).getSecretCombo().split(" ");
    }
    
    public static void loadSpojnice(Transaction transaction, String red, Spojnice spojnice){
        SpojniceVariables spojniceVars = (SpojniceVariables) transaction.createQuery("FROM SpojniceVariables WHERE red=?").
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
    
    public static Asocijacija loadAsocijacije(Transaction transaction, String red){
        return ((AsocijacijeVariables) transaction.
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
        return PreparationManager.generateSkocko(null, null, null, false);
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
