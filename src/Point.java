package traingame;

public record Point(int q, int r) {
	@Override
	public String toString() {
		return "(" + q + ", " + r + ")";
	}
}
