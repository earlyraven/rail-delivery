package traingame.engine.render;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.Version;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowPosCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import traingame.engine.InputManager;
import traingame.engine.Log;

// A wrapper for OpenGL's GLFW window
public class Window {
    private long window;
    private int width;
    private int height;
    private boolean focused;
    private boolean fullscreen;
    private String initialConfigPath;
    private static Thread mainThread;

    private static final int MIN_WIDTH = 20;
    private static final int MIN_HEIGHT = 20;

    // Cached values from before entering fullscreen
    // The GLFW function to update these values takes arrays (or IntBuffers)
    private int[] windowPosX = new int[1];
    private int[] windowPosY = new int[1];
    private int[] windowWidth = new int[1];
    private int[] windowHeight = new int[1];

    static {
        Log.info("Using LWJGL " + Version.getVersion());
        GLFWErrorCallback.createPrint(System.err).set();

        if (!GLFW.glfwInit()) {
			throw new IllegalStateException("Failed to initialize GLFW");
        }
        mainThread = Thread.currentThread();
        Log.info("Initialized GLFW " + GLFW.glfwGetVersionString());

        GLFW.glfwSetErrorCallback((error, description) -> handleError(error, description));

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
    }

    public boolean shouldClose() {
        return GLFW.glfwWindowShouldClose(window);
    }

