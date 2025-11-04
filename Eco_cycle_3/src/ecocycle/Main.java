// In file: src/ecocycle/Main.java
package ecocycle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL; // 1. YOU MUST IMPORT java.net.URL

/**
* Main application class.
* This is the entry point for the JavaFX application.
* It loads the initial login screen and sets up the primary stage.
*/
public class Main extends Application {

    /**
     * This method is called by JavaFX when the application starts.
     * @param primaryStage The main window of the application.
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the FXML file for the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ecocycle/view/Login.fxml"));
            Parent root = loader.load();

            // Set up the scene (the content inside the window)
            Scene scene = new Scene(root, 400, 450); // 400px wide, 450px tall
            
            // --- THIS IS THE FIX ---
            // We must apply the stylesheet to the VERY FIRST scene
            URL cssUrl = getClass().getResource("/ecocycle/view/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.err.println("Error: Could not find style.css file.");
            }
            // --- END OF FIX ---

            // Configure the primary stage (the window)
            primaryStage.setTitle("EcoCycle - Login");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false); // Don't let the user resize the login window
            primaryStage.show(); // Show the window

        } catch (IOException e) {
            System.err.println("Failed to load the Login.fxml file.");
            e.printStackTrace();
        }
    }

    /**
     * The main method that launches the JavaFX application.
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }
}