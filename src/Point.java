package traingame;

public record Point(int q, int r) {
    public int getQ() {
        return q;
    }

    public int getR() {
        return r;
    }

    @Override
    public String toString() {
        return "(" + q + ", " + r + ")";
    }
}
