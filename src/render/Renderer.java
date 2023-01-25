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
    private final double OUTER_RADIUS = 1;
    private double HEX_DISTORTION_RATIO_X_TO_Y = 1;

    //Adding Hexagon Terrain Support
    private final Texture mountainTexture = new Texture("terrain/mountain.png");
    private final Texture plainTexture = new Texture("terrain/plain.png");
    private final Texture forestTexture = new Texture("terrain/forest.png");


    public Renderer() {
        // Enable alpha blending (over)
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glClearColor(1f, 1f, 1f, 1f);
    }

    public int adjustQ(int q, int r) {
        // Adjusts the index of q so that it will be mapped in a rectangular shape instead
        // of that of a rhombus.
        return q - (r / (int)2);
    }

    public int getPixelX(int q, int r, double drawSizeFactor, double spacing) {
        return (int)Math.round(( drawSizeFactor * ( (adjustQ(q,r) * Q_BASIS_X) + (r * R_BASIS_X)) ) );
    }

    public int getPixelY(int q, int r, double drawSizeFactor, double spacing) {
        return (int)Math.round(( drawSizeFactor * ( (adjustQ(q,r) * Q_BASIS_Y) + (r * R_BASIS_Y)) ) );
    }

    //TODO: these will replace the above (or call the above)
    // public int getPixelXScaled(int q, int r, double drawSizeXFactor, double spacing) {
    //     return (int)Math.round(( drawSizeXFactor * ( (adjustQ(q,r) * Q_BASIS_X) + (r * R_BASIS_X)) ) );
    // }

    // public int getPixelYScaled(int q, int r, double drawSizeYFactor, double spacing) {
    //     return (int)Math.round(( drawSizeYFactor * ( (adjustQ(q,r) * Q_BASIS_Y) + (r * R_BASIS_Y)) ) );
    // }


    public void render(float lerp, World world, Overlay overlay) {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        // Set up for two-dimensional rendering
        shader2D.use();
        shader2D.setUniform("dimensions", uiDimensions);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        // Render the world
        if (world != null) {

            //To leave room on side for Contract Cards.
//            double fillFactorW = 0.8; //To fill as much of the screen uniformly as possible change to 1.
            double fillFactorW = 1; //To fill as much of the screen uniformly as possible change to 1.
            //TODO: When updating to spritesheet, fix remainder pixels issue.  Stretch the spritesheet.
            double fillFactorH = 1;
            int spacingPixels = width / 800;

            // Note:
            // width-to-height ratio of a regular hexagon (pointy top) is 1:1.1547005383792515290182975610039.
            // or more exactly:  1:sqrt(4/3)
            // for best results use ints that closely match this ratio.
//            int hexSizeMultiplierW = (int)(fillFactorW*Math.floor((this.width - world.mapWidth*spacingPixels) /(world.mapWidth + 0.5)));
////            int hexSizeMultiplierH = (int)(fillFactorH*Math.floor((this.height) /(world.mapHeight*R_BASIS_Y*(1/R_BASIS_X))));
//            int hexSizeMultiplierH = (int)(fillFactorH*Math.floor((this.height) / (0.75 * world.mapHeight * OUTER_RADIUS)));


            int hexWidth = (int)(fillFactorW*Math.floor((this.width - world.mapWidth*spacingPixels) /(world.mapWidth + 0.5)));
//            int hexSizeMultiplierH = (int)(fillFactorH*Math.floor((this.height) /(world.mapHeight*R_BASIS_Y*(1/R_BASIS_X))));
//            int hexHeight = (int)(fillFactorH*Math.floor((this.height) / (0.75 * world.mapHeight * OUTER_RADIUS)));
            int hexHeight = (int)(fillFactorH*Math.floor((this.height) /(world.mapHeight*R_BASIS_Y*(1/R_BASIS_X))));

            double scaleFactorXtoY = hexWidth / (double)hexHeight;
            System.out.println("SF: " + scaleFactorXtoY);
            hexHeight = (int)Math.round((hexHeight * (1/scaleFactorXtoY)));



//            System.out.println("(" + hexSizeMultiplierW + ", " + hexSizeMultiplierH + ")");
            System.out.println("(" + hexWidth + ", " + hexHeight + ")");

            //TODO: Use this to maintain aspect ratio.
    //        int hexSizeMultiplier = Math.max(hexSizeMultiplierW, hexSizeMultiplierH);
    //        Additional code needed.

    //        hexSizeMultiplierH not working properly, just using hexSizeMultiplierW for now.
//            hexSizeMultiplierH = hexSizeMultiplierW;

//            int hexWidth = (int)Math.round(hexSizeMultiplierW * 1);
//            int hexHeight = (int)Math.round(hexSizeMultiplierH * 1);

//            int hexHeight = (int)Math.round(hexSizeMultiplierH * Math.sqrt(4/3.));

            //POSSIBLE_BUG? This only updates when window width gets resized, but not when height does.
    //        System.out.println("(" + hexWidth + ", " + hexHeight + ")");

            int spacing = spacingPixels;
            double drawSizeFactor = (1 / Math.sqrt(3)) * (hexWidth + spacing);
//            double drawSizeXFactor = (1 / Math.sqrt(3)) * (hexWidth + spacing);
//            double drawSizeYFactor = (1 / Math.sqrt(3)) * (hexHeight);

            spriteBatch.setTexture(mapBackground);
            // TODO: Adjust so the aspect ratio is not distorted
            spriteBatch.blitScaled(0, 0, width, height, 0, 0, mapBackground.getWidth(), mapBackground.getHeight());
            spriteBatch.render();

            Terrain[][] map = world.map;

            for (int y = 0; y < world.mapHeight; y++) {
                for (int x = 0; x < world.mapWidth; x++) {
                    Terrain t = map[x][y];
                    int locX = getPixelX(x,y,drawSizeFactor,spacing);
                    int locY = getPixelY(x,y,drawSizeFactor,spacing);
//                    int locX = getPixelX(x,y,drawSizeXFactor,spacing);
//                    int locY = getPixelY(x,y,drawSizeYFactor,spacing);
                    if (map[x][y] == Terrain.MOUNTAIN){
                        spriteBatch.setTexture(mountainTexture);
                        spriteBatch.blitScaled(locX, locY, hexWidth, hexHeight, 0, 0, mountainTexture.getWidth(), mountainTexture.getHeight());
                    }
                    else if (map[x][y] == Terrain.PLAIN){
                        spriteBatch.setTexture(plainTexture);
                        spriteBatch.blitScaled(locX, locY, hexWidth, hexHeight, 0, 0, plainTexture.getWidth(), plainTexture.getHeight());
                    }
                    else if (map[x][y] == Terrain.FOREST){
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
