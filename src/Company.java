package traingame;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Company {
    private final String name;
    private final Color color;
    private Map<Point, Point> rails = new HashMap<>();
    public int trainQ;
    public int trainR;
    private List<CargoOrder> orders = new ArrayList<>();
    private int money = 50;

    public Company(String name, Color color) {
        this.name = name;
        this.color = color;
    }

    // Possibly refactor this to makeBasic(Color color) instead. <or String, or enum color>
    // probably enum color would be best.
    public static Company makeRed() {
        return new Company("Red Company", Color.RED);
    }
    public static Company makeBlue() {
        return new Company("Blue Company", Color.BLUE);
    }
    public static Company makeYellow() {
        return new Company("Yellow Company", Color.YELLOW);
    }
    public static Company makeGreen() {
        return new Company("Green Company", Color.GREEN);
    }

    @Override
    public String toString() {
        List<String> output = new ArrayList<>();

        output.add(this.getClass().getName());
        output.add(name);
        output.add(String.valueOf(trainQ));
        output.add(String.valueOf(trainR));
        output.add(String.valueOf(money));

        return output.toString();
    }
}
