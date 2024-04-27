package playground;
import java.awt.Dimension;
import java.awt.Toolkit;

public class ScreenResolutionExample {
    public static void main(String[] args) {
        // Get the default toolkit
        Toolkit toolkit = Toolkit.getDefaultToolkit();

        // Get the screen size
        Dimension screenSize = toolkit.getScreenSize();

        // Print the screen resolution
        System.out.println("Screen Resolution: " + screenSize.width + "x" + screenSize.height);
    }
}