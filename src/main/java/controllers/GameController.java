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
    public enum GameView { Waiting, Slagalica, MojBroj, Skocko, Spojnice, Asocijacije, GameOver }

    private static final Set<GameView> ALTERNATING_GAMES =
            new HashSet<>(Arrays.asList(GameView.Skocko, GameView.Spojnice, GameView.Asocijacije));
    private static final Set<GameView> TWO_ROUND_GAMES =
            new HashSet<>(Arrays.asList(GameView.Skocko, GameView.Spojnice));

    private GameView currentView, nextView;
    private Game currentGame;

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
    private void init() {
        currentView = GameView.Waiting;
        nextView = GameView.Slagalica;
        username = HttpSessionManager.getUser().getUsername();
        gameIsMultiplayer = "multiplayer".equals(HttpSessionManager.getGameMode());
        playerIsBlue = !gameIsMultiplayer || "blue".equals(HttpSessionManager.getPlayerSide());
        lastTimerTick = 0;

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
        if(gameIsMultiplayer && !waitingPeriod && ALTERNATING_GAMES.contains(currentGame.getView())) {
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
        int myPoints = currentGame.getPoints();

        if (gameIsMultiplayer) {
            updatePointsInDatabase(myPoints);
            currentView = GameView.Waiting;
            if (playerIsBlue) {
                prepareNextMultiplayerGameData();
            }
        } else {
            gamePoints.add(new GamePoints(currentGame.getView(), myPoints));
            startNextGame();
        }
    }

    /**
     * Gets periodically called while we are waiting for the next game.
     * If both players are ready it changes to the next game.
     */
    private void tryToStartNextGame() {
        boolean playersSynchronized = synchronizePlayersAndData();
        if (playersSynchronized) {
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
        return slagalica.getChosenWord();
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
        return mojBroj.getChosenExpression();
    }

    public String getMojBrojMessage() {
        return mojBroj.getOutputMessage();
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
        return asocijacije.getInputs();
    }

    public void setOpenedResults(String[] openedResults) {
        asocijacije.setInputs(openedResults);
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

            slagalica = SlagalicaFactory.create(game);
            mojBroj = MojBrojFactory.create(game);
            skocko = SkockoFactory.create(game);
            spojnice = SpojniceFactory.create(game);
            asocijacije = AsocijacijeFactory.create(game);

            game.setPlayed(true);
        }
    }

    private void prepareNextMultiplayerGameData() {
        try(Transaction transaction = new Transaction()) {
            ActiveGame game = myActiveGame(transaction, username);

            switch (nextView) {
                case Slagalica: slagalica = SlagalicaFactory.create(game, transaction); break;
                case MojBroj: mojBroj = MojBrojFactory.create(game, transaction); break;
                case Skocko: skocko = SkockoFactory.create(game, transaction); break;
                case Spojnice: spojnice = SpojniceFactory.create(game, transaction); break;
                case Asocijacije: asocijacije = AsocijacijeFactory.create(game, transaction); break;
                default: break;
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
    private boolean synchronizePlayersAndData() {
        try (Transaction transaction = new Transaction()) {
            ActiveGame game = myActiveGame(transaction, username);

            boolean otherPlayerReady = playerIsBlue ? game.isRedReady() : game.isBlueReady();
            if (otherPlayerReady) {
                loadPointsFromDatabase(transaction);

                if (playerIsBlue) {
                    game.setRedReady(false);
                } else {
                    game.setBlueReady(false);
                    loadPreparedGameData(transaction);
                    game.setRedReady(true);
                }
                return true;
            }
            return false;
        }
    }

    private void loadPointsFromDatabase(Transaction transaction) {
        if(currentGame != null && !roundOneOfTwo()) {
            GameVariables gameVars = currentGame.getMyVars(transaction, username);
            gameVars.fixPoints();
            gamePoints.add(new GamePoints(currentGame.getView(), gameVars.getPointsBlue(), gameVars.getPointsRed()));
        }
    }

    private void loadPreparedGameData(Transaction transaction){
        switch (nextView) {
            case Slagalica: slagalica = SlagalicaFactory.load(username, transaction); break;
            case MojBroj: mojBroj = MojBrojFactory.load(username, transaction); break;
            case Skocko: skocko = SkockoFactory.load(username, transaction);
            case Spojnice: spojnice = SpojniceFactory.load(username, transaction); break;
            case Asocijacije: asocijacije = AsocijacijeFactory.load(username, transaction); break;
            default: break;
        }
    }

    private void startNextGame() {
        if(gameIsMultiplayer) {
            if(roundOneOfTwo()) {
                beginRoundTwo();
                return;
            }
            roundTwo = false;
        }

        switch(nextView){
            case Slagalica: currentGame = slagalica; break;
            case MojBroj: currentGame = mojBroj; break;
            case Skocko: currentGame = skocko; break;
            case Spojnice: currentGame = spojnice; break;
            case Asocijacije: currentGame = asocijacije; break;
            case GameOver: if(gameIsMultiplayer) gameOverMultiplayer(); else gameOverSingleplayer();
        }

        currentView = nextView;
        nextView = currentGame.getNextView();
        timer = currentGame.getGameLength();
        waitingPeriod = false;
        blueIsPlaying = true;
    }

    private void beginRoundTwo() {
        roundTwo = true;
        blueIsPlaying = false;
        timer = 60;
        currentView = currentGame.getView();
    }

    private void updateTimer() {
        long currentTimerTick = System.currentTimeMillis();
        if(lastTimerTick==0 || currentTimerTick-lastTimerTick > 500) timer--;
        lastTimerTick = currentTimerTick;
    }

    private void updatePointsInDatabase(int myPoints) {
        if (roundOneOfTwo()) return;
        try (Transaction transaction = new Transaction()) {
            GameVariables gameVars = currentGame.getMyVars(transaction, username);

            if (currentGame.getView() == GameView.MojBroj) {
                ((MojBrojVariables) gameVars).setDifference(mojBroj.getDifference(), playerIsBlue);
            }

            if (playerIsBlue) gameVars.setPointsBlue(myPoints);
            else gameVars.setPointsRed(myPoints);
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
            if(currentGame instanceof  Asocijacije) {
                runWhileInAsocijacije(myAsocijacijeVars(transaction, username));
            }
            if(currentGame instanceof SidePlayerGame) {
                SidePlayerGame spGame = (SidePlayerGame) currentGame;
                if (spGame.isSidePlayer() == null) {
                    spGame.setSidePlayer(!myTurn());
                }
                runWhileInSidePlayerGame(spGame, spGame.getMyVars(transaction, username));
            }
        }
    }

    private void runWhileInAsocijacije(AsocijacijeVariables asocijacijeVars) {
        if (!myTurn()) {
            asocijacije.loadVariables(asocijacijeVars, !blueIsPlaying);
            blueIsPlaying = asocijacijeVars.isBlueIsPlaying();

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

    private void synchronizeAsocijacijeData() {
        try(Transaction transaction = new Transaction()) {
            AsocijacijeVariables asocijacijeVars = myAsocijacijeVars(transaction, username);
            asocijacijeVars.updateVariables(asocijacije, playerIsBlue);
            blueIsPlaying = asocijacijeVars.isBlueIsPlaying();
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
        boolean isSidePlayer = game.isSidePlayer() != null? game.isSidePlayer() : false;
        if (isSidePlayer) {
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

    private boolean roundOneOfTwo() {
        return TWO_ROUND_GAMES.contains(currentGame.getView()) && !roundTwo;
    }
}