package traingame;

public record City(String name, Product export, Point[] locations) {
    @Override
    public String toString() {
        String separator = "---";
        String output = "<City: " + name + separator + export + separator;
        String pointText = "";
        for (Point p : locations) {
            pointText += p.toString();
        }
        output += pointText + ">";
        return output;
    }
}
