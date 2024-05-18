	package de.danielluedecke.zettelkasten.config;

	import org.testng.Assert;
	import org.testng.annotations.AfterMethod;
	import org.testng.annotations.BeforeMethod;
	import org.testng.annotations.Test;

	import java.util.prefs.Preferences;

	public class SettingsManagerTest {
	    private SettingsManager settingsManager;
	    private Preferences preferences;
	    private static final String TEST_LOOK_AND_FEEL = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
	    private static final String DEFAULT_LOOK_AND_FEEL = "javax.swing.plaf.metal.MetalLookAndFeel";

	    @BeforeMethod
	    public void setUp() {
	        settingsManager = SettingsManager.getInstance();
	        preferences = Preferences.userNodeForPackage(SettingsManager.class);
	    }

	    @AfterMethod
	    public void tearDown() {
	        preferences.remove(SettingsManager.getLookAndFeelKey());
	    }

	    @Test
	    public void testGetLookAndFeel_Default() {
	        // Ensure the preference is not set
	        preferences.remove(SettingsManager.getLookAndFeelKey());

	        String lookAndFeel = settingsManager.getLookAndFeel();
	        Assert.assertNull(lookAndFeel, "Look and feel should be null if not set");
	    }

	    @Test
	    public void testSetLookAndFeel() {
	        settingsManager.setLookAndFeel(TEST_LOOK_AND_FEEL);
	        String lookAndFeel = settingsManager.getLookAndFeel();
	        Assert.assertEquals(lookAndFeel, TEST_LOOK_AND_FEEL, "Look and feel should match the set value");
	    }

	    @Test
	    public void testOverwriteLookAndFeel() {
	        settingsManager.setLookAndFeel(TEST_LOOK_AND_FEEL);
	        settingsManager.setLookAndFeel(DEFAULT_LOOK_AND_FEEL);
	        String lookAndFeel = settingsManager.getLookAndFeel();
	        Assert.assertEquals(lookAndFeel, DEFAULT_LOOK_AND_FEEL, "Look and feel should match the most recently set value");
	    }

	    @Test
	    public void testPersistence() {
	        settingsManager.setLookAndFeel(TEST_LOOK_AND_FEEL);
	        SettingsManager newInstance = SettingsManager.getInstance();
	        String lookAndFeel = newInstance.getLookAndFeel();
	        Assert.assertEquals(lookAndFeel, TEST_LOOK_AND_FEEL, "Look and feel should persist across instances");
	    }
	}
