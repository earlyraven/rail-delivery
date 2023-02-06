package traingame;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import java.util.ArrayList;
import java.util.List;
import traingame.engine.DataFolders;
import traingame.engine.Gameloop;
import traingame.engine.Log;

public class Main {
    private static int DEFAULT_FRAME_CAP = 120;

    public static void main(String[] args) {
        DataFolders.init(Game.GAME_ID);
        Log.info("Starting " + Game.GAME_ID + " " + Game.VERSION + " with args: " + String.join(" ", args));

        // USAGE: Run "build.py build run rgb" to Start a game with Red, Green and Blue Companies.
        // Check if arg for companies to select is valid (contains at least one valid color).
        if (args.length > 0 && args[0].matches(".*[rgby]+.*")) {
            List<Company> companies = new ArrayList<>();
            String companyArgs = args[0];
            String[] selections = companyArgs.split("");
            boolean rAdded = false;
            boolean gAdded = false;
            boolean bAdded = false;
            boolean yAdded = false;

            for (String letter : selections) {
                System.out.println(letter);
                if (letter.equals("r") && !rAdded) {
                    companies.add(Company.makeRed());
                    rAdded = true;
                }
                if (letter.equals("g") && !gAdded) {
                    companies.add(Company.makeGreen());
                    gAdded = true;
                }
                if (letter.equals("b") && !bAdded) {
                    companies.add(Company.makeBlue());
                    bAdded = true;
                }
                if (letter.equals("y") && !yAdded) {
                    companies.add(Company.makeYellow());
                    yAdded = true;
                }
            }
            Log.debug("Starting with companies: " + companies.toString());
            runWithColors(companies);
        }
        else {
            runWithMenuStart();
        }
    }

    public static void runWithMenuStart() {
        Game game = new Game(true);
        new Gameloop(game, DEFAULT_FRAME_CAP).run();
    }

    public static void runWithColors(List<Company> companies) {
        Game game = new Game(true);
        Gameloop gameLoop = new Gameloop(game, DEFAULT_FRAME_CAP);
        game.enterWorld(companies);
        gameLoop.run();
    }
}
