package entities;

public interface SidePlayerGameVariables extends GameVariables {
    boolean isSidePlayerDone();
    void setSidePlayerDone(boolean sidePlayerDone);
    void setBluePlaying(boolean bluePlaying);
    boolean isBluePlaying();
}
