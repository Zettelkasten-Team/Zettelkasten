package playground;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

public class GUITest {
    public static void main(String[] args) {
        try {
            // Create a Robot instance
            Robot robot = new Robot();

            // Simulate key presses or mouse actions
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_N);
            robot.keyRelease(KeyEvent.VK_N);
            robot.keyRelease(KeyEvent.VK_CONTROL);

            // You can also perform mouse actions like mouseMove, mousePress, mouseRelease, etc.

        } catch (AWTException e) {
            e.printStackTrace();
        }
    }
}
