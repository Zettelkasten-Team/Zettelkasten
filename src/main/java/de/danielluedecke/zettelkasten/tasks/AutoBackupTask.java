/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.danielluedecke.zettelkasten.tasks;

import de.danielluedecke.zettelkasten.ZettelkastenApp;
import de.danielluedecke.zettelkasten.ZettelkastenView;
import de.danielluedecke.zettelkasten.database.BibTeX;
import de.danielluedecke.zettelkasten.database.Bookmarks;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.DesktopData;
import de.danielluedecke.zettelkasten.database.SearchRequests;
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.database.Synonyms;
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.util.FileOperationsUtil;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JOptionPane;
import org.jdom2.output.XMLOutputter;

/**
 *
 * @author Luedeke
 */
public class AutoBackupTask extends org.jdesktop.application.Task<Object, Void> {

    /**
     * Store old value of status-label, so we can restore it after task is
     * finished
     */
    private final Daten dataObj;
    private final Settings settingsObj;
    private final Bookmarks bookmarksObj;
    private final BibTeX bibtexObj;
    private final Synonyms synonymsObj;
    private final SearchRequests searchObj;
    private final DesktopData desktopObj;
    /**
     * Reference to the main frame.
     */
    private final ZettelkastenView zknframe;
    /**
     * get the strings for file descriptions from the resource map
     */
    private String oldmsg;
    private final javax.swing.JLabel statusMsgLabel;
    private final org.jdesktop.application.ResourceMap rm
            = org.jdesktop.application.Application.getInstance(ZettelkastenApp.class).
            getContext().getResourceMap(ZettelkastenView.class);

    public AutoBackupTask(org.jdesktop.application.Application app, ZettelkastenView zkn,
            javax.swing.JLabel ml, Daten d, 
            DesktopData desk, Settings s, SearchRequests sr, Synonyms sy, Bookmarks bm, BibTeX bt) {
        // Runs on the EDT.  Copy GUI state that
        // doInBackground() depends on from parameters
        // to ImportFileTask fields, here.
        super(app);
        dataObj = d;
        settingsObj = s;
        bookmarksObj = bm;
        bibtexObj = bt;
        searchObj = sr;
        synonymsObj = sy;
        statusMsgLabel = ml;
        desktopObj = desk;
        zknframe = zkn;
    }

