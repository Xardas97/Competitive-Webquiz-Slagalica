package entities;

public interface GameVariables {
    int getPointsBlue();
    void setPointsBlue(int pointsBlue);
    int getPointsRed();
    void setPointsRed(int pointsRed);
    default void fixPoints() {}
}
