// In file: src/ecocycle/service/DataService.java
package ecocycle.service;

import ecocycle.model.*;
import ecocycle.util.DBConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp; // This is needed
import java.util.*;

/**
 * Data Service (JDBC Version - No Streams)
 * This is the final, complete service class.
 * - Uses JDBC for all database operations.
 * - Uses traditional for-loops and null checks (no streams).
 * - Implements Stack logic for "Undo Delete".
 * - Implements LinkedList logic for "Global Review Feed".
 */
public class DataService {

    // --- Session Management ---
    private static User currentUser = null;

    // --- Simulation Constants ---
    private static final long MINUTE_MS = 60_000;
    private static final Map<String, Long> RECYCLING_THRESHOLDS_MS = new HashMap<>();
    private static final long DEFAULT_RECYCLING_THRESHOLD_MS = 3 * MINUTE_MS;
    
    // --- DSA Feature: "Undo Delete" Stack ---
    private static Stack<Product> undoStack = new Stack<>();

    // --- DSA Feature: "Global Review Feed" LinkedList ---
    private static LinkedList<Review> globalReviewFeed = new LinkedList<>();

    // --- DSA Feature: "Undo Remove from Cart" Stack ---
    private static Stack<Product> removedCartItems = new Stack<>();


    /**
     * Static block to initialize mock data AND recycling rules
     */
    static {
        // Init time thresholds
        RECYCLING_THRESHOLDS_MS.put("clothing", 1 * MINUTE_MS);
        RECYCLING_THRESHOLDS_MS.put("electronics", 2 * MINUTE_MS);
        RECYCLING_THRESHOLDS_MS.put("plastic", 1 * MINUTE_MS);
        RECYCLING_THRESHOLDS_MS.put("furniture", 3 * MINUTE_MS);
        
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
                loadReviewFeedCache();
                return;
            }

            System.out.println("Database is empty. Inserting demo data...");
            
            try (Statement stmt = con.createStatement()) {
                // 1. Create Users
                stmt.executeUpdate("INSERT INTO users (user_id, username, password, role) VALUES ('u_s1', 'seller_A', 'pass', 'SELLER')");
                stmt.executeUpdate("INSERT INTO users (user_id, username, password, role) VALUES ('u_b1', 'buyer_X', 'pass', 'BUYER')");
                stmt.executeUpdate("INSERT INTO users (user_id, username, password, role) VALUES ('u_r1', 'recycler_Z', 'pass', 'RECYCLER')");
                
                // 2. Create Products
                long p2Time = System.currentTimeMillis() - (RECYCLING_THRESHOLDS_MS.get("clothing") + 5000);
                
                stmt.executeUpdate("INSERT INTO products (product_id, name, type, category, description, price, seller_id, status) VALUES " +
                    "('p_01', 'Old Laptop', 'Electronics', 'Electronics', 15000.00, '5yr old laptop', 'u_s1', 'AVAILABLE')");
                
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
    
    /**
     * Loads existing reviews from the DB into the LinkedList cache on startup.
     */
    private static void loadReviewFeedCache() {
        String sql = "SELECT * FROM reviews ORDER BY timestamp DESC";
        try (Connection con = DBConnector.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Review review = new Review(
                    rs.getString("review_id"),
                    rs.getString("product_id"),
                    rs.getString("buyer_id"),
                    rs.getString("text")
                );
                
                // --- FIX #1: Read a Timestamp from the DB, convert to long ---
                review.setTimestamp(rs.getTimestamp("timestamp").getTime());
                
                globalReviewFeed.add(review);
            }
            System.out.println("Loaded " + globalReviewFeed.size() + " reviews into cache.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- NEW HELPER METHODS (for JDBC) ---
    
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
        java.sql.Timestamp ts = rs.getTimestamp("upload_timestamp");
        if (ts != null) {
            product.setUploadTimestamp(ts.getTime());
        } else {
            product.setUploadTimestamp(System.currentTimeMillis());
        }
        
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
                    product.getBids().add(bid);
                }
            }
        }
        
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
        String userId = "u_" + UUID.randomUUID().toString().substring(0, 8);
        
        String sql = "INSERT INTO users (user_id, username, password, role) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnector.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            pstmt.setString(2, username);
            pstmt.setString(3, password);
            pstmt.setString(4, role.name());
            
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
                    userProducts.add(inflateProduct(rs));
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
        
