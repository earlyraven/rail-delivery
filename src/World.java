package traingame;

import traingame.Terrain;
import traingame.render.Renderer;

public class World {

//TODO:REVERT back to more reasonable values after testing.
//    public static final int mapWidth = 73;
//    public static final int mapHeight = 54;
    public final int mapWidth = 50;
    public final int mapHeight = 36;

    //TODO:  (Tiff: public Terrain[][] map; That should be private or protected)
    // -- will need to change other things to accomodate that.
    public Terrain[][] map;

    public World() {
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

    public Terrain getTerrain(int q, int r) {
        // TODO: Conversion code according to 
        // https://www.redblobgames.com/grids/hexagons/#map-storage
        // (rectangular, array of arrays)
        return map[q][r];
    }

    public void update() {
        // TODO
    }
}
