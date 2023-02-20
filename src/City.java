package traingame;

import java.util.ArrayList;
import java.util.List;

public record City(String name, Product export, List<Point> locations) {
    public static City getRandom(List<City> cities){
        int randomIndex = (int) (Math.random() * cities.size());
        return cities.get(randomIndex);
    }

    public Point getSpawnPoint() {
        return locations.get(0);
    }

    public List<Point> getLocationsFromPoint(Point point) {
        if (locations.contains(point)) {
            return locations;
        }
        List<Point> emptyList = new ArrayList<>();
        return emptyList;
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
