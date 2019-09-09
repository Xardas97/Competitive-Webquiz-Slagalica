/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import static services.ActiveGameService.*;
import services.ActiveGameService;
import classes.GamePoints;
import entities.*;
import games.*;
import util.*;
import java.io.Serializable;
import java.util.*;
import java.util.function.BiFunction;
import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

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

    private GameView currentView, nextGame, currentGame;

    private int timer;
    private long lastTimerTick;

    private boolean playerIsBlue, gameIsMultiplayer;

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
        currentView = GameView.Waiting;
        username = SessionManager.getUser().getUsername();
        gameIsMultiplayer = "multiplayer".equals(SessionManager.getGameMode());
        playerIsBlue = !gameIsMultiplayer || "blue".equals(SessionManager.getPlayerSide());
        lastTimerTick=0;

        if(playerIsBlue) {
            if (gameIsMultiplayer) {
                prepareNextMultiplayerGameData();
            }
            else {
                prepareSingleplayerGamesData();
                startNextGame();
            }
        }
    }

    /**
     * Called once every second
     */
    public void periodicCall(){
        if(isGameInProgress()) {
            runWhileInGame();
        }
        if(currentView == GameView.Waiting) {
            tryToStartNextGame(); // only multiplayer games are ever in waiting
        }
    }

    /**
     * Gets periodically called while we are in a game.
     * If the timer has run out calls finished().
     */
    private void runWhileInGame(){
        updateTimer();
        // if it's one of the games where players alternate call alternatingGamesTick() and exit
        if(gameIsMultiplayer && !waitingPeriod && ALTERNATING_GAMES.contains(currentGame)) {
            runWhileInAlternatingGame();
            return;
        }

        if(timer==0) finish();
    }

    /**
     * Updates our points after we are done with the game
     * and sends us to the waiting screen.
     */
    public void finish() {
        int myPoints = getMyCurrentGamePoints();

        if (gameIsMultiplayer) {
            updatePointsInDatabase(myPoints);
            currentView = GameView.Waiting;
            if (playerIsBlue) {
                prepareNextMultiplayerGameData();
            }
        } else {
            gamePoints.add(new GamePoints(currentGame, myPoints));
            startNextGame();
        }
    }

    /**
     * Gets periodically called while we are waiting for the next game.
     * In multiplayer:
     * If both players are ready it changes to the next game.
     * If it's the end of the game Blue updates the database.
     */
    private void tryToStartNextGame() {
        boolean dataSynchronized = synchronizeGameData();
        if (dataSynchronized) {
            startNextGame();
        }
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
        return skocko.buttonsAvailable(gameIsMultiplayer) && canPlay();
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

        if (gameIsMultiplayer) {
            synchronizeSidePlayerGameDataAndCloseIfGameCompleted(skocko, ActiveGameService::mySkockoVars);
        }
        else {
            if (skocko.isCompleted() || skocko.playerFinished()) {
                startTheWaitingPeriod();
            }
        }
    }

    /*
     ***** GAME: SPOJNICE *****
     */

    public void choosePair(int i){
        spojnice.submitPair(i, playerIsBlue);

        if(gameIsMultiplayer) {
            synchronizeSidePlayerGameDataAndCloseIfGameCompleted(spojnice, ActiveGameService::mySpojniceVars);
        }
        else {
            if(spojnice.playerFinished()){
                startTheWaitingPeriod();
            }
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

        if(gameIsMultiplayer){
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
        return myView== currentView;
    }

    public int getTimer() {
        return timer;
    }

    public boolean isGameInProgress() {
        return !(currentView == GameView.Waiting || currentView == GameView.GameOver);
    }

    public boolean isMultiplayer() {
        return gameIsMultiplayer;
    }

    public String applyColor(GamePoints game){
        if(!"Total".equals(game.game)) return "";
        if(game.blue == game.red) return "coloredNeutral";
        if(playerIsBlue == (game.blue > game.red)) return "coloredVictory";
        return "coloredDefeat";
    }

    public boolean isWaitingPeriod() {
        return waitingPeriod;
    }

    /*
     ***** PRIVATE HELPERS *****
     */

    private void prepareSingleplayerGamesData() {
        try (Transaction transaction = new Transaction()) {
            GameOfTheDay game = currentGameOfTheDay(transaction);

            slagalica = new Slagalica(game.getLetters());
            mojBroj = new MojBroj(game.getNumbers());
            skocko = new Skocko(game.getSecretCombo());
            spojnice = new Spojnice(game.getPairs());
            asocijacije = new Asocijacije(game.getAsocijacija());

            game.setPlayed(true);
        }
    }

    private void prepareNextMultiplayerGameData() {
        try(Transaction transaction = new Transaction()) {
            ActiveGame game = myActiveGame(transaction, username);

            switch (nextGame) {
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
                            .generateSkocko(transaction, game.getBlue(), game.getRed(), true, !roundTwo));
                    break;
                case Spojnice:
                    spojnice = new Spojnice();
                    PreparationManager.generateSpojnice(transaction, game.getBlue(), game.getRed(), spojnice, !roundTwo);
                    break;
                case Asocijacije:
                    asocijacije = new Asocijacije(PreparationManager
                            .generateAsocijacije(transaction, game.getBlue(), game.getRed()));
                    break;
                default:
                    break;
            }
            // Read synchronizeNextGameData() method documentation
            game.setBlueReady(true);
        }
    }

    /**
     * Blue lets red pass once he prepares the data for the next game
     * Red lets blue pass once he loads the prepared data
     * @return true if synchronization is successful.
     */
    private boolean synchronizeGameData() {
        try(Transaction transaction = new Transaction()) {
                ActiveGame game = myActiveGame(transaction, username);

                boolean otherPlayerReady = playerIsBlue? game.isRedReady(): game.isBlueReady();
                if (otherPlayerReady) {
                    savePoints(transaction);

                    if(playerIsBlue) {
                        game.setRedReady(false);
                    }
                    else {
                        game.setBlueReady(false);
                        loadPreparedGameData(transaction);
                        game.setRedReady(true);
                    }
                }
            return otherPlayerReady;
        }
    }

    private void savePoints(Transaction transaction) {
        if(currentGame != null) {
            GameVariables gameVars = null;
            switch(currentGame) {
                case Slagalica: gameVars = mySlagalicaVars(transaction, username); break;
                case MojBroj: gameVars = myMojBrojVars(transaction, username); break;
                case Skocko: if(roundTwo){ gameVars = mySkockoVars(transaction, username); } break;
                case Spojnice: if(roundTwo){ gameVars = mySpojniceVars(transaction, username); } break;
                case Asocijacije: gameVars = myAsocijacijeVars(transaction, username); break;
                default: break;
            }
            if(gameVars != null) {
                gameVars.fixPoints();
                gamePoints.add(new GamePoints(currentGame, gameVars.getPointsBlue(), gameVars.getPointsRed()));
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

    private void startNextGame() {
        waitingPeriod = false;

        if(gameIsMultiplayer) {
            if((currentGame == GameView.Skocko || currentGame == GameView.Spojnice) && !roundTwo) {
                beginRoundTwo();
                return;
            }
            roundTwo = false;
        }

        blueIsPlaying = true;
        currentView = currentGame = nextGame;

        switch(currentGame){
            case Slagalica: nextGame = GameView.MojBroj; timer = 60; break;
            case MojBroj: nextGame = GameView.Skocko; timer = 60; break;
            case Skocko: nextGame = GameView.Spojnice; timer = 60; break;
            case Spojnice: nextGame = GameView.Asocijacije; timer = 60; break;
            case Asocijacije: nextGame = GameView.GameOver; timer = 240; break;
            case GameOver: if(gameIsMultiplayer) gameOverMultiplayer(); else gameOverSingleplayer();
        }
    }

    private void beginRoundTwo() {
        roundTwo = true;
        blueIsPlaying = false;
        timer = 60;
        currentView = currentGame;
    }

    private void updateTimer() {
        long currentTimerTick = System.currentTimeMillis();
        if(lastTimerTick==0 || currentTimerTick-lastTimerTick > 500) timer--;
        lastTimerTick = currentTimerTick;
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

    private void updatePointsInDatabase(int myPoints) {
        try(Transaction transaction = new Transaction()) {
            GameVariables gameVars = null;

            switch (currentGame) {
                case MojBroj:
                    gameVars = myMojBrojVars(transaction, username);
                    ((MojBrojVariables) gameVars).setDifference(mojBroj.getDifference(), playerIsBlue);
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
        int totalBlue = 0, totalRed = 0;
        for(GamePoints game: gamePoints) {
            totalBlue+=game.blue;
            totalRed+=game.red;
        }
        gamePoints.add(new GamePoints("Total", totalBlue, totalRed));

        if(playerIsBlue) {
            finalDatabaseUpdate(totalBlue, totalRed);
        }
    }

    private void finalDatabaseUpdate(int totalBlue, int totalRed) {
        try(Transaction transaction = new Transaction()) {
            cleanUpDatabase(transaction, username);

            ActiveGame activeGame = myActiveGame(transaction, username);

            FinishedGame finishedGame = new FinishedGame(activeGame, totalBlue, totalRed);
            short result = (short) Integer.compare(finishedGame.getPointsBlue(), finishedGame.getPointsRed());
            finishedGame.setGameResult(result);

            transaction.delete(activeGame);
            transaction.save(finishedGame);
        }
    }

    private void runWhileInAlternatingGame() {
        try(Transaction transaction = new Transaction()) {
            switch (currentGame) {
                case Skocko: {
                    if (skocko.isSidePlayer() == null) {
                        skocko.setSidePlayer(!myTurn());
                    }
                    runWhileInSidePlayerGame(skocko, mySkockoVars(transaction, username));
                    break;
                }
                case Spojnice: {
                    if (spojnice.isSidePlayer() == null) {
                        spojnice.setSidePlayer(!myTurn());
                    }
                    runWhileInSidePlayerGame(spojnice, mySpojniceVars(transaction, username));
                    break;
                }
                case Asocijacije: {
                    runWhileInAsocijacije(myAsocijacijeVars(transaction, username));
                    break;
                }
            }
        }
    }

    private void runWhileInAsocijacije(AsocijacijeVariables asocijacijeVars) {
        if (!myTurn()) {
            asocijacije.loadOpenedArray(asocijacijeVars.getOpened());
            asocijacije.loadRevealedByArray(asocijacijeVars, !blueIsPlaying);
            blueIsPlaying = asocijacijeVars.isBluePlaying();

            if (asocijacije.isCompleted()) {
                startTheWaitingPeriod();
            }

            asocijacije.setFieldWasOpened(false);
        }

        if (timer == 0) {
            asocijacije.openAll();
            startTheWaitingPeriod();
        }
    }

    @SuppressWarnings("SimplifiableConditionalExpression")
    private void synchronizeAsocijacijeData() {
        try(Transaction transaction = new Transaction()) {
            AsocijacijeVariables asocijacijeVars = myAsocijacijeVars(transaction, username);
            blueIsPlaying = asocijacije.wasHit()? blueIsPlaying: !blueIsPlaying;
            asocijacijeVars.updateVariables(asocijacije.getOpened(), asocijacije.getRevealedByPlayer(playerIsBlue), blueIsPlaying);
        }
    }

    private void runWhileInSidePlayerGame(SidePlayerGame game, SidePlayerGameVariables variables) {
        if(myTurn()){
            if(timer == 0) {
                finishSidePlayerGame(game, variables);
            }
        }
        else {
            game.loadVariables(variables, !playerIsBlue);
            finishSidePlayerGameIfOpponentFinished(game, variables);

            if (timer < 0) {
                timer = 0;
            }
        }
    }

    private void synchronizeSidePlayerGameDataAndCloseIfGameCompleted(
            SidePlayerGame game,BiFunction<Transaction, String, SidePlayerGameVariables> getMyVars) {
        try(Transaction transaction = new Transaction()) {
            SidePlayerGameVariables variables = getMyVars.apply(transaction, username);

            variables.updateVariables(game, playerIsBlue);

            if (game.isCompleted()) {
                startTheWaitingPeriod();
            } else if (game.playerFinished()) {
                finishSidePlayerGame(game, variables);
            }
        }
    }

    private void finishSidePlayerGameIfOpponentFinished(SidePlayerGame game, SidePlayerGameVariables variables) {
        if (game.isCompleted() || variables.isSidePlayerDone()) {
            startTheWaitingPeriod();
        } else if (blueIsPlaying != variables.isBluePlaying()) {
            blueIsPlaying = !blueIsPlaying;
            timer = 60;
            game.getReadyForSidePlayer();
        }
    }

    private void finishSidePlayerGame(SidePlayerGame game, SidePlayerGameVariables variables) {
        if (game.isSidePlayer() != null? game.isSidePlayer() : false) {
            variables.setSidePlayerDone(true);
            startTheWaitingPeriod();
        } else {
            blueIsPlaying = !blueIsPlaying;
            variables.setBluePlaying(blueIsPlaying);
            timer = 60;
        }
    }

    private void startTheWaitingPeriod() {
        waitingPeriod = true;
        timer = 3;
    }

    private boolean canPlay() {
        return myTurn() && !waitingPeriod;
    }

    private boolean myTurn() {
        return blueIsPlaying == playerIsBlue;
    }
}