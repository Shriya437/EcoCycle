
package ecocycle.model;

/**
 * Model class for a User.
 * This class holds all data related to a user.
 */
public class User {
    private String userId;
    private String username;
    private String password; // In a real app, this would be a hash
    private Role role;
    private double carbonCredits;
    private double totalSales;

    public User(String userId, String username, String password, Role role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.carbonCredits = 0.0;
        this.totalSales = 0.0;
    }

    // --- Getters (Needed for UI) ---
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public double getCarbonCredits() { return carbonCredits; }
    public double getTotalSales() { return totalSales; }

    // --- Setters (for DataService to update) ---
    public void setCarbonCredits(double carbonCredits) { this.carbonCredits = carbonCredits; }
    public void setTotalSales(double totalSales) { this.totalSales = totalSales; }
}