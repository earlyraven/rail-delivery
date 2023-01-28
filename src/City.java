package traingame;

public record City(String name, Product export, Point[] locations){
	public String getPrintable(){
		String separator = "---";
		String output = name + separator + export.label + separator;
		for (Point p : locations){
			String orderedPairPoint = "(" + p.q() + ", " + p.r() + ")";
			output += orderedPairPoint;
		}
		return output;
	}
}
