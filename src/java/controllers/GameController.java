/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import classes.GamePoints;
import database.HibernateUtil;
import entities.ActiveGame;
import entities.Asocijacija;
import entities.AsocijacijeVariables;
import entities.FinishedGame;
import entities.GameOfTheDay;
import entities.MojBrojVariables;
import entities.SingleplayerGame;
import entities.SkockoVariables;
import entities.SlagalicaVariables;
import entities.SpojniceVariables;
import entities.WordPairs;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.hibernate.Query;
import org.hibernate.Session;
import util.PointsManager;
import util.PointsManager.IntegerWrapper;
import util.PreparationManager;
import static util.PreparationManager.createSpojniceWordAndPositionArrays;
import util.SessionManager;

/**
 *
 * @author Marko
 */

@ManagedBean
@ViewScoped
@Named(value="GameController")
public class GameController implements Serializable {
    public enum GameView{Waiting, Slagalica, MojBroj, Skocko, Spojnice, Asocijacije, GameOver}
    GameView gameView;
    GameView nextGame;
    GameView currentGame;
    
    int timer;
    long lastTimerTick;
    
    boolean playerBlue; //true-blue, false-red
    boolean modeMultiplayer; //true-multiplayer, false-singleplayer
    
    boolean preparationStarted;
    
    private String username;
    
    List<GamePoints> gamePoints = new LinkedList<>();; 
    //**********game variables**********
    // Slagalica
    String[] slagalicaLetters;
    String slagalicaWord;
    boolean[] slagalicaButtons = {true, true, true, true, true, true, true, true, true, true, true, true};
    // Moj Broj
    String[] mojBrojNumbers;
    String[] mojBrojOperations = {"+", "-", "*", "/", "(", ")"};
    String mojBrojWord;
    boolean[] mojBrojButtons = {true, true, true, true, true, true};
    boolean lastUsedNumber = false;
    String mojBrojMessage = "";
    // Skocko
    String[] skockoSymbols;
    String[] secretCombo;
    String[][] skockoInput = new String[7][4];
    int[][] skockoOutput = new int[7][2];
    boolean skockoBluePlaying = true;
    int currentRow = 0, currentSymbol = 0;
    boolean skockoRoundTwo = false;
    boolean skockoWaitingPeriod = false;
    int skockoPoints = 0;
    // Spojnice
    int activeLeft = 0;
    boolean spojniceRoundTwo = false;
    boolean spojniceBluePlaying = true;
    String gameName;
    String[][] spojniceWords = new String[10][2];
    boolean[] hitByBlue = new boolean[10];
    boolean[] hitByRed = new boolean[10];
    int[] pairPosition = new int[10];
    int spojnicePoints = 0;
    boolean spojniceWaitingPeriod = false;
    int spojniceSidePlayer = 0; //0 - undetermined, 1-main, 2-side
    // Asocijacije
    String[] asocijacijeColumns;
    String[] resultA, resultB, resultC, resultD;
    String[] resultEnd;
    String[] openedResults = {"", "", "", "", ""};
    final String[] asocijacijePlaceholders = {"A", "B", "C", "D", "? ? ?"};
    boolean[] asocijacijeOpened;
    boolean[] asocijacijeBlueReveal = {false, false, false, false, false}, asocijacijeRedReveal = {false, false, false, false, false};
    int asocijacijePoints = 0;
    boolean asocijacijeWaitingPeriod = false;
    boolean asocijacijeBluePlaying = true;
    boolean asocijacijeFieldOpened = false;
    boolean asocijacijeHit = false;
    //**********game variables**********
    
    @PostConstruct
    public void init(){
        currentGame = null;
        nextGame = GameView.Slagalica;
        gameView = GameView.Waiting;
        username = SessionManager.getUser().getUsername();
        modeMultiplayer = "multiplayer".equals(SessionManager.getGameMode());
        playerBlue = !modeMultiplayer || "blue".equals(SessionManager.getPlayerSide());
        lastTimerTick=0;
        
        slagalicaWord = "";
        mojBrojWord = "";
        skockoSymbols = PreparationManager.getSkockoSymbols();
        for(int i=0; i<7; i++) for(int j=0;j<4;j++) skockoInput[i][j] = "x";
        for(int i=0; i<10; i++) hitByBlue[i] = hitByRed[i] = false;
        
        asocijacijeOpened = new boolean[21];
        for(int i=0; i<21; i++) asocijacijeOpened[i]=false;
        
        if(playerBlue){
            preparationStarted = true;
            prepareNextGame();
        }
    }

    /** 
     * Called once every second 
     */
    public void periodicCall(){
        checkIfNextGameReady();
        checkIfCurrentGameOver();
    }
    
