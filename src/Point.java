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

    public Point getTarget(Direction direction) {
        return combineWithPoint(direction.value);
    }

    private Point combineWithPoint(Point point) {
        return new Point(this.q() + point.q(), this.r() + point.r());
    }

    @Override
    public String toString() {
        return "(" + q + ", " + r + ")";
    }
}
