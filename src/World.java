package traingame;

import traingame.engine.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class World {
    // Map size in amount of hexagonal tiles in each dimension.
    public final int mapWidth = 31;
    public final int mapHeight = 34;

    //Retrieve file that stores world related info (Cities, Products, etc.)
    public String mapDataFile = "./assets/data/map-EasternUS.txt";
    private Terrain[][] map;

    public World() {
        List<City> theCitiesOnMap = new ArrayList<>();
        File file = new File(mapDataFile);
        Scanner scanner;

        try {
            scanner = new Scanner(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            System.err.println("The file was not found: " + e.getMessage());
            return;
        }
        while (scanner.hasNext()) {
            String currentLine = scanner.nextLine();
            String commentMarker = "###";
            if(!currentLine.contains(commentMarker)){
                String[] currentLineSplit = currentLine.split(";");
                int entriesInLine = currentLineSplit.length;

                //Prepare data to create Cities:
                String currentCityName = "";
                Product currentCityExport = null;
                List<Point> localPointGroup = new ArrayList<>();

                for (int i=0; i<entriesInLine; i++) {
                    if(i==0) {
                        currentCityName = currentLineSplit[i];
                    }
                    else if(i==1) {
                        currentCityExport = Product.valueOf(currentLineSplit[i]);
                    }
                    else {
                        String[] partitionedCoordinateString = currentLineSplit[i].split(",");
                        int[] pointCoordinates = new int[partitionedCoordinateString.length];
                        for (int j=0; j<pointCoordinates.length; j++){
                            pointCoordinates[j] = Integer.parseInt(partitionedCoordinateString[j]);
                        }
                        Point somePoint = new Point(pointCoordinates[0], pointCoordinates[1]);
                        localPointGroup.add(somePoint);
                        Point[] locArray = localPointGroup.toArray(new Point[0]);
                    }
                }
                Point[] currentPointGroup = localPointGroup.toArray(new Point[0]);

                //Now use the data to make a city and add it to the City list.
                Log.debug("");
                Log.debug("Generating City:");
                City currentCity = new City(currentCityName, currentCityExport, currentPointGroup);
                Log.debug(currentCity.name().toString());
                Log.debug(currentCity.export().toString());
                Log.debug(currentCity.locations().toString());
                for (Point loc : currentCity.locations()) {
                    Log.debug(loc.toString());
                }
                theCitiesOnMap.add(currentCity);
            }
        }
        scanner.close();
        Log.debug("");
        Log.debug("Condensed city info:");
        for (City c : theCitiesOnMap) {
            System.out.println(c.toString());
        }
        Log.debug("");

        map = new Terrain[mapWidth][mapHeight];
        //TODO: read values from a text file or make a better random map alogrithm.
        for (int x = 0; x < mapWidth; ++x) {
            map[x] = new Terrain[mapHeight];
            for (int y = 0; y < mapHeight; ++y) {
                // An actual program would initialize terrain in some more complicated way
                // This will just alternate between avaiable terrains.
                int ordinal = (x + y) % Terrain.values().length;
                map[x][y] = Terrain.values()[ordinal];
            }
        }
    }

    public Terrain getTerrain(int x, int y) {
        return map[x][y];
    }

    public void update() {
        // TODO
    }
}
