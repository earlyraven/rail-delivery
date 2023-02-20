package traingame;

import traingame.engine.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

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

    private void changeActivePlayer() {
        activeCompanyIndex++;
        int playerCount = companies.length;
        if (activeCompanyIndex > playerCount) {
            activeCompanyIndex -= playerCount;
        }
    }

    private Set<Point> getSharedNetwork(Company company) {
        Set<Point> sharedNetwork = getDirectlyConnectedPoints(company);
        List<Company> needTesting = new ArrayList<>(Arrays.asList(companies));
        needTesting.remove(company);

        boolean detectedOverlap = true;
        while (detectedOverlap) {
            detectedOverlap = false;
            for (Company otherCompany : needTesting) {
                Set<Point> otherNetwork = getDirectlyConnectedPoints(otherCompany);
                for (Point point : otherNetwork) {
                    if (sharedNetwork.contains(point)) {
                        sharedNetwork.addAll(otherNetwork);
                        needTesting.remove(otherCompany);
                        detectedOverlap = true;
                        break;
                    }
                }
                if (detectedOverlap) {
                    break;
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

        // this gets just the starting cities but is no longer needed with the below code unless
        // unless cities aren't connected by rail.
        Point startPoint = new Point(company.trainQ, company.trainR);
        connectedPoints.addAll(getLocationsFromPoint(startPoint));

        // Can likely be made more efficient.
        // get all the tiles within connected cities to add them.
        // then add these to connectedPoints.
        // should work...but inefficient...probably good enough though.

        // TODO - test/verify if this still works for non-directly connected cities.
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
