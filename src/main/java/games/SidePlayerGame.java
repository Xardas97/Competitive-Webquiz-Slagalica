package games;

import entities.SidePlayerGameVariables;
import util.Transaction;

public interface SidePlayerGame extends Game {
    Boolean isSidePlayer();
    void setSidePlayer(Boolean sidePlayer);
    boolean isCompleted();
    boolean playerFinished();
    void getReadyForSidePlayer();
    void loadVariables(SidePlayerGameVariables variables, boolean forBlue);
    SidePlayerGameVariables getMyVars(Transaction t, String username);
}
