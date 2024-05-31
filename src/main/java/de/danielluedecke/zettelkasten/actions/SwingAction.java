package de.danielluedecke.zettelkasten.actions;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SwingAction implements UIAction {
	private final AbstractAction action;

	public SwingAction(AbstractAction action) {
		this.action = action;
	}

	@Override
	public void execute() {
		action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
	}
}
