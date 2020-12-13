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

import de.danielluedecke.zettelkasten.CBiggerEditField;
import de.danielluedecke.zettelkasten.ZettelkastenApp;
import de.danielluedecke.zettelkasten.ZettelkastenView;
import de.danielluedecke.zettelkasten.database.*;
import de.danielluedecke.zettelkasten.database.BibTeX;
import de.danielluedecke.zettelkasten.tasks.TaskProgressDialog;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Luedeke
 */
public class ZettelkastenViewUtil {

    private static TaskProgressDialog taskDlg = null;
    private static CBiggerEditField biggerEditDlg = null;

    /**
     * This method opens files or folders (attchments). Typically used when
     * opening attachments. First, this method tries to open a file using the
     * {@code Desktop} api from Java. If this fails, on Windows and Linux the
     * {@code Runtime}.
     *
     * @param path The path to the file that should be opened
     * @param settings a reference to the Settings class
     * @return {@code true} if opening the file was successfull, {@code false}
     * otherwise.
     */
    public static boolean openFilePath(String path, Settings settings) {
        File linuxpath = new File(path);
        // on linux, check whether we have white spaces
        if (linuxpath.toString().contains(" ")) {
            // if so, replace them with ascii-number
            linuxpath = new File("file://" + linuxpath.toString().replaceAll(Pattern.quote(" "), Matcher.quoteReplacement("%20")));
        }
        // log file path, in case we need to debug user info
        Constants.zknlogger.log(Level.INFO, "Verwendeter Anhangspfad: {0}", linuxpath);
        // check whether desktop-api is supported
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            try {
                // open file
                Desktop.getDesktop().open(new File(path));
            } catch (IOException ex) {
                // log error
                Constants.zknlogger.log(Level.WARNING, ex.getLocalizedMessage());
                return false;
            }
        } else {
            try {
                // on windows, use Runtime with rundll32
                if (PlatformUtil.isWindows()) {
                    Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL \"" + path + "\"");
                } else if (PlatformUtil.isLinux()) {
                    // on linux, use xdg-open
                    Runtime.getRuntime().exec("xdg-open " + linuxpath.getPath());
                }
            } catch (IOException ex) {
                Constants.zknlogger.log(Level.WARNING, ex.getLocalizedMessage());
                return false;
            }
        }
        return true;
    }

    /**
     * This method updates a jTable and a possible linked list which holds
     * filtered values from the jTables, by increasing ({@code diff} must be 1)
     * or decreasing ({@code diff} must be -1) an entry's occurences or
     * frequencies from the tablemodel and the linked list.
     * <br><br>
     * If no increase or decrease of frequencies (occurences) is requested, but
     * a complete removal, call
     * {@link #updateTableFrequencyRemove(javax.swing.JTable, java.util.LinkedList) updateTableFrequencyRemove(javax.swing.JTable, java.util.LinkedList)}
     * instead.
     *
     * @param table the table were we have to add a new value with frequency
     * @param list the possible linked list were we have to add a new value with
     * frequency
     * @param value the new value, for instance the author-string or
     * keyword-value
     * @param diff either +1, if a value was added, so frequency is increased by
     * 1. or -1, if a value was removed, so frequency is decreaded.
     * @return an updated linked list that was passed as parameter {@code list}
     */
    public static LinkedList<Object[]> updateTableFrequencyChange(JTable table, LinkedList<Object[]> list, String value, int diff) {
        // iterate all table rows
        for (int cnt = 0; cnt < table.getRowCount(); cnt++) {
            // check whether we have found the value that should be changed
            if (value.equals(table.getValueAt(cnt, 0).toString())) {
                // retrieve table data
                Object[] o = new Object[2];
                o[0] = table.getValueAt(cnt, 0);
                o[1] = table.getValueAt(cnt, 1);
                // convert frquency-counter to int
                int freq = Integer.parseInt(table.getValueAt(cnt, 1).toString());
                // set new value
                table.setValueAt(freq + diff, cnt, 1);
                // check whether we have a filtered list
                if (list != null) {
                    // if so, iterate list
                    for (int pos = 0; pos < list.size(); pos++) {
                        Object[] v = list.get(pos);
                        // check whether we have found the value that should be changed
                        if (o[0].toString().equals(v[0].toString())) {
                            // change frequency
                            o[1] = freq + diff;
                            list.set(pos, o);
                            break;
                        }
                    }
                }
            }
        }
        return list;
    }

    /**
     * This method updates a jTable and a possible linked list which holds
     * filtered values from the jTable, by adding new values to the table and
     * list. this method is called when new e.g. authors were edited (see
     * {@link ZettelkastenView#newAuthor() newAuthor()} or
     * {@link ZettelkastenView#editFinishedEvent() editFinishedEvent()}).
     *
     * @param table the table were we have to add a new value with frequency
     * @param list the possible linked list were we have to add a new value with
     * frequency
     * @param val the new value, for instance the author-string or keyword-value
     * @param occurences the frequency of the value {@code val}.
     * @return an updated linked list that was passed as parameter {@code list}
     */
    public static LinkedList<Object[]> updateTableFrequencyNew(JTable table, LinkedList<Object[]> list, String val, int occurences) {
        // get table model
        DefaultTableModel dtm = (DefaultTableModel) table.getModel();
        // create new item
        Object[] obj = new Object[2];
        obj[0] = val;
        obj[1] = occurences;
        // add to table
        dtm.addRow(obj);
        // check whether we have a list that contains filtered entries
        if (list != null) {
            // add item to list aswell
            list.add(obj);
        }
        return list;
    }

    /**
     * This method updates a jTable and a possible linked list which holds
     * filtered values from the jTables, by completely removing an entry/value
     * from the tablemodel and the linked list.
     * <br><br>
     * If no complete removal is requested, but a decrease in the frequencies,
     * call
     * {@link #updateTableFrequencyDelete(javax.swing.JTable, java.util.LinkedList) updateTableFrequencyDelete(javax.swing.JTable, java.util.LinkedList)}
     * instead.
     *
     * @param table the table were we have to add a new value with frequency
     * @param list the possible linked list were we have to add a new value with
     * frequency
     * @param zettelkastenView
     * @return an updated linked list that was passed as parameter {@code list}
     */
    public static LinkedList<Object[]> updateTableFrequencyRemove(JTable table, LinkedList<Object[]> list, ZettelkastenView zettelkastenView) {
        // get table model
        DefaultTableModel dtm = (DefaultTableModel) table.getModel();
        // retrieve selected rows
        int[] rows = table.getSelectedRows();
        for (int cnt = rows.length - 1; cnt >= 0; cnt--) {
            try {
                int selectedrow = table.convertRowIndexToModel(rows[cnt]);
                if (list != null) {
                    Object[] o = new Object[2];
                    o[0] = dtm.getValueAt(selectedrow, 0);
                    o[1] = dtm.getValueAt(selectedrow, 1);
                    int pos = findInLinkedList(list, o);
                    if (pos != -1) {
                        list.remove(pos);
                    }
                }
                dtm.removeRow(selectedrow);
            } catch (ArrayIndexOutOfBoundsException e) {
                Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
            }
        }
        return list;
    }

    private static int findInLinkedList(LinkedList<Object[]> list, Object[] val) {
        // iterate list
        for (int pos = 0; pos < list.size(); pos++) {
            // get each element
            Object[] o = list.get(pos);
            // if element equals requested value, change frequency
            if (val[0].toString().equals(o[0].toString()) && val[1].toString().equals(o[1].toString())) {
                return pos;
            }
        }
        return -1;
    }

    /**
     * This method retrieves the entry-number of the first selected entry in the
     * tabbed pane with the a jTable
     *
     * @param data
     * @param table a reference to the swing-table, from which we want to
     * retrieve the entry
     * @param column the column which holds the entry-numbers. in most cases
     * this is column 0, but some tables store the entry-numbers in column 1
     * @return the entry number of the selected entry in a table (<i>not</i> the
     * number of the selected table row!), or -1 if nothing is selected
     */
    public static int retrieveSelectedEntryFromTable(Daten data, javax.swing.JTable table, int column) {
        // if no data available, leave method
        if (data.getCount(Daten.ZKNCOUNT) < 1) {
            return -1;
        }
        // get the amount of selected rows
        int rowcount = table.getSelectedRowCount();
        // if we have no selected values, leave method
        if (rowcount < 1) {
            return -1;
        }
        int entrynr;
        // get the selected row
        int rows = table.getSelectedRow();
        try {
            // iterate all selected values and copy all values to array
            entrynr = Integer.parseInt(table.getValueAt(rows, column).toString());
        } catch (NumberFormatException e) {
            return -1;
        }
        // and return the array
        return entrynr;
    }

    /**
     * This method retrieves the entry-numbers of all selected entries in the
     * tabbed pane with the a jTable
     *
     * @param data
     * @param table a reference to the swing-table, from which we want to
     * retrieve the entry
     * @param column the column which holds the entry-numbers. in most cases
     * this is column 0, but some tables store the entry-numbers in column 1
     * @return the entry numbers of all selected entries in a table (<i>not</i>
     * the number of the selected table row!), or {@code null} if nothing is
     * selected
     */
    public static int[] retrieveSelectedEntriesFromTable(Daten data, javax.swing.JTable table, int column) {
        // if no data available, leave method
        if (data.getCount(Daten.ZKNCOUNT) < 1) {
            return null;
        }
        // get the amount of selected rows
        int rowcount = table.getSelectedRowCount();
        // if we have no selected values, leave method
        if (rowcount < 1) {
            return null;
        }
        // get the selected rows
        int[] rows = table.getSelectedRows();
        // create string array as parameter
        int[] selectedValues = new int[rows.length];
        try {
            // iterate all selected values and copy all values to array
            for (int cnt = 0; cnt < rows.length; cnt++) {
                selectedValues[cnt] = Integer.parseInt(table.getValueAt(rows[cnt], column).toString());
            }
            // and return the array
            return selectedValues;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * This method retrieves the input from the entry-number-textfield and
     * checks whether the user activated any "hidden feature".<br><br>
     * Use following input to activate feature:<br>
     * - Entrynumber: Displays the related entry<br> - {@code ?}: displays a
     * random entry<br> - {@code m}: toggles the memory-usage-label and logs
     * memory usage<br> - {@code e}: displays the (error-)logfile<br> -
     * {@code s}: displays the directory with the settings-file<br> -
     * {@code cs}: clears the search-request-data<br> - {@code cd}: clears the
     * desktop-data<br> - {@code rs}: resets the settings-file<br> - {@code ra}:
     * resets the accelerator-keys-file, i.e. the menu-shortcuts<br> -
     * {@code rf}: resets the keywords- and author-frequencies<br> -
     * {@code xml}: shows the current XML database in a new window. just for
     * testing purposes<br> - {@code usb}: copies the settings-files to the
     * current application's directory. this is helpful when the user wants a
     * portable version, so the settings-files can be copied to the
     * usb-flash-device.
     *
     * @param mainframe
     * @param jTextFieldEntryNumber
     * @param data
     * @param searchrequests
     * @param desktop
     * @param settings
     * @param acceleratorKeys
     * @param bibtex
     * @param displayedZettel
     */
    public static void hiddenFeatures(ZettelkastenView mainframe, javax.swing.JTextField jTextFieldEntryNumber, Daten data, SearchRequests searchrequests, DesktopData desktop, Settings settings, AcceleratorKeys acceleratorKeys, BibTeX bibtex, int displayedZettel) {
        // here we have some "hidden features".
        String t = jTextFieldEntryNumber.getText();
        // the "?" shows a random entry
        switch (t) {
            case "?":
                mainframe.showRandomEntry();
                break;
            case "m":
                mainframe.toggleMemoryTimer();
                jTextFieldEntryNumber.setText(String.valueOf(data.getCurrentZettelPos()));
                break;
            case "e":
                mainframe.showErrorLog();
                jTextFieldEntryNumber.setText(String.valueOf(data.getCurrentZettelPos()));
                break;
            case "cs":
                // open a confirm dialog
                int option = JOptionPane.showConfirmDialog(mainframe.getFrame(),
                        mainframe.getResourceMap().getString("confirmClearSearchesMsg"),
                        mainframe.getResourceMap().getString("confirmClearSearchesTitle"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.PLAIN_MESSAGE);
                // if action should be performed, do so
                if (JOptionPane.YES_OPTION == option) {
                    searchrequests.clear();
                    searchrequests.setModified(true);
                }
                jTextFieldEntryNumber.setText(String.valueOf(data.getCurrentZettelPos()));
                break;
            case "cd":
                // open a confirm dialog
                option = JOptionPane.showConfirmDialog(mainframe.getFrame(),
                        mainframe.getResourceMap().getString("confirmClearDesktopMsg"),
                        mainframe.getResourceMap().getString("confirmClearDesktopTitle"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.PLAIN_MESSAGE);
                // if action should be performed, do so
                if (JOptionPane.YES_OPTION == option) {
                    desktop.clear();
                    desktop.setModified(true);
                }
                jTextFieldEntryNumber.setText(String.valueOf(data.getCurrentZettelPos()));
                break;
            case "cb":
                // open a confirm dialog
                option = JOptionPane.showConfirmDialog(mainframe.getFrame(),
                        mainframe.getResourceMap().getString("confirmClearBibtexMsg"),
                        mainframe.getResourceMap().getString("confirmClearBibtexTitle"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.PLAIN_MESSAGE);
                // if action should be performed, do so
                if (JOptionPane.YES_OPTION == option) {
                    bibtex.clearEntries();
                }
                jTextFieldEntryNumber.setText(String.valueOf(data.getCurrentZettelPos()));
                break;
            case "rs":
                // open a confirm dialog
                option = JOptionPane.showConfirmDialog(mainframe.getFrame(),
                        mainframe.getResourceMap().getString("confirmResetSettingsMsg"),
                        mainframe.getResourceMap().getString("confirmResetSettingsTitle"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.PLAIN_MESSAGE);
                // if action should be performed, do so
                if (JOptionPane.YES_OPTION == option) {
                    settings.clear();
                }
                jTextFieldEntryNumber.setText(String.valueOf(data.getCurrentZettelPos()));
                break;
            case "ra":
                acceleratorKeys.initAcceleratorKeys();
                jTextFieldEntryNumber.setText(String.valueOf(data.getCurrentZettelPos()));
                break;
            case "s":
                try {
                    Desktop.getDesktop().open(new File(FileOperationsUtil.getZettelkastenHomeDir(false)));
                } catch (IOException ex) {
                    // log error-message
                    Constants.zknlogger.log(Level.WARNING, ex.getLocalizedMessage());
                }   // show current entry number again
                jTextFieldEntryNumber.setText(String.valueOf(data.getCurrentZettelPos()));
                break;
            case "rf":
                // open a confirm dialog
                option = JOptionPane.showConfirmDialog(mainframe.getFrame(),
                        mainframe.getResourceMap().getString("confirmResetFreqMsg"),
                        mainframe.getResourceMap().getString("confirmResetFreqTitle"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.PLAIN_MESSAGE);
                // if action should be performed, do so
                if (JOptionPane.YES_OPTION == option) {
                    // if dialog window isn't already created, do this now
                    if (null == taskDlg) {
                        // get parent und init window
                        taskDlg = new TaskProgressDialog(mainframe.getFrame(),
                                TaskProgressDialog.TASK_UPDATEFILE,
                                settings,
                                data,
                                desktop,
                                bibtex,
                                true);
                        // center window
                        taskDlg.setLocationRelativeTo(mainframe.getFrame());
                    }
                    ZettelkastenApp.getApplication().show(taskDlg);
                    // dispose the window and clear the object
                    taskDlg.dispose();
                    taskDlg = null;
                }
                jTextFieldEntryNumber.setText(String.valueOf(data.getCurrentZettelPos()));
                break;
            case "m2u":
                // open a confirm dialog
                option = JOptionPane.showConfirmDialog(mainframe.getFrame(),
                        mainframe.getResourceMap().getString("confirmM2UMsg"),
                        mainframe.getResourceMap().getString("confirmM2UTitle"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.PLAIN_MESSAGE);
                // if action should be performed, do so
                if (JOptionPane.YES_OPTION == option) {
                    // if dialog window isn't already created, do this now
                    if (null == taskDlg) {
                        // get parent und init window
                        taskDlg = new TaskProgressDialog(mainframe.getFrame(), TaskProgressDialog.TASK_CONVERTFORMATTAGS, data, Tools.MARKDOWN2UBB);
                        // center window
                        taskDlg.setLocationRelativeTo(mainframe.getFrame());
                    }
                    ZettelkastenApp.getApplication().show(taskDlg);
                    // dispose the window and clear the object
                    taskDlg.dispose();
                    taskDlg = null;
                    // update display
                    mainframe.updateDisplayParts(displayedZettel);
                }
                break;
            case "u2m":
                // open a confirm dialog
                option = JOptionPane.showConfirmDialog(mainframe.getFrame(), mainframe.getResourceMap().getString("confirmU2MMsg"), mainframe.getResourceMap().getString("confirmU2MTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
                // if action should be performed, do so
                if (JOptionPane.YES_OPTION == option) {
                    // if dialog window isn't already created, do this now
                    if (null == taskDlg) {
                        // get parent und init window
                        taskDlg = new TaskProgressDialog(mainframe.getFrame(), TaskProgressDialog.TASK_CONVERTFORMATTAGS, data, Tools.UBB2MARKDOWN);
                        // center window
                        taskDlg.setLocationRelativeTo(mainframe.getFrame());
                    }
                    ZettelkastenApp.getApplication().show(taskDlg);
                    // dispose the window and clear the object
                    taskDlg.dispose();
                    taskDlg = null;
                    // update display
                    mainframe.updateDisplayParts(displayedZettel);
                }
                break;
            case "xml":
                // open an input-dialog, setting the selected value as default-value
                if (null == biggerEditDlg) {
                    // create a new dialog with the bigger edit-field, passing some initial values
                    biggerEditDlg = new CBiggerEditField(mainframe.getFrame(), settings, "XML Database", Tools.retrieveXMLFileAsString(data), "", Constants.EDIT_OTHER);
                    // center window
                    biggerEditDlg.setLocationRelativeTo(mainframe.getFrame());
                }   // show window
                ZettelkastenApp.getApplication().show(biggerEditDlg);
                // delete the input-dialog
                biggerEditDlg.dispose();
                biggerEditDlg = null;
                jTextFieldEntryNumber.setText(String.valueOf(data.getCurrentZettelPos()));
                break;
            case "bib":
                // open an input-dialog, setting the selected value as default-value
                if (null == biggerEditDlg) {
                    try {
                        // create a new dialog with the bigger edit-field, passing some initial values
                        StringBuilder bibinfo = new StringBuilder("");
                        bibinfo.append("BibTeX-Daten (").append(String.valueOf(bibtex.getCount())).append(" Einträge)");
                        biggerEditDlg = new CBiggerEditField(mainframe.getFrame(), settings, bibinfo.toString(), bibtex.saveFile().toString("UTF-8"), "", Constants.EDIT_OTHER);
                        // center window
                        biggerEditDlg.setLocationRelativeTo(mainframe.getFrame());
                        // show window
                        ZettelkastenApp.getApplication().show(biggerEditDlg);
                    } catch (UnsupportedEncodingException ex) {
                    }
                }   // delete the input-dialog
                biggerEditDlg.dispose();
                biggerEditDlg = null;
                jTextFieldEntryNumber.setText(String.valueOf(data.getCurrentZettelPos()));
                break;
            case "html":
                // open an input-dialog, setting the selected value as default-value
                if (null == biggerEditDlg) {
                    // create a new dialog with the bigger edit-field, passing some initial values
                    biggerEditDlg = new CBiggerEditField(mainframe.getFrame(), settings, "HTML code of entry", data.getZettelContentAsHtml(displayedZettel), "", Constants.EDIT_OTHER);
                    // center window
                    biggerEditDlg.setLocationRelativeTo(mainframe.getFrame());
                }
                // show window
                ZettelkastenApp.getApplication().show(biggerEditDlg);
                // delete the input-dialog
                biggerEditDlg.dispose();
                biggerEditDlg = null;
                jTextFieldEntryNumber.setText(String.valueOf(data.getCurrentZettelPos()));
                break;
            case "html2":
                // open an input-dialog, setting the selected value as default-value
                if (null == biggerEditDlg) {
                    // create a new dialog with the bigger edit-field, passing some initial values
                    biggerEditDlg = new CBiggerEditField(mainframe.getFrame(),
                            settings, "HTML code of entry",
                            data.getEntryAsHtml(displayedZettel, null, Constants.FRAME_MAIN),
                            "", Constants.EDIT_OTHER);
                    // center window
                    biggerEditDlg.setLocationRelativeTo(mainframe.getFrame());
                }   // show window
                ZettelkastenApp.getApplication().show(biggerEditDlg);
                // delete the input-dialog
                biggerEditDlg.dispose();
                biggerEditDlg = null;
                jTextFieldEntryNumber.setText(String.valueOf(data.getCurrentZettelPos()));
                break;
            case "ubb":
                // open an input-dialog, setting the selected value as default-value
                if (null == biggerEditDlg) {
                    // create a new dialog with the bigger edit-field, passing some initial values
                    biggerEditDlg = new CBiggerEditField(mainframe.getFrame(), settings, "Original (unformatted) code of entry", data.getZettelContent(displayedZettel), "", Constants.EDIT_OTHER);
                    // center window
                    biggerEditDlg.setLocationRelativeTo(mainframe.getFrame());
                }   // show window
                ZettelkastenApp.getApplication().show(biggerEditDlg);
                // delete the input-dialog
                biggerEditDlg.dispose();
                biggerEditDlg = null;
                jTextFieldEntryNumber.setText(String.valueOf(data.getCurrentZettelPos()));
                break;
            case "usb":
                // create filepath for destination of settings-file. the destination is the application's directory
                File portableDestFile = new File(System.getProperty("user.dir") + File.separatorChar + "zettelkasten-settings.zks3");
                // create source-filepath for the settings-file, which usually is located in a sub-directory of the user's home dir.
                File portableSourceFile = new File(FileOperationsUtil.getZettelkastenHomeDir() + "zettelkasten-settings.zks3");
                // if source-path is valid and the settings-file exists, copy it to the flash-device
                if (portableSourceFile.exists()) {
                    try {
                        FileOperationsUtil.copyFile(portableSourceFile, portableDestFile, 1024);
                    } catch (IOException e) {
                        Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
                    }
                }   // create filepath for destination of settings-file. the destination is the application's directory
                portableDestFile = new File(System.getProperty("user.dir") + File.separatorChar + "zettelkasten-data.zkd3");
                // create source-filepath for the settings-file, which usually is located in a sub-directory of the user's home dir.
                portableSourceFile = new File(FileOperationsUtil.getZettelkastenHomeDir() + "zettelkasten-data.zkd3");
                // if source-path is valid and the settings-file exists, copy it to the flash-device
                if (portableSourceFile.exists()) {
                    try {
                        FileOperationsUtil.copyFile(portableSourceFile, portableDestFile, 1024);
                    } catch (IOException e) {
                        Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
                    }
                }
                jTextFieldEntryNumber.setText(String.valueOf(data.getCurrentZettelPos()));
                break;
            default:
                try {
                    // convert string into int value and show the entry
                    mainframe.showEntry(Integer.parseInt(t));
                } catch (NumberFormatException ex) {
                    // else reset textfield to old value
                    jTextFieldEntryNumber.setText(String.valueOf(data.getCurrentZettelPos()));
                }
                break;
        }
    }

    /**
     * This method retrieves the selected values of a table in the tabbed pane
     * and returns them in a string-array
     *
     * @param table a reference to the swing-table, from which we want to
     * retrieve the entry
     * @param column the column which holds the requested values.
     * @return an string-array with all values of the selected table-entries, or
     * null if nothing is selected
     */
    public static String[] retrieveSelectedValuesFromTable(JTable table, int column) {
        int[] rows = table.getSelectedRows();
        if (rows.length < 1) {
            return null;
        }
        String[] value = new String[rows.length];
        for (int cnt = 0; cnt < rows.length; cnt++) {
            value[cnt] = table.getValueAt(rows[cnt], column).toString();
        }
        return value;
    }
}
