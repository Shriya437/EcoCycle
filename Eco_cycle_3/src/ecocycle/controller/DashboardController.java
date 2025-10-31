package ecocycle.controller;

import ecocycle.model.Role;
import ecocycle.model.User;
import ecocycle.service.DataService;
import ecocycle.util.SceneNavigator;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class DashboardController {

    @FXML
    private Label welcomeLabel;
    
    @FXML
    private Label statsLabel;

    @FXML
    private VBox buttonContainer; // The VBox to hold dynamic buttons

    /**
     * Initializes the dashboard.
     * Fetches the current user and sets the welcome message.
     * Dynamically adds buttons based on the user's role.
     */
    @FXML
    public void initialize() {
        User user = DataService.getCurrentUser();
        if (user != null) {
            welcomeLabel.setText("Welcome, " + user.getUsername() + "!");
            // Build the UI based on user role
            buildRoleDashboard(user);
        } else {
            // Fallback, should not be reachable if login is required
            welcomeLabel.setText("Welcome, Guest!");
            statsLabel.setText("");
        }
    }

    /**
     * Dynamically creates and adds buttons to the dashboard
     * based on the user's role.
     */
    private void buildRoleDashboard(User user) {
        buttonContainer.getChildren().clear(); // Clear any existing buttons
        
        switch (user.getRole()) {
            case SELLER:
                statsLabel.setText(String.format("Credits: %.2f | Total Sales: ₹%.2f", 
                                   user.getCarbonCredits(), user.getTotalSales()));
                buttonContainer.getChildren().add(createNavButton("View My Products", this::handleViewProducts));
                buttonContainer.getChildren().add(createNavButton("Add New Product", this::handleAddProduct));
                buttonContainer.getChildren().add(createNavButton("Manage Recycling Approvals", this::handleManageRecycling));
                buttonContainer.getChildren().add(createNavButton("View Recycling Bids", this::handleViewBids));
                break;
            case BUYER:
                statsLabel.setText(String.format("Credits: %.2f", user.getCarbonCredits()));
                buttonContainer.getChildren().add(createNavButton("Browse Products", this::handleNotImplemented));
                buttonContainer.getChildren().add(createNavButton("View My Cart", this::handleNotImplemented));
                buttonContainer.getChildren().add(createNavButton("My Transactions", this::handleNotImplemented));
                buttonContainer.getChildren().add(createNavButton("Manage My Reviews", this::handleNotImplemented));
                break;
            case RECYCLER:
                statsLabel.setText(String.format("Credits: %.2f", user.getCarbonCredits()));
                buttonContainer.getChildren().add(createNavButton("View Recycling Market", this::handleNotImplemented));
                buttonContainer.getChildren().add(createNavButton("Submit Recycling Proof", this::handleNotImplemented));
                break;
        }
    }

    /**
     * Helper method to create a styled navigation button.
     */
    private Button createNavButton(String text, EventHandler<ActionEvent> handler) {
        Button button = new Button(text);
        button.setPrefWidth(250.0);
        button.setOnAction(handler);
        return button;
    }

    // --- Seller Actions ---
    @FXML
    void handleViewProducts(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/ecocycle/view/ProductList.fxml");
    }

    @FXML
    void handleAddProduct(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/ecocycle/view/AddProduct.fxml");
    }
    
    @FXML
    void handleManageRecycling(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/ecocycle/view/ManageRecycling.fxml");
    }
    
    @FXML
    void handleViewBids(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/ecocycle/view/ViewBids.fxml");
    }
    
    // --- Shared Actions ---
    @FXML
    void handleLeaderboards(ActionEvent event) {
        // You would create Leaderboard.fxml and its controller
        System.out.println("Leaderboard navigation not implemented yet.");
    }

    @FXML
    void handleLogout(ActionEvent event) {
        DataService.logout();
        SceneNavigator.navigateTo(event, "/ecocycle/view/Login.fxml");
    }
    
    // --- Placeholder for Buyer/Recycler actions ---
    @FXML
    void handleNotImplemented(ActionEvent event) {
        // A simple way to stub features
        Button b = (Button) event.getSource();
        b.setText(b.getText() + " (Not Implemented)");
        b.setDisable(true);
    }
}