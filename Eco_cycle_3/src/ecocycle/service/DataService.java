// In file: src/ecocycle/service/DataService.java
package ecocycle.service;

import ecocycle.model.*;
import ecocycle.util.DBConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Data Service (JDBC Version)
 * This class now handles all database operations, replacing the
 * in-memory lists with SQL queries.
 *
 * All public method signatures remain the same, so no
 * controllers need to be changed.
 */
public class DataService {

    // --- All in-memory lists are now GONE ---
    // private static final List<User> users = new ArrayList<>();
    // private static final List<Product> products = new ArrayList<>();
    // ...etc...

    // --- Session Management (Stays the same) ---
    private static User currentUser = null;

    // --- Simulation Constants (Stays the same) ---
    private static final long MINUTE_MS = 60_000;
    private static final Map<String, Long> RECYCLING_THRESHOLDS_MS = new HashMap<>();
    private static final long DEFAULT_RECYCLING_THRESHOLD_MS = 3 * MINUTE_MS;

    /**
     * Static block to initialize mock data AND recycling rules
     */
    static {
        // Init time thresholds (still in-memory, that's fine)
        RECYCLING_THRESHOLDS_MS.put("clothing", 1 * MINUTE_MS);
        RECYCLING_THRESHOLDS_MS.put("electronics", 2 * MINUTE_MS);
        RECYCLING_THRESHOLDS_MS.put("plastic", 1 * MINUTE_MS);
        RECYCLING_THRESHOLDS_MS.put("furniture", 3 * MINUTE_MS);
        
        // This method will populate the DB with demo data (if it's empty)
        initializeDemoData();
    }

    /**
     * Helper method to insert demo data into the database ONCE.
     */
    private static void initializeDemoData() {
        String sqlCheck = "SELECT 1 FROM users LIMIT 1";
        
        try (Connection con = DBConnector.getConnection();
             PreparedStatement checkStmt = con.prepareStatement(sqlCheck);
             ResultSet rs = checkStmt.executeQuery()) {

            if (rs.next()) {
                System.out.println("Database already contains data. Skipping demo data insertion.");
                return; // Data already exists
            }

            System.out.println("Database is empty. Inserting demo data...");
            
            // Use Statement for batch insertion
            try (Statement stmt = con.createStatement()) {
                // 1. Create Users
                stmt.executeUpdate("INSERT INTO users (user_id, username, password, role) VALUES ('u_s1', 'seller_A', 'pass', 'SELLER')");
                stmt.executeUpdate("INSERT INTO users (user_id, username, password, role) VALUES ('u_b1', 'buyer_X', 'pass', 'BUYER')");
                stmt.executeUpdate("INSERT INTO users (user_id, username, password, role) VALUES ('u_r1', 'recycler_Z', 'pass', 'RECYCLER')");
                
                // 2. Create Products
                long p2Time = System.currentTimeMillis() - (RECYCLING_THRESHOLDS_MS.get("clothing") + 5000);
                
                stmt.executeUpdate("INSERT INTO products (product_id, name, type, category, description, price, seller_id, status) VALUES " +
                    "('p_01', 'Old Laptop', 'Electronics', 'Electronics', 15000.00, '5yr old laptop', 'u_s1', 'AVAILABLE')");
                
                // We must use PreparedStatement for precise timestamp insertion
                String p2Sql = "INSERT INTO products (product_id, name, type, category, description, price, seller_id, status, upload_timestamp) VALUES " +
                               "('p_02', 'Vintage Jeans', 'Clothing', 'Clothing', 2500.00, '90s denim', 'u_s1', 'AVAILABLE', ?)";
                try (PreparedStatement p2Stmt = con.prepareStatement(p2Sql)) {
                    p2Stmt.setTimestamp(1, new java.sql.Timestamp(p2Time));
                    p2Stmt.executeUpdate();
                }

                stmt.executeUpdate("INSERT INTO products (product_id, name, type, category, description, price, seller_id, status) VALUES " +
                    "('p_03', 'Plastic Toys', 'Plastic', 'Plastic', 500.00, 'Bag of toys', 'u_s1', 'SOLD')");
                
                System.out.println("Demo data inserted successfully.");
            }
            
        } catch (SQLException e) {
            System.err.println("Error during demo data initialization.");
            e.printStackTrace();
        }
    }

