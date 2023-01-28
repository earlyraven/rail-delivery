package traingame.render;

import java.awt.Font;
import java.io.IOException;
import java.util.Scanner;

import org.lwjgl.opengl.GL11;

import traingame.engine.render.*;
import traingame.engine.render.SpriteBatch;
import traingame.engine.render.Texture;
import traingame.World;
import shadowfox.math.*;
import java.lang.Math.*;
import traingame.Terrain;

public class Renderer implements IFramebufferSizeListener {
    private final ShaderProgram shader2D = ShaderProgram.load("/shaders/2d.vsh", "/shaders/2d.fsh");
    private final Vector2f uiDimensions = new Vector2f(1, 1);
    private final SpriteBatch spriteBatch = new SpriteBatch();
    private final Texture mapBackground = new Texture("map_background.png");
    private final int MAP_PIXELS_WIDE = mapBackground.getWidth();
    private final int MAP_PIXELS_HIGH = mapBackground.getHeight();
    private final Texture mainMenuBackground = new Texture("main_menu_background.png");
    private int width;
    private int height;
    private final double Q_BASIS_X = Math.sqrt(3);
    private final double Q_BASIS_Y = 0;
    private final double R_BASIS_X = Math.sqrt(3)/2;
    private final double R_BASIS_Y = 3./2;

    //Adding Hexagon Terrain Support
    private final Texture mountainTexture = new Texture("terrain/mountain.png");
    private final Texture plainTexture = new Texture("terrain/plain.png");
    private final Texture forestTexture = new Texture("terrain/forest.png");

    // Note:
    // width-to-height ratio of a regular hexagon (pointy top) is 1:1.1547005383792515290182975610039.
    // or more exactly:  1:sqrt(4/3)
    // for best results use ints that closely match this ratio.
    //(This ends up as a 24 and 28 pair) (40 and 46 would be even closer, though might be too big)
    private int hexWidth = 24;
    private int hexHeight = (int)Math.round(hexWidth * Math.sqrt(4/3.));

    //or change to 0 to remove spacing, although that looks ugly.
    //Alternately, perhaps images used should just have a few blank pixels along edges.
    private int spacing = (int)Math.round((1*Math.min(hexWidth, hexHeight) / 10.));

    private double drawSizeFactor = (1 / Q_BASIS_X) * (hexWidth + spacing);


    public Renderer() {
        // Enable alpha blending (over)
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glClearColor(1f, 1f, 1f, 1f);
    }

    // q is used to find the pixel coordinates for rectangular mapping of x and y using the below formulas/functions.
    public int getQ(int x, int y) {
        return x - (y / 2);
    }    
    //r is simply the same as y.

    public int getPixelX(int x, int y, double drawSizeFactor, double spacing) {
        return (int)Math.round(( drawSizeFactor * ( (getQ(x,y) * Q_BASIS_X) + (y * R_BASIS_X)) ) );
    }

    public int getPixelY(int x, int y, double drawSizeFactor, double spacing) {
        return (int)Math.round(( drawSizeFactor * ( (getQ(x,y) * Q_BASIS_Y) + (y * R_BASIS_Y)) ) );
    }

    public String getPrintableHexSize() {
        return "(" + hexWidth + ", " + hexHeight + ")";
    }

    public void render(float lerp, World world, Overlay overlay) {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        // Set up for two-dimensional rendering
        shader2D.use();
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        // Render the world
        if (world != null) {
            int reservedLeft = 0;
            int reservedBottom = 0;
            // Temporary test code to see what moving the UI location would look like
            if ((double)MAP_PIXELS_WIDE / MAP_PIXELS_HIGH < (double)width / height) {
                reservedLeft = 200;
            }
            else {
                reservedBottom = 200;
            }
            renderWorld(lerp, world, reservedLeft, reservedBottom);
        }
        else {
            spriteBatch.setTexture(mainMenuBackground);
            spriteBatch.blitScaled(0, 0, width, height, 0, 0, mainMenuBackground.getWidth(), mainMenuBackground.getHeight());
            spriteBatch.render();
        }

        // Render UI
        shader2D.setUniform("dimensions", uiDimensions);
        shader2D.setFloat("left_margin", 0);
        shader2D.setFloat("right_margin", 0);
        shader2D.setFloat("top_margin", 0);
        shader2D.setFloat("bottom_margin", 0);
        overlay.render(spriteBatch, uiDimensions);
    }

    private void renderWorld(float lerp, World world, int reservedLeft, int reservedBottom) {
        int mapScreenWidth = width - reservedLeft;
        int mapScreenHeight = height - reservedBottom;
        int widthDiff = 0;
        int heightDiff = 0;
        double aspectRatio = (double)MAP_PIXELS_WIDE / MAP_PIXELS_HIGH;
        if (aspectRatio > (double)mapScreenWidth / mapScreenHeight) {
            heightDiff = mapScreenHeight - (int)(mapScreenWidth / aspectRatio);
        }
        else {
            widthDiff = mapScreenWidth - (int)(aspectRatio * mapScreenHeight);
        }

        shader2D.setUniform("dimensions", new Vector2f(MAP_PIXELS_WIDE, MAP_PIXELS_HIGH));
        shader2D.setFloat("left_margin", (reservedLeft + widthDiff / 2) / uiDimensions.x);
        shader2D.setFloat("right_margin", (widthDiff - (widthDiff / 2)) / uiDimensions.x);
        shader2D.setFloat("top_margin", heightDiff / 2 / uiDimensions.y);
        shader2D.setFloat("bottom_margin", (heightDiff - heightDiff / 2 + reservedBottom) / uiDimensions.y);


        spriteBatch.setTexture(mapBackground);
        spriteBatch.blit(0, 0, MAP_PIXELS_WIDE, MAP_PIXELS_HIGH, mapBackground.getWidth(), mapBackground.getHeight());
        spriteBatch.render();

        for (int y = 0; y < world.mapHeight; y++) {
            for (int x = 0; x < world.mapWidth; x++) {
                Terrain terrain = world.getTerrain(x,y);
                int locX = getPixelX(x,y,drawSizeFactor,spacing);
                int locY = getPixelY(x,y,drawSizeFactor,spacing);

                if (terrain == Terrain.MOUNTAIN){
                    spriteBatch.setTexture(mountainTexture);
                    spriteBatch.blitScaled(locX, locY, hexWidth, hexHeight, 0, 0, mountainTexture.getWidth(), mountainTexture.getHeight());
                }
                else if (terrain == Terrain.PLAIN){
                    spriteBatch.setTexture(plainTexture);
                    spriteBatch.blitScaled(locX, locY, hexWidth, hexHeight, 0, 0, plainTexture.getWidth(), plainTexture.getHeight());
                }
                else if (terrain == Terrain.FOREST){
                    spriteBatch.setTexture(forestTexture);
                    spriteBatch.blitScaled(locX, locY, hexWidth, hexHeight, 0, 0, forestTexture.getWidth(), forestTexture.getHeight());
                }
                spriteBatch.render();
            }
        }
    }

    @Override
    public void windowResized(int width, int height) {
        // Ban 0 width or height
        width = Math.max(width, 1);
        height = Math.max(height, 1);

        this.width = width;
        this.height = height;

        // This is the size of UI's canvas, so the scale is inversely proportional to actual element size
        float uiScale = (float)Overlay.getScale();
        uiDimensions.x = width * uiScale;
        uiDimensions.y = height * uiScale;
    }

    public void enterWireframe() {
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
    }

    public void exitWireframe() {
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
    }

    public void clean() {
        shader2D.delete();
    }
}
