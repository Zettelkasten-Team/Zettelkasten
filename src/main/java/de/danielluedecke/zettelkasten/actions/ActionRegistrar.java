package de.danielluedecke.zettelkasten.actions;

import java.util.HashMap;
import java.util.Map;

public class ActionRegistrar {
	private final Map<String, UIAction> actionMap;

	public ActionRegistrar() {
		actionMap = new HashMap<>();
	}

	public void registerAction(String name, UIAction action) {
		actionMap.put(name, action);
	}

	public boolean hasAction(String name) {
		return actionMap.containsKey(name);
	}

	public void executeAction(String name) {
		UIAction action = actionMap.get(name);
		if (action != null) {
			action.execute();
		} else {
			throw new IllegalArgumentException("Action not found: " + name);
		}
	}
}
