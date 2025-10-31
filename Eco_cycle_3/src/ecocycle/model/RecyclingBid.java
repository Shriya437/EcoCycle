
package ecocycle.model;

/**
 * A record to represent a single recycling bid.
 * Records are simple, immutable data carriers.
 */
public record RecyclingBid(String recyclerId, double bidPrice) implements Comparable<RecyclingBid> {
    
    // This allows it to be used in a PriorityQueue
    @Override
    public int compareTo(RecyclingBid other) {
        return Double.compare(other.bidPrice, this.bidPrice); // Max-heap
    }
}