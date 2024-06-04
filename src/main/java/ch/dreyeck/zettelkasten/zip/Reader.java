package ch.dreyeck.zettelkasten.zip;

import ch.dreyeck.zettelkasten.xml.Zettelkasten;
import javafx.beans.property.ObjectProperty;

import java.io.IOException;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Reader {
    private final ZipFileProcessor zipFileProcessor;
    private final ObjectProperty<Zettelkasten> zettelkastenObjectProperty;

    public Reader(String zipLocation, ObjectProperty<Zettelkasten> zettelkastenObjectProperty) {
        try {
            ZipFile zipFile = new ZipFile(zipLocation);
            this.zipFileProcessor = new ZipFileProcessor(zipFile);
            this.zettelkastenObjectProperty = zettelkastenObjectProperty;
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to initialize ZipFileProcessor", e);
        }
    }

    public ObjectProperty<Zettelkasten> filter(Predicate<ZipEntry> filter) {
        Logger logger = Logger.getLogger(Reader.class.getName());

        try {
            zipFileProcessor.processZipFile();
            ZipEntry zknFileEntry = zipFileProcessor.getZknFileXML();

            if (filter.test(zknFileEntry)) {
                logger.log(Level.INFO, "Filter accepted the entry: " + zknFileEntry.getName());
                Zettelkasten zettelkasten = zipFileProcessor.unmarshall(zknFileEntry);
                zettelkastenObjectProperty.set(zettelkasten);
            } else {
                logger.log(Level.INFO, "Filter rejected the entry: " + zknFileEntry.getName());
                // Set null to clear the property when filter rejects the entry
                zettelkastenObjectProperty.set(null);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error occurred while processing the zip file: ", e);
        }

        return zettelkastenObjectProperty;
    }
}
