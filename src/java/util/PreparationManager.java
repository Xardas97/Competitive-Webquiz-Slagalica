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
import org.hibernate.Query;
import org.hibernate.Session;

/**
 *
 * @author Marko
 */
public class PreparationManager {
    private static final String[] letters = {"A","B","V","G","D","Đ","E","Ž","Z","I","J","K","L","LJ","M","N","NJ","O","P","R","S","T","Ć","U","F","H","C","Č","DŽ","Š"};
    //private static final String[] letters = {"А","Б","В","Г","Д","Ђ","Е","Ж","З","И","Ј","К","Л","Љ","М","Н","Њ","О","П","Р","С","Т","Ћ","У","Ф","Х","Ц","Ч","Џ","Ш"};
    private static final String[] firstNumberPool = {"10", "15", "20"};
    private static final String[] secondNumberPool = {"25", "50", "75", "100"};
    private static final String[] skockoSymbols = {"Pik", "Tref", "Herc", "Karo", "Zvezda", "Skocko"};
    
    public static String generateSlagalica(Session session, String blue, String red, boolean multiplayer){
        Random rnd = ThreadLocalRandom.current();
        StringBuilder builder = new StringBuilder();
        builder.append(letters[rnd.nextInt(30)]);
        for(int i=1; i<12; i++){
            builder.append(' ').append(letters[rnd.nextInt(30)]);
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
        builder.append(' ').append(firstNumberPool[rnd.nextInt(3)]);
        builder.append(' ').append(secondNumberPool[rnd.nextInt(4)]);
        
        String generatedNumbers = builder.toString();
        if(multiplayer) session.save(new MojBrojVariables(blue, red, generatedNumbers));
        return generatedNumbers;
    }
    
    public static String generateSkocko(Session session, String blue, String red, boolean multiplayer, boolean newEntry){
        Random rnd = ThreadLocalRandom.current();
        StringBuilder builder = new StringBuilder();
        builder.append(skockoSymbols[rnd.nextInt(6)]);
        for(int i=1; i<4; i++){
            builder.append(' ').append(skockoSymbols[rnd.nextInt(6)]);
        }
        
        String generatedCombo = builder.toString();
        if(multiplayer) 
            if(newEntry) session.save(new SkockoVariables(blue, red, generatedCombo));
            else{
                Query query = session.createQuery("FROM SkockoVariables WHERE blue=:username");
                SkockoVariables skockoVars = (SkockoVariables) query.setString("username", blue).uniqueResult();
                skockoVars.prepareNewGame(generatedCombo, false);
            }
        return generatedCombo;
    }
    
    public static String generateSpojnice(Session session, String blue, String red, String[][] words, int[] pairPosition, boolean newEntry){
        WordPairs wordPairs = (WordPairs) session
                .createQuery("FROM WordPairs ORDER BY rand()")
                .setMaxResults(1).uniqueResult();
        
        String gameText = wordPairs.getText();
        createSpojniceWordAndPositionArrays(wordPairs.getPairs().split("-"), words, pairPosition);
        
        if (newEntry) session.save(new SpojniceVariables(blue, red, pairPosition, wordPairs));
        else {
            Query query = session.createQuery("FROM SpojniceVariables WHERE blue=:username");
            SpojniceVariables spojniceVars = (SpojniceVariables) query.setString("username", blue).uniqueResult();
            spojniceVars.prepareNewGame(pairPosition, wordPairs, false);
        }
        
        return gameText;
    }
    
    public static Asocijacija generateAsocijacije(Session session, String blue, String red){
        Asocijacija asocijacija = (Asocijacija) session
                .createQuery("FROM Asocijacija ORDER BY rand()")
                .setMaxResults(1).uniqueResult();
        
        session.save(new AsocijacijeVariables(blue, red, asocijacija));
        
        return asocijacija;
    }
    
    public static String[] loadSlagalica(Session session, String red) {
        return ((SlagalicaVariables) session.createQuery("FROM SlagalicaVariables WHERE red=:username").
                setString("username", red).uniqueResult()).getLetters().split(" ");
    }
    
    public static String[] loadMojBroj(Session session, String red){
        return ((MojBrojVariables) session.createQuery("FROM MojBrojVariables WHERE red=:username").
                setString("username", red).uniqueResult()).getNumbers().split(" ");
    }
    
    public static String[] loadSkocko(Session session, String red){
        return ((SkockoVariables) session.createQuery("FROM SkockoVariables WHERE red=:username").
                setString("username", red).uniqueResult()).getSecretCombo().split(" ");
    }
    
    public static String loadSpojnice(Session session, String red, String[][] words, int[] pairPosition){
        SpojniceVariables spojniceVars = (SpojniceVariables) session.createQuery("FROM SpojniceVariables WHERE red=:username").
                setString("username", red).uniqueResult();
        
        String[] pairPositionStrings = spojniceVars.getPairPosition().split(" ");
        for(int i=0; i<10; i++) pairPosition[i] = Integer.parseInt(pairPositionStrings[i]);
        
        String[] pairs = spojniceVars.getPairs().getPairs().split("-");
        String[] pair;
        for(int i=0; i<10; i++){
            pair = pairs[i].split("/");
            words[i][0] = pair[0];
            words[i][1] = pair[1];
        }
        
        return spojniceVars.getPairs().getText();
    }
    
    public static Asocijacija loadAsocijacije(Session session, String red){
        return ((AsocijacijeVariables) session.
                createQuery("FROM AsocijacijeVariables WHERE red=:username").
                setString("username", red).uniqueResult()).getAsocijacija();
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

    public static String[] getSkockoSymbols() {
        return skockoSymbols;
    }
        
    public static void createSpojniceWordAndPositionArrays(String[] pairs, String[][] words, int[] pairPosition){
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
