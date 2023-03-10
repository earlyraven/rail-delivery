package traingame.engine.render;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;


public class Texture {
    private static final String TEXTURE_LOCATION = "/assets/textures/";
    private int handle;
    private int width;
    private int height;
    private ByteBuffer pixels;

    public Texture(String name) {
        BufferedImage image = ImageUtils.loadResource(TEXTURE_LOCATION + name);
        if (!isPowerOfTwo(image.getWidth()) || !isPowerOfTwo(image.getHeight())) {
            throw new RuntimeException("Size of texture " + name + " is not a power of two");
        }
        init(image, false);
    }

    public Texture(Color color) {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, 1, 1);
        g.dispose();
        init(image, false);
    }

    public Texture(BufferedImage image, boolean needsMipmaps) {
        init(image, needsMipmaps);
    }

    private void init(BufferedImage image, boolean needsMipmaps) {
        handle = GL11.glGenTextures();
        bind();
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        this.pixels = ImageUtils.bufferedImageToByteBuffer(image);
        internalUpdate(image, needsMipmaps);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void bind() {
        // Activate texture unit before binding texture
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, handle);
    }

    public void bindSpecular() {
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, handle);
    }

    public void bindEmission() {
        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, handle);
    }

    private boolean isPowerOfTwo(int n) {
        return n > 0 && ((n & n - 1) == 0);
    }

    public void update(BufferedImage image, boolean needsMipmaps) {
        ImageUtils.updateImageBuffer(pixels, image);
        this.bind();
        internalUpdate(image, needsMipmaps);
    }

    private void internalUpdate(BufferedImage image, boolean needsMipmaps) {
        this.width = image.getWidth();
        this.height = image.getHeight();

        if (!needsMipmaps) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        }

        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
        if (needsMipmaps) {
            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
        }
    }
}
