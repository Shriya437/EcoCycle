// In file: src/ecocycle/controller/DashboardController.java
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

    @FXML
    public void initialize() {
        User user = DataService.getCurrentUser();
        if (user != null) {
            welcomeLabel.setText("Welcome, " + user.getUsername() + "!");
            buildRoleDashboard(user);
        } else {
            welcomeLabel.setText("Welcome, Guest!");
            statsLabel.setText("");
        }
    }

    private void buildRoleDashboard(User user) {
        buttonContainer.getChildren().clear();
        
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
                buttonContainer.getChildren().add(createNavButton("Browse Products", this::handleBrowseProducts));
                buttonContainer.getChildren().add(createNavButton("View My Cart", this::handleViewCart));
                buttonContainer.getChildren().add(createNavButton("My Transactions", this::handleViewTransactions));
                // buttonContainer.getChildren().add(createNavButton("Manage My Reviews", this::handleNotImplemented)); // Review logic is complex, stubbed for now
                break;
            case RECYCLER:
                statsLabel.setText(String.format("Credits: %.2f", user.getCarbonCredits()));
                buttonContainer.getChildren().add(createNavButton("View Recycling Market", this::handleRecyclingMarket));
                buttonContainer.getChildren().add(createNavButton("Submit Recycling Proof", this::handleSubmitProof));
                break;
        }
    }

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

    // --- Buyer Actions ---
    @FXML
    void handleBrowseProducts(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/ecocycle/view/ProductBrowse.fxml");
    }

    @FXML
    void handleViewCart(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/ecocycle/view/Cart.fxml");
    }
    
    @FXML
    void handleViewTransactions(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/ecocycle/view/TransactionList.fxml");
    }

    // --- Recycler Actions ---
    @FXML
    void handleRecyclingMarket(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/ecocycle/view/RecyclingMarket.fxml");
    }

    @FXML
    void handleSubmitProof(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/ecocycle/view/SubmitProof.fxml");
    }

    // --- Shared Actions ---
    @FXML
    void handleLeaderboards(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/ecocycle/view/Leaderboard.fxml");
    }

    @FXML
    void handleLogout(ActionEvent event) {
        DataService.logout();
        SceneNavigator.navigateTo(event, "/ecocycle/view/Login.fxml");
    }
    
    @FXML
    void handleNotImplemented(ActionEvent event) {
        Button b = (Button) event.getSource();
        b.setText(b.getText() + " (Not Implemented)");
        b.setDisable(true);
    }
}