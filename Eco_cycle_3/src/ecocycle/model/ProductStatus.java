// In file: src/ecocycle/model/ProductStatus.java
package ecocycle.model;

public enum ProductStatus {
    // 1. Define the constants, calling the constructor
    AVAILABLE("Available"),
    AVAILABLE_NO_RECYCLE("Available (Recycling Denied)"),
    SOLD("Sold"),
    PENDING_RECYCLING("Pending Recycling"),
    RECYCLING_PURCHASED("Recycling - Purchased"),
    RECYCLED("Recycled");

    // 2. Field to store the associated data
    private final String displayName;

    // 3. Constructor (must be private or package-private)
    ProductStatus(String displayName) {
        this.displayName = displayName;
    }

    // 4. Public getter to access the data
    public String getDisplayName() {
        return displayName;
    }

    // 5. (Optional but Recommended) Override toString()
    // This makes it automatically display the friendly name in JavaFX controls
    @Override
    public String toString() {
        return this.displayName;
    }
}