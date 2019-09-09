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
    private final String[] letters;
    private String word = "";

    Slagalica(String letters) {
        this.letters = letters.split(" ");
    }

    Slagalica(String[] letters) {
        this.letters = letters;
    }

    public void addLetter(int i){
        word += letters[i];
        buttons[i] = false;
    }

    public void reset() {
        for(int i=0; i<12; i++) {
            buttons[i] = true;
        }
        word = "";
    }

    public boolean buttonAvailable(int i){
        return buttons[i];
    }

    public String getLetter(int i) {
        return letters[i];
    }

    public boolean[] getButtons() {
        return buttons;
    }

    public String getWord() {
        return word;
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
    public GameView getMyView() {
        return MY_GAME;
    }
}
