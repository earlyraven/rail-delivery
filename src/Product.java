package traingame;

public enum Product {
	CORN,
	LEATHER_GOODS,
	FISH,
	IRON,
	STEEL;
	//TODO: EXPAND with more products.

	public final String label;

	public static Product getRandom(){
		int randomIndex = (int) (Math.random() * values().length);
		return values()[randomIndex];
	}

	private Product() {
		this.label = name().toLowerCase().replace("_", " ");
	}
}