    /**
     * Prepares data for the next game.
     * Announces that he is ready.
     * Sleep instead of preparing data for testing purposes.
     */
    public void prepareNextGame() {
        preparationStarted = true;
        
        Session session = database.HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        
        if (modeMultiplayer) {
            String mySide = "red";
            if (playerBlue) mySide = "blue";
            
            Query query = session.createQuery("FROM ActiveGame WHERE " + mySide + "=:username");
            ActiveGame game = (ActiveGame) query.setString("username", username).uniqueResult();

            //call the preparation method for the next game
            if(currentGame==GameView.Skocko && !skockoRoundTwo)
                secretCombo = PreparationManager.generateSkocko(session, game.getBlue(), game.getRed(), true, false).split(" "); 
            else if(currentGame==GameView.Spojnice && !spojniceRoundTwo)
                gameName = PreparationManager.generateSpojnice(session, game.getBlue(), game.getRed(), spojniceWords, pairPosition, false);
            else switch (nextGame) {
                case Slagalica: 
                    slagalicaLetters = 
                        PreparationManager.generateSlagalica(session, game.getBlue(), game.getRed(), true).split(" "); 
                    break;
                case MojBroj: 
                    mojBrojNumbers = 
                        PreparationManager.generateMojBroj(session, game.getBlue(), game.getRed(), true).split(" ");
                    break;
                case Skocko:
                    secretCombo = 
                            PreparationManager.generateSkocko(session, game.getBlue(), game.getRed(), true, true).split(" "); 
                    break;
                case Spojnice: 
                    gameName = 
                            PreparationManager.generateSpojnice(session, game.getBlue(), game.getRed(), spojniceWords, pairPosition, true);
                    break;
                case Asocijacije:
                    initAsocijacije(PreparationManager.generateAsocijacije(session, game.getBlue(), game.getRed()));
                    break;
                default: break;
            }

            //do the synchronization
            game.setBlueReady(true);
        }
        //singleplayer
        else if(nextGame == GameView.Slagalica){
                Query query = session.createQuery("FROM GameOfTheDay WHERE gameDate=:currentDate");
                GameOfTheDay game = (GameOfTheDay) query.setDate("currentDate", new Date()).uniqueResult();
                
                slagalicaLetters = game.getLetters().split(" ");
                mojBrojNumbers = game.getNumbers().split(" ");
                secretCombo = game.getSecretCombo().split(" ");
                
                WordPairs spojnice = game.getPairs();
                gameName = spojnice.getText();
                createSpojniceWordAndPositionArrays(spojnice.getPairs().split("-"), spojniceWords, pairPosition);
                
                initAsocijacije(game.getAsocijacija());
                
                game.setPlayed(true);
            }
        session.getTransaction().commit();
        session.close();
    }
        
    public void loadPreparedGameData(Session session){
        if(modeMultiplayer && nextGame==GameView.Spojnice && !skockoRoundTwo)
            secretCombo = PreparationManager.loadSkocko(session, username);
        else if(modeMultiplayer && nextGame==GameView.Asocijacije && !spojniceRoundTwo)
            gameName = PreparationManager.loadSpojnice(session, username, spojniceWords, pairPosition);
        else switch (nextGame) {
            case Slagalica: slagalicaLetters = PreparationManager.loadSlagalica(session, username); break;
            case MojBroj: mojBrojNumbers = PreparationManager.loadMojBroj(session, username); break;
            case Skocko: secretCombo = PreparationManager.loadSkocko(session, username); break;
            case Spojnice: gameName = PreparationManager.loadSpojnice(session, username, spojniceWords, pairPosition); break;
            case Asocijacije: initAsocijacije(PreparationManager.loadAsocijacije(session, username)); break;
            default: break;
        }
    }
    
    /**
     * Gets periodically called while we are in a game.
     * If the timer has run out calls finished().
     */
    public void checkIfCurrentGameOver(){
        if(!isGameInProgress()) return;
        
        long currentTimerTick = System.currentTimeMillis();
        if(lastTimerTick==0 || currentTimerTick-lastTimerTick > 500) timer--;
        lastTimerTick = currentTimerTick;
        
        if (modeMultiplayer && currentGame==GameView.Skocko && !skockoWaitingPeriod) {
            skockoTicks();
            return;
        }
        if (modeMultiplayer && currentGame==GameView.Spojnice && !spojniceWaitingPeriod) {
            spojniceTicks();
            return;
        }
        if (modeMultiplayer && currentGame==GameView.Asocijacije && !asocijacijeWaitingPeriod) {
            asocijacijeTicks();
            return;
        }
        
        if(timer==0) finished();
    }

    /**
     * Updates our points after we are done with the game
     * and sends us to the waiting screen.
     */
    public void finished() {
        int myPoints = 0;
        IntegerWrapper difference = new IntegerWrapper();
        switch (currentGame) {
            case Slagalica: myPoints = PointsManager.slagalica(slagalicaWord, slagalicaButtons); break;
            case MojBroj: myPoints = PointsManager.mojBroj(mojBrojWord, mojBrojNumbers[0], difference); break;
            case Skocko: myPoints = skockoPoints; break;
            case Spojnice: myPoints = spojnicePoints; break;
            case Asocijacije: myPoints = asocijacijePoints; break;
            default: break;
        }

        if (modeMultiplayer) {
            //update my points in database so that the other player can see them
            Session session = database.HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            
            Query query;
            
            switch (currentGame) {
                case Slagalica: 
                    query = session.createQuery("FROM SlagalicaVariables WHERE blue=:username OR red=:username");
                    SlagalicaVariables slagalicaVars = (SlagalicaVariables) query.setString("username", username).uniqueResult();
                    if(playerBlue) slagalicaVars.setPointsBlue(myPoints);
                    else slagalicaVars.setPointsRed(myPoints);
                    break;
                case MojBroj: 
                    query = session.createQuery("FROM MojBrojVariables WHERE blue=:username OR red=:username");
                    MojBrojVariables mojBrojVars = (MojBrojVariables) query.setString("username", username).uniqueResult();
                    if(playerBlue) {
                        mojBrojVars.setPointsBlue(myPoints);
                        mojBrojVars.setDifferenceBlue(difference.value);
                    }
                    else {
                        mojBrojVars.setPointsRed(myPoints);
                        mojBrojVars.setDifferenceRed(difference.value);
                    }
                    break;
                case Skocko:
                    if(skockoRoundTwo){
                        query = session.createQuery("FROM SkockoVariables WHERE blue=:username OR red=:username");
                        SkockoVariables skockoVars = (SkockoVariables) query.setString("username", username).uniqueResult();
                        if(playerBlue) skockoVars.setPointsBlue(myPoints);
                        else skockoVars.setPointsRed(myPoints);
                    }
                    break;
                case Spojnice:
                    if(spojniceRoundTwo){
                        query = session.createQuery("FROM SpojniceVariables WHERE blue=:username OR red=:username");
                        SpojniceVariables spojniceVars = (SpojniceVariables) query.setString("username", username).uniqueResult();
                        if(playerBlue) spojniceVars.setPointsBlue(myPoints);
                        else spojniceVars.setPointsRed(myPoints);
                    }
                    break;
                case Asocijacije: 
                    query = session.createQuery("FROM AsocijacijeVariables WHERE blue=:username OR red=:username");
                    AsocijacijeVariables asocijacijeVars = (AsocijacijeVariables) query.setString("username", username).uniqueResult();
                    if(playerBlue) asocijacijeVars.setPointsBlue(myPoints);
                    else asocijacijeVars.setPointsRed(myPoints);
                    break;
                default: break;
            }
            
            session.getTransaction().commit();
            session.close();
        } else {
            //update my points in local storage
            gamePoints.add(new GamePoints(currentGame, myPoints));
        }

        preparationStarted = false;
        gameView = GameView.Waiting;
    }
    