    // --- NEW HELPER METHODS (for JDBC) ---
    
    /**
     * Creates a User object from a single row in a ResultSet.
     */
    private static User inflateUser(ResultSet rs) throws SQLException {
        User user = new User(
            rs.getString("user_id"),
            rs.getString("username"),
            rs.getString("password"),
            Role.valueOf(rs.getString("role"))
        );
        user.setCarbonCredits(rs.getDouble("carbon_credits"));
        user.setTotalSales(rs.getDouble("total_sales"));
        return user;
    }

    /**
     * Creates a Product object from a ResultSet.
     * This method ALSO fetches and inflates the product's bids.
     */
    private static Product inflateProduct(ResultSet rs) throws SQLException {
        String productId = rs.getString("product_id");
        Product product = new Product(
            productId,
            rs.getString("name"),
            rs.getString("type"),
            rs.getString("category"),
            rs.getDouble("price"),
            rs.getString("description"),
            rs.getString("seller_id")
        );
        product.setStatus(ProductStatus.valueOf(rs.getString("status")));
        product.setUploadTimestamp(rs.getTimestamp("upload_timestamp").getTime());
        
        // --- SEAMLESS INTEGRATION ---
        // The controllers expect the Product's bid list to be populated.
        // We will fetch them from the `recycling_bids` table.
        String bidSql = "SELECT * FROM recycling_bids WHERE product_id = ?";
        try (Connection con = DBConnector.getConnection();
             PreparedStatement bidStmt = con.prepareStatement(bidSql)) {
            
            bidStmt.setString(1, productId);
            try (ResultSet bidRs = bidStmt.executeQuery()) {
                while (bidRs.next()) {
                    RecyclingBid bid = new RecyclingBid(
                        bidRs.getString("recycler_id"),
                        bidRs.getDouble("bid_price")
                    );
                    product.getBids().add(bid); // Add to the PriorityQueue
                }
            }
        } // The product object is now "fully hydrated"
        
        return product;
    }

    // --- Public Getters (for UI ComboBox) ---
    public static Set<String> getRecyclingCategories() {
        return RECYCLING_THRESHOLDS_MS.keySet();
    }
    
    // --- Helper Methods (JDBC Version) ---

