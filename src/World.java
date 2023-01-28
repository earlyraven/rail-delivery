package traingame;

import traingame.Terrain;
import traingame.render.Renderer;

import traingame.Product;
import traingame.City;
import traingame.CargoOrder;

import traingame.engine.Log;
import java.io.*;

public class World {
    // Map size in amount of hexagonal tiles in each dimension.
    public final int mapWidth = 31;
    public final int mapHeight = 34;

    //World related info (Cities, Products, etc.)

    //TODO: This will come from a csv file or other text file.
    String[] cityNames = {
        "New York, NY",
        "Chicago, IL",
        "Philadelphia, PA",
        "Detroit, MI"
    };

    int cityCount = cityNames.length;

    //TODO: This will come from a csv file or other text file.
    Point[][] pointsForCities = {
        {new Point(5,6), new Point(5,7), new Point(6,6)},
        {new Point(12,15)},
        {new Point(2,9)},
        {new Point(9,9), new Point(9,10)}
    };

    //TODO: This will come from a csv file or other text file.
    Product[][] productsProducedByCities = {
        {Product.IRON, Product.STEEL},
        {Product.CORN, Product.STEEL},
        {Product.IRON, Product.FISH},
        {Product.SOYBEAN_OIL, Product.CORN}
    };

    //TODO: Make a text file from which to retrieve and initialize the city and product info for the above.

    private Terrain[][] map;

    public World() {

        Log.debug("City count: " + String.valueOf(cityCount));
        Log.debug("TA: " + String.valueOf(cityCount));
        Log.debug("TB: " + String.valueOf(pointsForCities.length));
        Log.debug("TC: " + String.valueOf(productsProducedByCities.length));
        //TODO: FIX: This conditional restraint isn't written properly and causes compile error.
//        Log.debug("COMPARE TA and TB" + String.equals(String.valueOf(cityCount),String.pointsForCities.length).toString());
//        if (!((cityCount == pointsForCities.length) && (cityCount == productsProducedByCities.length))){
//            throw new Exception("Generic this shouldn't happen.  Please make sure array lengths for city names, points for cities, and products produced by cities are all equal.");
//        }
        Log.debug("");
        Log.debug("");


        City[] citiesOnMap = new City[cityCount];
        for (int i=0; i<cityCount; i++){
            citiesOnMap[i] = new City(cityNames[i], productsProducedByCities[i], pointsForCities[i]);
        }

        int chosenCity = 3;

        int productsInChosenCity = citiesOnMap[chosenCity].products().length;
        Log.debug(productsInChosenCity + " products in city.");
        Log.debug(citiesOnMap[chosenCity].name() + " produces...");
        for (int i=0; i<productsInChosenCity; i++){
            Log.debug(citiesOnMap[chosenCity].products()[i].getName() + " - " + citiesOnMap[chosenCity].products()[i].readableName());
        }

        int pointsInChosenCity = citiesOnMap[chosenCity].locations().length;
        Log.debug(citiesOnMap[chosenCity].name() + " is located at...");
        for (int i=0; i<pointsInChosenCity; i++){
            Point point = citiesOnMap[chosenCity].locations()[i]; 
            Log.debug("Location:  (" + point.qx() + ", " + point.ry() + ")");
        }


        Log.debug("");
        Log.debug("");
        for (City city : citiesOnMap) {
            Log.debug(city.name());
            String productString = "";
            for (Product product : city.products()){
                productString += product;
            }
            Log.debug(productString);
        }

        // This uses x, y but could instead use row, col if you transpose the array
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
