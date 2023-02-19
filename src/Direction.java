package traingame;

import traingame.Point;

// Uses Axial coordinates as described here:
// https://www.redblobgames.com/grids/hexagons/#neighbors
public enum Direction {
    EAST(new Point(1, 0)),
    SOUTHEAST(new Point(0,1)),
    SOUTHWEST(new Point(-1,1));

    public final Point value;

    private Direction(Point value) {
        this.value = value;
    }
}