    /**
     * Gets periodically called while we are waiting for the next game.
     * If both players are ready it changes to the next game.
     * If it's the end of the game Blue updates the database.
     */
    public void checkIfNextGameReady(){
        if(gameView!=GameView.Waiting) return;
        if(playerBlue && !preparationStarted) prepareNextGame();
        
        boolean gameReady = false;
        //multiplayer game, must synchronize and save points from the database
        if(modeMultiplayer){
            Session session = database.HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();

            Query query = session.createQuery("FROM ActiveGame WHERE blue=:username OR red=:username");
            query.setString("username", username);
            ActiveGame game = (ActiveGame) query.uniqueResult();

            if((game.isBlueReady() || playerBlue) && (game.isRedReady() || !playerBlue)) {
                //The other player is ready
                gameReady = true;
                //points of the last game are set, we can save them now
                if(currentGame!=null){
                    int pointsBlue=0, pointsRed=0;
                    switch(currentGame){
                        case Slagalica:
                            query = session.createQuery("FROM SlagalicaVariables WHERE blue=:username OR red=:username");
                            SlagalicaVariables slagalicaVars = (SlagalicaVariables) query.setString("username", username).uniqueResult();
                            
                            pointsBlue = slagalicaVars.getPointsBlue();
                            pointsRed = slagalicaVars.getPointsRed();
                            if(pointsBlue>pointsRed) pointsRed = 0;
                            else if(pointsRed > pointsBlue) pointsBlue = 0;
                            break;
                        case MojBroj: 
                            query = session.createQuery("FROM MojBrojVariables WHERE blue=:username OR red=:username");
                            MojBrojVariables mojBrojVars = (MojBrojVariables) query.setString("username", username).uniqueResult();
                            
                            pointsBlue = mojBrojVars.getPointsBlue();
                            pointsRed = mojBrojVars.getPointsRed();
                            
                            if(pointsBlue == 10 && pointsBlue == 10) {
                                if(mojBrojVars.getDifferenceBlue() > mojBrojVars.getDifferenceRed()) pointsBlue = 0;
                                else if(mojBrojVars.getDifferenceBlue() < mojBrojVars.getDifferenceRed()) pointsRed = 0;
                                else pointsBlue = pointsRed = 5;
                            }
                            break;
                        case Skocko:
                            if(skockoRoundTwo){
                                query = session.createQuery("FROM SkockoVariables WHERE blue=:username OR red=:username");
                                SkockoVariables skockoVars = (SkockoVariables) query.setString("username", username).uniqueResult();

                                pointsBlue = skockoVars.getPointsBlue();
                                pointsRed = skockoVars.getPointsRed(); 
                            }
                            break;
                        case Spojnice:
                            if(spojniceRoundTwo){
                                query = session.createQuery("FROM SpojniceVariables WHERE blue=:username OR red=:username");
                                SpojniceVariables spojniceVars = (SpojniceVariables) query.setString("username", username).uniqueResult();

                                pointsBlue = spojniceVars.getPointsBlue();
                                pointsRed = spojniceVars.getPointsRed(); 
                            }
                            break;
                        case Asocijacije:
                            query = session.createQuery("FROM AsocijacijeVariables WHERE blue=:username OR red=:username");
                            AsocijacijeVariables asocijacijeVars = (AsocijacijeVariables) query.setString("username", username).uniqueResult();

                            pointsBlue = asocijacijeVars.getPointsBlue();
                            pointsRed = asocijacijeVars.getPointsRed(); 
                            break;
                        default: break;
                    }
                    if((currentGame!=GameView.Skocko || skockoRoundTwo) 
                            && (currentGame!=GameView.Spojnice || spojniceRoundTwo))
                        gamePoints.add(new GamePoints(currentGame, pointsBlue, pointsRed));
                }
                //Red reads the next game data and announces that he is ready
                //Both reset the other's readiness
                if (!playerBlue) {
                    loadPreparedGameData(session);
                    game.setRedReady(true);
                    game.setBlueReady(false);
                }
                else game.setRedReady(false);
            }

            session.getTransaction().commit();
            session.close();
        }
        
        //if singleplayer (no sync required) or it's multiplayer and has been synchronized
        if(!modeMultiplayer || gameReady){
            if(modeMultiplayer){
                if(currentGame==GameView.Skocko && !skockoRoundTwo){
                    skockoVariableReset();
                    return;
                } 
                if(currentGame==GameView.Spojnice && !spojniceRoundTwo){
                     spojniceVariableReset();
                     return;
                } 
            }
            
            gameView = currentGame = nextGame;
            
            switch(currentGame){
                case Slagalica: nextGame = GameView.MojBroj; timer = 60; break;
                case MojBroj: nextGame = GameView.Skocko; timer = 60; break;
                case Skocko: nextGame = GameView.Spojnice; timer = 60; break;
                case Spojnice: nextGame = GameView.Asocijacije; timer = 60; break;
                case Asocijacije: nextGame = GameView.GameOver; timer = 240; break;
                case GameOver: if(modeMultiplayer) gameOverMultiplayer(); else gameOverSingleplayer();
            }
        }
    }

