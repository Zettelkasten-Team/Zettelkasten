/*
 * Zettelkasten - nach Luhmann
 * Copyright (C) 2001-2015 by Daniel Lüdecke (http://www.danielluedecke.de)
 * 
 * Homepage: http://zettelkasten.danielluedecke.de
 * 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * Dieses Programm ist freie Software. Sie können es unter den Bedingungen der GNU
 * General Public License, wie von der Free Software Foundation veröffentlicht, weitergeben
 * und/oder modifizieren, entweder gemäß Version 3 der Lizenz oder (wenn Sie möchten)
 * jeder späteren Version.
 * 
 * Die Veröffentlichung dieses Programms erfolgt in der Hoffnung, daß es Ihnen von Nutzen sein 
 * wird, aber OHNE IRGENDEINE GARANTIE, sogar ohne die implizite Garantie der MARKTREIFE oder 
 * der VERWENDBARKEIT FÜR EINEN BESTIMMTEN ZWECK. Details finden Sie in der 
 * GNU General Public License.
 * 
 * Sie sollten ein Exemplar der GNU General Public License zusammen mit diesem Programm 
 * erhalten haben. Falls nicht, siehe <http://www.gnu.org/licenses/>.
 */
package de.danielluedecke.zettelkasten.util;

import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.Settings;
import java.awt.FileDialog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Luedeke
 */
public class FileOperationsUtil {

    /**
     * create a variable for a list model. this list model is used for the
     * JList-component which displays the links of the current entry.
     */
    private static DefaultListModel linkListModel = new DefaultListModel();

    public static DefaultListModel getListModel() {
        return linkListModel;
    }
    private static String[] addedAttachments = null;

    public static String[] getAddedAttachments() {
        return addedAttachments;
    }

    /**
     * This method creates a backup-filepath by adding the extension
     * <b>.backup</b> to the filepath given in {@code sourcefile}. In case this
     * filepath already exists, this method tries to use a new extension, until
     * a non-existing filepath was created.<br><br>
     * Results might be:<br>
     * <i>sourcefile.ext.backup</i><br>
     * <i>sourcefile.ext.backup-1</i><br>
     * <i>sourcefile.ext.backup-2</i><br>
     * and so on...
     *
     * @param sourcefile the filepath of the source-file that should be backed
     * up
     * @return a filepath-value with a non-existing backup-extenstion.
     */
    public static File getBackupFilePath(File sourcefile) {
        // find correct extension. we use this in case we already
        // have several backups...
        // in case the user already created a backup, we concatenate a trainling
        // backup-counter-number to avoid overwriting existing backup-files
        // we start with a "1"
        int backupcounter = 1;
        String backupext = ".backup";
        File checkbackup = new File(sourcefile.toString() + backupext);
        while (checkbackup.exists()) {
            backupcounter++;
            backupext = ".backup-" + String.valueOf(backupcounter);
            checkbackup = new File(sourcefile.toString() + backupext);
        }
        return checkbackup;
    }

