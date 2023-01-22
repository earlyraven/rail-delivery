package traingame;

import traingame.Terrain;


public class World {

    int mapWidth = 73;
    int mapHeight = 44;
    Terrain[][] map;

    public World() {
        // Probably put this part in the world constructor -- done...lets see.
        // This uses x, y but could instead use row, col if you transpose the array
//orig        map = new Terrain[][mapWidth];
//        map = new Terrain[][mapWidth];
        map = new Terrain[mapWidth][mapWidth];

        for (int x = 0; x < mapWidth; ++x) {
            map[x] = new Terrain[mapHeight];
            for (int y = 0; y < mapHeight; ++y) {
                // An actual program would initialize terrain in some more complicated way
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
//        return Terrain.Terrain.pl;//Not yet implemented.
    }

    public void update() {
        // TODO
    }
}
