package games;

public class MojBroj {
    private static final String[] OPERATIONS = {"+", "-", "*", "/", "(", ")"};
    private final boolean[] buttons = {true, true, true, true, true, true};
    private final String[] numbers;
    private String word = "";
    private String message = "";
    private boolean lastUsedNumber = false;
    private int difference;

    public MojBroj(String numbers) {
        this.numbers = numbers.split(" ");
    }

    public MojBroj(String[] numbers) {
        this.numbers = numbers;
    }

    public String getNumberOrOperation(int i){
        if(i<7) return numbers[i];
        else return OPERATIONS[i-7];
    }

    public void chooseNumberOrOperation(int i){
        message = "";
        if(i<7) {
            if(lastUsedNumber) { message = "Can't use a Number again!"; return; }
            word += numbers[i];
            buttons[i-1] = false;
            lastUsedNumber = true;
        }
        else{
            word += OPERATIONS[i-7];
            lastUsedNumber = false;
        }
    }

    public boolean buttonAvailable(int i){
        if(i<7) return buttons[i-1];
        return true;
    }

    public void reset(){
        for(int i=0; i<6; i++) buttons[i] = true;
        word = "";
        message = "";
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
}
