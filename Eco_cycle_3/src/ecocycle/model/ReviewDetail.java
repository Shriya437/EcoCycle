// In file: src/ecocycle/model/ReviewDetail.java
package ecocycle.model;

/**
 * A "View Model" class. It doesn't represent a database table,
 * but it holds the combined data we need to display on the review feed.
 */
public class ReviewDetail {

    private String productName;
    private String buyerName;
    private String sellerName;
    private int rating;
    private String text;

    public ReviewDetail(String productName, String buyerName, String sellerName, int rating, String text) {
        this.productName = productName;
        this.buyerName = buyerName;
        this.sellerName = sellerName;
        this.rating = rating;
        this.text = text;
    }

    // --- Getters ---
    // These are required by the TableView's PropertyValueFactory

    public String getProductName() {
        return productName;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public String getSellerName() {
        return sellerName;
    }

    public int getRating() {
        return rating;
    }

    public String getText() {
        return text;
    }
}