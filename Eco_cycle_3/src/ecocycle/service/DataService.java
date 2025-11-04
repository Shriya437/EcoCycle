// In file: src/ecocycle/service/DataService.java
package ecocycle.service;

import ecocycle.model.*;

import java.util.*; 
// We no longer need java.util.stream.Collectors

/**
 * Mock Data Service (simulates a DAO/Repository).
 * This version uses traditional for-loops and classic data structures
 * instead of Java Streams, as requested for a college-level project.
 */
public class DataService {

    // --- Mock Database Tables (from your console app) ---
    private static final List<User> users = new ArrayList<>();
    private static final List<Product> products = new ArrayList<>();
    private static final List<Review> allReviews = new LinkedList<>(); // Use LinkedList for addFirst
    private static final List<Transaction> transactions = new ArrayList<>();
    private static final Map<String, List<String>> buyerCarts = new HashMap<>();

    // --- Session Management ---
    private static User currentUser = null;

    // --- Simulation Constants (from your console app) ---
    private static final long MINUTE_MS = 60_000;
    private static final Map<String, Long> RECYCLING_THRESHOLDS_MS = new HashMap<>();
    private static final long DEFAULT_RECYCLING_THRESHOLD_MS = 3 * MINUTE_MS;

    /**
     * Static block to initialize mock data (replaces initializeDemoData()).
     */
    static {
        // Init time thresholds
        RECYCLING_THRESHOLDS_MS.put("clothing", 1 * MINUTE_MS);
        RECYCLING_THRESHOLDS_MS.put("electronics", 2 * MINUTE_MS);
        RECYCLING_THRESHOLDS_MS.put("plastic", 1 * MINUTE_MS);
        RECYCLING_THRESHOLDS_MS.put("furniture", 3 * MINUTE_MS);

        // Create sample users
        User seller1 = new User("u_s1", "seller_A", "pass", Role.SELLER);
        User buyer1 = new User("u_b1", "buyer_X", "pass", Role.BUYER);
        User recycler1 = new User("u_r1", "recycler_Z", "pass", Role.RECYCLER);
        users.addAll(Arrays.asList(seller1, buyer1, recycler1));
        
        // Init cart/transactions for users
        buyerCarts.put(buyer1.getUserId(), new ArrayList<>());

        // Create sample products for the seller (FIXED CONSTRUCTOR)
        Product p1 = new Product("p_01", "Old Laptop", "Electronics", "Electronics", 15000.00, "5yr old laptop", "u_s1");
        Product p2 = new Product("p_02", "Vintage Jeans", "Clothing", "Clothing", 2500.00, "90s denim", "u_s1");
        Product p3 = new Product("p_03", "Plastic Toys", "Plastic", "Plastic", 500.00, "Bag of toys", "u_s1");
        
        // Set statuses and timestamps
        p3.setStatus(ProductStatus.SOLD); 
        long now = System.currentTimeMillis();
        p2.setUploadTimestamp(now - (RECYCLING_THRESHOLDS_MS.get("clothing") + 5000));
        
        products.addAll(Arrays.asList(p1, p2, p3));
    }

    /**
     * Returns the set of valid, known recycling categories.
     * This is used to populate the ComboBox in the AddProduct view.
     * @return A Set of category names (e.g., "clothing", "electronics").
     */
    public static Set<String> getRecyclingCategories() {
        return RECYCLING_THRESHOLDS_MS.keySet();
    }
    
    // --- Helper Methods (No Streams) ---

    private static User findUserByUsername(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }
    
    public static User findUserById(String userId) {
        for (User user : users) {
            if (user.getUserId().equals(userId)) {
                return user;
            }
        }
        return null;
    }
    
    public static Product findProductById(String productId) {
        for (Product product : products) {
            if (product.getProductId().equals(productId)) {
                return product;
            }
        }
        return null;
    }

    // --- User & Session Management ---
    
