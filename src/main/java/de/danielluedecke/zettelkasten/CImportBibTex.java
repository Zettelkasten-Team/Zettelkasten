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
package de.danielluedecke.zettelkasten;

import bibtex.dom.BibtexEntry;
import de.danielluedecke.zettelkasten.database.BibTeX;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.util.*;
import de.danielluedecke.zettelkasten.util.misc.Comparer;
import de.danielluedecke.zettelkasten.util.misc.InitStatusbarForTasks;
import org.jdesktop.application.Action;
import org.jdesktop.application.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.logging.Level;

/**
 *
 * @author danielludecke
 */
public class CImportBibTex extends javax.swing.JDialog {

    /**
     *
     */
    private final Settings settingsObj;
    /**
     *
     */
    private final BibTeX bibtexObj;
    /**
     *
     */
    private final Daten dataObj;
    /**
     * When the user wants to re-import BibTeX entries, abstracts/annotations of BibTeX entries that
     * create a new entry can be a) added as new entry, b) replace an existing entry that already
     * has been created from a BibTeX abstract or c) added to that existing entry.
     * <br><br>
     * The user's choice, made in {@link #addSelectedAuthors() addSelectedAuthors()}, is stored in
     * this variable
     */
    private int updateOption = -1;
    /**
     * The constants for the options stored in {@link #updateOption updateOption}.
     */
    private static final int UPDATE_OPTION_REPLACE = 0;
    private static final int UPDATE_OPTION_NEW = 1;
    private static final int UPDATE_OPTION_CONCAT = 2;
    private static final int UPDATE_OPTION_CANCEL = 3;

    public static final int BIBTEX_SOURCE_FILE = 1;
    public static final int BIBTEX_SOURCE_DB = 2;

    private final ZettelkastenView mainframe;

    /**
     * This array-list contains all entry-numbers of those entries that have been modified during
     * the import-operation, e.g. if BibTeX entries contained abstracts and the user chose to modify
     * existing entries.
     */
    private ArrayList<Integer> modifiedEntries;

    /**
     * This array-list contains all entry-numbers of those entries that have been modified during
     * the import-operation, e.g. if BibTeX entries contained abstracts and the user chose to modify
     * existing entries.
     *
     * @return all entry-numbers of those entries that have been modified during the
     * import-operation
     */
    public ArrayList<Integer> getModifiedEntries() {
        return modifiedEntries;
    }
    /**
     * This variable stores the amount of entries that have been added during the import-operation
     * (i.e. which are new), e.g. if BibTeX entries contained abstacts and the user chose to create
     * new entries.
     */
    private int newEntries = 0;

    /**
     * In case any BibTeX entries have been imported, this method returns the count of new added /
     * imported entries.
     *
     * @return The amount of imported entries.
     */
    public int getNewEntriesCount() {
        return newEntries;
    }
    /**
     *
     */
    private LinkedList<Object[]> linkedtablelist = null;

    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap
            = org.jdesktop.application.Application.getInstance(ZettelkastenApp.class).
            getContext().getResourceMap(CImportBibTex.class);

    /**
     *
     * @param parent
     * @param mf
     * @param d
     * @param bt
     * @param s
     */
    public CImportBibTex(java.awt.Frame parent, ZettelkastenView mf, Daten d, BibTeX bt, Settings s) throws IOException {
        super(parent);
        settingsObj = s;
        bibtexObj = bt;
        dataObj = d;
        mainframe = mf;
        initComponents();
        initComboBox();
        initBorders(settingsObj);
        // set combobox items
        jComboBoxShowBibTex.setModel(new javax.swing.DefaultComboBoxModel(new String[]{
            resourceMap.getString("comboBoxItem1"),
            resourceMap.getString("comboBoxItem2"),
            resourceMap.getString("comboBoxItem3")
        }));
        // set application icon
        setIconImage(Constants.zknicon.getImage());
        // set last settings
        switch (settingsObj.getLastUsedBibtexImportSource()) {
            case BIBTEX_SOURCE_DB:
                jRadioButtonSourceDB.setSelected(true);
                break;
            case BIBTEX_SOURCE_FILE:
                jRadioButtonSourceFile.setSelected(true);
                break;
            default:
                jRadioButtonSourceDB.setSelected(true);
                break;
        }
        initListeners();
        initTable();
        // init the progressbar and animated icon for background tasks
        InitStatusbarForTasks isb = new InitStatusbarForTasks(statusLabel, null, null);
        // initially, disable apply button
        jButtonApply.setEnabled(false);

        if (jRadioButtonSourceFile.isSelected()) {
            // retrieve file path of currently attached file, if any...
            File cuf = bibtexObj.getCurrentlyAttachedFile();
            File luf = bibtexObj.getFilePath();
            // and set it as initial value to the textfield
            if (cuf != null && cuf.exists()) {
                jTextFieldBibtexFilepath.setText(cuf.toString());
                fillBibTeXTable();
            } else if (luf != null && luf.exists()) {
                jTextFieldBibtexFilepath.setText(luf.toString());
                fillBibTeXTable();
            }
        } else {
            fillBibTeXTable();
        }
        jButtonRefresh.setEnabled(false);
    }