    /**
     * Creates a mac-aqua-like file dialog. Since the typical OS X dialog can
     * not be created using the swing file chooser, we use the awt-FileDialog
     * instead to get mac-like-look'n'feel.
     *
     * @param fc a reference to a FileDialog that should be initiated.
     * @param dlgmode either {@code FileDialog.LOAD} or {@code FileDialog.SAVE}
     * @param initdir the initial directory which can be set when the dialog is
     * shown
     * @param initfile the initial file which can be selected when the dialog is
     * shown
     * @param title the dialog's title
     * @param acceptedext the accepted file extensions that will be accepted,
     * i.e. the files that are selectable
     * @return a reference to the created <i>and initiated</i> FileDialog which
     * was passed as parameter {@code fc}.
     */
    static FileDialog getMacFileDialog(FileDialog fc, int dlgmode, String initdir, String initfile, String title, final String[] acceptedext) {
        fc.setTitle(title);
        fc.setMode(dlgmode);
        fc.setDirectory(initdir);
        fc.setFile(initfile);
        fc.setFilenameFilter(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (acceptedext != null && acceptedext.length > 0) {
                    boolean acc = false;
                    for (String ext : acceptedext) {
                        if (name.toLowerCase().endsWith(ext)) {
                            acc = true;
                        }
                    }
                    return acc;
                } else {
                    return true;
                }
            }
        });
        return fc;
    }

    /**
     *
     * @param f
     * @param ext
     * @return
     */
    public static String setFileExtension(File f, String ext) {
        if (null == f || null == ext || ext.isEmpty()) {
            return null;
        }
        if (!ext.startsWith(".")) {
            ext = "." + ext;
        }
        String s = f.getName();
        int i = s.lastIndexOf(".");
        return s.substring(0, i - 1).concat(ext);
    }

    /**
     * This method creates and shows a file chooser, depending on the operating
     * system. In case the os is Windows or Linux, the standard
     * Swing-JFileChooser will be opened. In case the os is Mac OS X, the old
     * awt-dialog is used, which looks more nativ.<br><br>
     * When the user chose a file, it will be returned, else {@code null} will
     * be returned.
     *
     * @param parent the parent-frame of the file chooser
     * @param dlgmode<br>
     * - in case of Mac OS X: either {@code FileDialog.LOAD} or
     * {@code FileDialog.SAVE} - else: {@code JFileChooser.OPEN_DIALOG} or
     * {@code JFileChooser.SAVE_DIALOG}
     * @param filemode<br>
     * - not important for Mac OS X. - else: {@code JFileChooser.FILES_ONLY} or
     * the other file-selection-mode-values
     * @param initdir the initial directory which can be set when the dialog is
     * shown
     * @param initfile the initial file which can be selected when the dialog is
     * shown
     * @param title the dialog's title
     * @param acceptedext the accepted file extensions that will be accepted,
     * i.e. the files that are selectable
     * @param desc the description of which file types the extensions are
     * @param settings a reference to the CSettings-class
     * @return The chosen file, or {@code null} if dialog was cancelled
     */
    public static File chooseFile(java.awt.Frame parent, int dlgmode, int filemode, String initdir, String initfile, String title, final String[] acceptedext, final String desc, Settings settings) {
        if (!settings.isMacAqua()) {
            File curdir = (null == initdir) ? null : new File(initdir);
            JFileChooser fc = createFileChooser(title, filemode, curdir, acceptedext, desc);
            int option = (JFileChooser.OPEN_DIALOG == dlgmode) ? fc.showOpenDialog(parent) : fc.showSaveDialog(parent);
            if (JFileChooser.APPROVE_OPTION == option) {
                return fc.getSelectedFile();
            }
        } else {
            FileDialog fd = getMacFileDialog(new FileDialog(parent), dlgmode, initdir, initfile, title, acceptedext);
            fd.setVisible(true);
            String file = fd.getFile();
            if (file != null) {
                return new File(fd.getDirectory() + fd.getFile());
            }
        }
        return null;
    }

    /**
     * This method creates and shows a file chooser, depending on the operating
     * system. In case the os is Windows or Linux, the standard
     * Swing-JFileChooser will be opened. In case the os is Mac OS X, the old
     * awt-dialog is used, which looks more nativ.<br><br>
     * When the user chose a file, it will be returned, else {@code null} will
     * be returned.
     *
     * @param parent the parent-dialog of the file chooser
     * @param dlgmode<br>
     * - in case of Mac OS X: either {@code FileDialog.LOAD} or
     * {@code FileDialog.SAVE} - else: {@code JFileChooser.OPEN_DIALOG} or
     * {@code JFileChooser.SAVE_DIALOG}
     * @param filemode<br>
     * - not important for Mac OS X. - else: {@code JFileChooser.FILES_ONLY} or
     * the other file-selection-mode-values
     * @param initdir the initial directory which can be set when the dialog is
     * shown
     * @param initfile the initial file which can be selected when the dialog is
     * shown
     * @param title the dialog's title
     * @param acceptedext the accepted file extensions that will be accepted,
     * i.e. the files that are selectable
     * @param desc the description of which file types the extensions are
     * @param settings a reference to the CSettings-class
     * @return The chosen file, or {@code null} if dialog was cancelled
     */
    public static File chooseFile(java.awt.Dialog parent, int dlgmode, int filemode, String initdir, String initfile, String title, final String[] acceptedext, final String desc, Settings settings) {
        if (!settings.isMacAqua()) {
            File curdir = (null == initdir) ? null : new File(initdir);
            JFileChooser fc = createFileChooser(title, filemode, curdir, acceptedext, desc);
            int option = (JFileChooser.OPEN_DIALOG == dlgmode) ? fc.showOpenDialog(parent) : fc.showSaveDialog(parent);
            if (JFileChooser.APPROVE_OPTION == option) {
                return fc.getSelectedFile();
            }
        } else {
            FileDialog fd = getMacFileDialog(new FileDialog(parent), dlgmode, initdir, initfile, title, acceptedext);
            fd.setVisible(true);
            String file = fd.getFile();
            if (file != null) {
                return new File(fd.getDirectory() + fd.getFile());
            }
        }
        return null;
    }

    /**
     * This method returns the file extension of a given file which is passed as
     * parameter.
     *
     * @param f the file which extension should be retrieved
     * @return the extension of the given file, <b>without</b> leading period
     * (e.g. "jpg" is returned, not ".jpg")
     */
    public static String getFileExtension(File f) {
        if (null == f) {
            return "";
        }
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf(".");
        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    /**
     * This method returns the file extension of a given file passed as
     * string-parameter.
     *
     * @param settingsObj
     * @param dataObj
     * @param f the file which extension should be retrieved
     * @return the extension of the given file, <b>without</b> leading period
     * (e.g. "jpg" is returned, not ".jpg"), in upper case letters.
     */
    public static String getFileExtension(Settings settingsObj, Daten dataObj, String f) {
        String ext = "";
        if (isHyperlink(f)) {
            return "URL";
        }
        f = getLinkFile(settingsObj, dataObj, f).toString();
        if (!new File(f).exists()) {
            return "";
        }
        int i = f.lastIndexOf(".");
        if (i > 0 && i < f.length() - 1) {
            ext = f.substring(i + 1).toUpperCase();
        }
        return ext;
    }

    /**
     * This method returns the file name of a given file-path which is passed as
     * parameter.
     *
     * @param f the filepath of the file which file name should be retrieved
     * @return the name of the given file, excluding extension, or {@code null}
     * if an error occured.
     */
    public static String getFileName(String f) {
        String fn = null;
        int i = f.lastIndexOf(String.valueOf(File.separatorChar));
        if (i != -1) {
            int j = f.lastIndexOf(".");
            if (j != -1) {
                try {
                    fn = f.substring(i + 1, j);
                } catch (IndexOutOfBoundsException ex) {
                    return null;
                }
            }
        }
        return fn;
    }

    /**
     * This method returns the file path only of a given file which is passed as
     * parameter, <em>excluding the file name</em>.
     *
     * @param f the filepath of the file which should be cleaned from file name
     * @return the path of the given file, excluding file name, or an empty
     * string if an error occured.
     */
    public static String getFilePath(String f) {
        String fn = "";
        int i = f.lastIndexOf(String.valueOf(File.separatorChar));
        if (i != -1) {
            try {
                fn = f.substring(0, i);
            } catch (IndexOutOfBoundsException ex) {
                return "";
            }
        }
        return fn;
    }

    /**
     * This method returns the file path only of a given file which is passed as
     * parameter, <em>excluding the file name</em>.
     *
     * @param f the filepath of the file which should be cleaned from file name
     * @return the path of the given file, excluding file name, or an empty
     * string if an error occured.
     */
    public static String getFilePath(File f) {
        try {
            return getFilePath(f.getCanonicalPath());
        } catch (IOException ex) {
            return "";
        }
    }

    /**
     * This method returns the file name of a given file-path which is passed as
     * parameter.
     *
     * @param f the filepath of the file which file name should be retrieved
     * @return the name of the given file, excluding extension, or {@code null}
     * if an error occured.
     */
    public static String getFileName(File f) {
        if (f != null) {
            String fname = f.getName();
            int extpos = fname.lastIndexOf(".");
            if (extpos != -1) {
                try {
                    return fname.substring(0, extpos);
                } catch (IndexOutOfBoundsException ex) {
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * This method copies a file. check for existing files is not done here,
     * needs to be checked from within the method which calls this method.
     *
     * @param src the source file that should be copied
     * @param dest the destination filename and path
     * @param bufSize the buffer size
     * @throws IOException
     */
    public static void copyFile(File src, File dest, int bufSize) throws IOException {
        byte[] buffer = new byte[bufSize];
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(src);
            out = new FileOutputStream(dest);
            while (true) {
                int read = in.read(buffer);
                if (read == -1) {
                    break;
                }
                out.write(buffer, 0, read);
            }
        } catch (IOException | NullPointerException e) {
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * This merthod creates a FileChooser Dialog, initiates it (based on the
     * passed parameters) and returns a reference to the FileChooser Dialog.
     *
     * @param name the file dialog's title
     * @param filemode {@code JFileChooser.FILES_ONLY} or the other
     * file-selection-mode-values (not important for Mac OS X)
     * @param curdir the initial directory which can be set when the dialog is
     * shown
     * @param acceptedext the accepted file extensions that will be accepted,
     * i.e. the files that are selectable
     * @param desc the description of which file types the extensions are
     * @return a reference to a new created file chooser
     */
    public static JFileChooser createFileChooser(String name, int filemode, File curdir, final String[] acceptedext, final String desc) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle(name);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setCurrentDirectory(curdir);
        fc.setFileSelectionMode(filemode);
        if (null == acceptedext || acceptedext.length < 1) {
            fc.setAcceptAllFileFilterUsed(true);
        } else {
            fc.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    }
                    // set file extensions in the file dialog according
                    // to user's choice of import type
                    // other accepted files are located in the resource map
                    boolean retval = false;
                    String fileext = "." + getFileExtension(f);
                    if (acceptedext != null && acceptedext.length > 0) {
                        for (String ext : acceptedext) {
                            if (fileext.equals(ext.toLowerCase())) {
                                retval = true;
                            }
                        }
                    }
                    return retval;
                }

                @Override
                public String getDescription() {
                    return desc;
                }
            });
        }
        return fc;
    }

    /**
     * A directory-chooser for choosing directories (when file-selecting should
     * be restricted).
     *
     * @param name the dialog's title
     * @param desc the locale for "folders"
     * @param curdir the initial directory to be set
     * @return the selected directory as File-variable, or {@code null} if user
     * cancels the dialog
     */
    public static File chooseDirectory(String name, final String desc, File curdir) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle(name);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setCurrentDirectory(curdir);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }

            @Override
            public String getDescription() {
                return desc;
            }
        });
        int option = fc.showOpenDialog(null);
        if (JFileChooser.APPROVE_OPTION == option) {
            return fc.getSelectedFile();
        }
        return null;
    }

    public static boolean insertAttachments(Daten dataObj, Settings settingsObj, JFrame parentFrame, File[] sources, DefaultListModel lm) {
        org.jdesktop.application.ResourceMap resourceMap
                = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
                getContext().getResourceMap(FileOperationsUtil.class);
        if (lm != null) {
            linkListModel = lm;
        } else {
            linkListModel = new DefaultListModel();
        }
        List<String> addedValues = new ArrayList<>();
        // modified tag
        boolean modified = false;
        // declare constants for moving/copying files
        // files should be copied
        final int ATT_COPY = 0;
        // files should be moved
        final int ATT_MOVE = 1;
        // files should remain in their original folder
        final int ATT_REMAIN = 2;
        // action should be cancelled
        final int ATT_CANCEL = 3;
        // declare constants for renaming/keeping files
        // files should be renamed
        final int ATT_RENAME = 0;
        // existing attachment file should be used
        final int ATT_USE_EXISTING = 1;
        // action should be cancelled
        final int ATT_CANCEL_RENAME = 2;
        // check whether we already have a saved data file or not. if not, we have no related
        // path for the subdirectory "img", thus we cannot copy the images
        if (null == settingsObj.getFilePath() || !settingsObj.getFilePath().exists()) {
            // display error message box
            JOptionPane.showMessageDialog(parentFrame, resourceMap.getString("noDataFileSavedMsg"), resourceMap.getString("noDataFileSavedTitle"), JOptionPane.PLAIN_MESSAGE);
            return false;
        }
        // retrieve the application's directory and add an "/img/" as subfolder for images
        // and append the file name of the image file. we need this string already now for the
        // message box to tell the user where the file will be copied to.
        String destdir = settingsObj.getAttachmentPath(dataObj.getUserAttachmentPath(), true);
        // create new linked list that will contain a "cleaned" list of files, i.e. only contains
        // those selected files that haven't been copied to the attachment directory yet.
        LinkedList<File> newfiles = new LinkedList<>();
        // iterate array
        for (File cf : sources) {
            // first off all, let's check whether the user chose an already existing attachment-file
            // which already has been copied to the application's attachment directory. if so, no
            // new copy operation is needed, thus we *exclude* that file from the copy-list,
            // but already *include* it to the jList with attachments...
            // if the source file does not start with the same string part as the application's
            // applications directory, we assume that the attachment has not been copied to that directory yet.
            if (!cf.toString().startsWith(destdir)) {
                newfiles.add(cf);
            } else {
                // remove first part of the file-path, so we have the related path
                // to the attachment left
                String remainingpath = cf.toString().substring(destdir.length());
                // add attachment-value to list
                linkListModel.addElement(remainingpath);
                addedValues.add(remainingpath);
                // set the modified state
                modified = true;
            }
        }
        // if we don't have any new files that haven't been copied to the attachment-directory before,
        // we can leave the method now...
        if (newfiles.size() < 1) {
            return modified;
        }
        // else create new array with files to be copied.
        sources = newfiles.toArray(new File[newfiles.size()]);
        // create a JOptionPane with moce/copy/cancel options
        int msgOption = JOptionPane.showOptionDialog(parentFrame,
                resourceMap.getString("msgConfirmAttachmentCopyMsg", destdir),
                resourceMap.getString("msgConfirmAttachmentCopyTitle"),
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new Object[]{
                    resourceMap.getString("optionFileCopy"),
                    resourceMap.getString("optionFileMove"),
                    resourceMap.getString("optionFileRemain"),
                    resourceMap.getString("optionFileCancel"),},
                resourceMap.getString("optioneFileCopy"));
        // if the user wants to proceed, copy the image now
        if (ATT_COPY == msgOption || ATT_MOVE == msgOption) {
            // first, check whether we already have an attachment directory
            // create the file-object with the necessary directory path
            File attachmentdir = new File(destdir);
            // if the directory does not exist, create it
            if (!attachmentdir.exists()) {
                // create directory
                try {
                    if (!attachmentdir.mkdir()) {
                        // if it fails, show warning message and leave method
                        // create a message string including the filepath of the directory
                        // which could not be created
                        JOptionPane.showMessageDialog(parentFrame, resourceMap.getString("errMsgCreateAttDirMsg", attachmentdir), resourceMap.getString("errMsgCreateDirTitle"), JOptionPane.PLAIN_MESSAGE);
                        return false;
                    }
                } catch (SecurityException e) {
                    Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
                    // if it fails, show warning message and leave method
                    // create a message string including the filepath of the directory
                    // which could not be created
                    JOptionPane.showMessageDialog(parentFrame, resourceMap.getString("errMsgCreateAttDirMsg", attachmentdir), resourceMap.getString("errMsgCreateDirTitle"), JOptionPane.PLAIN_MESSAGE);
                    return false;
                }
            }
            // go through all selected files
            for (File f : sources) {
                // store the fileextension for later use, see below
                String fileextension = FileOperationsUtil.getFileExtension(f);
                // create a string to replace german umlauts
                // we have to do this because it seems like the Java desktop-api
                // is buggy. Files with umlauts in their names or paths cannot be
                // opend by the desktop-api, although their path is correct
                String withoutumlauts = f.getName();
                // replace umlauts with normal alphabetical letters
                withoutumlauts = withoutumlauts.replace("ä", "ae")
                        .replace("Ä", "Ae")
                        .replace("ö", "oe")
                        .replace("Ö", "Oe")
                        .replace("ü", "ue")
                        .replace("Ü", "Ue")
                        .replace("ß", "ss")
                        .replace("\"", "");
                // create destionation file
                File dest = new File(destdir + withoutumlauts);
                // create loop-indicator
                boolean dest_ok = false;
                // indicator whether further action is needed
                boolean copyneeded = true;
                // check whether the file exists. if yes, the user should enter another name
                while (!dest_ok) {
                    // check whether file exists
                    if (dest.exists()) {
                        // tell user that file exist and ask whether file should be renamed, or
                        // existing file should be used as value
                        // create a JOptionPane with rename/use exiting file options
                        int msgOpt = JOptionPane.showOptionDialog(parentFrame,
                                resourceMap.getString("msgFileExistsChoice"),
                                resourceMap.getString("msgFileExistsChoiceTitle"),
                                JOptionPane.YES_NO_CANCEL_OPTION,
                                JOptionPane.PLAIN_MESSAGE,
                                null,
                                new Object[]{
                                    resourceMap.getString("optionFileRename"),
                                    resourceMap.getString("optionFileUseExisting"),
                                    resourceMap.getString("optionFileCancel"),},
                                resourceMap.getString("optionFileUseExisting"));
                        // if action cancelled, quit
                        if (ATT_CANCEL_RENAME == msgOpt) {
                            return false;
                        }
                        // if existing attachment should be used, do this here
                        if (ATT_USE_EXISTING == msgOpt) {
                            // add copied/moves file to link list...
                            linkListModel.addElement((String) dest.getName());
                            addedValues.add((String) dest.getName());
                            // set the modified state
                            modified = true;
                            // indicate that while-loop is over, destination is valid
                            dest_ok = true;
                            // no more copying needed
                            copyneeded = false;
                        } else if (ATT_RENAME == msgOpt) {
                            // open an option dialog and let the user prompt a new filename
                            Object fnobject = JOptionPane.showInputDialog(parentFrame, resourceMap.getString("msgFileExists"), resourceMap.getString("msgFileExistsTitle"), JOptionPane.PLAIN_MESSAGE, null, null, dest.getName());
                            // if the user cancelled the dialog, quit method
                            if (null == fnobject) {
                                return false;
                            }
                            // else copy object to string
                            String newfilename = fnobject.toString();
                            // check whether the user just typed in a name without extension
                            // if so, add extension here
                            if (!newfilename.endsWith("." + fileextension)) {
                                newfilename = newfilename.concat("." + fileextension);
                            }
                            // and create a new file
                            dest = new File(destdir + newfilename);
                        }
                    } else {
                        // indicate that while-loop is over, destination is valid
                        dest_ok = true;
                    }
                }

                if (copyneeded) {
                    try {
                        // here we go when the user wants to *copy* the files
                        if (ATT_COPY == msgOption) {
                            // create and copy file...
                            dest.createNewFile();
                            // if we have a file which does not already exist, copy the source to the dest
                            FileOperationsUtil.copyFile(f, dest, 1024);
                        } // here we go when the user wants to *move* the files
                        else if (ATT_MOVE == msgOption) {
                            // if moving the file failed...
                            if (!f.renameTo(dest)) {
                                // ... show error msg
                                JOptionPane.showMessageDialog(parentFrame, resourceMap.getString("errMsgFileMove"), resourceMap.getString("errMsgFileMoveTitle"), JOptionPane.PLAIN_MESSAGE);
                            }
                        }
                        // add copied/moves file to link list...
                        linkListModel.addElement((String) dest.getName());
                        addedValues.add((String) dest.getName());
                        // set the modified state
                        modified = true;
                        // set new default directory
                        settingsObj.setLastOpenedAttachmentDir(f);
                    } catch (IOException ex) {
                        Constants.zknlogger.log(Level.SEVERE, ex.getLocalizedMessage());
                        JOptionPane.showMessageDialog(parentFrame, resourceMap.getString("errMsgFileCopy"), resourceMap.getString("errMsgFileCopyTitle"), JOptionPane.PLAIN_MESSAGE);
                    }
                }
            }
        } else if (ATT_CANCEL == msgOption) {
            // do nothing...
        } else if (ATT_REMAIN == msgOption) {
            // else add the text to the keyword-list (JList)
            for (File f : sources) {
                linkListModel.addElement((String) f.toString());
                addedValues.add((String) f.toString());
                // set new default directory
                settingsObj.setLastOpenedAttachmentDir(f);
            }
            // set the modified state
            modified = true;
        }
        // convert list to string array
        addedAttachments = addedValues.toArray(new String[addedValues.size()]);
        return modified;
    }

    /**
     * This methods returns the path to an entry's attachment-file, which is
     * passed as paraneter {@code linktye}. It creates an absolute path out from
     * a relative path if necessray, which e.g. occures when having
     * entry-attachments.
     *
     * @param settingsObj a reference to the CSettings-class
     * @param dataObj
     * @param linktype a hyperlink-file, which is received when the user clicks
     * on a hyperlink or linked attachment in e jEditorPane
     * @return the absolute filepath to the linked file
     */
    public static File getLinkFile(Settings settingsObj, Daten dataObj, String linktype) {
        File linkfile = new File(linktype);
        if (!linkfile.exists()) {
            String sepchar = String.valueOf(File.separatorChar);
            if (linkfile.toString().startsWith(sepchar)) {
                sepchar = "";
            }
            linkfile = new File(settingsObj.getAttachmentPath(dataObj.getUserAttachmentPath(), false) + sepchar + linkfile.toString());
            if (!linkfile.exists()) {
                try {
                    linkfile = new File(linkfile.getCanonicalPath());
                    if (!linkfile.exists()) {
                        linkfile = new File(linktype);
                        File zknp = settingsObj.getBaseDir();
                        if (null == zknp) {
                            zknp = new File(new File("").getAbsolutePath());
                        }
                        if (linkfile.toString().startsWith(sepchar)) {
                            sepchar = "";
                        }
                        linkfile = new File(zknp.toString() + sepchar + linkfile.toString());
                        if (!linkfile.exists()) {
                            linkfile = new File(linkfile.getCanonicalPath());
                        }
                    }
                } catch (IOException | SecurityException ex) {
                }
            }
        }
        return linkfile;
    }

    /**
     * This method checks whether the file's {@code imgfile} extenstion is
     * corresponding to an image file, i.e. whether the file extension equals
     * one of the most common image formats.
     *
     * @param imgfile the file which extension should be checked
     * @return {@code true} if the the file seems to be an image, {@code false}
     * otherwise.
     */
    public static boolean isImageFile(File imgfile) {
        boolean retval;
        String ext = FileOperationsUtil.getFileExtension(imgfile);
        retval = ext.equalsIgnoreCase("jpg") | ext.equalsIgnoreCase("png") | ext.equalsIgnoreCase("bmp") | ext.equalsIgnoreCase("tif") | ext.equalsIgnoreCase("tiff") | ext.equalsIgnoreCase("gif") | ext.equalsIgnoreCase("jpeg") | ext.equalsIgnoreCase("tga");
        return retval;
    }

    /**
     * This method retrieves a complete formtag, extracts the information and
     * converts the tag into the related image-path, which can be used for
     * including the related form-image (which is stored as image-file in the
     * subdirectory {@code Constants.FORMIMAGEPATH_SUBDIR}.
     *
     * @param formtag the form-tag, which contains the form-data in UBB-format
     * @param addFileExtension
     * @param addLargeAppendix
     * @return a string containing the file-name of the related form-image that
     * represents the graphical darstellung of the form-tag, or an empty string
     * if an error occured.
     */
    public static String convertFormtagToImagepath(String formtag, boolean addFileExtension, boolean addLargeAppendix) {
        String imgpath = "";
        if (formtag != null && !formtag.isEmpty()) {
            formtag = formtag.replace(" ", "_");
            formtag = formtag.replace("=", "_");
            formtag = formtag.replace("#", "r");
            formtag = formtag.replace("|", "_");
            formtag = formtag.replace("^", "__");
            formtag = formtag.replace("[", "").replace("]", "");
            formtag = formtag.replace("\u00e4", "ae").replace("\u00c4", "Ae").replace("\u00f6", "oe").replace("\u00d6", "Oe").replace("\u00fc", "ue").replace("\u00dc", "Ue").replace("\u00df", "ss").replace("?", "_").replace("\"", "");
            imgpath = formtag;
            if (addLargeAppendix) {
                imgpath = imgpath + Constants.FORMIMAGE_LARGE_APPENDIX;
            }
            if (addFileExtension) {
                imgpath = imgpath + Constants.FORMIMAGE_EXTENSION;
            }
        }
        return imgpath;
    }

    /**
     * This method checks whether the file's {@code imgfile} extenstion is
     * corresponding to an image file and whether this image format is supported
     * by the JEditorPane component.
     *
     * @param imgfile the file which extension should be checked
     * @return {@code true} if the the file seems to be an image which is also
     * supported to be displayed in a JEditorPane, {@code false} otherwise.
     */
    public static boolean isSupportedImageFile(File imgfile) {
        // TODO andere Grafikformate später, wenn unterstützt
        boolean retval;
        String ext = FileOperationsUtil.getFileExtension(imgfile);
        retval = ext.equalsIgnoreCase("jpg") | ext.equalsIgnoreCase("png") | ext.equalsIgnoreCase("gif") | ext.equalsIgnoreCase("jpeg");
        return retval;
    }

    /**
     * This method checks whether a given string, usually passed as parameter
     * from the
     * {@link #eventHyperlinkActivated(javax.swing.event.HyperlinkEvent) eventHyperlinkActivated},
     * is a hyperlink or not. this is decided from the prefix, i.e. whether the
     * string starts with common things like "http:" etc.
     *
     * @param linktype the clicked "hyperlink" from the hyperlink-event, as
     * string
     * @return {@code true} if the string seems to be a hyperlink, false
     * otherwise
     */
    public static boolean isHyperlink(String linktype) {
        return linktype.toLowerCase().startsWith("http://") || linktype.toLowerCase().startsWith("https://") || linktype.toLowerCase().startsWith("ftp://") || linktype.toLowerCase().startsWith("webdav://") || linktype.toLowerCase().startsWith("news:") || linktype.toLowerCase().startsWith("outlook:");
    }

    public static String getZettelkastenDataDir(Settings settings) {
        return getZettelkastenDataDir(settings, true);
    }

    /**
     *
     * @param settings
     * @param addTrailingSeparatorChar
     * @return
     */
    public static String getZettelkastenDataDir(Settings settings, boolean addTrailingSeparatorChar) {
        // retrieve path of data file
        File f = settings.getBaseDir();
        // check for valid value
        if (f != null) {
            // convert file path to string
            String path = f.getPath();
            // check whether trailing separator should be included or not
            if (addTrailingSeparatorChar) {
                path = path.concat(String.valueOf(File.separatorChar));
            }
            // retrieve substring, excluding filename
            return path;
        }
        return null;
    }

    /**
     *
     * @return
     */
    public static String getWorkingDir() {
        return System.getProperty("user.dir").concat(String.valueOf(File.separatorChar));
    }

    /**
     * Retrieve the {@code .Zettelkasten} subdirectory of the user home
     * directory.
     *
     * @return The {@code .Zettelkasten} directory, which is a subdirectory of
     * the user.home directory, including a trailing separator char
     * ({@code <user-home>/.Zettelkasten/}).
     */
    public static String getZettelkastenHomeDir() {
        return getZettelkastenHomeDir(true);
    }

    /**
     * Retrieve the {@code .Zettelkasten} subdirectory of the user home
     * directory.
     *
     * @param addTrailingSeparatorChar if {@code true}, a trailing separator
     * char is added.
     * @return The {@code .Zettelkasten} directory, which is a subdirectory of
     * the user.home directory.
     */
    public static String getZettelkastenHomeDir(boolean addTrailingSeparatorChar) {
        String fp = System.getProperty("user.home") + File.separatorChar + ".Zettelkasten";
        if (addTrailingSeparatorChar) {
            fp = fp + File.separatorChar;
        }
        return fp;
    }
    
    /**
     * This method removes invalid characters from a file path.
     * 
     * @param path the (uncleaned) file path, which might contain illegal characters.
     * @return a cleaned path as {@code String} value, with illegal characters removed.
     */
    public static String getCleanFilePath(String path) {
        return path.replaceAll("[^a-zA-ZäöüÄÖÜß0-9.-]", "_");
    }
}
