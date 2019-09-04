package games;

import entities.Asocijacija;
import entities.AsocijacijeVariables;

public class Asocijacije {
    private static final String[] PLACEHOLDERS = {"A", "B", "C", "D", "? ? ?"};
    private final String[] columns;
    private final String[][] results;
    private String[] openedResults = {"", "", "", "", ""};
    private final boolean[] opened;
    private final boolean[] blueRevealed = {false, false, false, false, false};
    private final boolean[] redRevealed = {false, false, false, false, false};
    private int points = 0;
    private boolean fieldWasOpened = false;
    private boolean wasHit = false;

    public Asocijacije(Asocijacija asocijacija) {
        opened = new boolean[21];
        for(int i=0; i<21; i++) opened[i] = false;

        columns = asocijacija.getColumns().split("-");

        results = new String[5][];
        results[0] = asocijacija.getResultA().split("\n");
        results[1] = asocijacija.getResultB().split("\n");
        results[2] = asocijacija.getResultC().split("\n");
        results[3] = asocijacija.getResultD().split("\n");
        results[4] = asocijacija.getResultEnd().split("-");
        fixResultStrings(results);
    }

    private void fixResultStrings(String[][] results ){
        for(int i = 0; i < 4; i++){
            for (int j = 0; j < results[i].length - 1; j++) {
                results[i][j] = results[i][j].substring(0, results[i][j].length() - 1);
            }
        }
    }

    public void submit(boolean playerBlue) {
        fieldWasOpened = false;

        boolean[] myRevealArray;
        if (playerBlue) myRevealArray = blueRevealed;
        else myRevealArray = redRevealed;

        int submitted;
        if (wasHit) submitted = 4;
        else
            for (submitted = 0; submitted < 5; submitted++)
                if (!opened[16 + submitted] && !"".equals(openedResults[submitted])) break;

        wasHit = false;

        if(submitted == 4) {
            if (isCorrect(openedResults[4], results[4])) {
                points += 10;
                myRevealArray[4] = true;
                for (int i = 0; i < 4; i++)
                    if (!opened[16 + i]) {
                        myRevealArray[i] = true;
                        points += 5;
                    }
                for (int i = 0; i < 21; i++) opened[i] = true;
            }
        }
        else {
            if(submitted < 4 && isCorrect(openedResults[submitted], results[submitted])) {
                points += 5;
                for(int i=submitted*4; i < (submitted + 1)*4; i++) opened[i] = true;
                opened[16 + submitted] = true;
                myRevealArray[submitted] = true;
                wasHit = true;
            }
        }

        for (int i = 0; i < 5; i++) openedResults[i] = "";

        if (wasHit) fieldWasOpened = true;
    }

    public void openField(int i){
        fieldWasOpened = true;
        opened[i] = true;
    }

    public void openAll() {
        for(int i=0; i<21; i++) opened[i] = true;
    }

    public String getFieldName(int i){
        if(i<0 || i>15) return "error";
        if(opened[i]) return columns[i];
        return PLACEHOLDERS[i/4] + (i % 4 + 1);
    }

    public String getColumnResultName(int i) {
        if(i<0 || i>5) return "error";
        if(opened[16+i]) {
            return results[i][0];
        }
        return PLACEHOLDERS[i];
    }

    public String getFieldColor(int i){
        if(i<16) i=i%4+16;
        if(blueRevealed[i-16]) return "background-color: #036fab;";
        if(redRevealed[i-16]) return "background-color: red;";
        return "";
    }

    public boolean getOpenFieldDisabled(int i){
        return opened[i] || fieldWasOpened;
    }

    public void setOpenedArray(String openedAsString){
        String[] temp = openedAsString.split(" ");
        for (int i = 0; i < 21; i++)
            if ("1".equals(temp[i])) opened[i] = true;
    }

    public void setRevealedByArray(AsocijacijeVariables variables, boolean forBlue){
        if(forBlue){
            setRevealedByArray(variables.getRevealedByBlue(), blueRevealed);
        }
        else{
            setRevealedByArray(variables.getRevealedByRed(), redRevealed);
        }
    }

    private void setRevealedByArray(String revealedAsString, boolean[] revealed) {
        String[] temp = revealedAsString.split(" ");
        for (int i = 0; i < 5; i++)
            if ("1".equals(temp[i])) revealed[i] = true;
    }


    private boolean isCorrect(String submitted, String[] acceptables){
        if(acceptables == null) return false;
        for(String acceptable: acceptables)
            if(acceptable.toLowerCase().equals(submitted.toLowerCase())) return true;

        return false;
    }

    public boolean[] getRevealedByPlayer(boolean playerBlue) {
        if(playerBlue) return blueRevealed;
        return redRevealed;
    }

    public void setFieldWasOpened(boolean fieldWasOpened) {
        this.fieldWasOpened = fieldWasOpened;
    }

    public String[] getOpenedResults() {
        return openedResults;
    }

    public void setOpenedResults(String[] openedResults) {
        this.openedResults = openedResults;
    }

    public boolean[] getOpened() {
        return opened;
    }

    public boolean isCompleted() {
        return opened[20];
    }

    public boolean wasHit() {
        return wasHit;
    }

    public int getPoints() {
        return points;
    }
}