    public static User login(String username, String password) {
        User user = findUserByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            currentUser = user;
            return currentUser;
        }
        currentUser = null;
        return null;
    }

    public static void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean register(String username, String password, Role role) {
        if (findUserByUsername(username) != null) {
            return false;
        }
        String userId = "u_" + (users.size() + 1);
        User newUser = new User(userId, username, password, role);
        users.add(newUser);
        
        if (role == Role.BUYER) {
            buyerCarts.put(userId, new ArrayList<>());
        }
        return true;
    }

    // --- Seller Logic (No Streams) ---

    public static List<Product> getProductsForCurrentUser() {
        List<Product> userProducts = new ArrayList<>();
        if (currentUser == null) return userProducts;
        
        for (Product p : products) {
            if (p.getSellerId().equals(currentUser.getUserId())) {
                userProducts.add(p);
            }
        }
        return userProducts;
    }

    public static void addProduct(String name, String type, String category, double price, String description) {
        if (currentUser == null) return;
        String productId = "p_" + (products.size() + 1);
        
        Product newProduct = new Product(productId, name, type, category, price, description, currentUser.getUserId());
        products.add(newProduct);
    }
    
    public static void updateProduct(String productId, String newDesc, double newPrice) {
         Product product = findProductById(productId);
         if (product != null) {
             if (product.getSellerId().equals(currentUser.getUserId())) {
                 product.setDescription(newDesc);
                 product.setPrice(newPrice);
             }
         }
    }

    public static boolean deleteProduct(String productId) {
        Product p = findProductById(productId);
        if (p == null || !p.getSellerId().equals(currentUser.getUserId())) {
            return false;
        }
        
        if (p.getStatus() == ProductStatus.AVAILABLE || p.getStatus() == ProductStatus.AVAILABLE_NO_RECYCLE) {
            products.remove(p);
            return true;
        }
        return false;
    }

    private static boolean isProductEligibleForRecycling(Product p) {
        if (p == null || p.getStatus() != ProductStatus.AVAILABLE) {
            return false;
        }
        long threshold = RECYCLING_THRESHOLDS_MS.getOrDefault(p.getCategory(), DEFAULT_RECYCLING_THRESHOLD_MS);
        return (System.currentTimeMillis() - p.getUploadTimestamp()) > threshold;
    }

    public static List<Product> getEligibleProductsForApproval() {
        List<Product> eligible = new ArrayList<>();
        if (currentUser == null) return eligible;
        
        for (Product p : products) {
            if (p.getSellerId().equals(currentUser.getUserId()) && isProductEligibleForRecycling(p)) {
                eligible.add(p);
            }
        }
        return eligible;
    }

    public static void updateProductRecyclingStatus(String productId, ProductStatus status) {
        Product p = findProductById(productId);
        if (p != null) {
            p.setStatus(status);
        }
    }
    
    public static List<Product> getBiddableProductsForSeller() {
        List<Product> biddable = new ArrayList<>();
        if (currentUser == null) return biddable;
        
        for (Product p : products) {
            if (p.getSellerId().equals(currentUser.getUserId()) &&
                p.getStatus() == ProductStatus.PENDING_RECYCLING &&
                !p.getBids().isEmpty()) {
                biddable.add(p);
            }
        }
        return biddable;
    }

    /**
     * THIS METHOD IS NOW FIXED (from previous step)
     * It now updates seller sales and returns the winning bid.
     */
    public static RecyclingBid acceptBid(String productId) {
        Product p = findProductById(productId);
        if (p != null) {
            if (p.getSellerId().equals(currentUser.getUserId()) && !p.getBids().isEmpty()) {
                
                // 1. Get the winning bid *without* removing it
                RecyclingBid winningBid = p.getBids().peek();
                
                // 2. Find the seller
                User seller = findUserById(p.getSellerId());
                if (seller != null) {
                    // 3. Add the bid price to the seller's total sales
                    seller.setTotalSales(seller.getTotalSales() + winningBid.bidPrice());
                }
                
                // 4. Now remove the bid
                p.getBids().poll(); 
                
                // 5. Update status
                p.setStatus(ProductStatus.RECYCLING_PURCHASED);
                
                // 6. Return the bid so the controller can show who won
                return winningBid; 
            }
        }
        return null; // Return null on failure
    }

    // --- Buyer Logic (No Streams) ---

    public static List<Product> getAvailableProducts() {
        List<Product> available = new ArrayList<>();
        for (Product p : products) {
            if (p.getStatus() == ProductStatus.AVAILABLE || p.getStatus() == ProductStatus.AVAILABLE_NO_RECYCLE) {
                available.add(p);
            }
        }
        return available;
    }

    public static boolean addToCart(String productId) {
        Product p = findProductById(productId);
        if (p == null || currentUser == null || currentUser.getRole() != Role.BUYER) {
            return false;
        }
        
        if (p.getStatus() == ProductStatus.AVAILABLE || p.getStatus() == ProductStatus.AVAILABLE_NO_RECYCLE) {
            buyerCarts.get(currentUser.getUserId()).add(productId);
            return true;
        }
        return false;
    }
    
    public static List<Product> getCart() {
        List<Product> cartProducts = new ArrayList<>();
        if (currentUser == null || currentUser.getRole() != Role.BUYER) return cartProducts;
        
        List<String> productIds = buyerCarts.get(currentUser.getUserId());
        for (String pid : productIds) {
            Product p = findProductById(pid);
            if (p != null) {
                cartProducts.add(p);
            }
        }
        return cartProducts;
    }
    
    public static boolean purchaseCart() {
        if (currentUser == null) return false;
        List<String> cartItems = new ArrayList<>(buyerCarts.get(currentUser.getUserId()));
        
        for (String pid : cartItems) {
            Product p = findProductById(pid);
            if (p != null) {
                if (p.getStatus() == ProductStatus.AVAILABLE || p.getStatus() == ProductStatus.AVAILABLE_NO_RECYCLE) {
                    p.setStatus(ProductStatus.SOLD);
                    
                    User seller = findUserById(p.getSellerId());
                    if (seller != null) {
                        seller.setTotalSales(seller.getTotalSales() + p.getPrice());
                    }
                    
                    String tid = "t_" + (transactions.size() + 1);
                    // Use the 'new Transaction()' constructor
                    transactions.add(new Transaction(tid, currentUser.getUserId(), pid, p.getPrice(), 
                                     System.currentTimeMillis(), TransactionStatus.COMPLETED));
                }
            }
        }
        buyerCarts.get(currentUser.getUserId()).clear();
        return true;
    }
    
    /**
     * THIS METHOD IS NOW FIXED (from record to class)
     * Gets all transactions for the current buyer using a for-loop.
     */
    public static List<Transaction> getTransactionsForBuyer() {
        List<Transaction> userTransactions = new ArrayList<>();
        if (currentUser == null || currentUser.getRole() != Role.BUYER) {
            return userTransactions;
        }
        
        for (Transaction t : transactions) {
            // FIX: Changed t.buyerId() to t.getBuyerId()
            if (t.getBuyerId().equals(currentUser.getUserId())) {
                userTransactions.add(t);
            }
        }
        
        // Sort by newest first
        Collections.sort(userTransactions, new Comparator<Transaction>() {
            @Override
            public int compare(Transaction t1, Transaction t2) {
                // FIX: Changed t.timestamp() to t.getTimestamp()
                return Long.compare(t2.getTimestamp(), t1.getTimestamp());
            }
        });
        
        return userTransactions;
    }

    // --- Recycler Logic (No Streams) ---

    public static List<Product> getEligibleProductsForBidding() {
        List<Product> eligible = new ArrayList<>();
        for (Product p : products) {
            if (p.getStatus() == ProductStatus.PENDING_RECYCLING) {
                eligible.add(p);
            }
        }
        return eligible;
    }
    
    public static boolean placeBid(String productId, double bidPrice) {
        if (currentUser == null || currentUser.getRole() != Role.RECYCLER) return false;
        
        Product p = findProductById(productId);
        if (p == null) return false;
        
        if (p.getStatus() != ProductStatus.PENDING_RECYCLING) return false;
        
        if (bidPrice >= p.getBaseCost()) {
            p.getBids().add(new RecyclingBid(currentUser.getUserId(), bidPrice));
            return true;
        }
        return false;
    }
    
    public static List<Product> getAcquiredProducts() {
        List<Product> acquired = new ArrayList<>();
        if (currentUser == null) return acquired;

        for (Product p : products) {
            if (p.getStatus() == ProductStatus.RECYCLING_PURCHASED) {
                acquired.add(p);
            }
        }
        return acquired;
    }
    
    public static boolean submitRecyclingProof(String productId) {
        if (currentUser == null) return false;
        
        Product p = findProductById(productId);
        if (p == null) return false;
        
        if (p.getStatus() != ProductStatus.RECYCLING_PURCHASED) {
            return false;
        }
        
        p.setStatus(ProductStatus.RECYCLED);
        
        int totalCredits = p.getCarbonValue();
        double recyclerShare = totalCredits * 0.70;
        double sellerShare = totalCredits * 0.30;
        
        currentUser.setCarbonCredits(currentUser.getCarbonCredits() + recyclerShare);
        
        User seller = findUserById(p.getSellerId());
        if (seller != null) {
            seller.setCarbonCredits(seller.getCarbonCredits() + sellerShare);
        }
            
        return true;
    }
    
    // --- Shared Logic (No Streams) ---

    public static List<User> getSellerLeaderboard() {
        List<User> sellers = new ArrayList<>();
        for (User u : users) {
            if (u.getRole() == Role.SELLER) {
                sellers.add(u);
            }
        }
        
        Collections.sort(sellers, new Comparator<User>() {
            @Override
            public int compare(User u1, User u2) {
                return Double.compare(u2.getTotalSales(), u1.getTotalSales());
            }
        });
        
        return sellers;
    }
    
    public static List<User> getRecyclerLeaderboard() {
        List<User> recyclers = new ArrayList<>();
        for (User u : users) {
            if (u.getRole() == Role.RECYCLER) {
                recyclers.add(u);
            }
        }
        
        Collections.sort(recyclers, new Comparator<User>() {
            @Override
            public int compare(User u1, User u2) {
                return Double.compare(u2.getCarbonCredits(), u1.getCarbonCredits());
            }
        });
        
        return recyclers;
    }
}