package traingame;

import traingame.Terrain;
import traingame.render.Renderer;

public class World {

//TODO:REVERT back to more reasonable values after testing.
//    public final int mapWidth = 73;
//    public final int mapHeight = 44;
    public final int mapWidth = 50;
    public final int mapHeight = 36;
    public static final double Q_BASIS_X = Math.sqrt(3);
    public static final double Q_BASIS_Y = 0;
    public static final double R_BASIS_X = Math.sqrt(3)/2;
    public static final double R_BASIS_Y = 3./2;

    //TODO:  May need to adjust this as window resizes.
    public static final double SPACING = Renderer.HEX_WIDTH/(int)18;
    public double drawSizeFactor = (1 / Math.sqrt(3)) * (Renderer.HEX_WIDTH + SPACING);

    public Terrain[][] map;

    public World() {
        // This uses x, y but could instead use row, col if you transpose the array
        map = new Terrain[mapWidth][mapWidth];

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

    public int adjustQ(int q, int r) {
        // Adjusts the index of q so that it will be mapped in a rectangular shape instead
        // of that of a rhombus.
        return q - (r / (int)2);
    }
    public int getPixelX(int q, int r) {
        return (int)Math.round(( drawSizeFactor * ( (adjustQ(q,r) * Q_BASIS_X) + (r * R_BASIS_X)) ) );
    }

    public int getPixelY(int q, int r) {
        return (int)Math.round(( drawSizeFactor * ( (adjustQ(q,r) * Q_BASIS_Y) + (r * R_BASIS_Y)) ) );
    }

    //See above methods.
    public Terrain getTerrain(int q, int r) {
        // TODO: Conversion code according to 
        // https://www.redblobgames.com/grids/hexagons/#map-storage
        // (rectangular, array of arrays)
        return map[q][r];
    }

    public Terrain[][] getMap() {
        return this.map;
    }

    public void update() {
        // TODO
    }
}
