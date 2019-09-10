package games;

import controllers.GameController.GameView;
import entities.*;
import services.ActiveGameService;
import util.Transaction;

public class Spojnice implements SidePlayerGame {
    private static final GameView MY_GAME = GameView.Spojnice;
    private static final GameView NEXT_GAME = GameView.Asocijacije;

    private final String gameName;
    private final String[][] pairs;
    private final int[] pairPositions;
    private int activeLeft = 0;
    private final boolean[] hitByBlue = new boolean[10];
    private final boolean[] hitByRed = new boolean[10];
    private int points = 0;
    private Boolean sidePlayer = null;

    Spojnice(String[][] pairs, int[] pairPositions, String gameName) {
        this.pairs = pairs;
        this.pairPositions = pairPositions;
        this.gameName = gameName;
        init();
    }

    private void init() {
        for(int i=0; i<10; i++) {
            hitByBlue[i] = hitByRed[i] = false;
        }
    }

    public void submitPair(int i, boolean playerBlue) {
        if(sidePlayer == null) sidePlayer = Boolean.FALSE;

        if(activeLeft == pairPositions[i]) {
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

    public String getHitByMeAsString(boolean playerBlue) {
        StringBuilder builder = new StringBuilder();

        boolean[] hitByMe = playerBlue? hitByBlue: hitByRed;

        for (boolean b : hitByMe) {
            if (b) {
                builder.append("1");
            }
            else {
                builder.append("0");
            }
            builder.append(" ");
        }
        builder.deleteCharAt(builder.length() - 1);

        return builder.toString();
    }

    private void setHitByArray(String hitByAsString, boolean[] hitBy) {
        String[] temp = hitByAsString.split(" ");

        for (int i = 0; i < 10; i++){
            hitBy[i] = "1".equals(temp[i]);
        }
    }

    public String colorLeft(int i){
        if(i == activeLeft) return "coloredActive";
        if(hitByBlue[i]) return "coloredBlue";
        if(hitByRed[i]) return "coloredRed";
        return "";
    }

    public String colorRight(int i){
        if(hitByBlue[pairPositions[i]]) return "coloredBlue";
        if(hitByRed[pairPositions[i]]) return "coloredRed";
        return "";
    }

    public boolean buttonDisabled(int i){
        return hitByBlue[pairPositions[i]] || hitByRed[pairPositions[i]]
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

    public String getRightWord(int i){
        return pairs[pairPositions[i]][1];
    }

    public String getLeftWord(int i){
        return pairs[i][0];
    }

    public String getGameName() {
        return gameName;
    }

    @Override
    public boolean isCompleted() {
        for(int i=0; i<10; i++) {
            if(!hitByBlue[i] && !hitByRed[i]){
                return false;
            }
        }
        return true;
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
    public GameView getView() {
        return MY_GAME;
    }
}
