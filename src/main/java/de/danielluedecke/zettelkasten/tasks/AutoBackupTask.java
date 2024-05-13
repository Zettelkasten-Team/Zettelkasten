package de.danielluedecke.zettelkasten.tasks;

import de.danielluedecke.zettelkasten.ZettelkastenView;
import de.danielluedecke.zettelkasten.database.*;
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.util.FileOperationsUtil;
import java.io.ByteArrayOutputStream;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JOptionPane;

public class AutoBackupTask extends org.jdesktop.application.Task<Object, Void> {

    private final Daten dataObj;
    private final Settings settingsObj;
    private final Bookmarks bookmarksObj;
    private final BibTeX bibtexObj;
    private final Synonyms synonymsObj;
    private final SearchRequests searchObj;
    private final DesktopData desktopObj;
    private final ZettelkastenView zknframe;
    private final javax.swing.JLabel statusMsgLabel;
    private String oldmsg;

    public AutoBackupTask(org.jdesktop.application.Application app, ZettelkastenView zkn,
            javax.swing.JLabel statusMsgLabel, Daten dataObj,
            DesktopData desktopObj, Settings settingsObj,
            SearchRequests searchObj, Synonyms synonymsObj,
            Bookmarks bookmarksObj, BibTeX bibtexObj) {
        super(app);
        this.dataObj = dataObj;
        this.settingsObj = settingsObj;
        this.bookmarksObj = bookmarksObj;
        this.bibtexObj = bibtexObj;
        this.searchObj = searchObj;
        this.synonymsObj = synonymsObj;
        this.statusMsgLabel = statusMsgLabel;
        this.desktopObj = desktopObj;
        this.zknframe = zkn;
    }

    @Override
    protected Object doInBackground() {
        try {
            File backupFile = createBackupFile();
            if (backupFile == null) {
                return null;
            }
            performBackup(backupFile);
        } catch (IOException e) {
            handleBackupError(e);
        }
        return null;
    }

    File createBackupFile() {
        File mainDataFile = settingsObj.getMainDataFile();
        if (mainDataFile == null) {
            return null;
        }
        String filePath = mainDataFile.toString();
        int lastDotIndex = filePath.lastIndexOf(".");
        if (lastDotIndex == -1) {
            Constants.zknlogger.log(Level.WARNING, "Couldn't find file extension! Auto-backup was not created!");
            return null;
        }
        return new File(filePath.substring(0, lastDotIndex) + ".zkb3");
    }

    void performBackup(File backupFile) throws IOException {
        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(backupFile))) {
            XMLOutputter out = new XMLOutputter();
            // Check if the backup file is write-protected
            if (!backupFile.canWrite()) {
                // Log a warning message
                Constants.zknlogger.log(Level.WARNING, "Autobackup-file is write-protected. Removing write protection...");
                try {
                    // Attempt to remove write protection
                    backupFile.setWritable(true);
                } catch (SecurityException ex) {
                    // Log an error message if write protection couldn't be removed
                    Constants.zknlogger.log(Level.SEVERE, ex.getLocalizedMessage());
                    Constants.zknlogger.log(Level.SEVERE, "Autobackup-file is write-protected. Write protection could not be removed!");
                    // Handle the situation where write protection cannot be removed
                    // For example, you might prompt the user to manually remove write protection or choose a different location for the backup file
                    // You can also choose to abort the backup process here
                    return; // or throw an exception
                }
            }
            saveDataToZip(zip, out);
        }
    }

    private void saveDataToZip(ZipOutputStream zip, XMLOutputter out) throws IOException {
        zip.putNextEntry(new ZipEntry(Constants.metainfFileName));
        out.output(dataObj.getMetaInformationData(), zip);
        zip.putNextEntry(new ZipEntry(Constants.zknFileName));
        out.output(dataObj.getZknData(), zip);
        zip.putNextEntry(new ZipEntry(Constants.authorFileName));
        out.output(dataObj.getAuthorData(), zip);
        zip.putNextEntry(new ZipEntry(Constants.keywordFileName));
        out.output(dataObj.getKeywordData(), zip);
        zip.putNextEntry(new ZipEntry(Constants.bookmarksFileName));
        out.output(bookmarksObj.getBookmarkData(), zip);
        zip.putNextEntry(new ZipEntry(Constants.searchrequestsFileName));
        out.output(searchObj.getSearchData(), zip);
        zip.putNextEntry(new ZipEntry(Constants.synonymsFileName));
        out.output(synonymsObj.getDocument(), zip);
        zip.putNextEntry(new ZipEntry(Constants.bibTexFileName));
        ByteArrayOutputStream bout = bibtexObj.saveFile();
        bout.writeTo(zip);
        zip.putNextEntry(new ZipEntry(Constants.desktopFileName));
        out.output(desktopObj.getDesktopData(), zip);
        zip.putNextEntry(new ZipEntry(Constants.desktopModifiedEntriesFileName));
        out.output(desktopObj.getDesktopModifiedEntriesData(), zip);
        zip.putNextEntry(new ZipEntry(Constants.desktopNotesFileName));
        out.output(desktopObj.getDesktopNotesData(), zip);
    }

    @Override
    protected void succeeded(Object result) {
        // Runs on the EDT.  Update the GUI based on
        // the result computed by doInBackground().
    }

    @Override
    protected void finished() {
        super.finished();
        // restore old status message
        statusMsgLabel.setText(oldmsg);
        // tell program that task has finished
        zknframe.setAutoBackupRunning(false);
        // no autobackup necessary at the moment
        zknframe.backupNecessary(false);
        // and log info message
        Constants.zknlogger.log(Level.INFO, "Automatic backup was successfully created.");
    }

    void handleBackupError(IOException e) {
        Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
        // Log error message
        Constants.zknlogger.log(Level.SEVERE, "Error creating auto-backup. Creating a copy of the data file...");

        // Attempt to create a copy of the data file
        File fallbackBackupFile = settingsObj.getMainDataFile();
        if (fallbackBackupFile != null && fallbackBackupFile.exists()) {
            try {
                File backupCopy = FileOperationsUtil.getBackupFilePath(fallbackBackupFile);
                FileOperationsUtil.copyFile(fallbackBackupFile, backupCopy, 1024);
                // Log path
                Constants.zknlogger.log(Level.INFO, "A backup of the data file was saved to {0}", backupCopy.toString());

                // Check if the file is write-protected
                if (!backupCopy.canWrite()) {
                    // Log error message
                    Constants.zknlogger.log(Level.SEVERE, "Autobackup failed. The file is write-protected.");
                    // Show error message
                    JOptionPane.showMessageDialog(
                            zknframe.getFrame(),
                            "Error message here", // Message to display
                            "Error Title", // Title of the dialog
                            JOptionPane.PLAIN_MESSAGE
                    );
                }

                // Show error message
                JOptionPane.showMessageDialog(
                        zknframe.getFrame(),
                        "Error message here", // Message to display
                        "Error Title", // Title of the dialog
                        JOptionPane.PLAIN_MESSAGE
                );
            } catch (IOException e2) {
                Constants.zknlogger.log(Level.SEVERE, e2.getLocalizedMessage());
            }
        } else {
            // Log error message if the main data file is null or doesn't exist
            Constants.zknlogger.log(Level.SEVERE, "Main data file is null or doesn't exist.");
        }
    }
}
