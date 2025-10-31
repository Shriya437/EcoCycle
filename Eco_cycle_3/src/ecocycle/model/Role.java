// In file: src/ecocycle/model/Role.java
package ecocycle.model;

public enum Role {
    // 1. Define the constants, calling the constructor
    SELLER("Seller"),
    BUYER("Buyer"),
    RECYCLER("Recycler");

    // 2. Field to store the associated data
    private final String displayName;

    // 3. Constructor (must be private)
    Role(String displayName) {
        this.displayName = displayName;
    }

    // 4. Public getter to access the data
    public String getDisplayName() {
        return displayName;
    }

    // 5. Override toString()
    // This makes JavaFX controls (like ComboBox) show the friendly name
    @Override
    public String toString() {
        return this.displayName;
    }
}