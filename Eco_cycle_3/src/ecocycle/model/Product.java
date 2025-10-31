
package ecocycle.model;

import java.util.PriorityQueue;

/**
 * Model class for a Product.
 * Holds all data for a single product item.
 */
public class Product {

    private String productId;
    private String name;
    private String type;
    private String category;
    private double price;
    private String description;
    private String sellerId;
    private ProductStatus status;
    private long uploadTimestamp;
    
    // We keep the PriorityQueue here as it's part of the object's state
    private PriorityQueue<RecyclingBid> bids;

    public Product(String productId, String name, String type, String category, double price,
                   String description, String sellerId) {
        this.productId = productId;
        this.name = name;
        this.type = type.toLowerCase();
        this.category = category.toLowerCase();
        this.price = price;
        this.description = description;
        this.sellerId = sellerId;
        this.status = ProductStatus.AVAILABLE;
        this.uploadTimestamp = System.currentTimeMillis();
        this.bids = new PriorityQueue<>(); // Uses the compareTo in RecyclingBid
    }

    // --- Getters (Required by PropertyValueFactory in the TableView) ---
    public String getProductId() { return productId; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public String getDescription() { return description; }
    public String getSellerId() { return sellerId; }
    public ProductStatus getStatus() { return status; }
    public long getUploadTimestamp() { return uploadTimestamp; }
    public PriorityQueue<RecyclingBid> getBids() { return bids; }
    
    // --- Setters ---
    public void setStatus(ProductStatus status) { this.status = status; }
    public void setUploadTimestamp(long timestamp) { this.uploadTimestamp = timestamp; }
    public void setPrice(double price) { this.price = price; }
    public void setDescription(String description) { this.description = description; }

    // --- Helper methods from console app ---
    public int getCarbonValue() {
        return switch (type) {
            case "electronics" -> 100;
            case "plastic" -> 50;
            case "clothing" -> 30;
            case "furniture" -> 70;
            default -> 20;
        };
    }
    
    public double getBaseCost() {
        return this.price * 0.5;
    }
}