    private static User findUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection con = DBConnector.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return inflateUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static User findUserById(String userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection con = DBConnector.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return inflateUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static Product findProductById(String productId) {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        try (Connection con = DBConnector.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setString(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return inflateProduct(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- User & Session Management (JDBC) ---
    
    public static User login(String username, String password) {
        // Find user by username
        User user = findUserByUsername(username);
        
        // Check password (in a real app, use hashing!)
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
            return false; // Username taken
        }
        String userId = "u_" + UUID.randomUUID().toString().substring(0, 8);
        
        String sql = "INSERT INTO users (user_id, username, password, role) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnector.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            pstmt.setString(2, username);
            pstmt.setString(3, password);
            pstmt.setString(4, role.name()); // e.g., "SELLER"
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- Seller Logic (JDBC) ---

    public static List<Product> getProductsForCurrentUser() {
        List<Product> userProducts = new ArrayList<>();
        if (currentUser == null) return userProducts;
        
        String sql = "SELECT * FROM products WHERE seller_id = ?";
        try (Connection con = DBConnector.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setString(1, currentUser.getUserId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    userProducts.add(inflateProduct(rs)); // Inflate product + its bids
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userProducts;
    }

    public static void addProduct(String name, String type, String category, double price, String description) {
        if (currentUser == null) return;
        String productId = "p_" + UUID.randomUUID().toString().substring(0, 8);
        
        String sql = "INSERT INTO products (product_id, name, type, category, price, description, seller_id, status, upload_timestamp) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        
        try (Connection con = DBConnector.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setString(1, productId);
            pstmt.setString(2, name);
            pstmt.setString(3, type);
            pstmt.setString(4, category);
            pstmt.setDouble(5, price);
            pstmt.setString(6, description);
            pstmt.setString(7, currentUser.getUserId());
            pstmt.setString(8, ProductStatus.AVAILABLE.name());
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static boolean deleteProduct(String productId) {
        String sql = "DELETE FROM products WHERE product_id = ? AND seller_id = ? AND (status = 'AVAILABLE' OR status = 'AVAILABLE_NO_RECYCLE')";
        
        try (Connection con = DBConnector.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setString(1, productId);
            pstmt.setString(2, currentUser.getUserId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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
        
        // 1. Get all of the seller's available products
        String sql = "SELECT * FROM products WHERE seller_id = ? AND status = 'AVAILABLE'";
        try (Connection con = DBConnector.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setString(1, currentUser.getUserId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // 2. Inflate the product (which gets its bids, though likely 0)
                    Product p = inflateProduct(rs);
                    // 3. Check the time-based logic in Java
                    if (isProductEligibleForRecycling(p)) {
                        eligible.add(p);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return eligible;
    }

    public static void updateProductRecyclingStatus(String productId, ProductStatus status) {
        String sql = "UPDATE products SET status = ? WHERE product_id = ?";
        try (Connection con = DBConnector.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setString(1, status.name());
            pstmt.setString(2, productId);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static List<Product> getBiddableProductsForSeller() {
        List<Product> biddable = new ArrayList<>();
        if (currentUser == null) return biddable;
        
        // SQL query to get only products that have at least one bid
        String sql = "SELECT p.* FROM products p " +
                     "WHERE p.seller_id = ? AND p.status = 'PENDING_RECYCLING' " +
                     "AND p.product_id IN (SELECT DISTINCT rb.product_id FROM recycling_bids rb)";
        
        try (Connection con = DBConnector.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setString(1, currentUser.getUserId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    biddable.add(inflateProduct(rs)); // Inflates product + bids
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return biddable;
    }

    public static RecyclingBid acceptBid(String productId) {
        String findBidSql = "SELECT * FROM recycling_bids WHERE product_id = ? ORDER BY bid_price DESC LIMIT 1";
        
        Connection con = null;
        try {
            con = DBConnector.getConnection();
            con.setAutoCommit(false); // Start transaction

            // 1. Find the highest bid
            RecyclingBid winningBid = null;
            Product p = findProductById(productId); // Get product to find seller
            if (p == null) throw new SQLException("Product not found.");

            try (PreparedStatement findBidStmt = con.prepareStatement(findBidSql)) {
                findBidStmt.setString(1, productId);
                try (ResultSet rs = findBidStmt.executeQuery()) {
                    if (rs.next()) {
                        winningBid = new RecyclingBid(
                            rs.getString("recycler_id"),
                            rs.getDouble("bid_price")
                        );
                    }
                }
            }
            if (winningBid == null) throw new SQLException("No winning bid found.");

            // 2. Update seller's sales
            String updateSellerSql = "UPDATE users SET total_sales = total_sales + ? WHERE user_id = ?";
            try (PreparedStatement updateSellerStmt = con.prepareStatement(updateSellerSql)) {
                updateSellerStmt.setDouble(1, winningBid.bidPrice());
                updateSellerStmt.setString(2, p.getSellerId());
                updateSellerStmt.executeUpdate();
            }

            // 3. Update product status
            String updateProductSql = "UPDATE products SET status = 'RECYCLING_PURCHASED' WHERE product_id = ?";
            try (PreparedStatement updateProductStmt = con.prepareStatement(updateProductSql)) {
                updateProductStmt.setString(1, productId);
                updateProductStmt.executeUpdate();
            }
            
            // 4. Clear ALL bids for this product
            String deleteBidsSql = "DELETE FROM recycling_bids WHERE product_id = ?";
            try (PreparedStatement deleteBidsStmt = con.prepareStatement(deleteBidsSql)) {
                deleteBidsStmt.setString(1, productId);
                deleteBidsStmt.executeUpdate();
            }

            con.commit(); // Commit all changes
            return winningBid;

        } catch (SQLException e) {
            e.printStackTrace();
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return null; // Return null on failure
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    // --- Buyer Logic (JDBC) ---

 // PASTE THESE 2 METHODS INTO: src/ecocycle/service/DataService.java
 // You can replace your old getAvailableProducts() method with these.

 /**
  * Gets all available products. This is the original method
  * that the controller's initialize() method can call.
  */
 public static List<Product> getAvailableProducts() {
     // Call the new, powerful method with default values (no filter, newest first)
     return getAvailableProducts("All", 0, Double.MAX_VALUE, false);
 }

 /**
  * NEW: Gets available products with filtering and sorting.
  * @param category The category to filter by (or "All")
  * @param minPrice The minimum price (or 0)
  * @param maxPrice The maximum price (or Double.MAX_VALUE)
  * @param sortByPriceAsc True to sort by price (low-high), False to sort by date (newest)
  * @return A filtered and sorted list of products.
  */
 public static List<Product> getAvailableProducts(String category, double minPrice, double maxPrice, boolean sortByPriceAsc) {
     List<Product> available = new ArrayList<>();
     
     // 1. Build a dynamic SQL query
     // We use StringBuilder to safely add conditions.
     StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE (status = 'AVAILABLE' OR status = 'AVAILABLE_NO_RECYCLE') ");
     
     // This list will hold the parameters for our PreparedStatement
     List<Object> params = new ArrayList<>();

     // Add category filter if it's not "All"
     if (category != null && !category.isEmpty() && !category.equals("All")) {
         sql.append("AND category = ? ");
         params.add(category);
     }

     // Add min price filter
     if (minPrice > 0) {
         sql.append("AND price >= ? ");
         params.add(minPrice);
     }

     // Add max price filter
     if (maxPrice > 0 && maxPrice != Double.MAX_VALUE) {
         sql.append("AND price <= ? ");
         params.add(maxPrice);
     }

     // 2. Add sorting
     if (sortByPriceAsc) {
         sql.append("ORDER BY price ASC");
     } else {
         sql.append("ORDER BY upload_timestamp DESC");
     }

     // 3. Execute the query
     try (Connection con = DBConnector.getConnection();
          PreparedStatement pstmt = con.prepareStatement(sql.toString())) {

         // 4. Safely set all the parameters we added
         for (int i = 0; i < params.size(); i++) {
             // Use setObject, which works for both String and Double
             pstmt.setObject(i + 1, params.get(i));
         }
         
         try (ResultSet rs = pstmt.executeQuery()) {
             while (rs.next()) {
                 // The inflateProduct helper method does all the hard work!
                 available.add(inflateProduct(rs));
             }
         }
     } catch (SQLException e) {
         e.printStackTrace();
     }
     return available;
 }

    public static boolean addToCart(String productId) {
        Product p = findProductById(productId);
        if (p == null || currentUser == null || currentUser.getRole() != Role.BUYER) {
            return false;
        }
        
        if (p.getStatus() == ProductStatus.AVAILABLE || p.getStatus() == ProductStatus.AVAILABLE_NO_RECYCLE) {
            String sql = "INSERT INTO cart (buyer_id, product_id) VALUES (?, ?)";
            try (Connection con = DBConnector.getConnection();
                 PreparedStatement pstmt = con.prepareStatement(sql)) {
                
                pstmt.setString(1, currentUser.getUserId());
                pstmt.setString(2, productId);
                pstmt.executeUpdate();
                return true;
                
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
    
    public static List<Product> getCart() {
        List<Product> cartProducts = new ArrayList<>();
        if (currentUser == null || currentUser.getRole() != Role.BUYER) return cartProducts;
        
        String sql = "SELECT p.* FROM products p JOIN cart c ON p.product_id = c.product_id WHERE c.buyer_id = ?";
        
        try (Connection con = DBConnector.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setString(1, currentUser.getUserId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    cartProducts.add(inflateProduct(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cartProducts;
    }
    
    public static boolean purchaseCart() {
        if (currentUser == null) return false;
        
        // 1. Get cart items first
        List<Product> cartItems = getCart();
        if (cartItems.isEmpty()) return true; // Nothing to purchase
        
        Connection con = null;
        try {
            con = DBConnector.getConnection();
            con.setAutoCommit(false); // Start transaction

            // 2. Prepare all SQL statements
            String updateProdSql = "UPDATE products SET status = 'SOLD' WHERE product_id = ?";
            String updateSellerSql = "UPDATE users SET total_sales = total_sales + ? WHERE user_id = ?";
            String insertTransSql = "INSERT INTO transactions (transaction_id, buyer_id, product_id, price, timestamp, status) VALUES (?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement updateProdStmt = con.prepareStatement(updateProdSql);
                 PreparedStatement updateSellerStmt = con.prepareStatement(updateSellerSql);
                 PreparedStatement insertTransStmt = con.prepareStatement(insertTransSql)) {
                
                for (Product p : cartItems) {
                    // Check if it's still available (important for concurrency)
                    if (p.getStatus() == ProductStatus.AVAILABLE || p.getStatus() == ProductStatus.AVAILABLE_NO_RECYCLE) {
                        
                        // Update product
                        updateProdStmt.setString(1, p.getProductId());
                        updateProdStmt.executeUpdate();

                        // Update seller sales
                        updateSellerStmt.setDouble(1, p.getPrice());
                        updateSellerStmt.setString(2, p.getSellerId());
                        updateSellerStmt.executeUpdate();

                        // Add transaction log
                        String tid = "t_" + UUID.randomUUID().toString().substring(0, 8);
                        insertTransStmt.setString(1, tid);
                        insertTransStmt.setString(2, currentUser.getUserId());
                        insertTransStmt.setString(3, p.getProductId());
                        insertTransStmt.setDouble(4, p.getPrice());
                        insertTransStmt.setLong(5, System.currentTimeMillis());
                        insertTransStmt.setString(6, TransactionStatus.COMPLETED.name());
                        insertTransStmt.executeUpdate();
                    }
                }
            }
            
            // 3. Clear the user's cart
            String deleteCartSql = "DELETE FROM cart WHERE buyer_id = ?";
            try (PreparedStatement deleteCartStmt = con.prepareStatement(deleteCartSql)) {
                deleteCartStmt.setString(1, currentUser.getUserId());
                deleteCartStmt.executeUpdate();
            }

            con.commit(); // All good, commit
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }
    
    public static List<Transaction> getTransactionsForBuyer() {
        List<Transaction> userTransactions = new ArrayList<>();
        if (currentUser == null || currentUser.getRole() != Role.BUYER) {
            return userTransactions;
        }
        
        String sql = "SELECT * FROM transactions WHERE buyer_id = ? ORDER BY timestamp DESC";
        
        try (Connection con = DBConnector.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setString(1, currentUser.getUserId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    userTransactions.add(new Transaction(
                        rs.getString("transaction_id"),
                        rs.getString("buyer_id"),
                        rs.getString("product_id"),
                        rs.getDouble("price"),
                        rs.getLong("timestamp"),
                        TransactionStatus.valueOf(rs.getString("status"))
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userTransactions;
    }

    // --- Recycler Logic (JDBC) ---

    public static List<Product> getEligibleProductsForBidding() {
        List<Product> eligible = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE status = 'PENDING_RECYCLING'";
        
        try (Connection con = DBConnector.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                eligible.add(inflateProduct(rs)); // Inflates product + bids
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return eligible;
    }
    
    public static boolean placeBid(String productId, double bidPrice) {
        if (currentUser == null || currentUser.getRole() != Role.RECYCLER) return false;
        
        Product p = findProductById(productId); // Check product status
        if (p == null || p.getStatus() != ProductStatus.PENDING_RECYCLING) return false;
        
        if (bidPrice >= p.getBaseCost()) {
            String sql = "INSERT INTO recycling_bids (product_id, recycler_id, bid_price) VALUES (?, ?, ?)";
            try (Connection con = DBConnector.getConnection();
                 PreparedStatement pstmt = con.prepareStatement(sql)) {
                
                pstmt.setString(1, productId);
                pstmt.setString(2, currentUser.getUserId());
                pstmt.setDouble(3, bidPrice);
                pstmt.executeUpdate();
                return true;
                
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
    
    public static List<Product> getAcquiredProducts() {
        List<Product> acquired = new ArrayList<>();
        if (currentUser == null) return acquired;

        // In a real app, we'd have a column on 'products' for 'winning_recycler_id'
        // For now, this is fine.
        String sql = "SELECT * FROM products WHERE status = 'RECYCLING_PURCHASED'";
        
        try (Connection con = DBConnector.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                acquired.add(inflateProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return acquired;
    }
    
    public static boolean submitRecyclingProof(String productId) {
        if (currentUser == null) return false;
        
        Product p = findProductById(productId);
        if (p == null || p.getStatus() != ProductStatus.RECYCLING_PURCHASED) {
            return false;
        }
        
        Connection con = null;
        try {
            con = DBConnector.getConnection();
            con.setAutoCommit(false); // Start transaction
            
            // 1. Update product status
            String updateProdSql = "UPDATE products SET status = 'RECYCLED' WHERE product_id = ?";
            try (PreparedStatement pstmt = con.prepareStatement(updateProdSql)) {
                pstmt.setString(1, productId);
                pstmt.executeUpdate();
            }
            
            // 2. Distribute credits
            int totalCredits = p.getCarbonValue();
            double recyclerShare = totalCredits * 0.70;
            double sellerShare = totalCredits * 0.30;
            
            // 3. Update recycler
            String updateRecyclerSql = "UPDATE users SET carbon_credits = carbon_credits + ? WHERE user_id = ?";
            try (PreparedStatement pstmt = con.prepareStatement(updateRecyclerSql)) {
                pstmt.setDouble(1, recyclerShare);
                pstmt.setString(2, currentUser.getUserId());
                pstmt.executeUpdate();
            }

            // 4. Update seller
            String updateSellerSql = "UPDATE users SET carbon_credits = carbon_credits + ? WHERE user_id = ?";
            try (PreparedStatement pstmt = con.prepareStatement(updateSellerSql)) {
                pstmt.setDouble(1, sellerShare);
                pstmt.setString(2, p.getSellerId());
                pstmt.executeUpdate();
            }
            
            con.commit(); // All good
            return true;
            
        } catch (SQLException e) {
            e.printStackTrace();
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }
    
    // --- Shared Logic (JDBC) ---

    public static List<User> getSellerLeaderboard() {
        List<User> sellers = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'SELLER' ORDER BY total_sales DESC LIMIT 10";
        
        try (Connection con = DBConnector.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                sellers.add(inflateUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sellers;
    }
    
    public static List<User> getRecyclerLeaderboard() {
        List<User> recyclers = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'RECYCLER' ORDER BY carbon_credits DESC LIMIT 10";
        
        try (Connection con = DBConnector.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                recyclers.add(inflateUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return recyclers;
    }
}