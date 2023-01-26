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
import java.awt.Graphics;
import java.awt.Image;
import javax.imageio.ImageIO;

public class Renderer implements IFramebufferSizeListener {
    private final ShaderProgram shader2D = ShaderProgram.load("/shaders/2d.vsh", "/shaders/2d.fsh");
    private final Vector2f uiDimensions = new Vector2f(1, 1);
    private final SpriteBatch spriteBatch = new SpriteBatch();
    private final Texture mapBackground = new Texture("map_background.png");
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
        shader2D.setUniform("dimensions", uiDimensions);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        // Render the world
        if (world != null) {
            spriteBatch.setTexture(mapBackground);
            // TODO: Adjust so the aspect ratio is not distorted
            spriteBatch.blitScaled(0, 0, width, height, 0, 0, mapBackground.getWidth(), mapBackground.getHeight());
            spriteBatch.render();

            int hexWidth = this.hexWidth;
            int hexHeight = this.hexHeight;
            int spacing = this.spacing;
            double drawSizeFactor = this.drawSizeFactor;

            for (int y = 0; y < world.mapHeight; y++) {
                for (int x = 0; x < world.mapWidth; x++) {
                    Terrain t = world.getTerrain(x,y);
                    int locX = getPixelX(x,y,drawSizeFactor,spacing);
                    int locY = getPixelY(x,y,drawSizeFactor,spacing);
                    if (world.getTerrain(x,y) == Terrain.MOUNTAIN){
                        spriteBatch.setTexture(mountainTexture);
                        spriteBatch.blitScaled(locX, locY, hexWidth, hexHeight, 0, 0, mountainTexture.getWidth(), mountainTexture.getHeight());
                    }
                    else if (world.getTerrain(x,y) == Terrain.PLAIN){
                        spriteBatch.setTexture(plainTexture);
                        spriteBatch.blitScaled(locX, locY, hexWidth, hexHeight, 0, 0, plainTexture.getWidth(), plainTexture.getHeight());
                    }
                    else if (world.getTerrain(x,y) == Terrain.FOREST){
                        spriteBatch.setTexture(forestTexture);
                        spriteBatch.blitScaled(locX, locY, hexWidth, hexHeight, 0, 0, forestTexture.getWidth(), forestTexture.getHeight());
                    }
                    spriteBatch.render();
                }
            }
        }
        else {
            spriteBatch.setTexture(mainMenuBackground);
            spriteBatch.blitScaled(0, 0, width, height, 0, 0, mainMenuBackground.getWidth(), mainMenuBackground.getHeight());
            spriteBatch.render();
        }

        // Render UI
        overlay.render(spriteBatch, uiDimensions);
    }

    @Override
    public void windowResized(int width, int height) {
        // Ban 0 width or height
        width = Math.max(width, 1);
        height = Math.max(height, 1);

        this.width = width;
        this.height = height;

        // This is the size of UI's canvas, so the scale is inversly proportional to actual element size
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
