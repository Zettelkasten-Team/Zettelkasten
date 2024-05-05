package playground;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class JavaFXMenuExample extends Application {

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        // Create a menu bar
        MenuBar menuBar = new MenuBar();

        // Create a menu
        Menu fileMenu = new Menu("File");

        // Create menu items
        MenuItem aboutItem = new MenuItem("About");
        MenuItem preferencesItem = new MenuItem("Preferences");
        MenuItem quitItem = new MenuItem("Quit");

        // Add event handlers to menu items
        aboutItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // Handle About event
                System.out.println("About event triggered");
            }
        });

        preferencesItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // Handle Preferences event
                System.out.println("Preferences event triggered");
            }
        });

        quitItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // Handle Quit event
                System.out.println("Quit event triggered");
                primaryStage.close();
            }
        });

        // Add menu items to the menu
        fileMenu.getItems().addAll(aboutItem, preferencesItem, quitItem);

        // Add the menu to the menu bar
        menuBar.getMenus().add(fileMenu);

        // Set the menu bar as the top node of the BorderPane
        root.setTop(menuBar);

        Scene scene = new Scene(root, 400, 300);

        primaryStage.setScene(scene);
        primaryStage.setTitle("JavaFX Menu Example");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
