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
    private final String[] numbers;
    private String word = "";
    private String message = "";
    private boolean lastUsedNumber = false;
    private int difference;

    MojBroj(String numbers) {
        this.numbers = numbers.split(" ");
    }

    MojBroj(String[] numbers) {
        this.numbers = numbers;
    }

    public void chooseNumberOrOperation(int i){
        message = "";
        if(i<7) {
            if(lastUsedNumber) {
                message = "Can't use a Number again!";
                return;
            }
            word += numbers[i];
            buttons[i-1] = false;
            lastUsedNumber = true;
        }
        else{
            word += OPERATIONS[i-7];
            lastUsedNumber = false;
        }
    }

    public void reset(){
        for(int i=0; i<6; i++) buttons[i] = true;
        word = "";
        message = "";
    }

    public String getNumberOrOperation(int i){
        if(i<7) return numbers[i];
        else return OPERATIONS[i-7];
    }

    public boolean buttonAvailable(int i){
        if(i<7) return buttons[i-1];
        return true;
    }

    public String getDesiredNumber() {
        return numbers[0];
    }


    public String getMessage() {
        return message;
    }

    public String getWord() {
        return word;
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
    public GameView getMyView() {
        return MY_GAME;
    }
}
