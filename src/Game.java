package traingame;

import java.net.InetSocketAddress;

import traingame.engine.DataFolders;
import traingame.engine.ILoopable;
import traingame.engine.Log;
import traingame.engine.SoftwareVersion;
import traingame.engine.render.Window;
import traingame.render.*;
import java.util.List;

public class Game implements ILoopable {
    public static final SoftwareVersion VERSION = new SoftwareVersion(0, 0, 0);
    public static final String GAME_ID = "TrainGame";

    private boolean running = true;

    private World world;
    private Window window;
    private Renderer renderer;
    private Input input;
    private Overlay overlay;

    public Game(boolean graphical) {
        if (graphical) {
            this.window = new Window(DataFolders.getUserMachineFolder("initconfig.toml"), GAME_ID);
            Fonts.load();
            this.renderer = new Renderer();
            this.window.setResizeListener(renderer);
            this.input = new Input(this, renderer);
            this.window.setInput(input);
            this.overlay = new Overlay(this);
            this.input.setOverlay(this.overlay);
            this.input.updateConnectedGamepads();
        }
        this.world = null;
    }

    public void enterWorld(List<Company> companies) {
        this.world = new World(companies);
        initGraphical();
    }

    public void exitWorld() {
        this.world = null;
        overlay.pushScreen(new NewGameScreen(this));
    }

    private void initGraphical() {
        if (isGraphical()) {
            input.setWorld(world);
            while (overlay.hasScreen()) {
                overlay.popScreen();
            }
        }
    }

    private boolean isGraphical() {
        return window != null;
    }

    @Override
    public boolean isRunning() {
        return running && (window == null || !window.shouldClose());
    }

    @Override
    public void update() {
        if (window != null) {
            window.updateInput();
        }
        if (input != null) {
            input.update();
        }
    }

    @Override
    public void render(float interpolation) {
        if (window == null) {
            return;
        }
        window.updateInput();
        renderer.render(interpolation, world, overlay);
        window.swapBuffers();
    }

    public void stop() {
        running = false;
    }

    @Override
    public void close() {
        running = false;
        Fonts.clean();
        if (window != null) {
            window.close();
            window = null;
        }
    }

    public World getWorld() {
        return world;
    }

    // In-game as opposed to in the starting menu
    public boolean isInGame() {
        return world != null;
    }

    public void escape() {
        overlay.escape(this);
    }
}
