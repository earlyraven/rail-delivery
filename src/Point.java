package traingame;

public record Point(int q, int r) {
    public static Point fromXY(int x, int y) {
        return new Point(x - y/2, y);
    }

    public int x() {
        return q + (r / 2);
    }

    public int y() {
        return r;
    }

    @Override
    public String toString() {
        return "(" + q + ", " + r + ")";
    }
}
