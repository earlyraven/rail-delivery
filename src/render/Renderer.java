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
    private static final int HEX_SIZE_MULTIPLIER = 50; //TODO: ADJUST as needed/compute this based on
    //other things such as amount of tiles along x and y.
    private static final int HEX_WIDTH = (int)Math.round(HEX_SIZE_MULTIPLIER * 1);
    private static final int HEX_HEIGHT = (int)Math.round(HEX_SIZE_MULTIPLIER * (2/(Math.sqrt(3))));

    public Renderer() {
        // Enable alpha blending (over)
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glClearColor(1f, 1f, 1f, 1f);
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
