package games;

public class Slagalica {
    private final boolean[] buttons = {true, true, true, true, true, true, true, true, true, true, true, true};
    private final String[] letters;
    private String word = "";

    public Slagalica(String letters) {
        this.letters = letters.split(" ");
    }

    public Slagalica(String[] letters) {
        this.letters = letters;
    }

    public void addLetter(int i){
        word += letters[i];
        buttons[i] = false;
    }

    public boolean buttonAvailable(int i){
        return buttons[i];
    }

    public void reset() {
        for(int i=0; i<12; i++) buttons[i] = true;
        word = "";
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
}
