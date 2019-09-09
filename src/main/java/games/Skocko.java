package games;

import controllers.GameController.GameView;
import entities.SidePlayerGameVariables;
import entities.SkockoVariables;
import services.ActiveGameService;
import util.Transaction;

public class Skocko implements SidePlayerGame {
    private static final GameView MY_GAME = GameView.Skocko;
    private static final GameView NEXT_GAME = GameView.Spojnice;
    private static final String[] SYMBOLS = {"Pik", "Tref", "Herc", "Karo", "Zvezda", "Skocko"};
    private final String[] secretCombo;
    private String[][] input;
    private int[][] feedback;
    private int currentRow;
    private int currentSymbol;
    private int points = 0;
    private Boolean sidePlayer = null;
    private boolean completed;

    Skocko(String secretCombo) {
        this.secretCombo = secretCombo.split(" ");
        init();
    }

    Skocko(String[] secretCombo) {
        this.secretCombo = secretCombo;
        init();
    }

    private void init() {
        currentRow = currentSymbol = 0;
        completed = false;
        input = new String[7][4];
        feedback = new int[7][2];
        for (int i = 0; i < 7; i++)
            for (int j = 0; j < 4; j++)
                input[i][j] = "x";
    }

    public void chooseSymbol(int i) {
        input[currentRow][currentSymbol] = SYMBOLS[i];
        currentSymbol++;
    }

    public void submitRow(){
        gradeSkocko();
        currentSymbol = 0;
        currentRow++;
    }

    private void gradeSkocko() {
        feedback[currentRow][0] = feedback[currentRow][1] = 0;
        boolean[] hitInput = {false, false, false, false};
        boolean[] hitSecret = {false, false, false, false};

        for(int i=0; i<4; i++)
            if(input[currentRow][i].equals(secretCombo[i])) {
                hitSecret[i] = hitInput[i] = true;
                feedback[currentRow][0]++;
            }

        for(int i=0; i<4; i++)
            if(!hitInput[i])
                for(int j=0; j<4; j++)
                    if(!hitSecret[j] && input[currentRow][i].equals(secretCombo[j])) {
                        hitInput[i] = hitSecret[j] = true;
                        feedback[currentRow][1]++;
                        break;
                    }

        if(feedback[currentRow][0] == 4) {
            points = 10;
            completed = true;
        }
    }

    public void resetCurrentRow(){
        currentSymbol = 0;
        for(int i=0; i<4; i++) input[currentRow][i] = "x";
    }

    @Override
    public void loadVariables(SidePlayerGameVariables variables, boolean forBlue) {
        if(variables instanceof SkockoVariables){
            SkockoVariables skockoVars = (SkockoVariables) variables;
            setInputAndFeedback(skockoVars.getInputCombos(), (skockoVars.getOutputCombos()));
        }
    }

    private void setInputAndFeedback(String inputString, String feedbackString) {
        String[] temp = inputString.split("-");
        for (int i = 0; i < 7; i++) {
            input[i] = temp[i].split(" ");
        }

        temp = feedbackString.split("-");
        for (int i = 0; i < 7; i++) {
            String[] temp2 = temp[i].split(" ");
            for (int j = 0; j < 2; j++) {
                feedback[i][j] = Integer.parseInt(temp2[j]);
            }
            if (feedback[i][0] == 4) {
                completed = true;
            }
        }
    }

    public String getRowAsString(int row) {
        StringBuilder builder = new StringBuilder();
        for(int i=0; i<4; i++)
            builder.append(input[row][i]).append(" ");
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    public String getInputAsString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 4; j++) {
                builder.append(input[i][j]);
                if (j != 3) {
                    builder.append(" ");
                }
            }
            if (i != 6) {
                builder.append("-");
            }
        }
        return builder.toString();
    }

    public String getFeedbackAsString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 2; j++) {
                builder.append(feedback[i][j]);
                if (j != 1) {
                    builder.append(" ");
                }
            }
            if (i != 6) {
                builder.append("-");
            }
        }
        return builder.toString();
    }

    public String getFeedbackRow(int i){
        return "Complete: " + feedback[i][0] + "  Partial: " + feedback[i][1];
    }

    public boolean buttonsAvailable(boolean multiplayer){
        return currentSymbol<4 && (currentRow<6 || (currentRow<7 && multiplayer));
    }

    public boolean submittable() {
        return currentSymbol == 4;
    }

    public String[] getSecretCombo() {
        return secretCombo;
    }

    public static String getSymbol(int i){
        return SYMBOLS[i];
    }

    public static String[] getSymbols() {
        return SYMBOLS;
    }

    @Override
    public Boolean isSidePlayer() {
        return sidePlayer;
    }

    @Override
    public void setSidePlayer(Boolean sidePlayer) {
        this.sidePlayer = sidePlayer;
    }

    @Override
    public int getPoints() {
        return points;
    }

    @Override
    public SidePlayerGameVariables getMyVars(Transaction t, String username) {
        return ActiveGameService.mySkockoVars(t, username);
    }

    @Override
    public GameView getNextView() {
        return NEXT_GAME;
    }

    @Override
    public GameView getMyView() {
        return MY_GAME;
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    @Override
    public boolean playerFinished() {
        return currentRow > 5;
    }

    @Override
    public void getReadyForSidePlayer() {
        currentRow = 6;
    }
}
