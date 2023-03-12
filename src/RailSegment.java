package traingame;

import java.util.Arrays;
import java.util.List;

public record RailSegment(Point origin, Direction direction) {
    private Point destination() {
        int dest_q = origin.q() + direction.value.q();
        int dest_r = origin.r() + direction.value.r();
        return new Point(dest_q, dest_r);
    }

    public List<Point> points() {
        return Arrays.asList(origin, destination());
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