    /**
     * Deletes database entries needed for game variables
     * @param session
     */
    public void cleanUpDatabase(Session session){
        session.createQuery("DELETE FROM SlagalicaVariables WHERE blue=:username").
                setString("username", username).executeUpdate();
        session.createQuery("DELETE FROM MojBrojVariables WHERE blue=:username").
                setString("username", username).executeUpdate();
        session.createQuery("DELETE FROM SkockoVariables WHERE blue=:username").
                setString("username", username).executeUpdate();
        session.createQuery("DELETE FROM SpojniceVariables WHERE blue=:username").
                setString("username", username).executeUpdate();
        session.createQuery("DELETE FROM AsocijacijeVariables WHERE blue=:username").
                setString("username", username).executeUpdate();
    }
    
    public void gameOverSingleplayer() {
        //count the total points and save the final database entry
        int totalPoints = 0;
        for(GamePoints game: gamePoints) totalPoints+=game.blue;
        gamePoints.add(new GamePoints("Total", totalPoints));
        
        Session session = database.HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        
        SingleplayerGame finishedGame = new SingleplayerGame(username, totalPoints);
        
        session.save(finishedGame);

        session.getTransaction().commit();
        session.close();
    }
    
    public void gameOverMultiplayer(){
         //count the total points and save the final database entry
        int totalRed = 0;
        int totalBlue = 0;
        for(GamePoints game: gamePoints) { totalRed+=game.red; totalBlue+=game.blue; }
        gamePoints.add(new GamePoints("Total", totalBlue, totalRed));
        
        //if it's your job, save the final database entry
        if(playerBlue){
            Session session = database.HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();

            cleanUpDatabase(session);
            
            Query query = session.createQuery("FROM ActiveGame WHERE blue=:username OR red=:username");
            query.setString("username", username);
            ActiveGame activeGame = (ActiveGame) query.uniqueResult();

            FinishedGame finishedGame = new FinishedGame(activeGame, totalBlue, totalRed);
            if(finishedGame.getPointsBlue()>finishedGame.getPointsRed())
                finishedGame.setGameResult((short)1);
            else if(finishedGame.getPointsBlue()<finishedGame.getPointsRed())
                finishedGame.setGameResult((short)-1);
            else finishedGame.setGameResult((short)0);

            session.delete(activeGame);
            session.save(finishedGame);

            session.getTransaction().commit();
            session.close();            
        }
    }
    
    /*
    **** Asocijacije ****
    */
    
    public void submitAsocijacije(){
        asocijacijeFieldOpened = false;
        boolean[] myRevealArray;
        if(playerBlue) myRevealArray = asocijacijeBlueReveal;
        else myRevealArray = asocijacijeRedReveal;
        
        int submitted;
        if(asocijacijeHit) submitted = 4;
        else
            for(submitted=0; submitted<5; submitted++)
                if(!asocijacijeOpened[16+submitted] && !"".equals(openedResults[submitted])) break;
        
        asocijacijeHit = false;
        switch(submitted){
            case 0: 
                if(asocijacijeCorrect(openedResults[0], resultA)){
                    asocijacijePoints += 5;
                    for(int i=0; i<4; i++) asocijacijeOpened[i] = true;
                    asocijacijeOpened[16] = true;
                    myRevealArray[0] = true;
                    asocijacijeHit = true;
                }
                break;
            case 1: 
                if(asocijacijeCorrect(openedResults[1], resultB)){
                    asocijacijePoints += 5;
                    for(int i=4; i<8; i++) asocijacijeOpened[i] = true;
                    asocijacijeOpened[17] = true;
                    myRevealArray[1] = true;
                    asocijacijeHit = true;
                }
                break;
            case 2: 
                if(asocijacijeCorrect(openedResults[2], resultC)){
                    asocijacijePoints += 5;
                    for(int i=8; i<12; i++) asocijacijeOpened[i] = true;
                    asocijacijeOpened[18] = true;
                    myRevealArray[2] = true;
                    asocijacijeHit = true;
                }
                break;
            case 3: 
                if(asocijacijeCorrect(openedResults[3], resultD)){
                    asocijacijePoints += 5;
                    for(int i=12; i<16; i++) asocijacijeOpened[i] = true;
                    asocijacijeOpened[19] = true;
                    myRevealArray[3] = true;
                    asocijacijeHit = true;
                }
                break;
            case 4: 
                if(asocijacijeCorrect(openedResults[4], resultEnd)){
                    asocijacijePoints += 10;
                    myRevealArray[4] = true;
                    for(int i=0; i<4; i++) 
                        if(!asocijacijeOpened[16+i]) {
                        myRevealArray[i] = true;
                        asocijacijePoints+=5;
                    }
                    for(int i=0; i<21; i++) asocijacijeOpened[i] = true;
                    
                    asocijacijeWaitingPeriod = true;
                    timer = 3;
                }
                break;
            default: break;
        }
        for(int i=0; i<5; i++) openedResults[i] = "";
        
        if(modeMultiplayer){
            Session session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            Query query = session.createQuery("FROM AsocijacijeVariables WHERE blue=:username OR red=:username");
            AsocijacijeVariables asocijacijeVars = (AsocijacijeVariables) query.setString("username", username).uniqueResult();
            
            if(!asocijacijeHit){
                asocijacijeVars.setMessage(asocijacijeOpened, myRevealArray, !asocijacijeBluePlaying);
                asocijacijeBluePlaying = !asocijacijeBluePlaying;
            }
            else {
                asocijacijeVars.setMessage(asocijacijeOpened, myRevealArray, asocijacijeBluePlaying);
                asocijacijeFieldOpened = true;
            }
            
            session.getTransaction().commit();
            session.close();
        }
    }
    
