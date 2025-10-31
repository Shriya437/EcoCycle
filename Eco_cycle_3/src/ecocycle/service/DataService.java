// In file: src/ecocycle/service/DataService.java
package ecocycle.service;

import ecocycle.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Mock Data Service (simulates a DAO/Repository).
 * This class ports ALL logic from the console application.
 * It simulates a database using static Lists and Maps.
 * It also manages the session for the currently logged-in user.
 *
 * LATER, YOU CAN REPLACE THE LOGIC IN THESE METHODS WITH YOUR JDBC QUERIES.
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

        // Create sample products for the seller
        
        // --- THIS IS THE FIX (in the static block) ---
        
        // p1 is AVAILABLE by default, so the 7-arg constructor is perfect.
        Product p1 = new Product("p_01", "Old Laptop", "Electronics", "Electronics", 15000.00, "5yr old laptop", "u_s1");
        
        // p2 is also AVAILABLE by default.
        Product p2 = new Product("p_02", "Vintage Jeans", "Clothing", "Clothing", 2500.00, "90s denim", "u_s1");
        
        // p3 needs to be SOLD. We create it, then set its status.
        Product p3 = new Product("p_03", "Plastic Toys", "Plastic", "Plastic", 500.00, "Bag of toys", "u_s1");
        p3.setStatus(ProductStatus.SOLD); // Set status after creation
        
        // Make p2 eligible for recycling demo
        long now = System.currentTimeMillis();
        p2.setUploadTimestamp(now - (RECYCLING_THRESHOLDS_MS.get("clothing") + 5000));
        // p3's timestamp doesn't need to be modified since it's already sold

        // Now add the products to the list
        products.addAll(Arrays.asList(p1, p2, p3));
    }
    
    // --- Helper Methods to replace Maps ---
    private static Optional<User> findUserByUsername(String username) {
        return users.stream().filter(u -> u.getUsername().equals(username)).findFirst();
    }
    
    public static Optional<User> findUserById(String userId) {
        return users.stream().filter(u -> u.getUserId().equals(userId)).findFirst();
    }
    
    public static Optional<Product> findProductById(String productId) {
        return products.stream().filter(p -> p.getProductId().equals(productId)).findFirst();
    }

    // --- User & Session Management ---
    
    public static User login(String username, String password) {
        Optional<User> userOpt = findUserByUsername(username);
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            currentUser = userOpt.get();
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
        if (findUserByUsername(username).isPresent()) {
            return false; // Username taken
        }
        String userId = "u_" + (users.size() + 1);
        User newUser = new User(userId, username, password, role);
        users.add(newUser);
        
        // Init lists for new users
        if (role == Role.BUYER) {
            buyerCarts.put(userId, new ArrayList<>());
        }
        return true;
    }

    // --- Seller Logic ---

    public static List<Product> getProductsForCurrentUser() {
        if (currentUser == null) return new ArrayList<>();
        return products.stream()
                .filter(p -> p.getSellerId().equals(currentUser.getUserId()))
                .collect(Collectors.toList());
    }

    /**
     * Adds a new product for the current user.
     */
    public static void addProduct(String name, String type, String category, double price, String description) {
        if (currentUser == null) {
            return; // Can't add if not logged in
        }

        String productId = "p" + (products.size() + 1);
        
        // --- THIS IS THE FIX (in addProduct) ---
        // Call the 7-argument constructor
        Product newProduct = new Product(productId, name, type, category, price, description,
                currentUser.getUserId()
        );
        // The status is already set to AVAILABLE by default inside the constructor
        
        products.add(newProduct);
    }
    
    public static void updateProduct(String productId, String newDesc, double newPrice) {
         findProductById(productId).ifPresent(product -> {
             if (product.getSellerId().equals(currentUser.getUserId())) {
                 product.setDescription(newDesc);
                 product.setPrice(newPrice);
             }
         });
    }

    public static boolean deleteProduct(String productId) {
        Optional<Product> prodOpt = findProductById(productId);
        if (prodOpt.isEmpty() || !prodOpt.get().getSellerId().equals(currentUser.getUserId())) {
            return false;
        }
        Product p = prodOpt.get();
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
        if (currentUser == null) return new ArrayList<>();
        return products.stream()
            .filter(p -> p.getSellerId().equals(currentUser.getUserId()) && isProductEligibleForRecycling(p))
            .collect(Collectors.toList());
    }

    public static void updateProductRecyclingStatus(String productId, ProductStatus status) {
        findProductById(productId).ifPresent(p -> p.setStatus(status));
    }
    
    public static List<Product> getBiddableProductsForSeller() {
        if (currentUser == null) return new ArrayList<>();
        return products.stream()
            .filter(p -> p.getSellerId().equals(currentUser.getUserId()) &&
                         p.getStatus() == ProductStatus.PENDING_RECYCLING &&
                         !p.getBids().isEmpty())
            .collect(Collectors.toList());
    }

    public static void acceptBid(String productId) {
        findProductById(productId).ifPresent(p -> {
            if (p.getSellerId().equals(currentUser.getUserId()) && !p.getBids().isEmpty()) {
                p.getBids().poll(); 
                p.setStatus(ProductStatus.RECYCLING_PURCHASED);
            }
        });
    }

    // --- Buyer Logic ---

    public static List<Product> getAvailableProducts() {
        return products.stream()
            .filter(p -> p.getStatus() == ProductStatus.AVAILABLE || p.getStatus() == ProductStatus.AVAILABLE_NO_RECYCLE)
            .collect(Collectors.toList());
    }

    public static boolean addToCart(String productId) {
        Optional<Product> prodOpt = findProductById(productId);
        if (prodOpt.isEmpty() || currentUser == null || currentUser.getRole() != Role.BUYER) {
            return false;
        }
        Product p = prodOpt.get();
        if (p.getStatus() == ProductStatus.AVAILABLE || p.getStatus() == ProductStatus.AVAILABLE_NO_RECYCLE) {
            buyerCarts.get(currentUser.getUserId()).add(productId);
            return true;
        }
        return false;
    }
    
    public static List<Product> getCart() {
        if (currentUser == null || currentUser.getRole() != Role.BUYER) return new ArrayList<>();
        
        List<String> productIds = buyerCarts.get(currentUser.getUserId());
        return productIds.stream()
                .map(DataService::findProductById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
    
    public static boolean purchaseCart() {
        if (currentUser == null) return false;
        List<String> cartItems = new ArrayList<>(buyerCarts.get(currentUser.getUserId()));
        
        for (String pid : cartItems) {
            Optional<Product> prodOpt = findProductById(pid);
            if (prodOpt.isPresent()) {
                Product p = prodOpt.get();
                if (p.getStatus() == ProductStatus.AVAILABLE || p.getStatus() == ProductStatus.AVAILABLE_NO_RECYCLE) {
                    p.setStatus(ProductStatus.SOLD);
                    
                    findUserById(p.getSellerId()).ifPresent(seller -> 
                        seller.setTotalSales(seller.getTotalSales() + p.getPrice()));
                    
                    String tid = "t_" + (transactions.size() + 1);
                    transactions.add(new Transaction(tid, currentUser.getUserId(), pid, p.getPrice(), 
                                     System.currentTimeMillis(), TransactionStatus.COMPLETED));
                }
            }
        }
        buyerCarts.get(currentUser.getUserId()).clear();
        return true;
    }

    // --- Recycler Logic ---

    public static List<Product> getEligibleProductsForBidding() {
        return products.stream()
            .filter(p -> p.getStatus() == ProductStatus.PENDING_RECYCLING)
            .collect(Collectors.toList());
    }
    
    public static boolean placeBid(String productId, double bidPrice) {
        if (currentUser == null || currentUser.getRole() != Role.RECYCLER) return false;
        
        Optional<Product> prodOpt = findProductById(productId);
        if (prodOpt.isEmpty()) return false;
        
        Product p = prodOpt.get();
        if (p.getStatus() != ProductStatus.PENDING_RECYCLING) return false;
        
        if (bidPrice >= p.getBaseCost()) {
            p.getBids().add(new RecyclingBid(currentUser.getUserId(), bidPrice));
            return true;
        }
        return false;
    }
    
    public static List<Product> getAcquiredProducts() {
        if (currentUser == null) return new ArrayList<>();
        return products.stream()
            .filter(p -> p.getStatus() == ProductStatus.RECYCLING_PURCHASED)
            .collect(Collectors.toList());
    }
    
    public static boolean submitRecyclingProof(String productId) {
        if (currentUser == null) return false;
        
        Optional<Product> prodOpt = findProductById(productId);
        if (prodOpt.isEmpty()) return false;
        
        Product p = prodOpt.get();
        if (p.getStatus() != ProductStatus.RECYCLING_PURCHASED) {
            return false;
        }
        
        p.setStatus(ProductStatus.RECYCLED);
        
        int totalCredits = p.getCarbonValue();
        double recyclerShare = totalCredits * 0.70;
        double sellerShare = totalCredits * 0.30;
        
        currentUser.setCarbonCredits(currentUser.getCarbonCredits() + recyclerShare);
        findUserById(p.getSellerId()).ifPresent(seller -> 
            seller.setCarbonCredits(seller.getCarbonCredits() + sellerShare));
            
        return true;
    }
    
    // --- Shared Logic ---

    public static List<User> getSellerLeaderboard() {
        return users.stream()
            .filter(u -> u.getRole() == Role.SELLER)
            .sorted(Comparator.comparingDouble(User::getTotalSales).reversed())
            .collect(Collectors.toList());
    }
    
    public static List<User> getRecyclerLeaderboard() {
        return users.stream()
            .filter(u -> u.getRole() == Role.RECYCLER)
            .sorted(Comparator.comparingDouble(User::getCarbonCredits).reversed())
            .collect(Collectors.toList());
    }


}