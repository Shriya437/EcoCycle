// In file: src/ecocycle/model/Review.java
package ecocycle.model;

/**
 * Model class for a Review.
 * This version has been simplified to remove 'rating'.
 */
public class Review {

    private String reviewId;
    private String productId;
    private String buyerId;
    // --- 'rating' field has been REMOVED ---
    private String text;
    private long timestamp;

    // --- Constructor updated to remove 'rating' ---
    public Review(String reviewId, String productId, String buyerId, String text) {
        this.reviewId = reviewId;
        this.productId = productId;
        this.buyerId = buyerId;
        this.text = text;
        this.timestamp = System.currentTimeMillis();
    }

    // --- Getters ---
    public String getReviewId() { return reviewId; }
    public String getProductId() { return productId; }
    public String getBuyerId() { return buyerId; }
    public String getText() { return text; }
    public long getTimestamp() { return timestamp; }
    
    // --- Setters ---
    // 'rating' setter is removed
    public void setText(String text) { this.text = text; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}