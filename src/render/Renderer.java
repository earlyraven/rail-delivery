package traingame.render;

import java.awt.Font;
import java.io.IOException;
import java.lang.Math.*;
import java.util.Scanner;

import org.lwjgl.opengl.GL11;

import shadowfox.math.*;
import traingame.*;
import traingame.engine.render.*;

public class Renderer implements IFramebufferSizeListener {
    private static final int HEX_WIDTH = 14;
    private static final int HEX_HEIGHT = 16;
    private static final int HEX_ROW_HEIGHT = 12;

    private static final int STYLE = 0;

    private final ShaderProgram shader2D = ShaderProgram.load("/shaders/2d.vsh", "/shaders/2d.fsh");
    private final Vector2f uiDimensions = new Vector2f(1, 1);
    private final SpriteBatch spriteBatch = new SpriteBatch();
    private final Texture terrainTexture = new Texture("terrain.png");
    private final Texture mapBackground = new Texture("map_background.png");

    private final int MAP_PIXELS_WIDE = 1440;
    private final int MAP_PIXELS_HIGH = 1080;

    private int width;
    private int height;

    public Renderer() {
        // Enable alpha blending (over)
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glClearColor(1f, 1f, 1f, 1f);

        assert(HEX_WIDTH % 2 == 0);
    }

    public int getPixelX(int x, int y) {
        int halfWidth = HEX_WIDTH / 2;
        return halfWidth * (2 * x + y % 2);
    }

    public int getPixelY(int x, int y) {
        return y * HEX_ROW_HEIGHT;
    }

    public void render(float lerp, World world, Overlay overlay) {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        // Set up for two-dimensional rendering
        shader2D.use();
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        // Render the world
        if (world != null) {
            renderWorld(lerp, world);
        }

        // Render UI
        shader2D.setUniform("dimensions", uiDimensions);
        shader2D.setFloat("left_margin", 0);
        shader2D.setFloat("right_margin", 0);
        shader2D.setFloat("top_margin", 0);
        shader2D.setFloat("bottom_margin", 0);
        overlay.render(spriteBatch, uiDimensions);
    }

    private boolean sidebar() {
        return (double)MAP_PIXELS_WIDE / MAP_PIXELS_HIGH < (double)width / height;
    }

    private int getSidebarWidth() {
        if (sidebar()) {
            return 100;
        }
        return 0;
    }

    private int getBottomBarHeight() {
        if (sidebar()) {
            return 0;
        }
        return 100;
    }

    private double mapAspectRatio() {
        return (double)MAP_PIXELS_WIDE / MAP_PIXELS_HIGH;
    }

    // Extra padding on the x axis the make the map keep its aspect ratio
    private float getMapBufferWidth() {
        int mapScreenWidth = width - getSidebarWidth();
        int mapScreenHeight = height - getBottomBarHeight();

        if (mapAspectRatio() > (double)mapScreenWidth / mapScreenHeight) {
            return 0;
        }
        else {
            return mapScreenWidth - (int)(mapAspectRatio() * mapScreenHeight);
        }

    }

    // Extra padding on the y axis the make the map keep its aspect ratio
    private float getMapBufferHeight() {
        int mapScreenWidth = width - getSidebarWidth();
        int mapScreenHeight = height - getBottomBarHeight();

        if (mapAspectRatio() > (double)mapScreenWidth / mapScreenHeight) {
            return mapScreenHeight - (int)(mapScreenWidth / mapAspectRatio());
        }
        else {
            return 0;
        }
    }

    private float getLeftMargin() {
        return (getSidebarWidth() + getMapBufferWidth() / 2) / uiDimensions.x;
    }

    private float getRightMargin() {
        float widthDiff = getMapBufferWidth();
        return (widthDiff - (widthDiff / 2)) / uiDimensions.x;
    }

    private float getTopMargin() {
        return getMapBufferHeight() / 2 / uiDimensions.y;
    }

    private float getBottomMargin() {
        float heightDiff = getMapBufferHeight();
        return (heightDiff - heightDiff / 2 + getBottomBarHeight()) / uiDimensions.y;
    }

