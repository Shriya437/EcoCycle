// In file: src/ecocycle/util/SceneNavigator.java
package ecocycle.util;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Utility class for navigating between scenes (screens) in the application.
 * This helper class makes it easy to switch from one FXML file to another.
 */
public class SceneNavigator {

    /**
     * Navigates to a new FXML view.
     * @param event The ActionEvent from the control (e.g., button) that triggered the navigation.
     * @param fxmlFile The path to the new FXML file (e.g., "/ecocycle/view/Dashboard.fxml").
     */
    public static void navigateTo(ActionEvent event, String fxmlFile) {
        try {
            // Get the stage (the main window) from the button that was clicked
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();

            // Load the new FXML file from the /ecocycle/view/ folder
            URL fxmlUrl = SceneNavigator.class.getResource(fxmlFile);
            if (fxmlUrl == null) {
                System.err.println("Cannot find FXML file: " + fxmlFile);
                return;
            }
            Parent root = FXMLLoader.load(fxmlUrl);

            // Create a new scene with the loaded FXML
            Scene scene = new Scene(root);
            
            // --- THIS IS THE FIX ---
            // We must apply the stylesheet to every new scene we create.
            URL cssUrl = SceneNavigator.class.getResource("/ecocycle/view/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.err.println("Error: Could not find style.css file.");
            }
            // --- END OF FIX ---
            
            // Set the new scene on the stage
            stage.setScene(scene);
            
            // Set a title based on the FXML file name
            String title = fxmlFile.substring(fxmlFile.lastIndexOf('/') + 1, fxmlFile.lastIndexOf('.'));
            stage.setTitle("EcoCycle - " + title);
            
            // Show the new scene
            stage.show();

        } catch (IOException e) {
            System.err.println("Failed to load FXML file: " + fxmlFile);
            e.printStackTrace();
        }
    }
}