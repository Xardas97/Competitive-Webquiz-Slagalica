package games;

import controllers.GameController.GameView;
import entities.GameVariables;
import services.ActiveGameService;
import util.PointsManager;
import util.Transaction;

public class Slagalica implements Game {
    private static final GameView MY_GAME = GameView.Slagalica;
    private static final GameView NEXT_GAME = GameView.MojBroj;

    private final boolean[] buttons = {true, true, true, true, true, true, true, true, true, true, true, true};
    private final String[] possibleLetters;
    private String chosenWord = "";

    Slagalica(String[] possibleLetters) {
        this.possibleLetters = possibleLetters;
    }

    public void addLetter(int i){
        chosenWord += possibleLetters[i];
        buttons[i] = false;
    }

    public void reset() {
        for(int i=0; i<12; i++) {
            buttons[i] = true;
        }
        chosenWord = "";
    }

    public boolean buttonAvailable(int i){
        return buttons[i];
    }

    public String getLetter(int i) {
        return possibleLetters[i];
    }

    public boolean[] getButtons() {
        return buttons;
    }

    public String getChosenWord() {
        return chosenWord;
    }

    @Override
    public int getPoints() {
        return PointsManager.slagalica(this);
    }

    @Override
    public GameVariables getMyVars(Transaction t, String username) {
        return ActiveGameService.mySlagalicaVars(t, username);
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
