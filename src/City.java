package traingame;

import java.util.List;

public record City(String name, Product export, Point[] locations) {
    public static City getRandom(List<City> cities){
        int randomIndex = (int) (Math.random() * cities.size());
        return cities.get(randomIndex);
    }

    public Point getSpawnPoint() {
        return locations[0];
    }

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
