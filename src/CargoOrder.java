package traingame;

import java.util.Random;

//NOTE: For now, keeping it simple.  Just random.  Might want to re-balance payout later.
//For example, to care about the distance between pickup and delivery cities.
public record CargoOrder(Product product, City destination, int payment) {
	public static CargoOrder getRandom(City[] cities) {
	//Chose a random product, city and payout.
	Random random = new Random();
	Product chosenProduct = Product.getRandom();
	City chosenCity = cities[random.nextInt(cities.length)];
	int chosenPayout = random.nextInt(20,50);

	//Create and return the CargoOrder.
	return new CargoOrder(chosenProduct, chosenCity, chosenPayout);
	}

	@Override
	public String toString() {
		return String.format("%-15s %-20s %-5s%n", product.name(), destination.name(), payment);
	}
}