    private void asocijacijeTicks(){
        if ((asocijacijeBluePlaying && !playerBlue) || (!asocijacijeBluePlaying && playerBlue)) {
            Session session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();

            Query query = session.createQuery("FROM AsocijacijeVariables WHERE blue=:username OR red=:username");
            AsocijacijeVariables asocijacijeVars = (AsocijacijeVariables) query.setString("username", username).uniqueResult();
            asocijacijeVars.getOpened();
            asocijacijeVars.getRevealedByBlue();
            asocijacijeVars.getRevealedByRed();
            
            String[] temp = asocijacijeVars.getOpened().split(" ");
            for(int i=0; i<21; i++) 
                if("1".equals(temp[i])) asocijacijeOpened[i] = true;
            if(playerBlue){
                temp = asocijacijeVars.getRevealedByRed().split(" ");
                for(int i=0; i<5; i++) 
                    if("1".equals(temp[i])) asocijacijeRedReveal[i] = true;
            }
            else{
                temp = asocijacijeVars.getRevealedByBlue().split(" ");
                for(int i=0; i<5; i++) 
                    if("1".equals(temp[i])) asocijacijeBlueReveal[i] = true;
            }
            
            if (asocijacijeOpened[20]) {
                asocijacijeWaitingPeriod = true;
                timer = 3;
            }
            else asocijacijeBluePlaying = asocijacijeVars.isBluePlaying();
            
            asocijacijeFieldOpened = false;

            session.getTransaction().commit();
            session.close();
        }
        if (timer == 0) {
            for(int i=0; i<21; i++) asocijacijeOpened[i] = true;
            asocijacijeWaitingPeriod = true;
            timer = 3;
       }
    }
    
    private boolean asocijacijeCorrect(String submitted, String[] acceptables){
        for(String acceptable: acceptables) 
            if(acceptable.toLowerCase().equals(submitted.toLowerCase())) return true;
        
        return false;
    }
    
    public void openField(int i){
        asocijacijeFieldOpened = true;
        asocijacijeOpened[i] = true;
    }
    
    public String getName(int i){
        if(asocijacijeOpened[i]) return asocijacijeColumns[i];
        else {
            String columnName = "";
            switch(i/4){
                case 0: columnName = "A"; break;
                case 1: columnName = "B"; break;
                case 2: columnName = "C"; break;
                case 3: columnName = "D"; break;
            }
            columnName += Integer.toString(i%4+1);
            return columnName;
        }
    }

    public boolean[] getAsocijacijeOpened() {
        return asocijacijeOpened;
    }

    public String[] getOpenedResults() {
        return openedResults;
    }

    public void setOpenedResults(String[] openedResults) {
        this.openedResults = openedResults;
    }

    public String getAsocijacijePlaceholders(int i) {
        if(i<0 || i>5) return "error";
        if(asocijacijeOpened[16+i]) {
            switch(i){
                case 0: return resultA[0];
                case 1: return resultB[0];
                case 2: return resultC[0];
                case 3: return resultD[0];
                case 4: return resultEnd[0];
                default: return "error";
            }
        }
        else return asocijacijePlaceholders[i];
    }
    
    public boolean getAsocijacijeSubmitDisabled(){
        return (playerBlue && !asocijacijeBluePlaying) 
                || (!playerBlue && asocijacijeBluePlaying)
                || asocijacijeWaitingPeriod;
    }

    public boolean getOpenFieldDisabled(int i){
        return asocijacijeOpened[i] || asocijacijeFieldOpened
                || (playerBlue && !asocijacijeBluePlaying) || (!playerBlue && asocijacijeBluePlaying);
    }
    
    public boolean isAsocijacijeFieldOpened() {
        return asocijacijeFieldOpened;
    }

    public void setAsocijacijeFieldOpened(boolean asocijacijeFieldOpened) {
        this.asocijacijeFieldOpened = asocijacijeFieldOpened;
    }
    
    public String asocijacijeColor(int i){
        if(i<16) i=i%4+16;
        if(asocijacijeBlueReveal[i-16]) return "background-color: #036fab;";
        if(asocijacijeRedReveal[i-16]) return "background-color: red;";
        return "";
    }
    