    private void renderWorld(float lerp, World world) {
        shader2D.setUniform("dimensions", new Vector2f(MAP_PIXELS_WIDE, MAP_PIXELS_HIGH));
        shader2D.setFloat("left_margin", getLeftMargin());
        shader2D.setFloat("right_margin", getRightMargin());
        shader2D.setFloat("top_margin", getTopMargin());
        shader2D.setFloat("bottom_margin", getBottomMargin());

        spriteBatch.setTexture(mapBackground);
        spriteBatch.blit(0, 0, MAP_PIXELS_WIDE, MAP_PIXELS_HIGH, mapBackground.getWidth(), mapBackground.getHeight());
        spriteBatch.render();

        spriteBatch.setTexture(terrainTexture);
        final int SPRITE_PADDING = 1;
        int texY = (HEX_HEIGHT + SPRITE_PADDING) * STYLE;
        final int SPRITE_COLUMN = HEX_WIDTH + SPRITE_PADDING;
        for (int y = 0; y < world.mapHeight; y++) {
            for (int x = 0; x < world.mapWidth; x++) {
                Terrain terrain = world.getTerrainXY(x,y);
                int locX = getPixelX(x, y);
                int locY = getPixelY(x, y);

                int texX = 0;
                if (terrain == Terrain.PLAIN) {
                    texX = SPRITE_COLUMN * 1;
                }
                else if (terrain == Terrain.MOUNTAIN) {
                    texX = SPRITE_COLUMN * 2;
                }
                else if (terrain == Terrain.FOREST) {
                    texX = SPRITE_COLUMN * 3;
                }
                spriteBatch.blit(locX, locY, HEX_WIDTH, HEX_HEIGHT, texX, texY);
            }
        }

        for (City city : world.cities) {
            for (Point p : city.locations()) {
                int locX = getPixelX(p.x(), p.y());
                int locY = getPixelY(p.x(), p.y());
                spriteBatch.blit(locX, locY, HEX_WIDTH, HEX_HEIGHT, 75, texY);
            }
        }

        for (Company company : world.companies) {
            for (RailSegment rail : company.getRailNetwork()) {
                int x = rail.origin().x();
                int y = rail.origin().y();
                int locX = getPixelX(x, y);
                int locY = getPixelY(x, y);

                if (rail.direction() == Direction.EAST) {
                    spriteBatch.blit(locX + HEX_WIDTH / 2, locY, HEX_WIDTH, HEX_HEIGHT, 0, 34, company.color);
                }
                else if (rail.direction() == Direction.SOUTHEAST) {
                    spriteBatch.blit(locX, locY, 2 * HEX_WIDTH, 2 * HEX_HEIGHT, 15, 34, company.color);
                }
                else if (rail.direction() == Direction.SOUTHWEST) {
                    int width = 2 * HEX_WIDTH;
                    int height = 2 * HEX_HEIGHT;
                    spriteBatch.blitStretched(locX + HEX_WIDTH, locY, -1 * width, height,
                                              15, 34, width, height, company.color);
                }
                else {
                    assert(false);
                }
            }
        }

        Point highlighted = world.getHoverLocation();
        if (highlighted != null) {
            int locX = getPixelX(highlighted.x(), highlighted.y());
            int locY = getPixelY(highlighted.x(), highlighted.y());
            if (world.canBuildFrom(highlighted)) {
                spriteBatch.blit(locX, locY, HEX_WIDTH, HEX_HEIGHT, 90, texY);
            }
            spriteBatch.blit(locX, locY, HEX_WIDTH, HEX_HEIGHT, 0, texY);
        }

        spriteBatch.render();
    }

    // Note this function can return points outside of the map bounds. It's the caller's responsibilty
    // to check for that.
    public Point getHexAtScreenCoordinates(double xPos, double yPos) {
        double xLoc = (xPos / uiDimensions.x - getLeftMargin()) / (1.0 - getLeftMargin() - getRightMargin());
        double yLoc = (yPos / uiDimensions.y - getTopMargin()) / (1.0 - getTopMargin() - getBottomMargin());
        if (xLoc < 0 || xLoc > 1 || yLoc < 0 || yLoc > 1) {
            return null;
        }

        double xMap = xLoc * MAP_PIXELS_WIDE;
        double yMap = yLoc * MAP_PIXELS_HIGH;

        final int SIDE_LENGTH = HEX_HEIGHT / 2;
        int yGuess = (int)(yMap / HEX_ROW_HEIGHT);
        // Use floor to make -0.5 drop down to -1 instead of being rounded to 0
        int xGuess = (int)Math.floor(xMap / HEX_WIDTH - 0.5 * (yGuess % 2));
        if (yMap > HEX_ROW_HEIGHT * (yGuess + 1) - SIDE_LENGTH) {
            // Mouse is over a non-overlapping part of the row, so yGuess is the actual y value
            return Point.fromXY(xGuess, yGuess);
        }
        // Else, rows overlap. Actual y value could be yGuess or yGuess - 1
        int yGuess2 = yGuess - 1;
        int xGuess2 = (int)Math.floor(xMap / HEX_WIDTH - 0.5 * (yGuess2 % 2));

        double xCenter1 = HEX_WIDTH * (xGuess + 0.5 + 0.5 * (yGuess % 2));
        double xCenter2 = HEX_WIDTH * (xGuess2 + 0.5 + 0.5 * (yGuess2 % 2));
        double dx1 = xMap - xCenter1;
        double dx2 = xMap - xCenter2;
        double yCenter1 = yGuess * HEX_ROW_HEIGHT + SIDE_LENGTH;
        double yCenter2 = yGuess2 * HEX_ROW_HEIGHT + SIDE_LENGTH;
        double dy1 = yMap - yCenter1;
        double dy2 = yMap - yCenter2;
        if (dx1 * dx1 + dy1 * dy1 <= dx2 * dx2 + dy2 * dy2) {
            return Point.fromXY(xGuess, yGuess);
        }

        return Point.fromXY(xGuess2, yGuess2);
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
