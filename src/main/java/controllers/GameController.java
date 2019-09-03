/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import static services.TransactionService.*;
import static services.ActiveGameService.*;
import classes.GamePoints;
import entities.*;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import games.*;
import org.hibernate.Session;
import util.PointsManager;
import util.PreparationManager;
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
    private GameView gameView;
    private GameView nextGame;
    private GameView currentGame;
    
    private int timer;
    private long lastTimerTick;
    
    private boolean playerBlue; //true-blue, false-red
    private boolean modeMultiplayer; //true-multiplayer, false-singleplayer
    
    private boolean preparationStarted;
    private boolean roundTwo = false;
    private boolean waitingPeriod = false;
    private boolean bluePlaying = true;

    private String username;
    
    private final List<GamePoints> gamePoints = new LinkedList<>();

    private Slagalica slagalica;
    private MojBroj mojBroj;
    private Skocko skocko;
    private Spojnice spojnice;
    private Asocijacije asocijacije;
    
    @PostConstruct
    public void init(){
        currentGame = null;
        nextGame = GameView.Slagalica;
        gameView = GameView.Waiting;
        username = SessionManager.getUser().getUsername();
        modeMultiplayer = "multiplayer".equals(SessionManager.getGameMode());
        playerBlue = !modeMultiplayer || "blue".equals(SessionManager.getPlayerSide());
        lastTimerTick=0;
        
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
     */
    private void prepareNextGame() {
        preparationStarted = true;
        
        Session session = openTransaction();
        
        if (modeMultiplayer) {
            ActiveGame game = myActiveGame(session, username);

            //call the preparation method for the next game
            if(currentGame==GameView.Skocko && !roundTwo){
                skocko = new Skocko(PreparationManager
                        .generateSkocko(session, game.getBlue(), game.getRed(), true, false));
            }
            else if(currentGame==GameView.Spojnice && !roundTwo){
                spojnice = new Spojnice();
                PreparationManager.generateSpojnice(session, game.getBlue(), game.getRed(), spojnice, false);
            }
            else switch (nextGame) {
                case Slagalica:
                    slagalica = new Slagalica(PreparationManager
                            .generateSlagalica(session, game.getBlue(), game.getRed(), true));
                    break;
                case MojBroj:
                    mojBroj = new MojBroj(PreparationManager
                            .generateMojBroj(session, game.getBlue(), game.getRed(), true));
                    break;
                case Skocko:
                    skocko = new Skocko(PreparationManager
                            .generateSkocko(session, game.getBlue(), game.getRed(), true, true));
                    break;
                case Spojnice:
                    spojnice = new Spojnice();
                    PreparationManager.generateSpojnice(session, game.getBlue(), game.getRed(), spojnice, true);
                    break;
                case Asocijacije:
                    asocijacije = new Asocijacije(PreparationManager
                            .generateAsocijacije(session, game.getBlue(), game.getRed()));
                    break;
                default: break;
            }

            //do the synchronization
            game.setBlueReady(true);
        }
        //singleplayer
        else if(nextGame == GameView.Slagalica){
                GameOfTheDay game = currentGameOfTheDay(session);

                slagalica = new Slagalica(game.getLetters());
                mojBroj = new MojBroj(game.getNumbers());
                skocko = new Skocko(game.getSecretCombo());
                spojnice = new Spojnice(game.getPairs());
                asocijacije = new Asocijacije(game.getAsocijacija());
                
                game.setPlayed(true);
            }
        closeTransaction(session);
    }
        
    private void loadPreparedGameData(Session session){
        switch (nextGame) {
            case Slagalica: slagalica = new Slagalica(PreparationManager.loadSlagalica(session, username)); break;
            case MojBroj: mojBroj = new MojBroj(PreparationManager.loadMojBroj(session, username)); break;
            case Skocko: skocko = new Skocko(PreparationManager.loadSkocko(session, username)); break;
            case Spojnice:
                spojnice = new Spojnice();
                PreparationManager.loadSpojnice(session, username, spojnice); break;
            case Asocijacije: asocijacije = new Asocijacije(PreparationManager.loadAsocijacije(session, username)); break;
            default: break;
        }
    }
    
    /**
     * Gets periodically called while we are in a game.
     * If the timer has run out calls finished().
     */
    private void checkIfCurrentGameOver(){
        if(!isGameInProgress()) return;
        
        long currentTimerTick = System.currentTimeMillis();
        if(lastTimerTick==0 || currentTimerTick-lastTimerTick > 500) timer--;
        lastTimerTick = currentTimerTick;

        if(modeMultiplayer && !waitingPeriod){
            switch(currentGame){
                case Skocko: {
                    skockoTicks();
                    return;
                }
                case Spojnice: {
                    spojniceTicks();
                    return;
                }
                case Asocijacije: {
                    asocijacijeTicks();
                    return;
                }
            }
        }
        
        if(timer==0) finished();
    }

    /**
     * Updates our points after we are done with the game
     * and sends us to the waiting screen.
     */
    public void finished() {
        int myPoints = 0;
        switch (currentGame) {
            case Slagalica: myPoints = PointsManager.slagalica(slagalica); break;
            case MojBroj: myPoints = PointsManager.mojBroj(mojBroj); break;
            case Skocko: myPoints = skocko.getPoints(); break;
            case Spojnice: myPoints = spojnice.getPoints(); break;
            case Asocijacije: myPoints = asocijacije.getPoints(); break;
            default: break;
        }

        if (modeMultiplayer) {
            //update my points in database so that the other player can see them
            Session session = openTransaction();
            GameVariables gameVars = null;

            switch (currentGame) {
                case MojBroj:
                    MojBrojVariables mojBrojVars = myMojBrojVars(session, username);
                    if(playerBlue) {
                        mojBrojVars.setPointsBlue(myPoints);
                        mojBrojVars.setDifferenceBlue(mojBroj.getDifference());
                    }
                    else {
                        mojBrojVars.setPointsRed(myPoints);
                        mojBrojVars.setDifferenceRed(mojBroj.getDifference());
                    }
                    break;
                case Slagalica:
                    gameVars = mySlagalicaVars(session, username);
                    break;
                case Skocko:
                    if(roundTwo) {
                        gameVars = mySkockoVars(session, username);
                    }
                    break;
                case Spojnice:
                    if(roundTwo){
                        gameVars = mySpojniceVars(session, username);
                    }
                    break;
                case Asocijacije:
                    gameVars = myAsocijacijeVars(session, username);
                    break;
                default: break;
            }

            if(gameVars != null){
                if(playerBlue) gameVars.setPointsBlue(myPoints);
                else gameVars.setPointsRed(myPoints);
            }
            closeTransaction(session);
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
    private void checkIfNextGameReady(){
        if(gameView!=GameView.Waiting) return;
        if(playerBlue && !preparationStarted) prepareNextGame();
        
        boolean gameReady = false;
        //multiplayer game, must synchronize and save points from the database
        if(modeMultiplayer){
            Session session = openTransaction();
            ActiveGame game = myActiveGame(session, username);

            if((game.isBlueReady() || playerBlue) && (game.isRedReady() || !playerBlue)) {
                //The other player is ready
                gameReady = true;
                //points of the last game are set, we can save them now
                if(currentGame!=null) {
                    int pointsBlue=0, pointsRed=0;
                    GameVariables gameVars = null;
                    switch(currentGame) {
                        case Slagalica:
                            SlagalicaVariables slagalicaVars = mySlagalicaVars(session, username);
                            pointsBlue = slagalicaVars.getPointsBlue();
                            pointsRed = slagalicaVars.getPointsRed();
                            if(pointsBlue>pointsRed) pointsRed = 0;
                            else if(pointsRed > pointsBlue) pointsBlue = 0;
                            break;
                        case MojBroj:
                            MojBrojVariables mojBrojVars = myMojBrojVars(session, username);
                            
                            pointsBlue = mojBrojVars.getPointsBlue();
                            pointsRed = mojBrojVars.getPointsRed();
                            
                            if(pointsBlue == 10 && pointsRed== 10) {
                                if(mojBrojVars.getDifferenceBlue() > mojBrojVars.getDifferenceRed()) pointsBlue = 0;
                                else if(mojBrojVars.getDifferenceBlue() < mojBrojVars.getDifferenceRed()) pointsRed = 0;
                                else pointsBlue = pointsRed = 5;
                            }
                            break;
                        case Skocko:
                            if(roundTwo){
                                gameVars = mySkockoVars(session, username);
                            }
                            break;
                        case Spojnice:
                            if(roundTwo){
                                gameVars = mySpojniceVars(session, username);
                            }
                            break;
                        case Asocijacije:
                            gameVars = myAsocijacijeVars(session, username);
                            break;
                        default: break;
                    }
                    if(gameVars != null){
                        pointsBlue = gameVars.getPointsBlue();
                        pointsRed = gameVars.getPointsRed();
                    }
                    if((currentGame!=GameView.Skocko  && currentGame!=GameView.Spojnice) || roundTwo)
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

            closeTransaction(session);
        }
        
        //if singleplayer (no sync required) or it's multiplayer and has been synchronized
        if(!modeMultiplayer || gameReady){
            waitingPeriod = false;
            if(modeMultiplayer) {
                if(!roundTwo && currentGame == GameView.Skocko || currentGame == GameView.Spojnice){
                    roundTwo = true;
                    bluePlaying = false;
                    timer = 60;
                    gameView = currentGame;
                    return;
                }
                roundTwo = false;
            }

            bluePlaying = true;
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
    
    private void gameOverSingleplayer() {
        //count the total points and save the final database entry
        int totalPoints = 0;
        for(GamePoints game: gamePoints) totalPoints+=game.blue;
        gamePoints.add(new GamePoints("Total", totalPoints));
        
        Session session = openTransaction();
        
        SingleplayerGame finishedGame = new SingleplayerGame(username, totalPoints);
        
        session.save(finishedGame);

        closeTransaction(session);
    }
    
    private void gameOverMultiplayer(){
         //count the total points and save the final database entry
        int totalRed = 0;
        int totalBlue = 0;
        for(GamePoints game: gamePoints) { totalRed+=game.red; totalBlue+=game.blue; }
        gamePoints.add(new GamePoints("Total", totalBlue, totalRed));
        
        //if it's your job, save the final database entry
        if(playerBlue){
            Session session = openTransaction();

            cleanUpDatabase(session, username);

            ActiveGame activeGame = myActiveGame(session, username);

            FinishedGame finishedGame = new FinishedGame(activeGame, totalBlue, totalRed);
            if(finishedGame.getPointsBlue()>finishedGame.getPointsRed())
                finishedGame.setGameResult((short)1);
            else if(finishedGame.getPointsBlue()<finishedGame.getPointsRed())
                finishedGame.setGameResult((short)-1);
            else finishedGame.setGameResult((short)0);

            session.delete(activeGame);
            session.save(finishedGame);

            closeTransaction(session);
        }
    }

    /*
     ***** Slagalica *****
     */

    public String getSlagalicaLetter(int i) {
        return slagalica.getLetter(i);
    }

    public void chooseLetter(int i){
        slagalica.addLetter(i);
    }

    public boolean slagalicaButtonAvailable(int i){
        return slagalica.buttonAvailable(i);
    }

    public void resetSlagalica(){
        slagalica.reset();
    }

    public String getSlagalicaWord() {
        return slagalica.getWord();
    }

    /*
     ***** Moj Broj *****
     */

    public String getNumberOrOperation(int i){
        return mojBroj.getNumberOrOperation(i);
    }

    public void chooseNumberOrOperation(int i){
        mojBroj.chooseNumberOrOperation(i);
    }

    public boolean mojBrojButtonAvailable(int i){
        return mojBroj.buttonAvailable(i);
    }

    public void resetMojBroj() {
        mojBroj.reset();
    }

    public String getMojBrojWord() {
        return mojBroj.getWord();
    }

    public String getMojBrojMessage() {
        return mojBroj.getMessage();
    }

    /*
     ***** Skocko *****
     */

    private void skockoTicks() {
        Session session = openTransaction();
        SkockoVariables skockoVars = mySkockoVars(session, username);

        if ((bluePlaying && !playerBlue) || (!bluePlaying && playerBlue)) {
            boolean opponentHit =
                    skocko.setInputAndFeedbackAndReturnIfCompleted(skockoVars.getInputCombos(), skockoVars.getOutputCombos());

            if (opponentHit) {
                timer = 3;
                waitingPeriod = true;
            } else {
                if (skockoVars.isSidePlayerDone()) {
                    timer = 3;
                    waitingPeriod = true;
                }
                if (bluePlaying != skockoVars.isBluePlaying()) {
                    timer = 60;
                    bluePlaying = !bluePlaying;
                    skocko.setCurrentRow(6);
                }
            }

            if (timer < 0) {
                timer = 0;
            }
        } else if (timer == 0) {
            if(skocko.getCurrentRow() == 6){
                skockoVars.setSidePlayerDone(true);
                waitingPeriod = true;
                timer = 3;
            }
            else{
                skockoVars.setBluePlaying(!bluePlaying);
                bluePlaying = !bluePlaying;
                timer = 60;
            }
        }
        closeTransaction(session);
    }

    public String getSkockoSymbol(int i) {
        return Skocko.getSymbol(i);
    }

    public void chooseSymbol(int i) {
        skocko.chooseSymbol(i);
    }

    public boolean getSkockoButtonsAvailable(){
        return skocko.buttonsAvailable(modeMultiplayer) && canPlay();
    }

    public boolean getSkockoSubmitButtonAvailable(){
        return skocko.submittable() && canPlay();
    }

    public boolean getSkockoResetButtonAvailable(){
        return canPlay();
    }

    public String getSkockoRow(int row) {
        return skocko.getRowAsString(row);
    }

    public String getSkockoOutputRow(int i){
        return skocko.getFeedbackRow(i);
    }

    public void resetSkockoRow(){
        skocko.resetCurrentRow();
    }

    public void submitSkockoRow(){
        skocko.submitRow();

        if(modeMultiplayer) {
            Session session = openTransaction();
            SkockoVariables skockoVars = mySkockoVars(session, username);

            skockoVars.setInputCombos(skocko.getInputAsString());
            skockoVars.setOutputCombos(skocko.getFeedbackAsString());

            if(skocko.isCompleted()){
                waitingPeriod = true;
                timer = 3;
            }
            else if(skocko.getCurrentRow() == 6){
                skockoVars.setBluePlaying(!bluePlaying);
                bluePlaying = !bluePlaying;
                timer = 60;
            }
            else if(skocko.getCurrentRow() == 7){
                skockoVars.setSidePlayerDone(true);
                timer = 3;
                waitingPeriod = true;
            }
            closeTransaction(session);
        }
        else{
            if(skocko.isCompleted()) {
                waitingPeriod = true;
                timer = 3;
            } else if(skocko.getCurrentRow() == 6) {
                waitingPeriod = true;
                timer = 3;
            }
        }
    }

    public String[] getSecretCombo() {
        return skocko.getSecretCombo();
    }

    /*
     ***** Spojnice *****
     */

    private void spojniceTicks() {
        if ((bluePlaying && !playerBlue) || (!bluePlaying && playerBlue)) {
            if(spojnice.getSidePlayer() == null){
                spojnice.setSidePlayer(Boolean.TRUE);
            }
            spojnice.setActiveLeft(10);
            Session session = openTransaction();

            SpojniceVariables spojniceVars = mySpojniceVars(session, username);

            boolean opponentHit;

            if(playerBlue) {
                opponentHit = spojnice.setHitByRedAndReturnIfCompleted(spojniceVars.getHitByRed());
            }
            else {
                opponentHit = spojnice.setHitByBlueAndReturnIfCompleted(spojniceVars.getHitByBlue());
            }

            if (opponentHit) {
                timer = 3;
                waitingPeriod = true;
            } else {
                if (spojniceVars.isSidePlayerDone()) {
                    waitingPeriod = true;
                    timer = 3;
                }

                if (bluePlaying != spojniceVars.isBluePlaying()) {
                    timer = 60;
                    bluePlaying = !bluePlaying;
                    spojnice.updateActivePointer();
                }
            }

            closeTransaction(session);

            if (timer < 0) {
                timer = 0;
            }
        } else{
            if(spojnice.getSidePlayer() == null) spojnice.setSidePlayer(Boolean.FALSE);
            if (timer == 0) {
                Session session = openTransaction();

                SpojniceVariables spojniceVars = mySpojniceVars(session, username);
                spojniceVars.setBluePlaying(!bluePlaying);
                bluePlaying = !bluePlaying;

                closeTransaction(session);
                timer = 60;
            }
        }
    }

    public String getLeftWord(int i){
        return spojnice.getLeftWord(i);
    }

    public String getRightWord(int i){
        return spojnice.getRightWord(i);
    }

    public void choosePair(int i){
        spojnice.submitPair(i, playerBlue);

        if(modeMultiplayer) {
            Session session = openTransaction();
            SpojniceVariables spojniceVars = mySpojniceVars(session, username);

            if(playerBlue) spojniceVars.setHitByBlue(spojnice.hitByMeAsString(true));
            else spojniceVars.setHitByRed(spojnice.hitByMeAsString(false));

            if(spojnice.isCompleted()){
                waitingPeriod = true;
                timer = 3;
            }
            else if(spojnice.getActiveLeft() > 9){
                if(!spojnice.getSidePlayer()){
                    spojniceVars.setBluePlaying(!bluePlaying);
                    bluePlaying = !bluePlaying;
                    timer = 60;
                }
                else {
                    spojniceVars.setSidePlayerDone(true);
                    timer = 3;
                    waitingPeriod = true;
                }
            }

            closeTransaction(session);
        }
        else if(spojnice.getActiveLeft() > 9){
            waitingPeriod = true;
            timer = 3;
        }
    }

    public String spojniceColorLeft(int i){
        return spojnice.colorLeft(i);
    }

    public String spojniceColorRight(int i){
        return spojnice.colorRight(i);
    }

    public boolean spojniceButtonDisabled(int i){
        return spojnice.buttonDisabled(i) || !canPlay();
    }

    public String getGameName() {
        return spojnice.getGameName();
    }

    /*
     **** Asocijacije ****
     */

    public void submitAsocijacije() {
        asocijacije.submit(playerBlue);
        if(asocijacije.isCompleted()){
            waitingPeriod = true;
            timer = 3;
        }

        if(modeMultiplayer){
            Session session = openTransaction();

            AsocijacijeVariables asocijacijeVars = myAsocijacijeVars(session, username);
            if(asocijacije.wasHit()){
                asocijacijeVars.setMessage(asocijacije.getOpened(), asocijacije.getRevealedByPlayer(playerBlue), !bluePlaying);
                bluePlaying = !bluePlaying;
            }
            else {
                asocijacijeVars.setMessage(asocijacije.getOpened(), asocijacije.getRevealedByPlayer(playerBlue), bluePlaying);
            }

            closeTransaction(session);
        }
    }

    private void asocijacijeTicks(){
        if ((bluePlaying && !playerBlue) || (!bluePlaying && playerBlue)) {
            Session session = openTransaction();

            AsocijacijeVariables asocijacijeVars = myAsocijacijeVars(session, username);

            asocijacije.setOpenedArray(asocijacijeVars.getOpened());
            if(playerBlue){
                asocijacije.setRevealedByRed(asocijacijeVars.getRevealedByRed());
            }
            else{
                asocijacije.setRevealedByBlue(asocijacijeVars.getRevealedByBlue());
            }

            if (asocijacije.isCompleted()) {
                waitingPeriod = true;
                timer = 3;
            }
            else {
                bluePlaying = asocijacijeVars.isBluePlaying();
            }

            asocijacije.setFieldWasOpened(false);

            closeTransaction(session);
        }
        if (timer == 0) {
            asocijacije.openAll();
            waitingPeriod = true;
            timer = 3;
        }
    }

    public void openField(int i){
        asocijacije.openField(i);
    }

    public String getName(int i){
        return asocijacije.getFieldName(i);
    }

    public boolean[] getAsocijacijeOpened() {
        return asocijacije.getOpened();
    }

    public String[] getOpenedResults() {
        return asocijacije.getOpenedResults();
    }

    public void setOpenedResults(String[] openedResults) {
        asocijacije.setOpenedResults(openedResults);
    }

    public String getAsocijacijePlaceholders(int i) {
        return asocijacije.getColumnResultName(i);
    }

    public boolean getAsocijacijeSubmitDisabled(){
        return !canPlay();
    }

    public boolean getOpenFieldDisabled(int i){
        return asocijacije.getOpenFieldDisabled(i) || !canPlay();
    }

    public String asocijacijeColor(int i){
        return asocijacije.getFieldColor(i);
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

    public boolean isWaitingPeriod() {
        return waitingPeriod;
    }

    private boolean canPlay() {
        return ((playerBlue && bluePlaying) || (!playerBlue && !bluePlaying))
                && !waitingPeriod;
    }
}