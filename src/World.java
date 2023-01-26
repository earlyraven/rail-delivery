package traingame;

import traingame.Terrain;
import traingame.render.Renderer;

public class World {

    //This (73 by 46) fills a 1080p monitor we may wish to adjust size to accomodate mission cards. 
    public final int mapWidth = 73;
    public final int mapHeight = 46;

    private Terrain[][] map;

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

    public Terrain getTerrain(int x, int y) {
        // TODO: Conversion code according to -- I think this is done.  We can remove this comment now, right?
        // https://www.redblobgames.com/grids/hexagons/#map-storage
        // (rectangular, array of arrays)
        return map[x][y];
    }

    public void update() {
        // TODO
    }
}
