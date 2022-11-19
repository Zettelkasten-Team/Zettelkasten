package de.danielluedecke.zettelkasten;

import de.danielluedecke.zettelkasten.database.*;
import org.jdesktop.application.SingleFrameApplication;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Objects;

/**
 * This class offers convenience access testable Zettelkasten objects.
 *
 * @author Timm Heuss
 */
public class TestObjectFactory {

	/**
	 * Implementation of a thread safe, synchronized singleton pattern
	 *
	 * @see <a href=
	 *      "https://web.archive.org/web/20150910003303/http://de.wikibooks.org/wiki/Java_Standard:_Muster_Singleton">Java
	 *      Standard: Muster Singleton [archived copy]</a>
	 */
	private static TestObjectFactory instance; // A (hidden) class variable of the type of the own class
	private final AcceleratorKeys acceleratorKeys;
	private final AutoKorrektur autoKorrektur;
	private final Synonyms synonyms;
	private final StenoData stenoData;
	private final TasksData tasksData;

	private TestObjectFactory() {
		acceleratorKeys = new AcceleratorKeys();
		autoKorrektur = new AutoKorrektur();
		synonyms = new Synonyms();
		stenoData = new StenoData();
		// FIXME Settings --> ZKN3Settings and Daten getDaten
		tasksData = new TasksData();
	}

	public static synchronized TestObjectFactory getInstance() throws Exception {
		if (TestObjectFactory.instance == null) {
			TestObjectFactory.instance = new TestObjectFactory();
		}
		return TestObjectFactory.instance;
	}

	public static Daten getDaten(ZKN3Settings zkn3Settings) throws Exception {
		Settings settings = zkn3Settings.settings;

		ZettelkastenView zettelkastenView = new ZettelkastenView(new SingleFrameApplication() {
			@Override
			protected void startup() {
			}
		}, settings, TestObjectFactory.getInstance().tasksData);
		return (Daten) getPrivateField(zettelkastenView, "data");
	}

	/**
	 * Helper to retrieve a private / protected field.
	 */
	public static Object getPrivateField(Object instance, String fieldName) throws Exception {
		Field field = instance.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		return field.get(instance);
	}

	/**
	 * Abstracts sample files and settings
	 */
	public enum ZKN3Settings {
		ZKN3_SAMPLE("zkn3_sample.zkn3", false), ZKN3_TRICKY_MARKDOWN("zkn3_tricky.zkn3", true);

		public Settings settings;

		ZKN3Settings(String file, boolean markdownActivated) {
			try {
				settings = new Settings();

				settings.setMainDataFile(new File(
						Objects.requireNonNull(TestObjectFactory.class.getClassLoader().getResource(file)).getPath()));
				settings.setMarkdownActivated(markdownActivated);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
