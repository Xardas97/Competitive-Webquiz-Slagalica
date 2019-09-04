/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import static services.ActiveGameService.*;
import classes.GamePoints;
import entities.*;
import java.io.Serializable;
import java.util.*;
import java.util.function.BiFunction;
import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import games.*;
import services.ActiveGameService;
import util.PointsManager;
import util.PreparationManager;
import util.SessionManager;
import util.Transaction;

/**
 *
 * @author Marko
 */

@ManagedBean
@ViewScoped
@Named(value="GameController")
public class GameController implements Serializable {
    public enum GameView{Waiting, Slagalica, MojBroj, Skocko, Spojnice, Asocijacije, GameOver}
    private static final Set<GameView> ALTERNATING_GAMES =
            new HashSet<>(Arrays.asList(GameView.Skocko, GameView.Spojnice, GameView.Asocijacije));

    private GameView gameView, nextGame, currentGame;

    private int timer;
    private long lastTimerTick;

    private boolean playerIsBlue, modeMultiplayer;

    private boolean preparationStarted;
    private boolean roundTwo = false;
    private boolean waitingPeriod = false;
    private boolean blueIsPlaying = true;

    private String username;

    private final List<GamePoints> gamePoints = new LinkedList<>();

    private Slagalica slagalica;
    private MojBroj mojBroj;
    private Skocko skocko;
    private Spojnice spojnice;
    private Asocijacije asocijacije;

    /*
     ***** KEY METHODS *****
     */

    @PostConstruct
    private void init(){
        currentGame = null;
        nextGame = GameView.Slagalica;
        gameView = GameView.Waiting;
        username = SessionManager.getUser().getUsername();
        modeMultiplayer = "multiplayer".equals(SessionManager.getGameMode());
        playerIsBlue = !modeMultiplayer || "blue".equals(SessionManager.getPlayerSide());
        lastTimerTick=0;

        if(playerIsBlue){
            preparationStarted = true;
            prepareNextGameData();
        }
    }

    /**
     * Called once every second
     */
    public void periodicCall(){
        if(gameView == GameView.Waiting){
            checkIfNextGameReady();
        }
        if(isGameInProgress()){
            checkIfCurrentGameOver();
        }
    }

    /**
     * Gets periodically called while we are waiting for the next game.
     * In multiplayer:
     * If both players are ready it changes to the next game.
     * If it's the end of the game Blue updates the database.
     */
    private void checkIfNextGameReady(){
        if(playerIsBlue && !preparationStarted) prepareNextGameData();

        if(modeMultiplayer) {
            boolean gameReady = loadDataAndSynchronize();
            if(!gameReady) return;
        }

        prepareNextGame();
    }

    /**
     * Gets periodically called while we are in a game.
     * If the timer has run out calls finished().
     */
    private void checkIfCurrentGameOver(){
        updateTimer();

        // if it's one of the games where players alternate call alternatingGamesTick() and exit
        if(modeMultiplayer && !waitingPeriod && ALTERNATING_GAMES.contains(currentGame)) {
            alternatingGamesTick();
            return;
        }

        if(timer==0) finished();
    }

    /**
     * Updates our points after we are done with the game
     * and sends us to the waiting screen.
     */
    public void finished() {
        int myPoints = getMyCurrentGamePoints();

        if (modeMultiplayer) {
            updatePointsInDatabase(myPoints);
        } else {
            gamePoints.add(new GamePoints(currentGame, myPoints));
        }

        preparationStarted = false;
        gameView = GameView.Waiting;
    }

    /*
     ***** GAME: SLAGALICA *****
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
     ***** GAME: MOJ BROJ *****
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
     ***** GAME: SKOCKO *****
     */

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

    public String getSkockoFeedbackRow(int i){
        return skocko.getFeedbackRow(i);
    }

    public void resetSkockoRow(){
        skocko.resetCurrentRow();
    }

    public String[] getSecretCombo() {
        return skocko.getSecretCombo();
    }

    public void submitSkockoRow() {
        skocko.submitRow();

        if (modeMultiplayer) {
            synchronizeSidePlayerGameDataAndCloseIfDone(skocko, ActiveGameService::mySkockoVars);
        } else if (skocko.isCompleted() || skocko.playerFinished()) {
            startTheWaitingPeriod();
        }
    }

    /*
     ***** GAME: SPOJNICE *****
     */

