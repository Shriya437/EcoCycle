
package ecocycle.model;

/**
 * Model class for a Review.
 */
public class Review {
    private String reviewId;
    private String productId;
    private String buyerId;
    private int rating;
    private String text;
    private long timestamp;

    public Review(String reviewId, String productId, String buyerId, int rating, String text) {
        this.reviewId = reviewId;
        this.productId = productId;
        this.buyerId = buyerId;
        this.rating = rating;
        this.text = text;
        this.timestamp = System.currentTimeMillis();
    }

    // --- Getters ---
    public String getReviewId() { return reviewId; }
    public String getProductId() { return productId; }
    public String getBuyerId() { return buyerId; }
    public int getRating() { return rating; }
    public String getText() { return text; }
    public long getTimestamp() { return timestamp; }
    
    // --- Setters ---
    public void setRating(int rating) { this.rating = rating; }
    public void setText(String text) { this.text = text; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}