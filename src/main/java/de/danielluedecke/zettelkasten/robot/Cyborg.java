package de.danielluedecke.zettelkasten.robot;

import java.awt.AWTException;
import java.awt.Robot;

public class Cyborg {
	// Better Robot
	Robot robot;

	public Cyborg() {
		try {
			robot = new Robot();
			robot.setAutoWaitForIdle(true);
			/**
			 * Note the call robot.setAutoWaitForIdle(true), which causes the Robot to wait
			 * for each event to be consumed before generating the next. Tuning the behavior
			 * of the Robot in this way is essential to the working of Cyborg. If Cyborg
			 * extended Robot, clients would be free to undo this setting. This is a third
			 * reason why Cyborg does not extend Robot. (Lavers and Peters, Swing Extreme Testing)
			 */
		} catch (AWTException e) {
			e.printStackTrace();
			assert false : "Could not create robot";
		}
	}
}
