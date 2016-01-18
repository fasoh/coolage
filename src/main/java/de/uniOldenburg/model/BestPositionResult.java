package de.uniOldenburg.model;

/**
 * Created by adrian-jagusch on 18.01.16.
 */
public class BestPositionResult {
    public final int bestX;
    public final int bestY;
    public final double bestQuality;

    public BestPositionResult(int bestX, int bestY, double bestQuality) {
        this.bestX = bestX;
        this.bestY = bestY;
        this.bestQuality = bestQuality;
    }
}
