package playground.swing;

import javax.swing.*;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class SwingAppWithJavaFX {
    public static void main(String[] args) {
        // Create the main Swing frame
        JFrame frame = new JFrame("Swing and JavaFX Application");
        JPanel panel = new JPanel();
        JButton swingButton = new JButton("Swing Button");

        swingButton.addActionListener(e -> System.out.println("Swing Button Clicked"));

        panel.add(swingButton);

        // Create and add JFXPanel
        JFXPanel jfxPanel = new JFXPanel();
        panel.add(jfxPanel);
        
        // Load JavaFX content into JFXPanel
        Platform.runLater(() -> {
            Button fxButton = new Button("JavaFX Button");
            fxButton.setOnAction(e -> System.out.println("JavaFX Button Clicked"));

            StackPane root = new StackPane();
            root.getChildren().add(fxButton);

            Scene scene = new Scene(root, 150, 100);
            jfxPanel.setScene(scene);
        });

        frame.add(panel);
        frame.setSize(300, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
