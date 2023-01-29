package traingame;

import traingame.engine.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;
import java.io.FileNotFoundException;

public class World {
    // Map size in amount of hexagonal tiles in each dimension.
    public final int mapWidth = 31;
    public final int mapHeight = 34;



    //Retrieve file that stores world related info (Cities, Products, etc.)
    public String mapDataFile = "./assets/data/map-EasternUS.txt";
    private Terrain[][] map;

    public World() {
        System.out.println("START:");
        File file = new File(mapDataFile);

        try {
            Scanner scanner = new Scanner(new FileInputStream(file));
            while (scanner.hasNext()){
                String currentLine = scanner.nextLine();
                String[] currentLineSplit = currentLine.split(";");
                System.out.println(currentLine);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.err.println("The file was not found: " + e.getMessage());
        }



        List<String> cityNames = readIndexInfoOfMapDataFile(mapDataFile, 0);
        int cityCount = cityNames.size();
        List<String> exportNames = readIndexInfoOfMapDataFile(mapDataFile, 1);
        List<String> positionsString = readIndexInfoOfMapDataFile(mapDataFile, 2);
        List<Product> cityExports = new ArrayList<Product>();

        for (String export : exportNames){
            cityExports.add(Product.valueOf(export.replace(" ", "_").toUpperCase()));
        }

        List<Point[]> cityPoints = new ArrayList<Point[]>();
        for (String positionLine : positionsString){
            String[] pointValuesInLine = positionLine.replace(")","").replace("(","").split(",");
            List<String> coordinatesForPoints = new ArrayList<String>();
            for (String thePoint : pointValuesInLine){
                String singleNumber = thePoint.strip();
                coordinatesForPoints.add(singleNumber);
            }

            int amountOfNumbers = coordinatesForPoints.size();
            List<Point> pointList = new ArrayList<>();
            for (int i=0; i<coordinatesForPoints.size(); i+=2){
                int num1 = Integer.parseInt(coordinatesForPoints.get(i));
                int num2 = Integer.parseInt(coordinatesForPoints.get(i+1));
                Point thePoint = new Point(num1, num2);
                pointList.add(thePoint);
            }
            Point[] arrayOfPoints = pointList.toArray(new Point[pointList.size()]);
            cityPoints.add(arrayOfPoints);
        }

        Log.debug("");
        Log.debug("");
        List<City> cityAll = new ArrayList<>();
        Log.debug("CITY COUNT: " + cityCount);
        City[] cityArray = new City[cityCount];

        for (int i=0; i<cityCount; i++){
            cityArray[i] = new City(cityNames.get(i), cityExports.get(i), cityPoints.get(i));
            Log.debug(cityArray[i].getPrintable());
        }
        Log.debug("");
        Log.debug("");

        //As it is, cargo order count needs to equal city count.  For increased flexibility, it
        //should pick a random city.
        CargoOrder[] cargoOrderDeck = new CargoOrder[cityCount];
        for (int i=0; i<cargoOrderDeck.length; i++){
            int maxReward = 80;
            Product randomProduct = Product.valueOf(Product.getRandom().name());
            Log.debug(randomProduct.name());
            cargoOrderDeck[i] = new CargoOrder(randomProduct, cityArray[i], (int)(Math.random() * maxReward));
        }

        for (int i=0; i<cargoOrderDeck.length; i++){
            Log.debug(cargoOrderDeck[i].toString());
            Log.debug("");
        }

        Log.debug("");
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

    public List<String> readIndexInfoOfMapDataFile(String mapDataFile, int indexToRead) {
        List<String> output = new ArrayList<>();

        try (BufferedReader in = new BufferedReader(new FileReader(mapDataFile))) {
            String line;
            while ((line = in.readLine()) != null) {
                if (!line.contains("###")){
                    String[] theSplitLine = line.split(";");
                    output.add(theSplitLine[indexToRead].strip());
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return output;
    }

    public Terrain getTerrain(int x, int y) {
        return map[x][y];
    }

    public void update() {
        // TODO
    }
}