    private void initBorders(Settings settingsObj) {
        /*
         * Constructor for Matte Border
         * public MatteBorder(int top, int left, int bottom, int right, Color matteColor)
         */
        jScrollPane1.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ColorUtil.getBorderGray(settingsObj)));
    }

    private void initTable() {
        // create new table sorter
        TableRowSorter<TableModel> sorter = new TableRowSorter<>();
        // tell this jtable that it has an own sorter
        jTableBibEntries.setRowSorter(sorter);
        // and tell the sorter, which table model to sort.
        sorter.setModel(jTableBibEntries.getModel());
        // in this table, the first column needs a custom comparator.
        try {
            sorter.setComparator(0, new Comparer());
            sorter.setComparator(1, new Comparer());
        } catch (IndexOutOfBoundsException e) {
            Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
        }
        // noe header re-ordering
        jTableBibEntries.getTableHeader().setReorderingAllowed(false);
        // apply grid-settings and cell spacing
        jTableBibEntries.setGridColor(settingsObj.getTableGridColor());
        jTableBibEntries.setShowHorizontalLines(settingsObj.getShowGridHorizontal());
        jTableBibEntries.setShowVerticalLines(settingsObj.getShowGridVertical());
        jTableBibEntries.setIntercellSpacing(settingsObj.getCellSpacing());
        // get the default font size for tables and lists
        int defaultsize = settingsObj.getTableFontSize();
        // only set new fonts, when font size differs from the initial value
        if (defaultsize > 0) {
            // get current font
            Font f = jTableBibEntries.getFont();
            // create new font, add font size value
            f = new Font(f.getName(), f.getStyle(), f.getSize() + defaultsize);
            // set new font
            jTableBibEntries.setFont(f);
        }
    }

    /**
     * Here we set all available character-encodings for the BibTeX file. each reference manager
     * (jabref, refworks, citavi) has its own character-encoding, so we have to take this into
     * account when importing BibTeX files.
     */
    private void initComboBox() {
        // reset combobox
        jComboBoxEncoding.removeAllItems();
        // add items that show the BibTeX encodings
        for (String s : Constants.BIBTEX_DESCRIPTIONS) {
            jComboBoxEncoding.addItem(s);
        }
        try {
            // auto-select last used format, when we auto-load the last used BibTeX file
            jComboBoxEncoding.setSelectedIndex(settingsObj.getLastUsedBibtexFormat());
        } catch (IllegalArgumentException e) {
            Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
        }
        // reset combobox that contains the citation styles
        jComboBoxCiteStyle.removeAllItems();
        // add values for citation styles
        jComboBoxCiteStyle.addItem(resourceMap.getString("citeStyleGeneral"));
        jComboBoxCiteStyle.addItem(resourceMap.getString("citeStyleCBE"));
        jComboBoxCiteStyle.addItem(resourceMap.getString("citeStyleAPA"));
        try {
            // auto-select last used cite style, when we auto-load the last used BibTeX file
            jComboBoxCiteStyle.setSelectedIndex(bibtexObj.getCiteStyle());
        } catch (IllegalArgumentException e) {
            Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
        }
    }

    /**
     * Init several listeners for the components.
     */
    private void initListeners() {
        // these code lines add an escape-listener to the dialog. so, when the user
        // presses the escape-key, the same action is performed as if the user
        // presses the cancel button...
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        ActionListener cancelAction = new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                cancel();
            }
        };
        getRootPane().registerKeyboardAction(cancelAction, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        // when the radio-button for file-input is selected, we want to enable its related
        // components and disable all the stuff for the manual-selection-radio-button
        jComboBoxEncoding.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (jComboBoxEncoding.getSelectedIndex() != -1) {
                    settingsObj.setLastUsedBibtexFormat(jComboBoxEncoding.getSelectedIndex());
                }
            }
        });
        jComboBoxCiteStyle.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (jComboBoxCiteStyle.getSelectedIndex() != -1) {
                    // change cite-style-setting
                    bibtexObj.setCiteStyle(jComboBoxCiteStyle.getSelectedIndex());
                    // if we already have any attached BibTeX file, update table
                    try {
                        fillBibTeXTable();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        jComboBoxShowBibTex.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    fillBibTeXTable();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        SelectionListener listener = new SelectionListener();
        jTableBibEntries.getSelectionModel().addListSelectionListener(listener);
        jTableBibEntries.getColumnModel().getSelectionModel().addListSelectionListener(listener);
        jTextFieldFilterTable.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                if (Tools.isNavigationKey(evt.getKeyCode())) {
                    // if user pressed navigation key, select next table entry
                    TableUtils.navigateThroughList(jTableBibEntries, evt.getKeyCode());
                } else {
                    // select table-entry live, while the user is typing...
                    TableUtils.selectByTyping(jTableBibEntries, jTextFieldFilterTable, 1);
                }
            }
        });
        // when the radio.button for manual input is selected, we want to enable its related
        // components and disable all the stuff for the file-selection-radio-button
        jRadioButtonSourceDB.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    fillBibTeXTable();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                settingsObj.setLastUsedBibtexImportSource(BIBTEX_SOURCE_DB);
            }
        });
        // when the radio.button for manual input is selected, we want to enable its related
        // components and disable all the stuff for the file-selection-radio-button
        jRadioButtonSourceFile.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    fillBibTeXTable();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                settingsObj.setLastUsedBibtexImportSource(BIBTEX_SOURCE_FILE);
            }
        });
        // create action which should be executed when the user presses
        // the enter key
        AbstractAction a_enter = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jTextFieldFilterTable == e.getSource()) {
                    filterList(false);
                }
            }
        };
        // put action to the textfield's action maps
        jTextFieldFilterTable.getActionMap().put("EnterKeyPressed", a_enter);
        // associate enter-keystroke with that action
        KeyStroke ks = KeyStroke.getKeyStroke("ENTER");
        jTextFieldFilterTable.getInputMap().put(ks, "EnterKeyPressed");
        // create action which should be executed when the user presses
        // the alt + enter-key (reg ex search)
        AbstractAction a_regex_enter = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jTextFieldFilterTable == e.getSource()) {
                    filterList(true);
                }
            }
        };
        // put action to the textfield's action maps
        jTextFieldFilterTable.getActionMap().put("RegExEnterKeyPressed", a_regex_enter);
        // associate enter-keystroke with that action
        ks = KeyStroke.getKeyStroke("alt ENTER");
        jTextFieldFilterTable.getInputMap().put(ks, "RegExEnterKeyPressed");
        // create action which should be executed when the user presses
        // the delete/backspace-key
        AbstractAction a_delete = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jTableBibEntries == e.getSource()) {
                    try {
                        deleteBibtexEntry();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        };
        // put action to the tables' action maps
        jTableBibEntries.getActionMap().put("DeleteKeyPressed", a_delete);
        // check for os, and use appropriate controlKey
        ks = KeyStroke.getKeyStroke((PlatformUtil.isMacOS()) ? "BACK_SPACE" : "DELETE");
        jTableBibEntries.getInputMap().put(ks, "DeleteKeyPressed");
    }

    private void deleteBibtexEntry() throws IOException {
        // get the selected rows
        int[] rows = jTableBibEntries.getSelectedRows();
        // if we have no selected values, leave method
        if (rows.length < 1) {
            return;
        }
        // ask whether BibTeX entries really should be removed
        int option = JOptionPane.showConfirmDialog(null, resourceMap.getString("removeBibtexEntryMsg"), resourceMap.getString("removeBibtexEntryTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
        // the user chose to cancel the operation, so return "null"
        if (JOptionPane.YES_OPTION == option) {
            // iterate all selected values
            for (int cnt = 0; cnt < rows.length; cnt++) {
                // now to the bibkey
                Object bibkey = jTableBibEntries.getValueAt(rows[cnt], 0);
                // if we have any author, go on...
                bibtexObj.removeEntry(bibkey.toString());
                // check for filtered list
                if (linkedtablelist != null && linkedtablelist.size() > 0) {
                    // init the object-variable
                    Object[] o = new Object[2];
                    // fill object with values
                    o[0] = jTableBibEntries.getValueAt(rows[cnt], 0);
                    o[1] = jTableBibEntries.getValueAt(rows[cnt], 1);
                    // add object to linked list
                    linkedtablelist.remove(o);
                }
            }
            // check for filtered list
            if (linkedtablelist != null && linkedtablelist.size() > 0) {
                refreshList();
            } else {
                fillBibTeXTable();
            }
        }
    }

    private void filterList(boolean regEx) {
        // when we filter the table and want to restore it, we don't need to run the
        // time-consuming task that creates the author-list and related author-frequencies.
        // instead, we simply copy the values from the linked list to the table-model, which is
        // much faster. but therefore we have to apply all changes to the filtered-table
        // (like adding/changing values in a filtered list) to the linked list as well.

        // get text from the textfield containing the filter string
        // convert to lowercase, we don't want case-sensitive search
        String text = jTextFieldFilterTable.getText().toLowerCase();
        // when we have no text, do nothing
        if (text.isEmpty()) {
            return;
        }
        // get table model
        DefaultTableModel dtm = (DefaultTableModel) jTableBibEntries.getModel();
        // if we haven't already stored the current complete table data, do this now
        if (null == linkedtablelist) {
            // create new instance of list
            linkedtablelist = new LinkedList<>();
            // go through all table-data
            for (int cnt = 0; cnt < dtm.getRowCount(); cnt++) {
                // init the object-variable
                Object[] o = new Object[2];
                // fill object with values
                o[0] = dtm.getValueAt(jTableBibEntries.convertRowIndexToModel(cnt), 0);
                o[1] = dtm.getValueAt(jTableBibEntries.convertRowIndexToModel(cnt), 1);
                // add object to linked list
                linkedtablelist.add(o);
            }
        }
        TableUtils.filterTable(jTableBibEntries, dtm, text, new int[]{1}, regEx);
        // reset textfield
        jTextFieldFilterTable.setText("");
        jTextFieldFilterTable.requestFocusInWindow();
        // enable textfield only if we have more than 1 element in the jtable
        jTextFieldFilterTable.setEnabled(jTableBibEntries.getRowCount() > 0);
        // enable refresh button
        jButtonRefresh.setEnabled(true);
    }

    private void fillBibTeXTable() throws IOException {
        if (jRadioButtonSourceFile.isSelected()) {
            // retrieve currently attached file
            File currentlyattachedfile = bibtexObj.getCurrentlyAttachedFile();
            // if we have no currently attached BibTeX file, or the currently attached BibTeX file
            // differs from the new selected file of the user, open the BibTeX file now
            if ((null == currentlyattachedfile) || (!currentlyattachedfile.toString().equals(bibtexObj.getFilePath().toString()))) {
                // open selected file, using the character encoding of the related reference-manager (i.e.
                // the programme that has exported the bib-tex-file).
                bibtexObj.openAttachedFile(Constants.BIBTEX_ENCODINGS[jComboBoxEncoding.getSelectedIndex()], false);
                // retrieve currently attached BibTeX file
                currentlyattachedfile = bibtexObj.getCurrentlyAttachedFile();
            }
            // set file path to textfield
            jTextFieldBibtexFilepath.setText((currentlyattachedfile != null && currentlyattachedfile.exists()) ? currentlyattachedfile.toString() : "");
        }
        // block all components
        setComponentsBlocked(true);
        // reset linked list
        linkedtablelist = null;
        // start the background task manually
        Task impBibT = startImport();
        // get the application's context...
        ApplicationContext appC = Application.getInstance().getContext();
        // ...to get the TaskMonitor and TaskService
        TaskMonitor tM = appC.getTaskMonitor();
        TaskService tS = appC.getTaskService();
        // with these we can execute the task and bring it to the foreground
        // i.e. making the animated progressbar and busy icon visible
        tS.execute(impBibT);
        tM.setForegroundTask(impBibT);
    }

    private void setComponentsBlocked(boolean block) {
        jButtonCancel.setEnabled(!block);
        jButtonApply.setEnabled(!block);
        jButtonSelectAll.setEnabled(!block);
        jButtonBrowseBibtex.setEnabled(!block);
        jCheckBoxAddAsEntry.setEnabled(!block);
        jCheckBoxImportKeywords.setEnabled(!block);
        jComboBoxEncoding.setEnabled(!block);
        jComboBoxShowBibTex.setEnabled(!block);
        jComboBoxCiteStyle.setEnabled(!block);
        jTableBibEntries.setEnabled(!block);
        jTextFieldFilterTable.setEnabled(!block);
        jRadioButtonSourceDB.setEnabled(!block);
        jRadioButtonSourceFile.setEnabled(!block);
        // refresh button is either blockes (disabled) or enabled whether
        // we have any content in linkedtablelist.
        jButtonRefresh.setEnabled((!block) && linkedtablelist != null);
    }

    @Action
    public Task addSelectedAuthors() {
        // check whether user wants to import existing entries as well, i.e. the user
        // wants to import BibTeX abstracts AND automatically create entries from those
        // abstracts, where existing entries will be replaced by new BibTeX updates.
        // in this case, the combo box's selected index must be greater than 0
        if (jComboBoxShowBibTex.getSelectedIndex() > 0) {
            // create a JOptionPane with replace/new/concat/cancel options
            updateOption = JOptionPane.showOptionDialog(this,
                    resourceMap.getString("msgConfirmUpdateEntry"),
                    resourceMap.getString("msgConfirmUpdateEntryTitle"),
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    new Object[]{
                        resourceMap.getString("optionReplace"),
                        resourceMap.getString("optionNew"),
                        resourceMap.getString("optionConcat"),
                        resourceMap.getString("optionCancel"),},
                    resourceMap.getString("optioneReplace"));
            // if user closes or cancels the dialog, return...
            if (JOptionPane.CLOSED_OPTION == updateOption || updateOption == UPDATE_OPTION_CANCEL) {
                return null;
            }
        } // in this case, only new entries are imported, thus we can ignore the variable updateOption
        else {
            updateOption = -1;
        }
        // disable components
        setComponentsBlocked(true);
        // return task
        return new addSelectedAuthorsTask(org.jdesktop.application.Application.getInstance(ZettelkastenApp.class));
    }

    private class addSelectedAuthorsTask extends org.jdesktop.application.Task<Object, Void> {

        private Object au;
        private Object bibkey;

        addSelectedAuthorsTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to createLinksTask fields, here.
            super(app);
        }

        @Override
        protected Object doInBackground() {
            // get the selected rows
            int[] rows = jTableBibEntries.getSelectedRows();
            // create new list with possible modified entries.
            modifiedEntries = new ArrayList<>();
            // if we have no selected values, leave method
            if (rows.length < 1) {
                return null;
            }
            // iterate all selected values
            for (int cnt = 0; cnt < rows.length; cnt++) {
                // retrieve author from table-selection
                Object au = jTableBibEntries.getValueAt(rows[cnt], 1);
                // now to the bibkey
                Object bibkey = jTableBibEntries.getValueAt(rows[cnt], 0);
                // if we have any author, go on...
                if (au != null && !au.toString().isEmpty()) {
                    // init bibkey-pos
                    int bibkeypos = -1;
                    // check if we have any bibkey value and retrieve bibkey-position in author-data
                    // the variable "bibkeypos" stores the index-number of that author value that has
                    // the bibkey "bibkey" associated.
                    if (bibkey != null) {
                        // retrieve bibkey position in author data base
                        bibkeypos = dataObj.getBibkeyPosition(bibkey.toString());
                        // retrieve BibTeX entry from attached file
                        BibtexEntry be = (jRadioButtonSourceDB.isSelected()) ? bibtexObj.getEntry(bibkey.toString()) : bibtexObj.getEntryFromAttachedFile(bibkey.toString());
                        // add to data base
                        bibtexObj.addEntry(be);
                    }
                    // check whether bibkey already exists...
                    // if we have *not* found any bibkey, or if we have no bibkey (e.g. if bibkey
                    // value was null), we can add the author value
                    if (-1 == bibkeypos) {
                        // ...add author to the data file
                        int pos = dataObj.addAuthor(au.toString(), 0);
                        // if author was successfully added...
                        if (pos != -1) {
                            // check for valid bibkey-value
                            if (bibkey != null) {
                                // and add it to the recently added author-value
                                dataObj.setAuthorBibKey(pos, bibkey.toString());
                                // if the user also wants to add imported literature as entries, do
                                // this here. we then have to check whether the imported BibTeX entry
                                // has an abstract or annotation, and if so, we use this as content for
                                // the new Zettel
                                createZettelFromBibTeXAbstract(au, bibkey);
                            }
                        }
                    } // here we have an already existing bibkey and update the author-values...
                    // so we have the index-number of that author-value with the bibkey "bibkey"
                    // stored in the value "bibkeypos".
                    else {
                        // set new author value, i.e. overwrite existing author with new value
                        dataObj.setAuthor(bibkeypos, au.toString());
                        // if the user also wants to *update* imported literature as entries, we do
                        // this here. we then have to check whether the imported BibTeX entry
                        // has an abstract or annotation, and if so, we use this as content for
                        // the new entry or update an existing entry with this content
                        if (jCheckBoxAddAsEntry.isSelected() && bibkey != null) {
                            // before we check whether 
                            // retrieve abstract (i.e. content for entry)
                            String content = (jRadioButtonSourceDB.isSelected()) ? bibtexObj.getAbstract(bibkey.toString()) : bibtexObj.getAbstractFromAttachedFile(bibkey.toString());
                            // only create content/new entry, if any abstract was found...
                            if (content != null && !content.isEmpty()) {
                                // we need an indicator variable that tells as, whether any entry
                                // was found at all. It can happen, that a user later adds an abstract
                                // and wants to import it now...
                                boolean anyAbstractEntyrFound = false;
                                // we now have to go through all entries and check whether they have an
                                // attribute "fromBibTex" (we do this by calling the method
                                // "Daten.isContentFromBibTex(int pos)"). If an entry has this
                                // this attribute set to true, we then check whether the entry's
                                // author-value equals the imported author-value (author-index-number
                                // has to equal "bibkeypos"). if both isContentFromBibTex is true and
                                // author-index-number equals bibkeypos, we know that we have to update
                                // the entry's content. We do this according to the user's choice, which
                                // is stored in "updateOption".
                                for (int counter = 1; counter <= dataObj.getCount(Daten.ZKNCOUNT); counter++) {
                                    // retrieve entry's BibTeX attribute. here we check, whether
                                    // the entry with the index-number "counter" has content from
                                    // a BibTeX file. only in this case we need to update the entry
                                    if (dataObj.isContentFromBibTex(counter)) {
                                        // now we have to check, whether any of the entry's author-values
                                        // has the same bibkey like the imported BibTeX entry...
                                        // only in this case we need to update the content
                                        if (dataObj.existsInAuthors(bibkeypos, counter)) {
                                            // now we know, that the entry with the index-number "counter"
                                            // - was created from a formerly BibTeX import
                                            // - has an author with the index-number "bibkeypos"
                                            // so it has to be updated or replaced, but only
                                            // if the content differs from the previous content
                                            //
                                            // replace UTF-chars with UBB-tags for creating/changing an entry
                                            content = Tools.replaceUnicodeToUbb(content);
                                            // now check what the user chose to do.
                                            switch (updateOption) {
                                                // the user wants to add a new entry from the abstract
                                                case UPDATE_OPTION_NEW:
                                                    // we have to check whether the new imported content *differs*
                                                    // from the existing zettel-content
                                                    if (!content.equals(dataObj.getZettelContent(counter))) {
                                                        // init variable
                                                        String[] keywords = null;
                                                        // if user also wants keywords, retrieve them now
                                                        if (jCheckBoxImportKeywords.isSelected()) {
                                                            keywords = (jRadioButtonSourceDB.isSelected()) ? bibtexObj.getKeywords(bibkey.toString()) : bibtexObj.getKeywordsFromAttachedFile(bibkey.toString());
                                                        }
                                                        // finally, add entry to dataset
                                                        dataObj.addEntryFromBibTex("", content, new String[]{au.toString()}, keywords, Tools.getTimeStamp());
                                                        // and delete BibTeX attribute from the *old* entry...
                                                        dataObj.setContentFromBibTexRemark(counter, false);
                                                        // update found-variable
                                                        anyAbstractEntyrFound = true;
                                                        // and increase entry counter
                                                        newEntries++;
                                                    }
                                                    break;
                                                // the user wants to add a new entry from the abstract
                                                case UPDATE_OPTION_REPLACE:
                                                    // init variable
                                                    String[] keywords = null;
                                                    // if user also wants keywords, retrieve them now
                                                    if (jCheckBoxImportKeywords.isSelected()) {
                                                        keywords = (jRadioButtonSourceDB.isSelected()) ? bibtexObj.getKeywords(bibkey.toString()) : bibtexObj.getKeywordsFromAttachedFile(bibkey.toString());
                                                    }
                                                    // change entry's content
                                                    dataObj.setZettelContent(counter, content, true);
                                                    // and add new keywords
                                                    dataObj.addKeywordsToEntry(keywords, counter, 1);
                                                    // update found-variable
                                                    anyAbstractEntyrFound = true;
                                                    // store entry-number of modified entry
                                                    modifiedEntries.add(counter);
                                                    break;
                                                // the user wants to concatenate a the abstract to an existing entry
                                                case UPDATE_OPTION_CONCAT:
                                                    // init variable
                                                    keywords = null;
                                                    // if user also wants keywords, retrieve them now
                                                    if (jCheckBoxImportKeywords.isSelected()) {
                                                        keywords = (jRadioButtonSourceDB.isSelected()) ? bibtexObj.getKeywords(bibkey.toString()) : bibtexObj.getKeywordsFromAttachedFile(bibkey.toString());
                                                    }
                                                    // we have to check whether the new imported content *differs*
                                                    // from the existing Zettel content
                                                    if (!content.equals(dataObj.getZettelContent(counter))) {
                                                        // change entry's content
                                                        dataObj.setZettelContent(counter, dataObj.getZettelContent(counter) + "[br][br]" + content, true);
                                                    }
                                                    // and add new keywords
                                                    dataObj.addKeywordsToEntry(keywords, counter, 1);
                                                    // update found-variable
                                                    anyAbstractEntyrFound = true;
                                                    // store entry-number of modified entry
                                                    modifiedEntries.add(counter);
                                                    break;
                                                default:
                                                    throw new IllegalStateException("Unexpected value: " + updateOption);
                                            }
                                        }
                                    }
                                }
                                // check whether we have found any abstract at all... if not,
                                // we create a new entry here
                                if (!anyAbstractEntyrFound) {
                                    // init variable
                                    String[] keywords = null;
                                    // if user also wants keywords, retrieve them now
                                    if (jCheckBoxImportKeywords.isSelected()) {
                                        keywords = (jRadioButtonSourceDB.isSelected()) ? bibtexObj.getKeywords(bibkey.toString()) : bibtexObj.getKeywordsFromAttachedFile(bibkey.toString());
                                    }
                                    // finally, add entry to dataset
                                    dataObj.addEntryFromBibTex("", content, new String[]{au.toString()}, keywords, Tools.getTimeStamp());
                                    // and increase entry counter
                                    newEntries++;
                                }
                            }
                        }
                    }
                }
                // update progress bar
                setProgress(cnt, 0, rows.length);
            }
            return null;
        }

        private void createZettelFromBibTeXAbstract(Object au, Object bibkey) {
            this.au = au;
            this.bibkey = bibkey;
            if (jCheckBoxAddAsEntry.isSelected()) {
                // init variable
                String[] keywords = null;
                // if user also wants keywords, retrieve them now
                if (jCheckBoxImportKeywords.isSelected()) {
                    keywords = (jRadioButtonSourceDB.isSelected()) ? bibtexObj.getKeywords(bibkey.toString()) : bibtexObj.getKeywordsFromAttachedFile(bibkey.toString());
                }
                // retrieve abstract (i.e. content for entry)
                String content = (jRadioButtonSourceDB.isSelected()) ? bibtexObj.getAbstract(bibkey.toString()) : bibtexObj.getAbstractFromAttachedFile(bibkey.toString());
                // only create content/new entry, if any abstract was found...
                if (content != null && !content.isEmpty()) {
                    // finally, add entry to dataset
                    dataObj.addEntryFromBibTex("", content, new String[]{au.toString()}, keywords, Tools.getTimeStamp());
                    // and increase entry counter
                    newEntries++;
                } // if nothing found, add at least the keywords
                else if (keywords != null && keywords.length > 0) {
                    // add keywords to database
                    dataObj.addKeywordsToDatabase(keywords);
                }
            } else if (jCheckBoxImportKeywords.isSelected()) {
                String[] keywords = (jRadioButtonSourceDB.isSelected()) ? bibtexObj.getKeywords(bibkey.toString()) : bibtexObj.getKeywordsFromAttachedFile(bibkey.toString());
                if (keywords != null && keywords.length > 0) {
                    // add keywords to database
                    dataObj.addKeywordsToDatabase(keywords);
                }
            }
        }

        @Override
        protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
        }

        @Override
        protected void finished() {
            // enable components
            setComponentsBlocked(false);
            // import was not cancelled.
            // if we have modified entries, add all modified entries to the history,
            // so the user can go through all modified entries, and auto-select last entry
            if (getModifiedEntries() != null && getModifiedEntries().size() > 0) {
                // iterate modified entries
                for (int cnt = 0; cnt < getModifiedEntries().size(); cnt++) {
                    // add modified entry to history
                    dataObj.addToHistory(getModifiedEntries().get(cnt));
                }
                // set displayed zettel to last modified entry.
                mainframe.displayedZettel = getModifiedEntries().get(getModifiedEntries().size() - 1);
            }
            // update display, since we have new authors and possibly new entries as well
            mainframe.updateDisplay();
            // tell user about success
            JOptionPane.showMessageDialog(null,
                    resourceMap.getString("authorImportOkMsg", String.valueOf(getNewEntriesCount()), String.valueOf((getModifiedEntries() != null) ? getModifiedEntries().size() : 0)),
                    resourceMap.getString("authorImportOkTitle"),
                    JOptionPane.PLAIN_MESSAGE);
            // close window
            setVisible(false);
            dispose();
        }
    }

    @Action
    public void refreshList() {
        // first check whether we have any saved values at all
        if (linkedtablelist != null) {
            // get table model
            DefaultTableModel dtm = (DefaultTableModel) jTableBibEntries.getModel();
            // delete all data from the author-table
            dtm.setRowCount(0);
            // create an iterator for the linked list
            ListIterator<Object[]> iterator = linkedtablelist.listIterator();
            // go through complete linked list and add each element to the table(model)
            while (iterator.hasNext()) {
                dtm.addRow(iterator.next());
            }
            // enable filter field
            jTextFieldFilterTable.setEnabled(true);
            // disable refresh button
            jButtonRefresh.setEnabled(false);
        }
        linkedtablelist = null;
    }

    @Action
    public void selectAllTableContent() {
        jTableBibEntries.getSelectionModel().setSelectionInterval(0, jTableBibEntries.getRowCount() - 1);
    }

    @Action
    public Task startImport() {
        return new startImportTask(org.jdesktop.application.Application.getInstance(ZettelkastenApp.class));
    }

    private class startImportTask extends org.jdesktop.application.Task<Object, Void> {

        ArrayList<String[]> rowdata = new ArrayList<>();

        startImportTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to createLinksTask fields, here.
            super(app);
        }

        @Override
        protected Object doInBackground() {
            // retrieve count of entries
            int count = (jRadioButtonSourceFile.isSelected()) ? bibtexObj.getAttachedFileCount() : bibtexObj.getCount();
            // iterate all entries of the BibTeX file
            for (int cnt = 0; cnt < count; cnt++) {
                String bibkey;
                String au;
                // check which BibTeX data source we need
                if (jRadioButtonSourceFile.isSelected()) {
                    // ...retrieve bibkey
                    bibkey = bibtexObj.getBibkeyFromAttachedFile(cnt);
                    // retrieve each bibtex-author
                    au = bibtexObj.getFormattedEntryFromAttachedFile(cnt);
                } else {
                    // ...retrieve bibkey
                    bibkey = bibtexObj.getBibkey(cnt);
                    // retrieve each BibTeX author
                    au = bibtexObj.getFormattedEntry(cnt);
                }
                // check whether *all* entries should be displayed or not
                switch (jComboBoxShowBibTex.getSelectedIndex()) {
                    // here we want to display only *new* entries
                    case 0: // if author does not exist AND the bibkey of the imported literature does not already exist...
                        if (au != null && !au.isEmpty() && -1 == dataObj.getAuthorPosition(au) && -1 == dataObj.getAuthorBibKeyPosition(bibkey)) {
                            // we have a new entry and add it to the table
                            rowdata.add(new String[]{bibkey, au});
                        }
                        break;
                    // here we want to display only *existing* entries
                    case 1: // if bibkey already exists...
                        if (au != null && !au.isEmpty() && dataObj.getAuthorBibKeyPosition(bibkey) != -1) {
                            // we have an existing entry and can add it to the table
                            rowdata.add(new String[]{bibkey, au});
                        }
                        break;
                    // here we want to display only *all* entries
                    case 2: // if author does not exist...
                        if (au != null && !au.isEmpty()) {
                            // we have any valid entry, whether new or existing, and can add it to the table
                            rowdata.add(new String[]{bibkey, au});
                        }
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + jComboBoxShowBibTex.getSelectedIndex());
                }
                setProgress(cnt, 0, count);
            }

            return null;
        }

        @Override
        protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
        }

        @Override
        protected void finished() {
            // get iterator for all row data
            Iterator<String[]> it = rowdata.iterator();
            // create table model for the table data, which is not editable
            DefaultTableModel tm = (DefaultTableModel) jTableBibEntries.getModel();
            tm.setRowCount(0);
            // and iterate all loaded BibTeX entries
            while (it.hasNext()) {
                tm.addRow(it.next());
            }
            setComponentsBlocked(false);
            // initially, disable apply button
            jButtonApply.setEnabled(false);
            // set status text, how many entries are selected.
            jLabel2.setText(resourceMap.getString("jLabel2num", String.valueOf(jTableBibEntries.getSelectedRowCount()), String.valueOf(jTableBibEntries.getRowCount())));
        }
    }

    @Action
    public void browseFile() throws IOException {
        // retrieve attached BibTeX file
        File selectedfile = bibtexObj.getCurrentlyAttachedFile();
        // if we have no attached file, set last used file as filepath
        if (null == selectedfile || !selectedfile.exists()) {
            selectedfile = bibtexObj.getFilePath();
        }
        selectedfile = FileOperationsUtil.chooseFile(this,
                JFileChooser.OPEN_DIALOG,
                JFileChooser.FILES_ONLY,
                (null == selectedfile) ? null : selectedfile.toString(),
                (null == selectedfile) ? null : selectedfile.getName(),
                resourceMap.getString("fileChooserTitle"),
                new String[]{".bib", ".txt"},
                resourceMap.getString("bibTexDesc"),
                settingsObj);
        if (selectedfile != null) {
            // set new BibTeX file path
            bibtexObj.setFilePath(selectedfile);
            bibtexObj.detachCurrentlyAttachedFile();
            fillBibTeXTable();
        }
    }

    /**
     * Closes the window
     */
    @Action
    public void cancel() {
        dispose();
        setVisible(false);
    }

    /**
     * This class sets up a selection listener for the tables. each table which shall react on
     * selections, e.g. by showing an entry, gets this selectionlistener in the method
     * {@link #initSelectionListeners() initSelectionListeners()}.
     */
    private class SelectionListener implements ListSelectionListener {

        SelectionListener() {
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            // en- or disable apply button
            jButtonApply.setEnabled(jTableBibEntries.getSelectedRowCount() > 0);
            // set status text, how many entries are selected.
            jLabel2.setText(resourceMap.getString("jLabel2num", String.valueOf(jTableBibEntries.getSelectedRowCount()), String.valueOf(jTableBibEntries.getRowCount())));
        }
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jComboBoxEncoding = new javax.swing.JComboBox();
        jLabelEncoding = new javax.swing.JLabel();
        jCheckBoxAddAsEntry = new javax.swing.JCheckBox();
        jCheckBoxImportKeywords = new javax.swing.JCheckBox();
        jButtonApply = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jComboBoxCiteStyle = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableBibEntries = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        jButtonSelectAll = new javax.swing.JButton();
        jButtonRefresh = new javax.swing.JButton();
        jTextFieldFilterTable = new javax.swing.JTextField();
        jComboBoxShowBibTex = new javax.swing.JComboBox();
        statusLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jButtonBrowseBibtex = new javax.swing.JButton();
        jTextFieldBibtexFilepath = new javax.swing.JTextField();
        jRadioButtonSourceFile = new javax.swing.JRadioButton();
        jRadioButtonSourceDB = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ZettelkastenApp.class).getContext().getResourceMap(CImportBibTex.class);
        setTitle(resourceMap.getString("FormCImportBibTex.title")); // NOI18N
        setMinimumSize(new java.awt.Dimension(100, 100));
        setName("FormCImportBibTex"); // NOI18N

        jComboBoxEncoding.setName("jComboBoxEncoding"); // NOI18N

        jLabelEncoding.setText(resourceMap.getString("jLabelEncoding.text")); // NOI18N
        jLabelEncoding.setName("jLabelEncoding"); // NOI18N

        jCheckBoxAddAsEntry.setText(resourceMap.getString("jCheckBoxAddAsEntry.text")); // NOI18N
        jCheckBoxAddAsEntry.setToolTipText(resourceMap.getString("jCheckBoxAddAsEntry.toolTipText")); // NOI18N
        jCheckBoxAddAsEntry.setName("jCheckBoxAddAsEntry"); // NOI18N

        jCheckBoxImportKeywords.setText(resourceMap.getString("jCheckBoxImportKeywords.text")); // NOI18N
        jCheckBoxImportKeywords.setToolTipText(resourceMap.getString("jCheckBoxImportKeywords.toolTipText")); // NOI18N
        jCheckBoxImportKeywords.setName("jCheckBoxImportKeywords"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(ZettelkastenApp.class).getContext().getActionMap(CImportBibTex.class, this);
        jButtonApply.setAction(actionMap.get("addSelectedAuthors")); // NOI18N
        jButtonApply.setName("jButtonApply"); // NOI18N

        jButtonCancel.setAction(actionMap.get("cancel")); // NOI18N
        jButtonCancel.setName("jButtonCancel"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jComboBoxCiteStyle.setName("jComboBoxCiteStyle"); // NOI18N

        jScrollPane1.setBorder(null);
        jScrollPane1.setMinimumSize(new java.awt.Dimension(25, 25));
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTableBibEntries.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "BibKey", "Literaturangabe"
            }
        ) {
            final Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            final boolean[] canEdit = new boolean [] {
                false, false
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTableBibEntries.setName("jTableBibEntries"); // NOI18N
        jTableBibEntries.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(jTableBibEntries);
        if (jTableBibEntries.getColumnModel().getColumnCount() > 0) {
            jTableBibEntries.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("jTableBibEntries.columnModel.title0")); // NOI18N
            jTableBibEntries.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("jTableBibEntries.columnModel.title1")); // NOI18N
        }

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jButtonSelectAll.setAction(actionMap.get("selectAllTableContent")); // NOI18N
        jButtonSelectAll.setName("jButtonSelectAll"); // NOI18N

        jButtonRefresh.setAction(actionMap.get("refreshList")); // NOI18N
        jButtonRefresh.setIcon(resourceMap.getIcon("jButtonRefresh.icon")); // NOI18N
        jButtonRefresh.setBorderPainted(false);
        jButtonRefresh.setContentAreaFilled(false);
        jButtonRefresh.setFocusPainted(false);
        jButtonRefresh.setName("jButtonRefresh"); // NOI18N
        jButtonRefresh.setPreferredSize(new java.awt.Dimension(22, 22));

        jTextFieldFilterTable.setToolTipText(resourceMap.getString("jTextFieldFilterTable.toolTipText")); // NOI18N
        jTextFieldFilterTable.setName("jTextFieldFilterTable"); // NOI18N

        jComboBoxShowBibTex.setName("jComboBoxShowBibTex"); // NOI18N

        statusLabel.setIcon(resourceMap.getIcon("statusLabel.icon")); // NOI18N
        statusLabel.setText(resourceMap.getString("statusLabel.text")); // NOI18N
        statusLabel.setName("statusLabel"); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jButtonBrowseBibtex.setAction(actionMap.get("browseFile")); // NOI18N
        jButtonBrowseBibtex.setName("jButtonBrowseBibtex"); // NOI18N

        jTextFieldBibtexFilepath.setEditable(false);
        jTextFieldBibtexFilepath.setName("jTextFieldBibtexFilepath"); // NOI18N

        buttonGroup1.add(jRadioButtonSourceFile);
        jRadioButtonSourceFile.setText(resourceMap.getString("jRadioButtonSourceFile.text")); // NOI18N
        jRadioButtonSourceFile.setName("jRadioButtonSourceFile"); // NOI18N

        buttonGroup1.add(jRadioButtonSourceDB);
        jRadioButtonSourceDB.setText(resourceMap.getString("jRadioButtonSourceDB.text")); // NOI18N
        jRadioButtonSourceDB.setName("jRadioButtonSourceDB"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jRadioButtonSourceFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldBibtexFilepath)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonBrowseBibtex))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jRadioButtonSourceDB)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldBibtexFilepath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonBrowseBibtex)
                    .addComponent(jRadioButtonSourceFile))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButtonSourceDB)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jCheckBoxAddAsEntry, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTextFieldFilterTable)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonCancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonApply)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(statusLabel))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabelEncoding)
                                    .addComponent(jLabel1))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jComboBoxCiteStyle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jComboBoxEncoding, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jCheckBoxImportKeywords))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jComboBoxShowBibTex, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonSelectAll)))
                .addContainerGap())
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelEncoding)
                    .addComponent(jComboBoxEncoding, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jComboBoxCiteStyle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxAddAsEntry)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxImportKeywords)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonSelectAll)
                    .addComponent(jComboBoxShowBibTex, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextFieldFilterTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonCancel)
                        .addComponent(jButtonApply))
                    .addComponent(statusLabel))
                .addGap(3, 3, 3))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButtonApply;
    private javax.swing.JButton jButtonBrowseBibtex;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonRefresh;
    private javax.swing.JButton jButtonSelectAll;
    private javax.swing.JCheckBox jCheckBoxAddAsEntry;
    private javax.swing.JCheckBox jCheckBoxImportKeywords;
    private javax.swing.JComboBox jComboBoxCiteStyle;
    private javax.swing.JComboBox jComboBoxEncoding;
    private javax.swing.JComboBox jComboBoxShowBibTex;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelEncoding;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton jRadioButtonSourceDB;
    private javax.swing.JRadioButton jRadioButtonSourceFile;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTableBibEntries;
    private javax.swing.JTextField jTextFieldBibtexFilepath;
    private javax.swing.JTextField jTextFieldFilterTable;
    private javax.swing.JLabel statusLabel;
    // End of variables declaration//GEN-END:variables

}
