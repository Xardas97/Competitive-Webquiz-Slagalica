package games;

import entities.SidePlayerGameVariables;

public interface SidePlayerGame {
    Boolean isSidePlayer();
    void setSidePlayer(Boolean sidePlayer);
    boolean isCompleted();
    boolean playerFinished();
    void getReadyForSidePlayer();
    void updateVariables(SidePlayerGameVariables variables, boolean forBlue);
}
