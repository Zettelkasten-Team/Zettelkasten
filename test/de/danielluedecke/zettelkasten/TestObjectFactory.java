package de.danielluedecke.zettelkasten;

import java.io.File;
import java.lang.reflect.Field;

import org.jdesktop.application.SingleFrameApplication;

import de.danielluedecke.zettelkasten.database.AcceleratorKeys;
import de.danielluedecke.zettelkasten.database.AutoKorrektur;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.database.StenoData;
import de.danielluedecke.zettelkasten.database.Synonyms;
import de.danielluedecke.zettelkasten.database.TasksData;

/**
 * This class offers convenience access testable Zettelkasten objects.
 *
 * @author Timm Heuss
 *
 */
public class TestObjectFactory {

    /**
     * Implementation of a thread safe, synchronized singleton pattern
     * {@link http://de.wikibooks.org/wiki/Muster:_Java:_Singleton}.
     */
    private static TestObjectFactory instance;

    public static synchronized TestObjectFactory getInstance() throws Exception {
        if (TestObjectFactory.instance == null) {
            TestObjectFactory.instance = new TestObjectFactory();
        }
        return TestObjectFactory.instance;
    }

    private final StenoData stenoData;
    private final AutoKorrektur autoKorrektur;
    private final TasksData tasksData;
    private final AcceleratorKeys acceleratorKeys;
    private final Synonyms synonyms;

    private TestObjectFactory() throws Exception {
        stenoData = null;
        autoKorrektur = null;
        tasksData = null;

        acceleratorKeys = new AcceleratorKeys();
        synonyms = new Synonyms();
    }

    /**
     * Abstracts sample files and settings
     */
    public enum ZKN3Settings {
        ZKN3_SAMPLE("zkn3_sample.zkn3", false), ZKN3_TRICKY_NON_MARKDOWN(
                "zkn3_tricky.zkn3", false), ZKN3_TRICKY_MARKDOWN(
                "zkn3_tricky.zkn3", true);

        public Settings settings;
        public File file;

        ZKN3Settings(String file, boolean markdownActivated) {
            try {
                settings = new Settings(
                        TestObjectFactory.getInstance().acceleratorKeys,
                        TestObjectFactory.getInstance().autoKorrektur,
                        TestObjectFactory.getInstance().synonyms,
                        TestObjectFactory.getInstance().stenoData);

                File fileObject;
                fileObject = new File(file);

                settings.setFilePath(fileObject);
                this.file = fileObject;

                settings.setMarkdownActivated(markdownActivated);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Daten getDaten(ZKN3Settings zkn3Settings) throws Exception {
        Settings settings = zkn3Settings.settings;

        ZettelkastenView zettelkastenView = new ZettelkastenView(
                new SingleFrameApplication() {
            @Override
            protected void startup() {
            }
        }, settings, TestObjectFactory.getInstance().acceleratorKeys,
                TestObjectFactory.getInstance().autoKorrektur,
                TestObjectFactory.getInstance().synonyms,
                TestObjectFactory.getInstance().stenoData,
                TestObjectFactory.getInstance().tasksData);
        return (Daten) getPrivateField(zettelkastenView, "data");
    }

    /**
     * Helper to retrieve a private / protected field.
     *
     * @param instance
     * @param fieldName
     * @return
     * @throws java.lang.Exception
     */
    public static Object getPrivateField(Object instance, String fieldName)
            throws Exception {
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (Daten) field.get(instance);
    }
}
