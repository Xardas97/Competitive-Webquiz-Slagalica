package games;

import controllers.GameController.GameView;
import entities.GameVariables;
import services.ActiveGameService;
import util.PointsManager;
import util.Transaction;

public class MojBroj implements Game {
    private static final GameView MY_GAME = GameView.MojBroj;
    private static final GameView NEXT_GAME = GameView.Skocko;

    private static final String[] OPERATIONS = {"+", "-", "*", "/", "(", ")"};

    private final boolean[] buttons = {true, true, true, true, true, true};
    private final String[] possibleNumbers;
    private String chosenExpression = "";
    private boolean lastUsedIsNumber = false;
    private int difference;
    private String outputMessage = "";

    MojBroj(String[] possibleNumbers) {
        this.possibleNumbers = possibleNumbers;
    }

    public void chooseNumberOrOperation(int i){
        outputMessage = "";
        if(i<7) {
            if(lastUsedIsNumber) {
                outputMessage = "Can't use a Number again!";
                return;
            }
            chosenExpression += possibleNumbers[i];
            buttons[i-1] = false;
            lastUsedIsNumber = true;
        }
        else{
            chosenExpression += OPERATIONS[i-7];
            lastUsedIsNumber = false;
        }
    }

    public void reset(){
        for(int i=0; i<6; i++) buttons[i] = true;
        chosenExpression = "";
        outputMessage = "";
    }

    public String getNumberOrOperation(int i){
        if(i<7) return possibleNumbers[i];
        else return OPERATIONS[i-7];
    }

    public boolean buttonAvailable(int i){
        if(i<7) return buttons[i-1];
        return true;
    }

    public String getDesiredNumber() {
        return possibleNumbers[0];
    }


    public String getOutputMessage() {
        return outputMessage;
    }

    public String getChosenExpression() {
        return chosenExpression;
    }

    public int getDifference() {
        return difference;
    }

    public void setDifference(int difference) {
        this.difference = difference;
    }

    @Override
    public int getPoints() {
        return PointsManager.mojBroj(this);
    }

    @Override
    public GameVariables getMyVars(Transaction t, String username) {
        return ActiveGameService.myMojBrojVars(t, username);
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
