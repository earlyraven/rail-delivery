package traingame;

import java.util.List;

public record RailSegment(Point origin, Direction direction) {
    private Point destination() {
        return origin.getTarget(direction);
    }

    public RailSegment opposite() {
        // the origin should be the destination of original and vice versa.
        return new RailSegment(destination(), this.direction.opposite());
    }

    @Override
    public String toString() {
        String separator = "---";
        String output = "<RailSegment: " + origin + "-->" + destination();
        String pointText = "";
        output += pointText + ">";
        return output;
    }
}