    private void initAsocijacije(Asocijacija asocijacija){
        asocijacijeColumns = asocijacija.getColumns().split("-");
        resultA = asocijacija.getResultA().split("\n");
        for (int i = 0; i < resultA.length - 1; i++) {
            resultA[i] = resultA[i].substring(0, resultA[i].length() - 1);
        }
        resultB = asocijacija.getResultB().split("\n");
        for (int i = 0; i < resultB.length - 1; i++) {
            resultB[i] = resultB[i].substring(0, resultB[i].length() - 1);
        }
        resultC = asocijacija.getResultC().split("\n");
        for (int i = 0; i < resultC.length - 1; i++) {
            resultC[i] = resultC[i].substring(0, resultC[i].length() - 1);
        }
        resultD = asocijacija.getResultD().split("\n");
        for (int i = 0; i < resultD.length - 1; i++) {
            resultD[i] = resultD[i].substring(0, resultD[i].length() - 1);
        }
        resultEnd = asocijacija.getResultEnd().split("-");
    }
    
    
    /*
    ***** Spojnice *****
    */
         
    private void spojniceVariableReset() {
        spojniceRoundTwo = true;
        spojniceBluePlaying = false;
        activeLeft = 0;
        spojniceWaitingPeriod = false;
        timer = 60;
        gameView = GameView.Spojnice;
        spojniceSidePlayer = 0;
        for(int i=0; i<10; i++)
            hitByBlue[i] = hitByRed[i] = false;
    }
    
    private void spojniceTicks() {
        if ((spojniceBluePlaying && !playerBlue) || (!spojniceBluePlaying && playerBlue)) {
            if(spojniceSidePlayer==0) spojniceSidePlayer = 2;
            activeLeft = 10;
            Session session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();

            Query query = session.createQuery("FROM SpojniceVariables WHERE blue=:username OR red=:username");
            SpojniceVariables spojniceVars = (SpojniceVariables) query.setString("username", username).uniqueResult();

            boolean opponentHit = true;
            if(playerBlue){
                String[] temp = spojniceVars.getHitByRed().split(" ");
                for(int i=0; i<10; i++) 
                    if("1".equals(temp[i])) hitByRed[i] = true;
                    else { hitByRed[i] = false; opponentHit = false; }
            }
            else{
                String[] temp = spojniceVars.getHitByBlue().split(" ");
                for(int i=0; i<10; i++) 
                    if("1".equals(temp[i])) hitByBlue[i] = true;
                    else { hitByBlue[i] = false; opponentHit = false; }
            }
            
            if (opponentHit) {
                timer = 3;
                spojniceWaitingPeriod = true;
            } else { 
                if (spojniceVars.isSidePlayerDone()) {
                    spojniceWaitingPeriod = true;
                    timer = 3;
                }

                if (spojniceBluePlaying != spojniceVars.isBluePlaying()) {
                    timer = 60;
                    spojniceBluePlaying = !spojniceBluePlaying;
                    activeLeft = 0;
                    while(hitByBlue[activeLeft] || hitByRed[activeLeft]) activeLeft++;
                }
            }

            session.getTransaction().commit();
            session.close();

            if (timer < 0) {
                timer = 0;
            }
        } else{
            if(spojniceSidePlayer==0) spojniceSidePlayer = 1;
            if (timer == 0) {
                Session session = HibernateUtil.getSessionFactory().openSession();
                session.beginTransaction();
                Query query = session.createQuery("FROM SpojniceVariables WHERE blue=:username OR red=:username");
                SpojniceVariables spojniceVars = (SpojniceVariables) query.setString("username", username).uniqueResult();
                spojniceVars.setBluePlaying(!spojniceBluePlaying);
                spojniceBluePlaying = !spojniceBluePlaying;
                session.getTransaction().commit();
                session.close();
                timer = 60;
            }
        }
    }
    
    public String getLeftWord(int i){
        return spojniceWords[i][0];
    }
    
    public String getRightWord(int i){
        return spojniceWords[pairPosition[i]][1];
    }
    
    public void choosePair(int i){
        if(spojniceSidePlayer==0) spojniceSidePlayer = 1;
        if(activeLeft == pairPosition[i]){
            spojnicePoints++;
            if(playerBlue) hitByBlue[activeLeft] = true;
            else hitByRed[activeLeft] = true;
        }
        activeLeft++;
        while(activeLeft<10 && (hitByBlue[activeLeft] || hitByRed[activeLeft])) activeLeft++;
        
        
        if(modeMultiplayer){
            Session session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            Query query = session.createQuery("FROM SpojniceVariables WHERE blue=:username OR red=:username");
            SpojniceVariables spojniceVars = (SpojniceVariables) query.setString("username", username).uniqueResult();
            
            StringBuilder builder = new StringBuilder();
            boolean allHit = true;
            boolean[] hitByMe;
            if(playerBlue) hitByMe = hitByBlue;
            else hitByMe = hitByRed;
            if (hitByMe[0]) {
                builder.append("1");
            } else {
                builder.append("0");
                allHit = false;
            }
            for (int cnt = 1; cnt < 10; cnt++) {
                builder.append(" ");
                if (hitByMe[cnt]) {
                    builder.append("1");
                } else {
                    builder.append("0");
                    allHit = false;
                }
            }
            if(playerBlue) spojniceVars.setHitByBlue(builder.toString());
            else spojniceVars.setHitByRed(builder.toString());
            
            if(allHit){
                spojniceWaitingPeriod = true;
                timer = 3;
            }
            else if(activeLeft>9){
                if(spojniceSidePlayer == 1){
                    spojniceVars.setBluePlaying(!spojniceBluePlaying);
                    spojniceBluePlaying = !spojniceBluePlaying;
                    timer = 60;
                }
                else if(spojniceSidePlayer == 2){
                    spojniceVars.setSidePlayerDone(true);
                    timer = 3;
                    spojniceWaitingPeriod = true;
                }
            }
            
            session.getTransaction().commit();
            session.close();
        }
        else if(activeLeft>9){
                spojniceWaitingPeriod = true;
                timer = 3;
            }
    }
    
