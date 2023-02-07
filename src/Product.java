package traingame;

public enum Product {
    CORN,
    LEATHER_GOODS,
    FISH,
    IRON,
    STEEL;
    //TODO: EXPAND with more products.

    public final String label;

    public static Product getRandomExcluding(Product exclusion) {
        Product product = values()[(int) (Math.random() * values().length)];
        if (product == exclusion) {
            return getRandomExcluding(exclusion);
        }
        return product;
    }

    private Product() {
        this.label = name().toLowerCase().replace("_", " ");
    }
}
