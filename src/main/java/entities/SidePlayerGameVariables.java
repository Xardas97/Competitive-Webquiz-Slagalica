package entities;

import games.SidePlayerGame;

public interface SidePlayerGameVariables extends GameVariables {
    boolean isSidePlayerDone();
    void setSidePlayerDone(boolean sidePlayerDone);
    void setBluePlaying(boolean bluePlaying);
    boolean isBluePlaying();
    void updateVariables(SidePlayerGame game, boolean forBlue);
}