    public void choosePair(int i){
        spojnice.submitPair(i, playerIsBlue);

        if(modeMultiplayer) {
            synchronizeSidePlayerGameDataAndCloseIfDone(spojnice, ActiveGameService::mySpojniceVars);
        }
        else if(spojnice.playerFinished()){
            startTheWaitingPeriod();
        }
    }

    public String getLeftWord(int i){
        return spojnice.getLeftWord(i);
    }

    public String getRightWord(int i){
        return spojnice.getRightWord(i);
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
     **** GAME: ASOCIJACIJE ****
     */

    public void submitAsocijacije() {
        asocijacije.submit(playerIsBlue);

        if(modeMultiplayer){
            synchronizeAsocijacijeData();
        }
        if(asocijacije.isCompleted()){
            startTheWaitingPeriod();
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
    ***** OTHER *****
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
        if(playerIsBlue && game.blue>game.red || !playerIsBlue && game.blue<game.red) return "coloredVictory";
        return "coloredDefeat";
    }

    public boolean isWaitingPeriod() {
        return waitingPeriod;
    }

    /*
     ***** PRIVATE HELPERS *****
     */

    private void prepareNextGameData() {
        preparationStarted = true;

        try(Transaction transaction = new Transaction()) {
            if (modeMultiplayer) {
                prepareMultiplayerGameData(transaction);
            } else if (nextGame == GameView.Slagalica) {
                prepareSingleplayerGameData(transaction);
            }
        }
    }

    private void updateTimer() {
        long currentTimerTick = System.currentTimeMillis();
        if(lastTimerTick==0 || currentTimerTick-lastTimerTick > 500) timer--;
        lastTimerTick = currentTimerTick;
    }

    private void prepareSingleplayerGameData(Transaction transaction) {
        GameOfTheDay game = currentGameOfTheDay(transaction);

        slagalica = new Slagalica(game.getLetters());
        mojBroj = new MojBroj(game.getNumbers());
        skocko = new Skocko(game.getSecretCombo());
        spojnice = new Spojnice(game.getPairs());
        asocijacije = new Asocijacije(game.getAsocijacija());

        game.setPlayed(true);
    }

    private void prepareMultiplayerGameData(Transaction transaction) {
        ActiveGame game = myActiveGame(transaction, username);

        if(currentGame== GameView.Skocko && !roundTwo){
            skocko = new Skocko(PreparationManager
                    .generateSkocko(transaction, game.getBlue(), game.getRed(), true, false));
        }
        else if(currentGame== GameView.Spojnice && !roundTwo){
            spojnice = new Spojnice();
            PreparationManager.generateSpojnice(transaction, game.getBlue(), game.getRed(), spojnice, false);
        }
        else switch (nextGame) {
                case Slagalica:
                    slagalica = new Slagalica(PreparationManager
                            .generateSlagalica(transaction, game.getBlue(), game.getRed(), true));
                    break;
                case MojBroj:
                    mojBroj = new MojBroj(PreparationManager
                            .generateMojBroj(transaction, game.getBlue(), game.getRed(), true));
                    break;
                case Skocko:
                    skocko = new Skocko(PreparationManager
                            .generateSkocko(transaction, game.getBlue(), game.getRed(), true, true));
                    break;
                case Spojnice:
                    spojnice = new Spojnice();
                    PreparationManager.generateSpojnice(transaction, game.getBlue(), game.getRed(), spojnice, true);
                    break;
                case Asocijacije:
                    asocijacije = new Asocijacije(PreparationManager
                            .generateAsocijacije(transaction, game.getBlue(), game.getRed()));
                    break;
                default: break;
            }

        //do the synchronization
        game.setBlueReady(true);
    }

    private int getMyCurrentGamePoints() {
        switch (currentGame) {
            case Slagalica: return PointsManager.slagalica(slagalica);
            case MojBroj: return PointsManager.mojBroj(mojBroj);
            case Skocko: return skocko.getPoints();
            case Spojnice: return spojnice.getPoints();
            case Asocijacije: return asocijacije.getPoints();
            default: return 0;
        }
    }

    private boolean loadDataAndSynchronize() {
        try(Transaction transaction = new Transaction()) {
            ActiveGame game = myActiveGame(transaction, username);

            boolean gameReady = false;
            if ((game.isBlueReady() || playerIsBlue) && (game.isRedReady() || !playerIsBlue)) {
                //The other player is ready
                gameReady = true;
                //points of the last game are set, we can save them now
                savePoints(transaction);
                //Red reads the next game data and announces that he is ready
                //Both reset the other's readiness
                if (!playerIsBlue) {
                    loadPreparedGameData(transaction);
                    game.setRedReady(true);
                    game.setBlueReady(false);
                } else {
                    game.setRedReady(false);
                }
            }
            return gameReady;
        }
    }

    private void updatePointsInDatabase(int myPoints) {
        try(Transaction transaction = new Transaction()) {
            GameVariables gameVars = null;

            switch (currentGame) {
                case MojBroj:
                    gameVars = myMojBrojVars(transaction, username);
                    if (playerIsBlue) {
                        ((MojBrojVariables) gameVars).setDifferenceBlue(mojBroj.getDifference());
                    } else {
                        ((MojBrojVariables) gameVars).setDifferenceRed(mojBroj.getDifference());
                    }
                    break;
                case Slagalica: gameVars = mySlagalicaVars(transaction, username); break;
                case Skocko: if (roundTwo) { gameVars = mySkockoVars(transaction, username); } break;
                case Spojnice: if (roundTwo) { gameVars = mySpojniceVars(transaction, username); } break;
                case Asocijacije: gameVars = myAsocijacijeVars(transaction, username); break;
                default: break;
            }

            if (gameVars != null) {
                if (playerIsBlue) gameVars.setPointsBlue(myPoints);
                else gameVars.setPointsRed(myPoints);
            }
        }
    }

    private void loadPreparedGameData(Transaction transaction){
        switch (nextGame) {
            case Slagalica: slagalica = new Slagalica(PreparationManager.loadSlagalica(transaction, username)); break;
            case MojBroj: mojBroj = new MojBroj(PreparationManager.loadMojBroj(transaction, username)); break;
            case Skocko: skocko = new Skocko(PreparationManager.loadSkocko(transaction, username)); break;
            case Spojnice:
                spojnice = new Spojnice();
                PreparationManager.loadSpojnice(transaction, username, spojnice); break;
            case Asocijacije: asocijacije = new Asocijacije(PreparationManager.loadAsocijacije(transaction, username)); break;
            default: break;
        }
    }

    private void savePoints(Transaction transaction) {
        if(currentGame!=null) {
            GameVariables gameVars = null;
            switch(currentGame) {
                case Slagalica: gameVars = mySlagalicaVars(transaction, username); break;
                case MojBroj: gameVars = myMojBrojVars(transaction, username); break;
                case Skocko: if(roundTwo){ gameVars = mySkockoVars(transaction, username); } break;
                case Spojnice: if(roundTwo){ gameVars = mySpojniceVars(transaction, username); } break;
                case Asocijacije: gameVars = myAsocijacijeVars(transaction, username); break;
                default: break;
            }
            int pointsBlue=0, pointsRed=0;
            if(gameVars != null) {
                gameVars.fixPoints();
                pointsBlue = gameVars.getPointsBlue();
                pointsRed = gameVars.getPointsRed();
            }
            if((currentGame!= GameView.Skocko  && currentGame!= GameView.Spojnice) || roundTwo) {
                gamePoints.add(new GamePoints(currentGame, pointsBlue, pointsRed));
            }
        }
    }

    private void prepareNextGame() {
        waitingPeriod = false;
        if(modeMultiplayer) {
            if(!roundTwo && (currentGame == GameView.Skocko || currentGame == GameView.Spojnice)) {
                roundTwo = true;
                blueIsPlaying = false;
                timer = 60;
                gameView = currentGame;
                return;
            }
            roundTwo = false;
        }

        blueIsPlaying = true;
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

    private void gameOverSingleplayer() {
        int totalPoints = 0;
        for(GamePoints game: gamePoints) {
            totalPoints+=game.blue;
        }
        gamePoints.add(new GamePoints("Total", totalPoints));

        try(Transaction transaction = new Transaction()) {
            transaction.save(new SingleplayerGame(username, totalPoints));
        }
    }

    private void gameOverMultiplayer() {
        int totalRed = 0;
        int totalBlue = 0;
        for(GamePoints game: gamePoints) {
            totalRed+=game.red;
            totalBlue+=game.blue;
        }
        gamePoints.add(new GamePoints("Total", totalBlue, totalRed));

        //if it's your job, save the final database entry
        if(playerIsBlue){
            try(Transaction transaction = new Transaction()) {

                cleanUpDatabase(transaction, username);

                ActiveGame activeGame = myActiveGame(transaction, username);

                FinishedGame finishedGame = new FinishedGame(activeGame, totalBlue, totalRed);
                if (finishedGame.getPointsBlue() > finishedGame.getPointsRed())
                    finishedGame.setGameResult((short) 1);
                else if (finishedGame.getPointsBlue() < finishedGame.getPointsRed())
                    finishedGame.setGameResult((short) -1);
                else finishedGame.setGameResult((short) 0);

                transaction.delete(activeGame);
                transaction.save(finishedGame);
            }
        }
    }

    private boolean canPlay() {
        return myTurn() && !waitingPeriod;
    }

    private boolean myTurn() {
        return (blueIsPlaying && playerIsBlue) || (!blueIsPlaying && !playerIsBlue);
    }

    private void alternatingGamesTick() {
        try(Transaction transaction = new Transaction()) {
            switch (currentGame) {
                case Skocko: {
                    if (skocko.isSidePlayer() == null) {
                        skocko.setSidePlayer(!myTurn());
                    }
                    sidePlayerGameTicks(skocko, mySkockoVars(transaction, username));
                    break;
                }
                case Spojnice: {
                    if (spojnice.isSidePlayer() == null) {
                        spojnice.setSidePlayer(!myTurn());
                    }
                    sidePlayerGameTicks(spojnice, mySpojniceVars(transaction, username));
                    break;
                }
                case Asocijacije: {
                    asocijacijeTicks(myAsocijacijeVars(transaction, username));
                    break;
                }
            }
        }
    }

    private void asocijacijeTicks(AsocijacijeVariables asocijacijeVars){
        if (!myTurn()) {
            asocijacije.setOpenedArray(asocijacijeVars.getOpened());
            asocijacije.setRevealedByArray(asocijacijeVars, !blueIsPlaying);

            if (asocijacije.isCompleted()) {
                startTheWaitingPeriod();
            }
            else {
                blueIsPlaying = asocijacijeVars.isBluePlaying();
            }

            asocijacije.setFieldWasOpened(false);
        }
        if (timer == 0) {
            asocijacije.openAll();
            startTheWaitingPeriod();
        }
    }

    private void synchronizeAsocijacijeData() {
        try(Transaction transaction = new Transaction()) {
            AsocijacijeVariables asocijacijeVars = myAsocijacijeVars(transaction, username);
            if (!asocijacije.wasHit()) {
                asocijacijeVars.setMessage(asocijacije.getOpened(), asocijacije.getRevealedByPlayer(playerIsBlue), !blueIsPlaying);
                blueIsPlaying = !blueIsPlaying;
            } else {
                asocijacijeVars.setMessage(asocijacije.getOpened(), asocijacije.getRevealedByPlayer(playerIsBlue), blueIsPlaying);
            }
        }
    }

    private void synchronizeSidePlayerGameDataAndCloseIfDone(SidePlayerGame game,
                                                             BiFunction<Transaction, String, SidePlayerGameVariables> myVars) {
        try(Transaction transaction = new Transaction()) {
            SidePlayerGameVariables variables = myVars.apply(transaction, username);

            variables.updateVariables(game, playerIsBlue);

            if (game.isCompleted()) {
                startTheWaitingPeriod();
            } else if (game.playerFinished()) {
                finishSidePlayerGame(game, variables);
            }
        }
    }

    private void sidePlayerGameTicks(SidePlayerGame game, SidePlayerGameVariables variables) {
        if(myTurn()){
            if(timer == 0) {
                finishSidePlayerGame(game, variables);
            }
        }
        else {
            game.updateVariables(variables, !playerIsBlue);
            finishSidePlayerGameIfOpponentFinished(game, variables);
        }
    }

    private void finishSidePlayerGameIfOpponentFinished(SidePlayerGame game, SidePlayerGameVariables variables) {
        if (game.isCompleted() || variables.isSidePlayerDone()) {
            timer = 3;
            waitingPeriod = true;
        } else if (blueIsPlaying != variables.isBluePlaying()) {
            timer = 60;
            blueIsPlaying = !blueIsPlaying;
            game.getReadyForSidePlayer();
        }

        if (timer < 0) {
            timer = 0;
        }
    }

    private void finishSidePlayerGame(SidePlayerGame game, SidePlayerGameVariables variables) {
        if (game.isSidePlayer() != null? game.isSidePlayer() : false) {
            variables.setSidePlayerDone(true);
            startTheWaitingPeriod();
        } else {
            variables.setBluePlaying(!blueIsPlaying);
            blueIsPlaying = !blueIsPlaying;
            timer = 60;
        }
    }

    private void startTheWaitingPeriod() {
        waitingPeriod = true;
        timer = 3;
    }
}