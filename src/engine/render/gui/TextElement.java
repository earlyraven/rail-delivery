package traingame.engine.render.gui;

import java.awt.Color;

import traingame.engine.render.SpriteBatch;
import traingame.engine.render.text.BitmapFont;

public class TextElement extends GuiElement {
    // This class does not "own" the font and is not responsible for rendering or cleanup
    protected BitmapFont font;
    protected String text;
    protected Color color;

    public TextElement(BitmapFont font, String text, Color color, int x, int y) {
        this.font = font;
        this.text = text;
        this.color = color;
        this.x = x;
        this.y = y;
    }

    public TextElement(BitmapFont font, String text, Color color) {
        this(font, text, color, 0, 0);
    }

    public TextElement(BitmapFont font, String text) {
        this(font, text, Color.BLACK);
    }

    @Override
    public int getWidth() {
        return font.getWidth(text);
    }

    @Override
    public int getHeight() {
        return font.getHeight();
    }

    @Override
    public void blit(SpriteBatch spriteBatch, boolean focused) {
        font.blit(text, x, y, color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f);
    }
}
