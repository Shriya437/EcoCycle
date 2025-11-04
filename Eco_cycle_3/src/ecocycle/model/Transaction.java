// In file: src/ecocycle/model/Transaction.java
package ecocycle.model;

// Converted from a record to a class to fix TableView errors
public class Transaction {

    private final String transactionId;
    private final String buyerId;
    private final String productId;
    private final double price;
    private final long timestamp;
    private final TransactionStatus status;

    public Transaction(String transactionId, String buyerId, String productId, double price,
                       long timestamp, TransactionStatus status) {
        this.transactionId = transactionId;
        this.buyerId = buyerId;
        this.productId = productId;
        this.price = price;
        this.timestamp = timestamp;
        this.status = status;
    }

    // --- Public Getters ---
    // These methods (e.g., getTransactionId()) are what the
    // PropertyValueFactory in your TableView is looking for.

    public String getTransactionId() {
        return transactionId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public String getProductId() {
        return productId;
    }

    public double getPrice() {
        return price;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public TransactionStatus getStatus() {
        return status;
    }
}