package traingame.engine.render.gui;

import java.awt.Color;

import traingame.engine.render.SpriteBatch;
import traingame.engine.render.text.BitmapFont;

public class TextButton extends TextElement {
    private static final Color HIGHLIGHT_COLOR = new Color(0.6f, 0.6f, 0.9f);

    private Runnable onTrigger;

    public TextButton(BitmapFont font, String text, int x, int y, Runnable onTrigger) {
        super(font, text, Color.BLACK, x, y);
        this.onTrigger = onTrigger;
    }

    public TextButton(BitmapFont font, String text, Runnable onTrigger) {
        this(font, text, 0, 0, onTrigger);
    }

    @Override
    public boolean trigger() {
        onTrigger.run();
        return true;
    }

    @Override
    public void blit(SpriteBatch spriteBatch, boolean focused) {
        if (focused) {
            font.blit(text, x, y, 
                HIGHLIGHT_COLOR.getRed() / 255f, HIGHLIGHT_COLOR.getGreen() / 255f, HIGHLIGHT_COLOR.getBlue() / 255f);
        }
        else {
            font.blit(text, x, y, color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f);
        }
    }
}
