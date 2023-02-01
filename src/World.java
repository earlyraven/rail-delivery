package traingame;

import traingame.engine.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class World {
    // Map size in amount of hexagonal tiles in each dimension.
    public final int mapWidth = 31;
    public final int mapHeight = 34;

    private final City[] cities;

    //Retrieve file that stores world related info (Cities, Products, etc.)
    private Terrain[][] map;

    public World() {

        cities = readCitiesFromFile("/assets/data/map-EasternUS.txt").toArray(new City[0]);

        runTests(); //Once actual display implementation is achieved, this can be removed.


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

    private List<City> readCitiesFromFile(String filePath) {
        List<City> theCitiesOnMap = new ArrayList<>();
        try (Scanner scanner = new Scanner(World.class.getResourceAsStream(filePath))) {

            while (scanner.hasNextLine()) {
                String currentLine = scanner.nextLine();
                String commentMarker = "###";
                if(!currentLine.startsWith(commentMarker)) {
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
                        }
                    }
                    Point[] currentPointGroup = localPointGroup.toArray(new Point[0]);

                    //Now use the data to make a city and add it to the City list.
                    Log.debug("");
                    Log.debug("Generating City:");
                    Log.debug("Name: " + currentCityName);
                    Log.debug("Export: " + currentCityExport);
                    Log.debug("Locations: " + Arrays.toString(currentPointGroup));
                    City currentCity = new City(currentCityName, currentCityExport, currentPointGroup);
                    theCitiesOnMap.add(currentCity);
                }
            }
        } catch (Exception e) {
            Log.debug("Error reading cities from file: " + e.getMessage());
        }
        return theCitiesOnMap;
    }

    public Terrain getTerrain(int x, int y) {
        return map[x][y];
    }

    public void runTests() {
        Log.debug("");
        Log.debug("Condensed city info:");
        for (City c : cities) {
            Log.debug(c.toString());
        }

        Log.debug("");
        Log.debug("Test Cargo Order Generation:");
        for (int i=0; i<15; i++) {
            Log.debug(CargoOrder.getRandom(cities).toString());
        }
    }

    public void update() {
        // TODO
    }
}
