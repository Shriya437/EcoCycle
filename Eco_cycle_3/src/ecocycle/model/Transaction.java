
package ecocycle.model;

/**
 * A record to represent a single transaction.
 */
public record Transaction(String transactionId, String buyerId, String productId, double price,
                           long timestamp, TransactionStatus status) {

	public Transaction(String transactionId, String buyerId, String productId , double price , long timestamp,
			TransactionStatus status) {
				this.buyerId = buyerId;
				this.price = price;
				this.productId = productId;
				this.status = status;
				this.timestamp = timestamp;
				this.transactionId = transactionId;
		// TODO Auto-generated constructor stub
	
		
	}
}