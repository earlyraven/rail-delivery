package traingame;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

// narrow down later.
import java.util.*;

public class Company {
    public final String name;
    public final Color color;
    private List<RailSegment> railNetwork = new ArrayList<>();
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

    public void addRail(RailSegment railSegment) {
        // External caller should check to make sure rail is not already present.
        assert(!hasRail(railSegment));
        railNetwork.add(railSegment);
    }

    public boolean hasRail(RailSegment railSegment) {
        for (RailSegment segment : railNetwork) {
            if (segment.equals(railSegment)) {
                return true;
            }
        }
        return false;
    }

    public List<RailSegment> getRailNetwork() {
        return railNetwork;
    }

    private Set<Point> getNetworkConnectionPoints() {
        Set<Point> thePoints = new HashSet<>();
        for (RailSegment railSegment : railNetwork) {
            thePoints.addAll(railSegment.points());
        }
        // also get city points similarly.
        // perhaps Company should store a list of cities it's connected to for easy reference here?
        //code here. ^^ would require the above, as Company does not know about world or cities in existance.

        return thePoints;
    }

    public boolean isConnectedTo(Company company) {
        Set<Point> copyOfSet = new HashSet<>(this.getNetworkConnectionPoints());

        int startingSize = copyOfSet.size();
        copyOfSet.removeAll(company.getNetworkConnectionPoints());
        if (copyOfSet.size() < startingSize) {
            // if there was a change in size, it means there was an overlap.
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        List<String> output = new ArrayList<>();

        output.add(this.getClass().getName());
        output.add(name);
        output.add(String.valueOf(trainQ));
        output.add(String.valueOf(trainR));
        output.add(String.valueOf(money));
        output.add(String.valueOf(railNetwork));

        return output.toString();
    }
}