    public String spojniceColorLeft(int i){
        if(i == activeLeft) return "coloredActive";
        if(hitByBlue[i]) return "coloredBlue";
        if(hitByRed[i]) return "coloredRed";
        return "";
    }
    
    public String spojniceColorRight(int i){
        if(hitByBlue[pairPosition[i]]) return "coloredBlue";
        if(hitByRed[pairPosition[i]]) return "coloredRed";
        return "";
    }
    
    public boolean spojniceButtonDisabled(int i){
        return hitByBlue[pairPosition[i]] || hitByRed[pairPosition[i]]
                || activeLeft>9
                || ((playerBlue && !spojniceBluePlaying) || (!playerBlue && spojniceBluePlaying))
                || spojniceWaitingPeriod;
    }
  
    /*
    ***** Skocko *****
    */
        
    private void skockoTicks() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        Query query = session.createQuery("FROM SkockoVariables WHERE blue=:username OR red=:username");
        SkockoVariables skockoVars = (SkockoVariables) query.setString("username", username).uniqueResult();
        
        if ((skockoBluePlaying && !playerBlue) || (!skockoBluePlaying && playerBlue)) {
            String[] temp = skockoVars.getInputCombos().split("-");
            for (int i = 0; i < 7; i++) {
                skockoInput[i] = temp[i].split(" ");
            }

            boolean opponentHit = false;
            temp = skockoVars.getOutputCombos().split("-");
            for (int i = 0; i < 7; i++) {
                String[] temp2 = temp[i].split(" ");
                for (int j = 0; j < 2; j++) {
                    skockoOutput[i][j] = Integer.parseInt(temp2[j]);
                }
                if (skockoOutput[i][0] == 4) {
                    opponentHit = true;
                }
            }
            if (opponentHit) {
                timer = 3;
                skockoWaitingPeriod = true;
            } else {
                if (skockoVars.isSidePlayerDone()) {
                    timer = 3;
                    skockoWaitingPeriod = true;
                }

                if (skockoBluePlaying != skockoVars.isBluePlaying()) {
                    timer = 60;
                    skockoBluePlaying = !skockoBluePlaying;
                    currentRow = 6;
                }
            }


            if (timer < 0) {
                timer = 0;
            }
        } else if (timer == 0) {
            if(currentRow == 6){
                skockoVars.setSidePlayerDone(true);
                skockoWaitingPeriod = true;
                timer = 3;
            }
            else{
                skockoVars.setBluePlaying(!skockoBluePlaying);
                skockoBluePlaying = !skockoBluePlaying;
                session.getTransaction().commit();
                session.close();
                timer = 60;
            }
        }
        
