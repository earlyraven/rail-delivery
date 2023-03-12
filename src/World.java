package traingame;

import traingame.engine.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import traingame.util.SetHelper;

public class World {
    // Map size in amount of hexagonal tiles in each dimension.
    public final int mapWidth = 102;
    public final int mapHeight = 89;

    private Random random = new Random();

    public final City[] cities;
    public final Company[] companies;
    private Terrain[][] map;

    private Point hoverHex = null;
    private int activeCompanyIndex = 0;

    public World(List<Company> companies) {
        Log.debug("Generating world with " + companies.size() + " companies.");

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
        Log.debug("\nCompanies (with updated trainQ and trainR)");
        for (Company c : companies) {
            int randomIndex = random.nextInt(candidateStartCities.size());
            City startingCity = candidateStartCities.get(randomIndex);
            Log.debug(startingCity.toString());
            candidateStartCities.remove(randomIndex);
            c.trainQ = startingCity.getSpawnPoint().q();
            c.trainR = startingCity.getSpawnPoint().r();

            Log.debug(c.toString());
            Log.debug("");
        }
        this.companies = companies.toArray(new Company[0]);

        //if all...red
        if (this.companies.length > 0) {
            this.companies[0].addRail(new RailSegment(new Point(23, 26), Direction.EAST));
            this.companies[0].addRail(new RailSegment(new Point(28, 36), Direction.EAST));
            this.companies[0].addRail(new RailSegment(new Point(23, 26), Direction.SOUTHEAST));
            this.companies[0].addRail(new RailSegment(new Point(29, 36), Direction.SOUTHEAST));
            this.companies[0].addRail(new RailSegment(new Point(30, 35), Direction.SOUTHWEST));
            this.companies[0].addRail(new RailSegment(new Point(31, 34), Direction.SOUTHWEST));
        }
        //if all...green
        if (this.companies.length > 1) {
            this.companies[1].addRail(new RailSegment(new Point(12, 6), Direction.EAST));
            this.companies[1].addRail(new RailSegment(new Point(18, 9), Direction.EAST));
        }
        //if all...blue.
        if (this.companies.length > 2) {
            this.companies[2].addRail(new RailSegment(new Point(35, 30), Direction.SOUTHWEST));
            this.companies[2].addRail(new RailSegment(new Point(36, 29), Direction.SOUTHWEST));
            this.companies[2].addRail(new RailSegment(new Point(37, 28), Direction.SOUTHWEST));
            this.companies[2].addRail(new RailSegment(new Point(38, 27), Direction.SOUTHWEST));
        }
        //if all...yellow.
        if (this.companies.length > 3) {
            //NOTE: If yellow is connected to Green's start city, then green will be considered to be connected
            // as well.
            this.companies[3].addRail(new RailSegment(new Point(2, 6), Direction.SOUTHWEST));
            this.companies[3].addRail(new RailSegment(new Point(9, 9), Direction.SOUTHWEST));

            this.companies[3].addRail(new RailSegment(new Point(32, 33), Direction.SOUTHWEST));
            this.companies[3].addRail(new RailSegment(new Point(33, 32), Direction.SOUTHWEST));
            this.companies[3].addRail(new RailSegment(new Point(34, 31), Direction.SOUTHWEST));
        }

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
                    List<Point> currentPointGroup = new ArrayList<>();

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
                            currentPointGroup.add(somePoint);
                        }
                    }
                    //Now use the data to make a city and add it to the City list.
                    Log.debug("");
                    Log.debug("Generating City:");
                    Log.debug("Name: " + currentCityName);
                    Log.debug("Export: " + currentCityExport);
                    Log.debug("Locations: " + currentPointGroup.toString());
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

    /**
     * Checks if active company can start building rails from the specified point.
     * @param point The point to start building rails from.
     * @return true if the active company can start building rails from the specified point, false otherwise.
     */
    public boolean canBuildFrom(Point point) {
        Company activeCompany = companies[activeCompanyIndex];
        return getSharedNetwork(activeCompany).contains(point);
    }

    private Set<Point> getSharedNetwork(Company company) {
        Set<Point> sharedNetwork = getDirectlyConnectedPoints(company);
        List<Company> needTesting = new ArrayList<>(Arrays.asList(companies));
        needTesting.remove(company); // Starts with all companies except current.

        List<Set<Point>> otherNetworkPointSets = new ArrayList<>();
        for (Company c : needTesting) {
            otherNetworkPointSets.add(getDirectlyConnectedPoints(c));
        }

        // Repetition is needed to ensure that order of connections does not matter.
        int otherNetworkCount = otherNetworkPointSets.size();
        for (int i=0; i<otherNetworkCount; i++) {
            for (Set<Point> otherNetworkPointSet : otherNetworkPointSets) {
                if (SetHelper.hasOverlap(otherNetworkPointSet, sharedNetwork)) {
                    // There is a connection, so add it to the network, and
                    // keep track if any change occured so we know if we can exit loop early.
                    boolean sharedNetworkChangeOccured = sharedNetwork.addAll(otherNetworkPointSet);
                    if(sharedNetworkChangeOccured) {
                        otherNetworkPointSets.remove(otherNetworkPointSet);
                        i = 0;
                        break;
                    }
                }
            }
        }
        return sharedNetwork;
    }

    private Set<Point> getDirectlyConnectedPoints(Company company) {
        Set<Point> connectedPoints = new HashSet<>();
        for (RailSegment railSegment : company.getRailNetwork()) {
            connectedPoints.addAll(railSegment.points());
        }

        // This gets just the starting cities but is no longer needed with the below code unless
        // unless cities aren't connected by rail (as is currently the case).  This section can be
        // removed once starting cities and rails are setup properly.
        Point startPoint = new Point(company.trainQ, company.trainR);
        connectedPoints.addAll(getLocationsFromPoint(startPoint));

        // Optimization: --> Probably restructure this to have company just keep track of the cities
        // that it is connected to (and reference that) rather than search through the whole city space.
        Set<Point> additionalPoints = new HashSet<>();
        for (City city : cities) {
            for (Point point : connectedPoints) {
                boolean cityFound = additionalPoints.addAll(city.getLocationsFromPoint(point));
                if (cityFound) {
                    break;
                }
            }
        }
        connectedPoints.addAll(additionalPoints);
        return connectedPoints;
    }

    private Set<Point> getLocationsFromPoint(Point point) {
        Set<Point> locations = new HashSet<>();
        for (City city : cities) {
            Set<Point> cityPoints = new HashSet<>(city.getLocationsFromPoint(point));
            locations.addAll(cityPoints);
        }
        return locations;
    }

    public void setHoverLocation(Point hover) {
        if (hover != null && (hover.x() < 0 || hover.x() >= mapWidth 
                              || hover.y() < 0 || hover.y() >= mapHeight)) {
            this.hoverHex = null;
            return;
        }
        this.hoverHex = hover;
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
