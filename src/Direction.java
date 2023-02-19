package traingame;

import java.lang.IllegalArgumentException;
import traingame.Point;

// Uses Axial coordinates as described here:
// https://www.redblobgames.com/grids/hexagons/#neighbors
public enum Direction {
    EAST(new Point(1, 0)),
    SOUTHEAST(new Point(0,1)),
    SOUTHWEST(new Point(-1,1)),
    //These are the opposites.
    WEST(new Point(-1, 0)),
    NORTHWEST(new Point(0,-1)),
    NORTHEAST(new Point(1,-1));

    public final Point value;

    private Direction(Point value) {
        this.value = value;
    }

    public static Direction valueOf(Point point) {
        for (Direction direction : Direction.values()) {
            if (direction.value.equals(point)) {
                return direction;
            }
        }
        throw new IllegalArgumentException("Point has no matching direction.  Point: " + point);
    }

    public Direction opposite() {
        return Direction.valueOf(new Point(-value.q(), -value.r()));
    }
}
