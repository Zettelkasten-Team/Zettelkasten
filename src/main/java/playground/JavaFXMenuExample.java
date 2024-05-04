package playground;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class JavaFXMenuExample extends Application {
    @Override
    public void start(Stage primaryStage) {
        // Create a MenuBar
        MenuBar menuBar = new MenuBar();

        // Create Menus
        Menu fileMenu = new Menu("File");
        Menu editMenu = new Menu("Edit");

        // Create MenuItems
        MenuItem openItem = new MenuItem("Open");
        MenuItem saveItem = new MenuItem("Save");
        MenuItem exitItem = new MenuItem("Exit");

        // Add MenuItems to File menu
        fileMenu.getItems().addAll(openItem, saveItem, exitItem);

        // Add Menus to MenuBar
        menuBar.getMenus().addAll(fileMenu, editMenu);

        // Create a BorderPane to hold the MenuBar
        BorderPane root = new BorderPane();
        root.setTop(menuBar);

        // Create the Scene
        Scene scene = new Scene(root, 400, 300);

        // Set the Scene and show the Stage
        primaryStage.setScene(scene);
        primaryStage.setTitle("JavaFX Menu Example");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
