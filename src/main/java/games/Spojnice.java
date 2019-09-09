package games;

import controllers.GameController.GameView;
import entities.*;
import services.ActiveGameService;
import util.Transaction;

public class Spojnice implements SidePlayerGame {
    private static final GameView MY_GAME = GameView.Spojnice;
    private static final GameView NEXT_GAME = GameView.Asocijacije;
    private int activeLeft = 0;
    private String gameName;
    private final String[][] words = new String[10][2];
    private final boolean[] hitByBlue = new boolean[10];
    private final boolean[] hitByRed = new boolean[10];
    private final int[] pairPosition = new int[10];
    private int points = 0;
    private Boolean sidePlayer = null;
    private boolean completed = false;

    Spojnice() {
        init();
    }

    Spojnice(WordPairs pairs) {
        init();
        gameName = pairs.getText();
    }

    private void init() {
        for(int i=0; i<10; i++) {
            hitByBlue[i] = hitByRed[i] = false;
        }
    }

    public void submitPair(int i, boolean playerBlue) {
        if(sidePlayer == null) sidePlayer = Boolean.FALSE;

        if(activeLeft == pairPosition[i]) {
            points++;
            if(playerBlue) hitByBlue[activeLeft] = true;
            else hitByRed[activeLeft] = true;
        }

        activeLeft++;
        while(activeLeft<10 && (hitByBlue[activeLeft] || hitByRed[activeLeft])) {
            activeLeft++;
        }
    }

    @Override
    public void loadVariables(SidePlayerGameVariables variables, boolean forBlue) {
        if(variables instanceof SpojniceVariables) {
            activeLeft = 10;
            if(forBlue){
                setHitByArray(((SpojniceVariables) variables).getHitByBlue(), hitByBlue);
            }
            else{
                setHitByArray(((SpojniceVariables) variables).getHitByRed(), hitByRed);
            }
        }
    }

    public String hitByMeAsString(boolean playerBlue) {
        StringBuilder builder = new StringBuilder();
        completed = true;
        boolean[] hitByMe;
        if(playerBlue) hitByMe = hitByBlue;
        else hitByMe = hitByRed;
        if (hitByMe[0]) {
            builder.append("1");
        } else {
            builder.append("0");
            completed = false;
        }
        for (int cnt = 1; cnt < 10; cnt++) {
            builder.append(" ");
            if (hitByMe[cnt]) {
                builder.append("1");
            } else {
                builder.append("0");
                completed = false;
            }
        }
        return builder.toString();
    }

    private void setHitByArray(String hitByAsString, boolean[] hitBy) {
        completed = true;
        String[] temp = hitByAsString.split(" ");
        for (int i = 0; i < 10; i++)
            if ("1".equals(temp[i])) hitBy[i] = true;
            else {
                hitBy[i] = false;
                completed = false;
            }
    }

    public String colorLeft(int i){
        if(i == activeLeft) return "coloredActive";
        if(hitByBlue[i]) return "coloredBlue";
        if(hitByRed[i]) return "coloredRed";
        return "";
    }

    public String colorRight(int i){
        if(hitByBlue[pairPosition[i]]) return "coloredBlue";
        if(hitByRed[pairPosition[i]]) return "coloredRed";
        return "";
    }

    public boolean buttonDisabled(int i){
        return hitByBlue[pairPosition[i]] || hitByRed[pairPosition[i]]
                || activeLeft>9;
    }

    @Override
    public Boolean isSidePlayer() {
        return sidePlayer;
    }

    @Override
    public void setSidePlayer(Boolean sidePlayer) {
        this.sidePlayer = sidePlayer;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getRightWord(int i){
        return words[pairPosition[i]][1];
    }

    public String getLeftWord(int i){
        return words[i][0];
    }

    public String[][] getWords() {
        return words;
    }

    public int[] getPairPosition() {
        return pairPosition;
    }

    public String getGameName() {
        return gameName;
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    @Override
    public boolean playerFinished() {
        return activeLeft > 9;
    }

    @Override
    public void getReadyForSidePlayer() {
        activeLeft = 0;
        while(hitByBlue[activeLeft] || hitByRed[activeLeft]) activeLeft++;
    }

    @Override
    public int getPoints() {
        return points;
    }

    @Override
    public SidePlayerGameVariables getMyVars(Transaction t, String username) {
        return ActiveGameService.mySpojniceVars(t, username);
    }

    @Override
    public GameView getNextView() {
        return NEXT_GAME;
    }

    @Override
    public GameView getMyView() {
        return MY_GAME;
    }
}
