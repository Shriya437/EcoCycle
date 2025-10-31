// In file: src/ecocycle/model/TransactionStatus.java
package ecocycle.model;

public enum TransactionStatus {
    // 1. Define the constants, calling the constructor
    PENDING("Pending"),
    COMPLETED("Completed");

    // 2. Field to store the associated data
    private final String displayName;

    // 3. Constructor (must be private or package-private)
    TransactionStatus(String displayName) {
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