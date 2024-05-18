package de.danielluedecke.zettelkasten.config;

import java.util.prefs.Preferences;

public class SettingsManager {
    // Singleton instance
    private static final SettingsManager instance = new SettingsManager();

    // Preferences node for storing settings
    private final Preferences preferences;

    // Keys for storing settings
    private static final String LOOK_AND_FEEL_KEY = "lookAndFeel";

    // Private constructor to enforce singleton pattern and initialize preferences
    private SettingsManager() {
        preferences = Preferences.userNodeForPackage(SettingsManager.class);
    }

    // Method to get the singleton instance
    public static SettingsManager getInstance() {
        return instance;
    }

    // Method to get the stored look and feel preference
    public String getLookAndFeel() {
        return preferences.get(getLookAndFeelKey(), null);
    }

    // Method to set the look and feel preference
    public void setLookAndFeel(String lookAndFeel) {
        preferences.put(getLookAndFeelKey(), lookAndFeel);
    }

	public static String getLookAndFeelKey() {
		return LOOK_AND_FEEL_KEY;
	}
}
