package games;

import entities.Asocijacija;

public class Asocijacije {
    private static final String[] PLACEHOLDERS = {"A", "B", "C", "D", "? ? ?"};
    private final String[] columns;
    private final String[] resultA;
    private final String[] resultB;
    private final String[] resultC;
    private final String[] resultD;
    private final String[] resultEnd;
    private String[] openedResults = {"", "", "", "", ""};
    private boolean[] opened;
    private final boolean[] blueRevealed = {false, false, false, false, false};
    private final boolean[] redRevealed = {false, false, false, false, false};
    private int points = 0;
    private boolean fieldWasOpened = false;
    private boolean wasHit = false;

    public Asocijacije(Asocijacija asocijacija) {
        opened = new boolean[21];
        for(int i=0; i<21; i++) opened[i] = false;

        columns = asocijacija.getColumns().split("-");
        resultA = asocijacija.getResultA().split("\n");
        resultB = asocijacija.getResultB().split("\n");
        resultC = asocijacija.getResultC().split("\n");
        resultD = asocijacija.getResultD().split("\n");
        resultEnd = asocijacija.getResultEnd().split("-");
        fixResultString(resultA);
        fixResultString(resultB);
        fixResultString(resultC);
        fixResultString(resultD);
    }

    private void fixResultString(String[] result){
        for (int i = 0; i < result.length - 1; i++) {
            result[i] = result[i].substring(0, result[i].length() - 1);
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

        String[] result = null;
        switch(submitted){
            case 0: result = resultA; break;
            case 1: result = resultB; break;
            case 2: result = resultC; break;
            case 3: result = resultD; break;
            case 4: result = resultEnd; break;
            default: break;
        }

        switch (submitted) {
            case 0:
            case 1:
            case 2:
            case 3:
                if( isCorrect(openedResults[submitted], result)) {
                    points += 5;
                    for(int i=submitted*4; i < (submitted + 1)*4; i++) opened[i] = true;
                    opened[16 + submitted] = true;
                    myRevealArray[submitted] = true;
                    wasHit = true;
                }
                break;
            case 4:
                if (isCorrect(openedResults[4], resultEnd)) {
                    points += 10;
                    myRevealArray[4] = true;
                    for (int i = 0; i < 4; i++)
                        if (!opened[16 + i]) {
                            myRevealArray[i] = true;
                            points += 5;
                        }
                    for (int i = 0; i < 21; i++) opened[i] = true;
                }
                break;
            default:
                break;
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
        if(opened[i]) return columns[i];
        else {
            String columnName = "";
            switch(i/4){
                case 0: columnName = "A"; break;
                case 1: columnName = "B"; break;
                case 2: columnName = "C"; break;
                case 3: columnName = "D"; break;
            }
            columnName += Integer.toString(i%4+1);
            return columnName;
        }
    }

    public String getColumnResultName(int i) {
        if(i<0 || i>5) return "error";
        if(opened[16+i]) {
            switch(i){
                case 0: return resultA[0];
                case 1: return resultB[0];
                case 2: return resultC[0];
                case 3: return resultD[0];
                case 4: return resultEnd[0];
                default: return "error";
            }
        }
        else return PLACEHOLDERS[i];
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

    public void setRevealedByBlue(String blueRevealedAsString) {
        setRevealedByArray(blueRevealedAsString, blueRevealed);
    }

    public void setRevealedByRed(String redRevealedString) {
        setRevealedByArray(redRevealedString, redRevealed);
    }

    private void setRevealedByArray(String revealedAsString, boolean[] revealed) {
        String[] temp = revealedAsString.split(" ");
        for (int i = 0; i < 5; i++)
            if ("1".equals(temp[i])) revealed[i] = true;
    }


    private boolean isCorrect(String submitted, String[] acceptables){
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