    @Override
    protected Object doInBackground() throws IOException {
        // Your Task's code here.  This method runs
        // on a background thread, so don't reference
        // the Swing GUI from here.
        // prevent task from processing when the file path is incorrect

        // get filepath
        File fp = settingsObj.getFilePath();
        // copy current filepath to string
        String newfp = fp.toString();
        // look for last occurence of the extension-period. this
        // is needed to set another extension for the backup-file
        int lastDot = newfp.lastIndexOf(".");
        // if extension was found...
        if (-1 == lastDot) {
            // log error
            Constants.zknlogger.log(Level.WARNING, "Couldn't find file-extension! Auto-backup was not created!");
            return null;
        }
        // create backup-file, with new extension
        File backup = new File(newfp.substring(0, lastDot) + ".zkb3");
        // create additional backup directory
        File backup_dir = new File(newfp.substring(0, lastDot) + "_zkb3");
        // and copy original file to backupfile
        // if the user did not cancel and the destination file does not already exist, go on here
        // tell programm that task is running
        zknframe.setAutoBackupRunning(true);
        // check whether file is write protected
        if (!backup.canWrite()) {
            // log error-message
            Constants.zknlogger.log(Level.WARNING, "Autobackup-file is write-protected. Removing write protection...");
            try {
                // try to remove write protection
                backup.setWritable(true);
            } catch (SecurityException ex) {
                // log error-message
                Constants.zknlogger.log(Level.SEVERE, ex.getLocalizedMessage());
                Constants.zknlogger.log(Level.SEVERE, "Autobackup-file is write-protected. Write protection could not be removed!");
            }
        }
        ByteArrayOutputStream bout = null;
        ZipOutputStream zip = null;
        // open the outputstream
        try {
            zip = new ZipOutputStream(new FileOutputStream(backup));
            // I first wanted to use a pretty output format, so advanced users who
            // extract the data file can better watch the xml-files. but somehow, this
            // lead to an error within the method "retrieveElement" in the class "CDaten.java",
            // saying the a org.jdom.text cannot be converted to org.jdom.element?!?
            // XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
            XMLOutputter out = new XMLOutputter();
            // save old statustext
            oldmsg = statusMsgLabel.getText();
            // show status text
            statusMsgLabel.setText(rm.getString("createAutoBackupMsg"));
            // save metainformation
            zip.putNextEntry(new ZipEntry(Constants.metainfFileName));
            out.output(dataObj.getMetaInformationData(), zip);
            // save main data.
            zip.putNextEntry(new ZipEntry(Constants.zknFileName));
            out.output(dataObj.getZknData(), zip);
            // save authors
            zip.putNextEntry(new ZipEntry(Constants.authorFileName));
            out.output(dataObj.getAuthorData(), zip);
            // save keywords
            zip.putNextEntry(new ZipEntry(Constants.keywordFileName));
            out.output(dataObj.getKeywordData(), zip);
            // save bookmarks
            zip.putNextEntry(new ZipEntry(Constants.bookmarksFileName));
            out.output(bookmarksObj.getBookmarkData(), zip);
            // save searchrequests
            zip.putNextEntry(new ZipEntry(Constants.searchrequestsFileName));
            out.output(searchObj.getSearchData(), zip);
            // save synonyms
            zip.putNextEntry(new ZipEntry(Constants.synonymsFileName));
            out.output(synonymsObj.getDocument(), zip);
            // save bibtex file
            zip.putNextEntry(new ZipEntry(Constants.bibTexFileName));
            bout = bibtexObj.saveFile();
            bout.writeTo(zip);
            // save desktops
            zip.putNextEntry(new ZipEntry(Constants.desktopFileName));
            out.output(desktopObj.getDesktopData(), zip);
            zip.putNextEntry(new ZipEntry(Constants.desktopModifiedEntriesFileName));
            out.output(desktopObj.getDesktopModifiedEntriesData(), zip);
            zip.putNextEntry(new ZipEntry(Constants.desktopNotesFileName));
            out.output(desktopObj.getDesktopNotesData(), zip);
        } catch (IOException e) {
            // log error message
            Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
            // create a copy of the data file in case we have problems creating the auto-backup
            File datafiledummy = settingsObj.getFilePath();
            // check for valid value
            if (datafiledummy != null && datafiledummy.exists()) {
                try {
                    // first, create basic backup-file
                    File checkbackup = FileOperationsUtil.getBackupFilePath(datafiledummy);
                    // copy data file as backup-file
                    FileOperationsUtil.copyFile(datafiledummy, checkbackup, 1024);
                    // log path.
                    Constants.zknlogger.log(Level.INFO, "A backup of the data file was saved to {0}", checkbackup.toString());
                    // check whether file is write protected
                    if (!backup.canWrite()) {
                        // log error-message
                        Constants.zknlogger.log(Level.SEVERE, "Autobackup failed. The file is write-protected.");
                        // show error message
                        JOptionPane.showMessageDialog(zknframe.getFrame(), rm.getString("errorSavingWriteProtectedMsg"), rm.getString("autobackupSaveErrTitle"), JOptionPane.PLAIN_MESSAGE);
                    }
                    // tell user that an error occured
                    JOptionPane.showMessageDialog(zknframe.getFrame(), rm.getString("autobackupSaveErrMsg", "\"" + checkbackup.getName() + "\""),
                            rm.getString("autobackupSaveErrTitle"),
                            JOptionPane.PLAIN_MESSAGE);
                } catch (IOException e2) {
                    Constants.zknlogger.log(Level.SEVERE, e2.getLocalizedMessage());
                }
            }
        } catch (SecurityException e) {
            // log error message
            Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
            // create a copy of the data file in case we have problems creating the auto-backup
            File datafiledummy = settingsObj.getFilePath();
            // check for valid value
            if (datafiledummy != null && datafiledummy.exists()) {
                try {
                    // first, create basic backup-file
                    File checkbackup = FileOperationsUtil.getBackupFilePath(datafiledummy);
                    // copy data file as backup-file
                    FileOperationsUtil.copyFile(datafiledummy, checkbackup, 1024);
                    // log path.
                    Constants.zknlogger.log(Level.INFO, "A backup of the data file was saved to {0}", checkbackup.toString());
                    // tell user that an error occured
                    JOptionPane.showMessageDialog(zknframe.getFrame(), rm.getString("autobackupSaveErrMsg", "\"" + checkbackup.getName() + "\""),
                            rm.getString("autobackupSaveErrTitle"),
                            JOptionPane.PLAIN_MESSAGE);
                } catch (IOException e2) {
                    Constants.zknlogger.log(Level.SEVERE, e2.getLocalizedMessage());
                }
            }
        } finally {
            try {
                if (bout != null) {
                    bout.close();
                }
                if (zip != null) {
                    zip.close();
                }
            } catch (IOException e) {
                Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
            }
        }

        return null;  // return your result
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
        // tell programm that task has finished
        zknframe.setAutoBackupRunning(false);
        // no autoback necessary at the moment
        zknframe.backupNecessary(false);
        // and log info message
        Constants.zknlogger.log(Level.INFO, "Automatic backup was successfully created.");
    }
}
