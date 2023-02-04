package traingame.render;

import java.util.*;

import traingame.engine.render.SpriteBatch;
import traingame.engine.render.Texture;
import traingame.engine.render.gui.*;
import traingame.engine.render.text.BitmapFont;
import traingame.Game;

public class NewGameScreen extends Screen {
    private ArrayList<GuiElement> left = new ArrayList<>();
    private ArrayList<GuiElement> right = new ArrayList<>();
    private ToggleButton redButton = new ToggleButton(0, 0);
    private ToggleButton blueButton = new ToggleButton(COLOR_WIDTH, 0);
    private ToggleButton yellowButton = new ToggleButton(0, COLOR_HEIGHT);
    private ToggleButton greenButton = new ToggleButton(COLOR_WIDTH, COLOR_HEIGHT);

    private String title = "New Game";
    private String info = "Select one color per player";

    private Texture texture = new Texture("new_game.png");
    private static final int COLOR_WIDTH = 144;
    private static final int COLOR_HEIGHT = 96;

    private static class ToggleButton extends GuiElement {
        private int texX;
        private int texY;
        private boolean on = false;

        public ToggleButton(int texX, int texY) {
            this.texX = texX;
            this.texY = texY;
        }

        @Override
        public int getWidth() {
            return COLOR_WIDTH;
        }

        @Override
        public int getHeight() {
            return COLOR_HEIGHT;
        }

        public boolean isOn() {
            return on;
        }

        @Override
        public boolean trigger() {
            on = !on;
            return true;
        }

        @Override
        public void blit(SpriteBatch spriteBatch, boolean focused) {
            spriteBatch.blit(getX(), getY(), getWidth(), getHeight(), texX, texY);
            if (on) {
                spriteBatch.blit(getX(), getY(), getWidth(), getHeight(), 0, 2 * COLOR_HEIGHT);
            }
            if (focused) {
                spriteBatch.blit(getX(), getY(), getWidth(), getHeight(), COLOR_WIDTH, 2 * COLOR_HEIGHT);
            }
        }
    };

    public NewGameScreen(Game game) {
        BitmapFont font = Fonts.getButtonFont();

        left.add(redButton);
        right.add(blueButton);
        left.add(yellowButton);
        right.add(greenButton);
        left.add(new TextButton(font, "Exit", () -> game.stop()));
        right.add(new TextButton(font, "Ready", () -> tryEnterWorld(game)));

        for (GuiElement element : left) {
            selectable.add(element);
        }
        for (GuiElement element : right) {
            selectable.add(element);
        }
    }

    private void tryEnterWorld(Game game) {
        // Require at least one button to be selected to start.
        // FUTURE: If/when doing network multi-player we may wish to further restrict this
        // to players being ready.
        boolean startable = redButton.on || greenButton.on || blueButton.on || yellowButton.on;
        if (startable) {
            game.enterWorld();
        }
        else {
            System.out.println("Select at least one color.");
        }
    }

    @Override
    public void blit(SpriteBatch spriteBatch, double screenWidth, double screenHeight)
    {
        BitmapFont font = Fonts.getButtonFont();
        BitmapFont titleFont = Fonts.getTitleFont();

        int height = 0;
        height += (int)(titleFont.getHeight() * 1.25f);
        height += (int)(font.getHeight() * 1.25f);
        assert(left.size() == right.size());
        int numAlignedElements = left.size();
        for (int i = 0; i < left.size(); ++i) {
            assert(left.get(i).getHeight() == right.get(i).getHeight());
            int h = left.get(i).getHeight();
            height += h;
            if (i < numAlignedElements - 1) {
                height += h / 4;
            }
        }

        int width = 0;
        for (GuiElement item : left) {
            width = Math.max(width, item.getWidth());
        }
        for (GuiElement item : right) {
            width = Math.max(width, item.getWidth());
        }

        spriteBatch.setTexture(texture);
        int yPos = ((int)screenHeight - height) / 2;
        GuiElement selected = getSelected();
        int xPos = (int)((screenWidth - titleFont.getWidth(title)) / 2);
        titleFont.blit(title, xPos, yPos, 0, 0, 0);
        yPos += (int)(titleFont.getHeight() * 1.25f);
        xPos = (int)((screenWidth - font.getWidth(info)) / 2);
        font.blit(info, xPos, yPos, 0, 0, 0);
        yPos += (int)(font.getHeight() * 1.25f);

        for (int i = 0; i < numAlignedElements; ++i) {
            int offset = (int)(width * 0.625);
            GuiElement leftItem = left.get(i);
            xPos = (int)((screenWidth - leftItem.getWidth()) / 2) - offset;
            leftItem.position(xPos, yPos);
            leftItem.blit(spriteBatch, leftItem == selected);
            GuiElement rightItem = right.get(i);
            xPos = (int)((screenWidth - rightItem.getWidth()) / 2) + offset;
            rightItem.position(xPos, yPos);
            rightItem.blit(spriteBatch, rightItem == selected);

            yPos += (int)(Math.max(leftItem.getHeight(), rightItem.getHeight()) * 1.25);
        }

        spriteBatch.render();
    }
}
