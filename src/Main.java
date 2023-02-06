package traingame;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import traingame.engine.DataFolders;
import traingame.engine.Gameloop;
import traingame.engine.Log;
import java.util.List;
import java.util.ArrayList;

public class Main {
    private static int DEFAULT_FRAME_CAP = 120;

    public static void main(String[] args) {
        DataFolders.init(Game.GAME_ID);
        Log.info("Starting " + Game.GAME_ID + " " + Game.VERSION + " with args: " + String.join(" ", args));

        System.out.println(args);

        // USAGE: Run build.py build run rgb to Start a game with Red, Green and Blue Companies.
        if (args.length > 0) {
            // TODO: Put further contraints on what args are valid. (Ex. disallow "rgoooy")
            // and limit check to just a single arg (instead of all args)
            System.out.println("PARAMS: " + args);
            List<Company> companies = new ArrayList<>();
            for (String val : args) {
                System.out.println(val);
                if (val.contains("r")) {
                    companies.add(Company.makeRed());
                }
                if (val.contains("g")) {
                    companies.add(Company.makeGreen());
                }
                if (val.contains("b")) {
                    companies.add(Company.makeBlue());
                }
                if (val.contains("y")) {
                    companies.add(Company.makeYellow());
                }
                Log.debug("Starting with companies: " + companies.toString());
                runWithColors(companies);
            }
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
