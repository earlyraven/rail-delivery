package traingame;

public enum Product {
	CORN,
	SOYBEAN_OIL,
	FISH,
	IRON,
	STEEL;
	//TODO: EXPAND with more products.

	public String getName() {
		return name();
	}

	public String readableName() {
		String name = name();
		return name.toLowerCase().replace("_", " ");
	}
}
