package traingame;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Company {
    private final String name;
    private final Color color;
    private Map<Point, Point> rails = new HashMap<>();
    private int trainQ;
    private int trainR;
    private List<CargoOrder> orders;
    private int money;

    public Company(String name, Color color, Map<Point, Point> rails, int trainQ, int trainR, List<CargoOrder> orders, int money) {
        this.name = name;
        this.color = color;
        this.rails = rails;
        this.trainQ = trainQ;
        this.trainR = trainR;
        this.orders = orders;
        this.money = money;
    }
}