    public Window(String configPath, String title) {
        this.initialConfigPath = configPath;
        // Try to use the same size and position as last session
        Toml toml = new Toml();
        try {
            FileInputStream initconfig = new FileInputStream(initialConfigPath);
            toml.read(initconfig);
        }
        catch (FileNotFoundException e) { }
        Long configWidth = toml.getLong("windowWidth");
        Long configHeight = toml.getLong("windowHeight");
        Long configPosX = toml.getLong("windowPosX");
        Long configPosY = toml.getLong("windowPosY");
        Boolean configFullscreen = toml.getBoolean("fullscreen");
        Boolean configMaximized = toml.getBoolean("maximized");
        boolean maximized = configMaximized == null? false : configMaximized.booleanValue();
        fullscreen = configFullscreen == null? true : configFullscreen.booleanValue();

        // Get screen size and monitor work area
        GLFWVidMode mode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        int[] workX = new int[1];
        int[] workY = new int[1];
        int[] workWidth = new int[1];
        int[] workHeight = new int[1];
        GLFW.glfwGetMonitorWorkarea(GLFW.glfwGetPrimaryMonitor(), workX, workY, workWidth, workHeight);

        int defaultWidth = workWidth[0] * 2 / 3;
        int defaultHeight = workHeight[0] * 2 / 3;

        this.width = configWidth == null? defaultWidth : (int)configWidth.longValue();
        this.height = configHeight == null? defaultHeight : (int)configHeight.longValue();
        this.width = Math.min(this.width, workWidth[0]);
        this.height = Math.min(this.height, workHeight[0]);
        this.width = Math.max(this.width, MIN_WIDTH);
        this.height = Math.max(this.height, MIN_HEIGHT);
        windowWidth[0] = this.width;
        windowHeight[0] = this.height;

        // Set OpenGL version
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GL11.GL_TRUE);

        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);

        long monitor = MemoryUtil.NULL;
        if (fullscreen) {
            monitor =  GLFW.glfwGetPrimaryMonitor();
            width = mode.width();
            height = mode.height();
        }
        window = GLFW.glfwCreateWindow(width, height, title, monitor, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create GLFW window.");
        }

        GLFW.glfwSetWindowSizeLimits(window, MIN_WIDTH, MIN_HEIGHT, GLFW.GLFW_DONT_CARE, GLFW.GLFW_DONT_CARE);
        if (maximized) {
            GLFW.glfwMaximizeWindow(window);
        }
        else {
            GLFW.glfwRestoreWindow(window);
            if (!fullscreen) {
                GLFW.glfwGetWindowSize(window, windowWidth, windowHeight);
            }
        }

        // Get window decorations size
        int[] frameLeft = new int[1];
        int[] frameTop = new int[1];
        int[] frameRight = new int[1];
        int[] frameBottom = new int[1];
        GLFW.glfwGetWindowFrameSize(window, frameLeft, frameTop, frameRight, frameBottom);

        // Position the window
        int defaultPosX = (mode.width() - windowWidth[0]) / 2;
        int defaultPosY = (mode.height() - windowHeight[0]) / 2;
        windowPosX[0] = configPosX == null? defaultPosX : (int)configPosX.longValue();
        windowPosY[0] = configPosY == null? defaultPosY : (int)configPosY.longValue();
        // Limit position
        windowPosX[0] = Math.min(windowPosX[0], workX[0] + workWidth[0] - this.width - frameRight[0]);
        windowPosY[0] = Math.min(windowPosY[0], workY[0] + workHeight[0] - this.height - frameBottom[0]);
        windowPosX[0] = Math.max(windowPosX[0], workX[0] + frameLeft[0]);
        windowPosY[0] = Math.max(windowPosY[0], workY[0] + frameTop[0]);

        if (!maximized && !fullscreen) {
            GLFW.glfwSetWindowPos(window, windowPosX[0], windowPosY[0]);
        }

        // Listen for focus loss/gain events
        GLFW.glfwSetWindowFocusCallback(window, 
            (window, focused) -> this.focused = focused
        );

        // Listen for position change events
        GLFW.glfwSetWindowPosCallback(window, new GLFWWindowPosCallback() {
                @Override
                public void invoke(long window, int xPos, int yPos) {
                    if (!fullscreen) {
                        windowPosX[0] = xPos;
                        windowPosY[0] = yPos;
                    }
                }
            }
        );

        GLFW.glfwMakeContextCurrent(window);

        GLFW.glfwSwapInterval(1);
        GLFW.glfwShowWindow(window);

		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.
        GL.createCapabilities();

        // Set window icon
        GLFWImage.Buffer icons = GLFWImage.malloc(4);
        icons.put(0, loadIcon("/assets/icon/icon16.png"));
        icons.put(1, loadIcon("/assets/icon/icon32.png"));
        icons.put(2, loadIcon("/assets/icon/icon48.png"));
        icons.put(3, loadIcon("/assets/icon/icon256.png"));

        GLFW.glfwSetWindowIcon(window, icons);
    }

    private GLFWImage loadIcon(String name) {
        BufferedImage image = ImageUtils.loadResource(name);
        ByteBuffer pixels = ImageUtils.bufferedImageToByteBuffer(image);
        GLFWImage icon = GLFWImage.malloc();
        icon.set(image.getWidth(), image.getHeight(), pixels);
        return icon;
    }

    public void swapBuffers() {
        GLFW.glfwSwapBuffers(window);
    }

    public void updateInput() {
        // Needed for the window to respond to events, e.g. user clicks the 'X'
        GLFW.glfwPollEvents();
    }

    public void close() {
        if (Thread.currentThread() != mainThread) {
            throw new RuntimeException("Window was closed from the wrong thread");
        }

        boolean maximized = isMaximized();

		// Free the window callbacks and destroy the window
        Callbacks.glfwFreeCallbacks(window);
        GLFW.glfwDestroyWindow(window);

	    // Terminate GLFW and free the error callback
        GLFW.glfwTerminate();
        GLFW.glfwSetErrorCallback(null).free();

        // Save size and position for next session
        TomlWriter tomlWriter = new TomlWriter();
        HashMap<String, Object> map = new HashMap<>();
        map.put("windowWidth", this.windowWidth[0]);
        map.put("windowHeight", this.windowHeight[0]);
        map.put("windowPosX", this.windowPosX[0]);
        map.put("windowPosY", this.windowPosY[0]);
        map.put("fullscreen", this.fullscreen);
        map.put("maximized", maximized);
        try {
            File config = new File(initialConfigPath);
            config.getParentFile().mkdirs();
            config.createNewFile();
            FileOutputStream out = new FileOutputStream(config);
            tomlWriter.write(map, out);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void handleError(int error, long description) {
        throw new RuntimeException(GLFWErrorCallback.getDescription(description));
    }

    public void setResizeListener(IFramebufferSizeListener listener) {
        listener.windowResized(width, height);
        // Handle resizing the window
        GLFW.glfwSetFramebufferSizeCallback(window, new GLFWFramebufferSizeCallback() {
                @Override
                public void invoke(long window, int widthIn, int heightIn) {
                    width = widthIn;
                    height = heightIn;
                    GL11.glViewport(0, 0, width, height);
                    listener.windowResized(width, height);
                    if (!fullscreen) {
                        GLFW.glfwGetWindowSize(window, windowWidth, windowHeight);
                        // Apparently resizing can change pos without calling that callback, so update here too
                        GLFW.glfwGetWindowPos(window, windowPosX, windowPosY);
                    }
                }
            }
        );

    }

    public void setInput(InputManager input) {
        input.window = this;

        GLFW.glfwSetKeyCallback(window, 
            (window, key, scancode, action, mods) -> input.processKey(key, scancode, action, mods)
        );

        GLFW.glfwSetCharCallback(window,
            (window, codepoint) -> input.inputCharacter((char)codepoint)
        );

        GLFW.glfwSetCursorPosCallback(window,
            (window, xPos, yPos) -> input.moveCursor(xPos, yPos)
        );

        GLFW.glfwSetMouseButtonCallback(window,
            (window, button, action, mods) -> input.processMouseClick(button, action, mods)
        );

        GLFW.glfwSetScrollCallback(window,
            (window, xOffset, yOffset) -> input.processScroll(xOffset, yOffset)
        );

        GLFW.glfwSetJoystickCallback(
            (joystickID, event) -> input.joystickConnectionChanged(joystickID, event)
        );
    }

    public int getMouseButton(int button) {
        return GLFW.glfwGetMouseButton(window, button);
    }

    public boolean isKeyDown(int key) {
        return GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
    }

    private boolean isFocused() {
        return this.focused;
    }

    private boolean isMaximized() {
        return GLFW.glfwGetWindowAttrib(window, GLFW.GLFW_MAXIMIZED) == GLFW.GLFW_TRUE;
    }

    public void toggleFullscreen() {
        fullscreen = !fullscreen;
        if (fullscreen) {
            long monitor = GLFW.glfwGetPrimaryMonitor();
            GLFWVidMode videoMode = GLFW.glfwGetVideoMode(monitor);
            GLFW.glfwSetWindowMonitor(window, monitor, 0, 0, videoMode.width(), videoMode.height(), GLFW.GLFW_DONT_CARE);
        }
        else {
            // First, set the window to not be fullscreen
            GLFW.glfwSetWindowMonitor(window, MemoryUtil.NULL, windowPosX[0], windowPosY[0], windowWidth[0], windowHeight[0], GLFW.GLFW_DONT_CARE);
            // Get monitor work area
            int[] workX = new int[1];
            int[] workY = new int[1];
            int[] workWidth = new int[1];
            int[] workHeight = new int[1];
            GLFW.glfwGetMonitorWorkarea(GLFW.glfwGetPrimaryMonitor(), workX, workY, workWidth, workHeight);
            // Get window decorations size
            int[] frameLeft = new int[1];
            int[] frameTop = new int[1];
            int[] frameRight = new int[1];
            int[] frameBottom = new int[1];
            GLFW.glfwGetWindowFrameSize(window, frameLeft, frameTop, frameRight, frameBottom);
            // Limit position, width, and height
            windowPosX[0] = Math.max(windowPosX[0], workX[0] + frameLeft[0]);
            windowPosY[0] = Math.max(windowPosY[0], workY[0] + frameTop[0]);
            windowWidth[0]  = Math.min(windowWidth[0], workWidth[0] - frameLeft[0] - frameRight[0]);
            windowHeight[0] = Math.min(windowHeight[0], workHeight[0] - frameTop[0] - frameBottom[0]);
            GLFW.glfwSetWindowPos(window, windowPosX[0], windowPosY[0]);
            GLFW.glfwSetWindowSize(window, windowWidth[0], windowHeight[0]);
        }
    }
}
