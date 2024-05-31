package de.danielluedecke.zettelkasten.actions;

//import javafx.event.ActionEvent;
//import javafx.event.EventHandler;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.swing.*;

import static org.testng.Assert.*;

public class ActionRegistrarTest {
	private ActionRegistrar actionRegistrar;

	@BeforeMethod
	public void setUp() {
		actionRegistrar = new ActionRegistrar();
	}

	@Test
	public void testRegisterSwingAction() {
		// Test registering a new Swing action
		String actionName = "swingAction";
		UIAction action = new SwingAction(new AbstractAction() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				System.out.println("Swing action executed");
			}
		});
		actionRegistrar.registerAction(actionName, action);

		assertTrue(actionRegistrar.hasAction(actionName));
	}

	@Test
	public void testExecuteSwingAction() {
		// Test executing a registered Swing action
		String actionName = "swingAction";
		StringBuilder output = new StringBuilder();
		UIAction action = new SwingAction(new AbstractAction() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				output.append("Swing action executed");
			}
		});
		actionRegistrar.registerAction(actionName, action);

		// Execute the action
		actionRegistrar.executeAction(actionName);

		assertEquals(output.toString(), "Swing action executed");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testExecuteNonexistentAction() {
		// Test executing a nonexistent action
		String actionName = "nonexistentAction";

		// This should throw an IllegalArgumentException
		actionRegistrar.executeAction(actionName);
	}
}
