package traingame;

import traingame.Terrain;
import traingame.render.Renderer;

public class World {

//TODO:REVERT back to more reasonable values after testing.
//    public final int mapWidth = 73;
//    public final int mapHeight = 44;
    public final int mapWidth = 10;
    public final int mapHeight = 7;
    public static final double Q_BASIS_X = Math.sqrt(3);
    public static final double Q_BASIS_Y = 0;
    public static final double R_BASIS_X = Math.sqrt(3)/2;
    public static final double R_BASIS_Y = 3./2;

    //TODO:  May need to adjust this as window resizes.
    public int drawSizeFactor = 50;

    public Terrain[][] map;

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
    //TODO NOT YET DONE>
    public int getPixelX(int q, int r) {
        return (int)( drawSizeFactor * ( (q * Q_BASIS_X) + (r * R_BASIS_X)) );
    }

    public int getPixelY(int q, int r) {
        return (int)( drawSizeFactor * ( (q * Q_BASIS_Y) + (r * R_BASIS_Y)) );
    }

    public Terrain getTerrain(int q, int r) {
        // TODO: Conversion code according to 
        // https://www.redblobgames.com/grids/hexagons/#map-storage
        // (rectangular, array of arrays)
        System.out.println(map[q][r]);
        return map[q][r];
//        return Terrain.Terrain.pl;//Not yet implemented.
    }
    public Terrain[][] getMap() {
        return this.map;
    }

    public int getMapWidth() {
        return mapWidth;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public void update() {
        // TODO
    }
}
