package traingame;

public record CargoOrder(Product product, City destination, int payment) {
	@Override
	public String toString() {
		return String.format("%-15s %-20s %-5s%n", product.name().toString(), destination.name().toString(), payment);
	}
}