        session.getTransaction().commit();
        session.close();
    }
    
    private void skockoVariableReset() {
        skockoRoundTwo = true;
        skockoBluePlaying = false;
        currentRow = currentSymbol = 0;
        skockoWaitingPeriod = false;
        timer = 60;
        gameView = GameView.Skocko;
        skockoOutput = new int[7][2];
        for (int i = 0; i < 7; i++)
            for (int j = 0; j < 4; j++)
                skockoInput[i][j] = "x";
    }
    
    public String getSkockoSymbol(int i) {
        return skockoSymbols[i];
    }
    
    public void chooseSymbol(int i){
        skockoInput[currentRow][currentSymbol] = skockoSymbols[i];
        currentSymbol++;
    }
    
    public boolean getSkockoButtonsAvailable(){
        return currentSymbol<4
                && (currentRow<6 || (currentRow<7 && modeMultiplayer))
                && ((playerBlue && skockoBluePlaying) || (!playerBlue && !skockoBluePlaying))
                && !skockoWaitingPeriod;
    }
    
    public boolean getSkockoSubmitButtonAvailable(){
        return currentSymbol == 4 
                &&((playerBlue && skockoBluePlaying) || (!playerBlue && !skockoBluePlaying))
                && !skockoWaitingPeriod;
    }
    
    public boolean getSkockoResetButtonAvailable(){
        return ((playerBlue && skockoBluePlaying) || (!playerBlue && !skockoBluePlaying))
                && !skockoWaitingPeriod;
    }
    
    public String getSkockoRow(int row) {
        //return skockoInput[i];
        StringBuilder builder = new StringBuilder();
        builder.append(skockoInput[row][0]);
        for(int i=1; i<4; i++) builder.append(" ").append(skockoInput[row][i]);
        return builder.toString();
    }
    
    public String getSkockoOutputRow(int i){
        return "Complete: " + skockoOutput[i][0] + "  Partial: " + skockoOutput[i][1];
    }
    
    public void resetSkockoRow(){
        currentSymbol = 0;
        for(int i=0; i<4; i++) skockoInput[currentRow][i] = "x";
    }
    
    public void submitSkockoRow(){
        gradeSkocko(skockoInput, skockoOutput, secretCombo, currentRow);
        currentSymbol = 0;
        currentRow++;
        
        if(modeMultiplayer){
            Session session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            Query query = session.createQuery("FROM SkockoVariables WHERE blue=:username OR red=:username");
            SkockoVariables skockoVars = (SkockoVariables) query.setString("username", username).uniqueResult();
            
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < 7; i++) {
                for (int j = 0; j < 4; j++) {
                    builder.append(skockoInput[i][j]);
                    if (j != 3) {
                        builder.append(" ");
                    }
                }
                if (i != 6) {
                    builder.append("-");
                }
            }
            skockoVars.setInputCombos(builder.toString());
            
            builder = new StringBuilder();
            for (int i = 0; i < 7; i++) {
                for (int j = 0; j < 2; j++) {
                    builder.append(skockoOutput[i][j]);
                    if (j != 1) {
                        builder.append(" ");
                    }
                }
                if (i != 6) {
                    builder.append("-");
                }
            }
            skockoVars.setOutputCombos(builder.toString());
            
            if(skockoOutput[currentRow-1][0] == 4){
                skockoPoints += 10;
                skockoWaitingPeriod = true;
                timer = 3;
            } 
            else if(currentRow==6){
                skockoVars.setBluePlaying(!skockoBluePlaying);
                skockoBluePlaying = !skockoBluePlaying;
                timer = 60;
            }
            else if(currentRow==7){
                skockoVars.setSidePlayerDone(true);
                timer = 3;
                skockoWaitingPeriod = true;
            }
            session.getTransaction().commit();
            session.close();
        }
        else{
            if(skockoOutput[currentRow-1][0] == 4){
                skockoPoints = 10;
                skockoWaitingPeriod = true;
                timer = 3;
            } else if(currentRow==6){ 
                skockoWaitingPeriod = true;
                timer = 3;
            }
        }
    }
    
    private void gradeSkocko(String[][] input, int[][] output, String[] secretCombo, int row){
        output[row][0] = output[row][1] = 0;
        boolean[] hitInput = {false, false, false, false};
        boolean[] hitSecret = {false, false, false, false};
        for(int i=0; i<4; i++)
            if(input[row][i].equals(secretCombo[i])){ output[row][0]++; hitSecret[i]=hitInput[i]=true; }
        for(int i=0; i<4; i++)
            if(!hitInput[i]) 
                for(int j=0; j<4; j++)
                    if(!hitSecret[j] && input[row][i].equals(secretCombo[j])){
                        hitInput[i] = hitSecret[j] = true;
                        output[row][1]++;
                        break;
                    }
    }

    public boolean isSkockoWaitingPeriod() {
        return skockoWaitingPeriod;
    }

    public String[] getSecretCombo() {
        return secretCombo;
    }
    
    /*
    ***** Slagalica *****
    */
    
    public String getSlagalicaLetter(int i) {
        return slagalicaLetters[i];
    }
    
    public void chooseLetter(int i){
        slagalicaWord+=slagalicaLetters[i];
        slagalicaButtons[i] = false;
    }
    
    public boolean slagalicaButtonAvailable(int i){
        return slagalicaButtons[i];
    }
    
    public void resetSlagalica(){
        for(int i=0; i<12; i++) slagalicaButtons[i] = true;
        slagalicaWord = "";
    }
    
    public String getSlagalicaWord() {
        return slagalicaWord;
    }
    
   /*
    ***** Moj Broj *****
    */
    
    public String getNumberOrOperation(int i){
        if(i<7) return mojBrojNumbers[i];
        else return mojBrojOperations[i-7];
    }
    
    public void chooseNumberOrOperation(int i){
        mojBrojMessage = "";
        if(i<7) {
            if(lastUsedNumber) { mojBrojMessage = "Can't use a Number again!"; return; }
            mojBrojWord += mojBrojNumbers[i];
            mojBrojButtons[i-1] = false;
            lastUsedNumber = true;
        }
        else{
            mojBrojWord += mojBrojOperations[i-7];
            lastUsedNumber = false;
        }
    }
    
    public boolean mojBrojButtonAvailable(int i){
        if(i<7) return mojBrojButtons[i-1];
        return true;
    }
    
    public void resetMojBroj(){
        for(int i=0; i<6; i++) mojBrojButtons[i] = true;
        mojBrojWord = "";
        mojBrojMessage = "";
    }
    
    public String getMojBrojWord() {
        return mojBrojWord;
    }

    public String getGameName() {
        return gameName;
    }

    public String getMojBrojMessage() {
        return mojBrojMessage;
    }

    public void setMojBrojMessage(String mojBrojMessage) {
        this.mojBrojMessage = mojBrojMessage;
    }
    
    /*
    ***** Other *****
    */
    
    public List<GamePoints> getGamePoints() {
        return gamePoints;
    }
    
    public boolean isMyPage(GameView myView){
        return myView==gameView;
    }

    public int getTimer() {
        return timer;
    }

    public boolean isGameInProgress() {
        return !(gameView==GameView.Waiting || gameView==GameView.GameOver);
    }

    public boolean isModeMultiplayer() {
        return modeMultiplayer;
    }
    
    public String applyColor(GamePoints game){
        if(!"Total".equals(game.game)) return "";
        if(game.blue == game.red) return "coloredNeutral";
        if(playerBlue && game.blue>game.red || !playerBlue && game.blue<game.red) return "coloredVictory";
        return "coloredDefeat";
    }
    
    public String[] getDummyStringArray(){ return new String[1]; }
    
}
