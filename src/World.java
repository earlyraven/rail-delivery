package traingame;

import traingame.engine.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class World {
    // Map size in amount of hexagonal tiles in each dimension.
    public final int mapWidth = 102;
    public final int mapHeight = 89;

    private Random random = new Random();

    public final City[] cities;
    private Company[] companies;
    private Terrain[][] map;

    private Point hoverHex = null;

    public World(List<Company> companies) {
        Log.debug("Generating world with " + companies.size() + " companies.");
        this.companies = new Company[companies.size()];

        cities = readCitiesFromFile("/assets/data/map-EasternUS.txt").toArray(new City[0]);
        int availableCities = cities.length;
        assert(companies.size() <= Game.MAX_PLAYERS);
        if (Game.MAX_PLAYERS > availableCities) {
            String message = "Insufficient cities to allocate a city to each company. "
            + "Max players: " + Game.MAX_PLAYERS + " , starting cities: " + availableCities;
            throw new RuntimeException(message);
        }

        ArrayList<City> candidateStartCities = new ArrayList<>(Arrays.asList(cities));

        // Finish initializing Companies.
        List<Company> theCompanies = new ArrayList<>();
        Log.debug("\nCompanies (with updated trainQ and trainR)");

// The next 3 comments at this indent level are test code to ensure assert works.
// uncomment them to test.
        // Starting rails lists
        // TODO:  Probably eventually load these from a file (or with an algorithm) instead.
        List<RailSegment> redStartRails = new ArrayList<>();
        redStartRails.add(new RailSegment(new Point(6, 6), Direction.EAST));
        redStartRails.add(new RailSegment(new Point(6, 6), Direction.WEST));
        // testing rejection of same, but opposite directioned rail.
//        redStartRails.add(new RailSegment(new Point(5, 6), Direction.EAST));

        List<RailSegment> greenStartRails = new ArrayList<>();
        greenStartRails.add(new RailSegment(new Point(12, 12), Direction.EAST));
        greenStartRails.add(new RailSegment(new Point(12, 12), Direction.WEST));

        List<RailSegment> blueStartRails = new ArrayList<>();
        blueStartRails.add(new RailSegment(new Point(16, 16), Direction.EAST));
        blueStartRails.add(new RailSegment(new Point(16, 16), Direction.WEST));
        // testing rejection of a previously entered rail (by other company)
//        blueStartRails.add(new RailSegment(new Point(6, 6), Direction.WEST));

        List<RailSegment> yellowStartRails = new ArrayList<>();
        yellowStartRails.add(new RailSegment(new Point(24, 26), Direction.EAST));
        yellowStartRails.add(new RailSegment(new Point(24, 26), Direction.WEST));
        // testing rejection of a duplicate.
//        yellowStartRails.add(new RailSegment(new Point(24, 26), Direction.WEST));

        for (Company c : companies) {
            Log.debug("Company:  " + c.name);
            int randomIndex = random.nextInt(candidateStartCities.size());
            City startingCity = candidateStartCities.get(randomIndex);
            Log.debug(startingCity.toString());
            candidateStartCities.remove(randomIndex);
            c.trainQ = startingCity.getSpawnPoint().q();
            c.trainR = startingCity.getSpawnPoint().r();

            Log.debug(c.toString());
            Log.debug("");
            theCompanies.add(c);

            // Better controlled rail addition
            if (c.name == "Red Company") {
                addRailNetwork(redStartRails, companies, c);
            }
            else if (c.name == "Green Company") {
                addRailNetwork(greenStartRails, companies, c);
            }
            else if (c.name == "Blue Company") {
                addRailNetwork(blueStartRails, companies, c);
            }
            else if (c.name == "Yellow Company") {
                addRailNetwork(yellowStartRails, companies, c);
            }
        }

        this.companies = theCompanies.toArray(new Company[theCompanies.size()]);
        Log.debug(theCompanies.toString());

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

    public Terrain getTerrainXY(int x, int y) {
        return map[x][y];
    }

    public Point getHoverLocation() {
        return hoverHex;
    }

    public void setHoverLocation(Point hover) {
        if (hover != null && (hover.x() < 0 || hover.x() >= mapWidth 
                              || hover.y() < 0 || hover.y() >= mapHeight)) {
            this.hoverHex = null;
            return;
        }
        this.hoverHex = hover;
    }

    // IF_POSSIBLE this should not require List<Company>.
    // current test code would lead to null error though (as world isn't created yet).
    private boolean addRail(RailSegment railSegment, List<Company> companies, Company company) {
        // TODO - further limit this to only ones adjacent/connected to network.
        for (Company com : companies) {
            if (com.hasRail(railSegment)) {
                Log.debug("DISALLOWED:  Company: " + company.name + " failed to add RailSegment: " + railSegment);
                return false;
            }
        }
        company.addRail(railSegment);
        return true;
    }

    private void addRailNetwork(List<RailSegment> railNetwork, List<Company> companies, Company company) {
        for (Company c : companies) {
            for (RailSegment railSegment : railNetwork) {
                // WARNING code duplication (from Company.java) w/ only slight modification.
                String errorMessage = "DUPLICATE: Rail is present:  Company: " + company.name + ", RailSegment: " + railSegment;
                assert !c.railExists(railSegment, companies) : errorMessage;
            }
        }
        company.addRailNetwork(railNetwork);
    }

    private boolean hasRail(RailSegment railSegment) {
        for (Company c : companies) {
            if (c.hasRail(railSegment)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsRail(List<RailSegment> railNetwork) {
        for (RailSegment railSegment : railNetwork) {
            if (hasRail(railSegment)) {
                return true;
            }
        }
        return false;
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
}