        String sql = "SELECT * FROM products WHERE seller_id = ? AND status = 'AVAILABLE'";
        try (Connection con = DBConnector.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setString(1, currentUser.getUserId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Product p = inflateProduct(rs);
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
        
        String sql = "SELECT p.* FROM products p " +
                     "WHERE p.seller_id = ? AND p.status = 'PENDING_RECYCLING' " +
                     "AND p.product_id IN (SELECT DISTINCT rb.product_id FROM recycling_bids rb)";
        
        try (Connection con = DBConnector.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setString(1, currentUser.getUserId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    biddable.add(inflateProduct(rs));
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

            RecyclingBid winningBid = null;
            Product p = findProductById(productId); 
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

            String updateSellerSql = "UPDATE users SET total_sales = total_sales + ? WHERE user_id = ?";
            try (PreparedStatement updateSellerStmt = con.prepareStatement(updateSellerSql)) {
                updateSellerStmt.setDouble(1, winningBid.bidPrice());
                updateSellerStmt.setString(2, p.getSellerId());
                updateSellerStmt.executeUpdate();
            }

            String updateProductSql = "UPDATE products SET status = 'RECYCLING_PURCHASED' WHERE product_id = ?";
            try (PreparedStatement updateProductStmt = con.prepareStatement(updateProductSql)) {
                updateProductStmt.setString(1, productId);
                updateProductStmt.executeUpdate();
            }
            
            String deleteBidsSql = "DELETE FROM recycling_bids WHERE product_id = ?";
            try (PreparedStatement deleteBidsStmt = con.prepareStatement(deleteBidsSql)) {
                deleteBidsStmt.setString(1, productId);
                deleteBidsStmt.executeUpdate();
            }

            con.commit(); 
            return winningBid;

        } catch (SQLException e) {
            e.printStackTrace();
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return null;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    // --- Buyer Logic (JDBC) ---

    public static List<Product> getAvailableProducts(String category, double minPrice, double maxPrice, boolean sortByPriceAsc) {
        List<Product> available = new ArrayList<>();
        
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE (status = 'AVAILABLE' OR status = 'AVAILABLE_NO_RECYCLE') ");
        List<Object> params = new ArrayList<>();

        if (category != null && !category.isEmpty() && !category.equals("All")) {
            sql.append("AND category = ? ");
            params.add(category);
        }
        if (minPrice > 0) {
            sql.append("AND price >= ? ");
            params.add(minPrice);
        }
        if (maxPrice > 0 && maxPrice != Double.MAX_VALUE) {
            sql.append("AND price <= ? ");
            params.add(maxPrice);
        }
        if (sortByPriceAsc) {
            sql.append("ORDER BY price ASC");
        } else {
            sql.append("ORDER BY upload_timestamp DESC");
        }

        try (Connection con = DBConnector.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    available.add(inflateProduct(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return available;
    }
    
    public static List<Product> getAvailableProducts() {
        return getAvailableProducts("All", 0, Double.MAX_VALUE, false);
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
        
        List<Product> cartItems = getCart();
        if (cartItems.isEmpty()) return true;
        
        Connection con = null;
        try {
            con = DBConnector.getConnection();
            con.setAutoCommit(false); 

            String updateProdSql = "UPDATE products SET status = 'SOLD' WHERE product_id = ?";
            String updateSellerSql = "UPDATE users SET total_sales = total_sales + ? WHERE user_id = ?";
            String insertTransSql = "INSERT INTO transactions (transaction_id, buyer_id, product_id, price, timestamp, status) VALUES (?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement updateProdStmt = con.prepareStatement(updateProdSql);
                 PreparedStatement updateSellerStmt = con.prepareStatement(updateSellerSql);
                 PreparedStatement insertTransStmt = con.prepareStatement(insertTransSql)) {
                
                for (Product p : cartItems) {
                    if (p.getStatus() == ProductStatus.AVAILABLE || p.getStatus() == ProductStatus.AVAILABLE_NO_RECYCLE) {
                        
                        updateProdStmt.setString(1, p.getProductId());
                        updateProdStmt.executeUpdate();

                        updateSellerStmt.setDouble(1, p.getPrice());
                        updateSellerStmt.setString(2, p.getSellerId());
                        updateSellerStmt.executeUpdate();

                        String tid = "t_" + UUID.randomUUID().toString().substring(0, 8);
                        insertTransStmt.setString(1, tid);
                        insertTransStmt.setString(2, currentUser.getUserId());
                        insertTransStmt.setString(3, p.getProductId());
                        insertTransStmt.setDouble(4, p.getPrice());
                        insertTransStmt.setTimestamp(5, new java.sql.Timestamp(System.currentTimeMillis()));
                        insertTransStmt.setString(6, TransactionStatus.COMPLETED.name());
                        insertTransStmt.executeUpdate();
                    }
                }
            }
            
            String deleteCartSql = "DELETE FROM cart WHERE buyer_id = ?";
            try (PreparedStatement deleteCartStmt = con.prepareStatement(deleteCartSql)) {
                deleteCartStmt.setString(1, currentUser.getUserId());
                deleteCartStmt.executeUpdate();
            }

            con.commit();
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

    public static boolean purchaseSingleItem(String productId) {
        if (currentUser == null) return false;

        Product p = findProductById(productId);
        if (p == null) return false;
        
        boolean inCart = false;
        List<Product> cartItems = getCart();
        for (Product cartItem : cartItems) {
            if (cartItem.getProductId().equals(productId)) {
                inCart = true;
                break;
            }
        }
        if (!inCart) {
            return false;
        }

        Connection con = null;
        try {
            con = DBConnector.getConnection();
            con.setAutoCommit(false); 

            String updateProdSql = "UPDATE products SET status = 'SOLD' WHERE product_id = ?";
            String updateSellerSql = "UPDATE users SET total_sales = total_sales + ? WHERE user_id = ?";
            String insertTransSql = "INSERT INTO transactions (transaction_id, buyer_id, product_id, price, timestamp, status) VALUES (?, ?, ?, ?, ?, ?)";
            String deleteCartSql = "DELETE FROM cart WHERE buyer_id = ? AND product_id = ?";

            try (PreparedStatement updateProdStmt = con.prepareStatement(updateProdSql);
                 PreparedStatement updateSellerStmt = con.prepareStatement(updateSellerSql);
                 PreparedStatement insertTransStmt = con.prepareStatement(insertTransSql);
                 PreparedStatement deleteCartStmt = con.prepareStatement(deleteCartSql)) {

                updateProdStmt.setString(1, p.getProductId());
                updateProdStmt.executeUpdate();

                updateSellerStmt.setDouble(1, p.getPrice());
                updateSellerStmt.setString(2, p.getSellerId());
                updateSellerStmt.executeUpdate();

                String tid = "t_" + UUID.randomUUID().toString().substring(0, 8);
                insertTransStmt.setString(1, tid);
                insertTransStmt.setString(2, currentUser.getUserId());
                insertTransStmt.setString(3, p.getProductId());
                insertTransStmt.setDouble(4, p.getPrice());
                insertTransStmt.setTimestamp(5, new java.sql.Timestamp(System.currentTimeMillis()));
                insertTransStmt.setString(6, TransactionStatus.COMPLETED.name());
                insertTransStmt.executeUpdate();
                
                deleteCartStmt.setString(1, currentUser.getUserId());
                deleteCartStmt.setString(2, p.getProductId());
                deleteCartStmt.executeUpdate();
            }
            
            con.commit();
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
                    
                    long timestamp = rs.getTimestamp("timestamp").getTime();
                    
                    userTransactions.add(new Transaction(
                        rs.getString("transaction_id"),
                        rs.getString("buyer_id"),
                        rs.getString("product_id"),
                        rs.getDouble("price"),
                        timestamp,
                        TransactionStatus.valueOf(rs.getString("status"))
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userTransactions;
    }
    
    public static boolean hasBuyerPurchasedProduct(String userId, String productId) {
        String sql = "SELECT 1 FROM transactions WHERE buyer_id = ? AND product_id = ? AND status = 'COMPLETED' LIMIT 1";
        
        try (Connection con = DBConnector.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            pstmt.setString(2, productId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- Recycler Logic (JDBC) ---

    public static List<Product> getEligibleProductsForBidding() {
        List<Product> eligible = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE status = 'PENDING_RECYCLING'";
        
        try (Connection con = DBConnector.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                eligible.add(inflateProduct(rs)); 
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return eligible;
    }
    
    public static boolean placeBid(String productId, double bidPrice) {
        if (currentUser == null || currentUser.getRole() != Role.RECYCLER) return false;
        
        Product p = findProductById(productId); 
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
            con.setAutoCommit(false);
            
            String updateProdSql = "UPDATE products SET status = 'RECYCLED' WHERE product_id = ?";
            try (PreparedStatement pstmt = con.prepareStatement(updateProdSql)) {
                pstmt.setString(1, productId);
                pstmt.executeUpdate();
            }
            
            int totalCredits = p.getCarbonValue();
            double recyclerShare = totalCredits * 0.70;
            double sellerShare = totalCredits * 0.30;
            
            String updateRecyclerSql = "UPDATE users SET carbon_credits = carbon_credits + ? WHERE user_id = ?";
            try (PreparedStatement pstmt = con.prepareStatement(updateRecyclerSql)) {
                pstmt.setDouble(1, recyclerShare);
                pstmt.setString(2, currentUser.getUserId());
                pstmt.executeUpdate();
            }

            String updateSellerSql = "UPDATE users SET carbon_credits = carbon_credits + ? WHERE user_id = ?";
            try (PreparedStatement pstmt = con.prepareStatement(updateSellerSql)) {
                pstmt.setDouble(1, sellerShare);
                pstmt.setString(2, p.getSellerId());
                pstmt.executeUpdate();
            }
            
            con.commit();
            
            // --- FIX FOR PROBLEM 2 ---
            // Refresh the static currentUser object with the new credit total
            if (currentUser != null) {
                currentUser = findUserById(currentUser.getUserId());
            }
            // --- END OF FIX ---
            
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

    // --- DSA Feature: "Undo Delete" Stack ---

    public static boolean archiveProductForUndo(String productId) {
        Product productToArchive = findProductById(productId);
        
        if (productToArchive == null || !productToArchive.getSellerId().equals(currentUser.getUserId())) {
            return false;
        }
        
        if (productToArchive.getStatus() != ProductStatus.AVAILABLE && productToArchive.getStatus() != ProductStatus.AVAILABLE_NO_RECYCLE) {
            return false;
        }

        String sql = "DELETE FROM products WHERE product_id = ?";
        try (Connection con = DBConnector.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setString(1, productId);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                undoStack.push(productToArchive);
                return true;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Product restoreLastDeletedProduct() {
        if (undoStack.isEmpty()) {
            return null;
        }
        
        Product productToRestore = undoStack.pop();
        
        String sql = "INSERT INTO products (product_id, name, type, category, price, description, seller_id, status, upload_timestamp) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection con = DBConnector.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setString(1, productToRestore.getProductId());
            pstmt.setString(2, productToRestore.getName());
            pstmt.setString(3, productToRestore.getType());
            pstmt.setString(4, productToRestore.getCategory());
            pstmt.setDouble(5, productToRestore.getPrice());
            pstmt.setString(6, productToRestore.getDescription());
            pstmt.setString(7, productToRestore.getSellerId());
            pstmt.setString(8, productToRestore.getStatus().name());
            pstmt.setTimestamp(9, new java.sql.Timestamp(productToRestore.getUploadTimestamp()));
            
            pstmt.executeUpdate();
            return productToRestore;
            
        } catch (SQLException e) {
            e.printStackTrace();
            undoStack.push(productToRestore);
            return null;
        }
    }

    public static boolean canUndoDelete() {
        return !undoStack.isEmpty();
    }

    // --- DSA Feature: "Global Review Feed" LinkedList ---

    public static boolean submitReview(String productId, String text) {
        if (currentUser == null) return false;
        
        String reviewId = "r_" + UUID.randomUUID().toString().substring(0, 8);
        long timestamp = System.currentTimeMillis();
        
        // --- FIX: Removed 'rating' from SQL ---
        String sql = "INSERT INTO reviews (review_id, product_id, buyer_id, text, timestamp) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnector.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setString(1, reviewId);
            pstmt.setString(2, productId);
            pstmt.setString(3, currentUser.getUserId());
            pstmt.setString(4, text);
            
            // --- FIX #4: Use setTimestamp (for reviews table) ---
            pstmt.setTimestamp(5, new java.sql.Timestamp(timestamp)); 
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                // --- FIX: Use updated constructor (no rating) ---
                Review newReview = new Review(reviewId, productId, currentUser.getUserId(), text);
                newReview.setTimestamp(timestamp);
                globalReviewFeed.addFirst(newReview); // O(1) insertion
                return true;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public static LinkedList<Review> getGlobalReviewFeed() {
        return globalReviewFeed;
    }
    
    // --- NEW DSA Feature: "Undo Remove from Cart" Stack ---
    
    public static boolean removeItemFromCart(String productId) {
        if (currentUser == null) return false;

        Product productToRemove = findProductById(productId);
        if (productToRemove == null) {
            return false;
        }

        String sql = "DELETE FROM cart WHERE buyer_id = ? AND product_id = ?";
        try (Connection con = DBConnector.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setString(1, currentUser.getUserId());
            pstmt.setString(2, productId);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                removedCartItems.push(productToRemove);
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Product restoreLastRemovedItem() {
        if (removedCartItems.isEmpty()) {
            return null;
        }
        
        Product productToRestore = removedCartItems.pop();
        
        String sql = "INSERT INTO cart (buyer_id, product_id) VALUES (?, ?)";
        try (Connection con = DBConnector.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setString(1, currentUser.getUserId());
            pstmt.setString(2, productToRestore.getProductId());
            pstmt.executeUpdate();
            return productToRestore;
            
        } catch (SQLException e) {
            e.printStackTrace();
            removedCartItems.push(productToRestore);
            return null;
        }
    }
    
    public static boolean canUndoRemove() {
        return !removedCartItems.isEmpty();
    }
}