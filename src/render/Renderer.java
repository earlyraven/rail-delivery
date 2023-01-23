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

    //Adding Hexagon Terrain Support
    private final Texture mountainTexture = new Texture("terrain/mountain.png");
    private final Texture plainTexture = new Texture("terrain/plain.png");


    public Renderer() {
        // Enable alpha blending (over)
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glClearColor(1f, 1f, 1f, 1f);
    }

    public void render(float lerp, World world, Overlay overlay) {
        //To leave room on side for Contract Cards.
        double fillFactorW = 0.8; //To fill as much of the screen uniformly as possible change to 1.
        //TODO: When updating to spritesheet, fix remainder pixels issue.  Stretch the spritesheet.
        double fillFactorH = 1;
        int spacingPixels = width / 800;

        // Note:
        // width-to-height ratio of a regular hexagon (pointy top) is 1:1.1547005383792515290182975610039.
        // or more exactly:  1:sqrt(4/3)
        // for best results use ints that closely match this ratio.
        int hexSizeMultiplierW = (int)(fillFactorW*Math.floor((this.width - World.mapWidth*spacingPixels) /(World.mapWidth + 0.5)));
        int hexSizeMultiplierH = (int)(fillFactorH*Math.floor((this.height) /(World.mapHeight*World.R_BASIS_Y*(1/World.R_BASIS_X))));

        //TODO: Use this to maintain aspect ratio.
//        int hexSizeMultiplier = Math.max(hexSizeMultiplierW, hexSizeMultiplierH);
//  Additional code needed.

//        hexSizeMultiplierH not working properly, just using hexSizeMultiplierW for now.
        hexSizeMultiplierH = hexSizeMultiplierW;

        int hexWidth = (int)Math.round(hexSizeMultiplierW * 1);
        int hexHeight = (int)Math.round(hexSizeMultiplierH * Math.sqrt(4/3.));

        //POSSIBLE_BUG? This only updates when window width gets resized, but not when height does.
//        System.out.println("(" + hexWidth + ", " + hexHeight + ")");

        int spacing = spacingPixels;
        double drawSizeFactor = (1 / Math.sqrt(3)) * (hexWidth + spacing);

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

            Terrain[][] map = world.getMap();

            for (int j = 0; j < world.mapHeight; j++) {
                for (int i = 0; i < world.mapWidth; i++) {
                    Terrain t = map[i][j];
                    int locX = world.getPixelX(i,j,drawSizeFactor,spacing);
                    int locY = world.getPixelY(i,j,drawSizeFactor,spacing);
                    if (map[i][j] == Terrain.MOUNTAIN){
                        spriteBatch.setTexture(mountainTexture);
                        spriteBatch.blitScaled(locX, locY, hexWidth, hexHeight, 0, 0, mountainTexture.getWidth(), mountainTexture.getHeight());
                    }
                    else if (map[i][j] == Terrain.PLAIN){
                        spriteBatch.setTexture(plainTexture);
                        spriteBatch.blitScaled(locX, locY, hexWidth, hexHeight, 0, 0, plainTexture.getWidth(), plainTexture.getHeight());
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
