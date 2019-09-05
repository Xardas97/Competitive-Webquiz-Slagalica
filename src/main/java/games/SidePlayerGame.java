package games;

import entities.SidePlayerGameVariables;

public interface SidePlayerGame {
    Boolean isSidePlayer();
    void setSidePlayer(Boolean sidePlayer);
    boolean isCompleted();
    boolean playerFinished();
    void getReadyForSidePlayer();
    void loadVariables(SidePlayerGameVariables variables, boolean forBlue);
}
