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

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.GroupLayout;
import javax.swing.LayoutStyle;
import de.danielluedecke.zettelkasten.database.BibTeX;
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.util.Tools;
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.util.classes.Comparer;
import de.danielluedecke.zettelkasten.database.Daten;
import com.explodingpixels.macwidgets.MacWidgetFactory;
import com.explodingpixels.widgets.TableUtils;
import de.danielluedecke.zettelkasten.util.ColorUtil;
import de.danielluedecke.zettelkasten.util.PlatformUtil;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.jdesktop.application.Action;

/**
 *
 * @author danielludecke
 */
public class CSetBibKey extends javax.swing.JDialog {

    private BibTeX bibtexObj;
    private Settings settingsObj;
    private Daten dataObj;

    private String currentAuthor = "";
    private LinkedList<String> selectedAuthors;
    private ZettelkastenView mainframe;
    /**
     * Constants indicating which radio button was selected
     */
    public static final int CHOOSE_BIBKEY_MANUAL = 1;
    public static final int CHOOSE_BIBKEY_FROM_DB = 2;
    /**
     * Constants have to be aligned with combo box items!!!
     */
    public static final int TYPE_BIBKEY_NEW = 0;
    public static final int TYPE_BIBKEY_EXIST = 1;
    public static final int TYPE_BIBKEY_ALL = 2;
    /**
     *
     */
    private LinkedList<Object[]> linkedtablelist;
    private int rowcounter;
    /**
     * get the strings for file descriptions from the resource map
     */
    private org.jdesktop.application.ResourceMap resourceMap
            = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
            getContext().getResourceMap(CSetBibKey.class);

    /**
     * This dialog sets or changed the bibkey of existing author-values. It is
     * called from the mainframe's tabbed pane, the jTableAuthors. When one or
     * more authors are selected, they can be passed to this dialog where the
     * user has the chance to change or set a new bibkey-value for each of the
     * selected authors.
     *
     * @param parent dialog's parent
     * @param mf
     * @param d a reference to the {@code CDaten}-class, the main dataclass
     * @param bt a reference to the {@code CBibTex}-class that handles the
     * import/export of bibtex-files
     * @param s a reference to the {@code CSettings}-class
     */
    public CSetBibKey(java.awt.Frame parent, ZettelkastenView mf, Daten d, BibTeX bt, Settings s) {
        super(parent);
        // copy parameters to our global variables
        bibtexObj = bt;
        settingsObj = s;
        dataObj = d;
        mainframe = mf;
        linkedtablelist = null;
        currentAuthor = "";
        // init components and listeners
        initComponents();
        // set application icon
        setIconImage(Constants.zknicon.getImage());
        // init components and listeners
        initListeners();
        initBorders(settingsObj);
        initTable();
        initTitleAndBibkey(true);
        // set last settings
        switch (settingsObj.getLastUsedSetBibyKeyChoice()) {
            case CHOOSE_BIBKEY_MANUAL:
                jRadioButtonManualBibkey.setSelected(true);
                break;
            case CHOOSE_BIBKEY_FROM_DB:
                jRadioButtonFileBibkey.setSelected(true);
                break;
            default:
                jRadioButtonManualBibkey.setSelected(true);
                break;
        }
        // set last settings
        jComboBoxShowBibTex.setSelectedIndex(settingsObj.getLastUsedSetBibyKeyType());

        // change layout style if user wishes itunes-like scrollbars...
        if (settingsObj.isMacStyle()) {
            // make extra table-sorter for itunes-tables
            TableUtils.SortDelegate sortDelegate = new TableUtils.SortDelegate() {
                @Override
                public void sort(int columnModelIndex, TableUtils.SortDirection sortDirection) {
                }
            };
            TableUtils.makeSortable(jTablePreview, sortDelegate);
            // set back default resize mode
            jTablePreview.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        }

        // in case we have mac os x with aqua look&feel, make components look more mac-like...
        if (settingsObj.isMacStyle() || settingsObj.isSeaGlass()) {
            // textfield should look like search-textfield...
            jTextFieldFilterTable.putClientProperty("JTextField.variant", "search");
            if (settingsObj.isSeaGlass()) {
                jButtonApply.putClientProperty("JComponent.sizeVariant", "small");
                jButtonCancel.putClientProperty("JComponent.sizeVariant", "small");
            }
        }
        fillBibtexTable();
    }

    private void initBorders(Settings settingsObj) {
        /*
         * Constructor for Matte Border
         * public MatteBorder(int top, int left, int bottom, int right, Color matteColor)
         */
        jScrollPane1.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ColorUtil.getBorderGray(settingsObj)));
    }

    public void initTitleAndBibkey() {
        initTitleAndBibkey(true);
    }

    /**
     * Since we probably have more that one value to change, this method sets
     * the current author-value to a jLabel, so the user knows which
     * author-value is currently being edited. furthermore, any existing bibkey
     * of the current authorvalue is set to the textfield.
     */
    private void initTitleAndBibkey(boolean resetAuthors) {
        // get currently edited author
        if (resetAuthors) {
            selectedAuthors = mainframe.getSelectedAuthors();
        }
        // check for valid value
        if (selectedAuthors != null && selectedAuthors.size() > 0) {
            String author = currentAuthor = selectedAuthors.get(0);
            // if the string is too long, truncate it.
            if (author.length() > 50) {
                author = author.substring(0, 50) + "...";
            }
            // set title, so the user knows which author-value is currently being set...
            jLabelTitle.setText(resourceMap.getString("authorTitle", author));
            // retrieve authors bibkey, if any. since this attribute is optional,
            // null might be returned
            String bibkey = dataObj.getAuthorBibKey(currentAuthor);
            // either set bibkey-value or empty string to textfield
            jTextFieldManualBibkey.setText((bibkey != null) ? bibkey : "");
            // en- or disable apply-button
            if (jRadioButtonManualBibkey.isSelected()) {
                // when manual option is selected, en- or disable depending on textinput
                jButtonApply.setEnabled(!jTextFieldManualBibkey.getText().isEmpty());
            } else {
                // else when file-option is selected, en- or disable depending on table-selection
                jButtonApply.setEnabled(jTablePreview.getSelectedRow() != -1);
            }
        } else {
            jLabelTitle.setText(resourceMap.getString("noAuthorValues"));
            jTextFieldManualBibkey.setText("");
            jButtonApply.setEnabled(false);
        }
    }

    private void deleteBibtexEntry() {
        // get the selected rows
        int[] rows = jTablePreview.getSelectedRows();
        // if we have no selected values, leave method
        if (rows.length < 1) {
            return;
        }
        // ask whether bibtex entries really should be removed
        int option = JOptionPane.showConfirmDialog(null, resourceMap.getString("removeBibtexEntryMsg"), resourceMap.getString("removeBibtexEntryTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
        // the user chose to cancel the operation, so return "null"
        if (JOptionPane.YES_OPTION == option) {
            // iterate all selected values
            for (int cnt = 0; cnt < rows.length; cnt++) {
                // now to the bibey
                Object bibkey = jTablePreview.getValueAt(rows[cnt], 0);
                // if we have any author, go on...
                bibtexObj.removeEntry(bibkey.toString());
                // check for filtered list
                if (linkedtablelist != null && linkedtablelist.size() > 0) {
                    // init the object-variable
                    Object[] o = new Object[2];
                    // fill object with values
                    o[0] = jTablePreview.getValueAt(rows[cnt], 0);
                    o[1] = jTablePreview.getValueAt(rows[cnt], 1);
                    // add object to linked list
                    linkedtablelist.remove(o);
                }
            }
            // check for filtered list
            if (linkedtablelist != null && linkedtablelist.size() > 0) {
                refreshList();
            } else {
                fillBibtexTable();
            }
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
        // when the radio.button for manual input is selected, we want to enable its related
        // components and disable all the stuff for the file-selection-radio-button
        jRadioButtonManualBibkey.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // disable all file-options
                toggleOptions(false);
                // set keyboard-focus to textfield
                jTextFieldManualBibkey.requestFocusInWindow();
                // en- or disable apply-button, depending on whether we already have textinput or not
                jButtonApply.setEnabled(!jTextFieldManualBibkey.getText().isEmpty());
            }
        });
        // whenever the user changes the text of the textfield, en- or disable apply-button
        // depending on whether we already have textinput or not
        jTextFieldManualBibkey.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                jButtonApply.setEnabled(!jTextFieldManualBibkey.getText().isEmpty());
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                jButtonApply.setEnabled(!jTextFieldManualBibkey.getText().isEmpty());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                jButtonApply.setEnabled(!jTextFieldManualBibkey.getText().isEmpty());
            }
        });
        // when the radio-button for file-input is selected, we want to enable its related
        // components and disable all the stuff for the manual-selection-radio-button
        jRadioButtonFileBibkey.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // disable all manual-options
                toggleOptions(true);
                // en- or disable apply-button depending on whether we have a selection or not
                jButtonApply.setEnabled(jTablePreview.getSelectedRow() != 1);
                // set keyboard-focus to the encoding-combobox, so the user can choose
                // the input-format
                jTablePreview.requestFocusInWindow();
            }
        });
        jComboBoxShowBibTex.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fillBibtexTable();
            }
        });
        jTextFieldFilterTable.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                if (Tools.isNavigationKey(evt.getKeyCode())) {
                    // if user pressed navigation key, select next table entry
                    de.danielluedecke.zettelkasten.util.TableUtils.navigateThroughList(jTablePreview, evt.getKeyCode());
                } else {
                    // select table-entry live, while the user is typing...
                    de.danielluedecke.zettelkasten.util.TableUtils.selectByTyping(jTablePreview, jTextFieldFilterTable, 1);
                }
            }
        });
        // create action which should be executed when the user presses
        // the enter-key
        AbstractAction a_enter = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jTextFieldFilterTable == e.getSource()) {
                    filterList(false);
                }
            }
        };
        // put action to the textfield's actionmaps
        jTextFieldFilterTable.getActionMap().put("EnterKeyPressed", a_enter);
        // associate enter-keystroke with that action
        KeyStroke ks = KeyStroke.getKeyStroke("ENTER");
        jTextFieldFilterTable.getInputMap().put(ks, "EnterKeyPressed");
        // create action which should be executed when the user presses
        // the alt+enter-key (reg ex filter)
        AbstractAction a_regex_enter = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jTextFieldFilterTable == e.getSource()) {
                    filterList(true);
                }
            }
        };
        // put action to the textfield's actionmaps
        jTextFieldFilterTable.getActionMap().put("RegExEnterKeyPressed", a_regex_enter);
        // associate enter-keystroke with that action
        ks = KeyStroke.getKeyStroke("alt ENTER");
        jTextFieldFilterTable.getInputMap().put(ks, "RegExEnterKeyPressed");
        // create action which should be executed when the user presses
        // the delete/backspace-key
        AbstractAction a_delete = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jTablePreview == e.getSource()) {
                    deleteBibtexEntry();
                }
            }
        };
        // put action to the tables' actionmaps
        jTablePreview.getActionMap().put("DeleteKeyPressed", a_delete);
        // check for os, and use appropriate controlKey
        ks = KeyStroke.getKeyStroke((PlatformUtil.isMacOS()) ? "BACK_SPACE" : "DELETE");
        jTablePreview.getInputMap().put(ks, "DeleteKeyPressed");
    }

    private void filterList(boolean regEx) {
        // when we filter the table and want to restore it, we don't need to run the
        // time-consuming task that creates the author-list and related author-frequencies.
        // instead, we simply copy the values from the linkedlist to the table-model, which is
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
        DefaultTableModel dtm = (DefaultTableModel) jTablePreview.getModel();
        // if we haven't already stored the current complete table data, do this now
        if (null == linkedtablelist) {
            // create new instance of list
            linkedtablelist = new LinkedList<>();
            // go through all table-data
            for (int cnt = 0; cnt < dtm.getRowCount(); cnt++) {
                // init the object-variable
                Object[] o = new Object[2];
                // fill object with values
                o[0] = dtm.getValueAt(jTablePreview.convertRowIndexToModel(cnt), 0);
                o[1] = dtm.getValueAt(jTablePreview.convertRowIndexToModel(cnt), 1);
                // add object to linked list
                linkedtablelist.add(o);
            }
        }
        de.danielluedecke.zettelkasten.util.TableUtils.filterTable(jTablePreview, dtm, text, new int[]{1}, regEx);
        // reset textfield
        jTextFieldFilterTable.setText("");
        jTextFieldFilterTable.requestFocusInWindow();
        // enable textfield only if we have more than 1 element in the jtable
        jTextFieldFilterTable.setEnabled(jTablePreview.getRowCount() > 0);
        // enable refresh button
        jButtonRefreshView.setEnabled(true);
    }

    /**
     * Inits the jTable, i.e. sets up comparers, sorters, and selection
     * listeners.
     */
    private void initTable() {
        // create new table sorter
        TableRowSorter<TableModel> sorter = new TableRowSorter<>();
        // tell tgis jtable that it has an own sorter
        jTablePreview.setRowSorter(sorter);
        // and tell the sorter, which table model to sort.
        sorter.setModel((DefaultTableModel) jTablePreview.getModel());
        // in this table, the first column needs a custom comparator.
        try {
            sorter.setComparator(0, new Comparer());
            sorter.setComparator(1, new Comparer());
        } catch (IndexOutOfBoundsException e) {
            Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
        }
        // noe header re-ordering
        jTablePreview.getTableHeader().setReorderingAllowed(false);
        // apply grid-settings and cellspacing
        jTablePreview.setGridColor(settingsObj.getTableGridColor());
        jTablePreview.setShowHorizontalLines(settingsObj.getShowGridHorizontal());
        jTablePreview.setShowVerticalLines(settingsObj.getShowGridVertical());
        jTablePreview.setIntercellSpacing(settingsObj.getCellSpacing());
        // clear table
        DefaultTableModel dtm = (DefaultTableModel) jTablePreview.getModel();
        dtm.setRowCount(0);
        
        // Set font.
        Font settingsTableFont = settingsObj.getTableFont();
        jTablePreview.setFont(settingsTableFont);
        
        // create selection listener for table, so that the apply button is being
        // enabled, when bibkeys for multiple authors are set...
        SelectionListener listener = new SelectionListener();
        jTablePreview.getSelectionModel().addListSelectionListener(listener);
        jTablePreview.getColumnModel().getSelectionModel().addListSelectionListener(listener);
    }

    /**
     * En- or disables the components that are related to the choice of bibkeys
     *
     * @param value
     */
    private void toggleOptions(boolean value) {
        // toggle file-options
        jTablePreview.setEnabled(value);
        jComboBoxShowBibTex.setEnabled(value);
        jLabel1.setEnabled(value);
        jButtonRefreshView.setEnabled((value) ? linkedtablelist != null : false);
        jTextFieldFilterTable.setEnabled((value) ? jTablePreview.getRowCount() > 0 : false);
        // toggle manual options
        jTextFieldManualBibkey.setEnabled(!value);
        jLabelManualBibkey.setEnabled(!value);
    }

    /**
     * Closes the window
     */
    @Action
    public void cancel() {
        settingsObj.setLastUsedSetBibyKeyChoice((jRadioButtonFileBibkey.isSelected()) ? CHOOSE_BIBKEY_FROM_DB : CHOOSE_BIBKEY_MANUAL);
        settingsObj.setLastUsedSetBibyKeyType(jComboBoxShowBibTex.getSelectedIndex());
        dispose();
        setVisible(false);
    }

    @Action
    public void applyChanges() {
        settingsObj.setLastUsedSetBibyKeyChoice((jRadioButtonFileBibkey.isSelected()) ? CHOOSE_BIBKEY_FROM_DB : CHOOSE_BIBKEY_MANUAL);
        settingsObj.setLastUsedSetBibyKeyType(jComboBoxShowBibTex.getSelectedIndex());
        // init varibale for the new bibkey
        String selectedbibkey = null;
        String selectedauthor;
        // check which option for choosing
        if (jRadioButtonManualBibkey.isSelected()) {
            // select user-input from textfield
            selectedbibkey = jTextFieldManualBibkey.getText();
        } else {
            // else retrieve selected row
            int row = jTablePreview.getSelectedRow();
            // if we have any valid selection...
            if (row != -1) {
                // get table model
                DefaultTableModel dtm = (DefaultTableModel) jTablePreview.getModel();
                // retrieve row-index from the model
                int rowindex = jTablePreview.convertRowIndexToModel(row);
                // retrieve bibkey-value and author from row
                selectedbibkey = dtm.getValueAt(rowindex, 0).toString();
                selectedauthor = dtm.getValueAt(rowindex, 1).toString();
                // and delete table-row, so already used values don't appear anymore
                dtm.removeRow(rowindex);
                // check whether to delete from filtered table list
                if (linkedtablelist != null) {
                    try {
                        Iterator<Object[]> i = linkedtablelist.iterator();
                        // iterate linked list
                        while (i.hasNext()) {
                            // retrieve list data
                            Object[] o = i.next();
                            // check if should be removed
                            if (o[0].toString().equals(selectedbibkey) && o[1].toString().equals(selectedauthor)) {
                                // remove it
                                i.remove();
                            }
                        }
                    } catch (ClassCastException | NullPointerException | UnsupportedOperationException | IllegalStateException ex) {
                        Constants.zknlogger.log(Level.WARNING, ex.getLocalizedMessage());
                    }
                }
            }
        }
        // if no input made or the input was empty, show message dialog
        // and tell user, he has to make a selection or an input...
        if (null == selectedbibkey || selectedbibkey.isEmpty()) {
            JOptionPane.showMessageDialog(null, resourceMap.getString("noBibkeyMsg"), resourceMap.getString("noBibkeyTitle"), JOptionPane.PLAIN_MESSAGE);
            return;
        }
        // if everything is ok, get the authorposition of that author-value where the new bibkey
        // should be set
        int authorpos = dataObj.getAuthorPosition(currentAuthor);
        // get the position of the entered/selected bibkey-value. if it does *not* exist, -1 will be returned,
        // else the authorposition of that author-value that contains that bibkey will be returned
        int bibkeypos = dataObj.getBibkeyPosition(selectedbibkey);
        // check whether the bibkey already exists (bibkey!=-1) in another author-value
        if (authorpos != bibkeypos && bibkeypos != -1) {
            // retrieve author-value of existing bibkey
            String author = dataObj.getAuthor(bibkeypos);
            // if the string is too long, truncate it.
            if (author.length() > 80) {
                author = author.substring(0, 80) + "...";
            }
            // if so, ask user whether to take the same bibkey anyway
            int option = JOptionPane.showConfirmDialog(null, resourceMap.getString("bibkeyExistsMsg", author), resourceMap.getString("bibkeyExistsTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
            // if not, return and do nothing
            if (JOptionPane.NO_OPTION == option) {
                return;
            }
        }
        // else set new author-bibkey
        dataObj.setAuthorBibKey(authorpos, selectedbibkey);
        // update bibkey in author preview
        mainframe.showAuthorText();
        // remove author
        try {
            selectedAuthors.removeFirst();
            // decrease counter
            rowcounter--;
            // set label text
            jLabel1.setText(resourceMap.getString("entryTxt", String.valueOf(rowcounter)));
        } catch (NoSuchElementException ex) {
            Constants.zknlogger.log(Level.WARNING, ex.getLocalizedMessage());
        }
        // init next author (title in label and bibkey in textfield)
        initTitleAndBibkey(false);
    }

    @Action
    public void refreshList() {
        // first check whether we have any saved values at all
        if (linkedtablelist != null) {
            // get table model
            DefaultTableModel dtm = (DefaultTableModel) jTablePreview.getModel();
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
            jButtonRefreshView.setEnabled(false);
        }
        linkedtablelist = null;
    }

    private void fillBibtexTable() {
        // reset linked list
        linkedtablelist = null;
        // get table model
        DefaultTableModel dtm = (DefaultTableModel) jTablePreview.getModel();
        dtm.setRowCount(0);
        // go through all found entries...
        for (int cnt = 0; cnt < bibtexObj.getCount(); cnt++) {
            // ...retrieve bibkey
            String bibkey = bibtexObj.getBibkey(cnt);
            // retrieve each bibtex-author
            String au = bibtexObj.getFormattedEntry(cnt);
            // check whether *all* entries should be displayed or not
            switch (jComboBoxShowBibTex.getSelectedIndex()) {
                // here we want to display only *new* entries
                case 0: // if author does not exist AND the bibkey of the imported literature does not already exist...
                    if (au != null && !au.isEmpty() && -1 == dataObj.getAuthorPosition(au) && -1 == dataObj.getAuthorBibKeyPosition(bibkey)) {
                        // we have a new entry and add it to the table
                        dtm.addRow(new String[]{bibkey, au});
                    }
                    break;
                // here we want to display only *existing* entries
                case 1: // if bibkey already exists...
                    if (au != null && !au.isEmpty() && dataObj.getAuthorBibKeyPosition(bibkey) != -1) {
                        // we have an existing entry and can add it to the table
                        dtm.addRow(new String[]{bibkey, au});
                    }
                    break;
                // here we want to display only *all* entries
                case 2: // if author does not exist...
                    if (au != null && !au.isEmpty()) {
                        // we have any valid entry, whether new or existing, and can add it to the table
                        dtm.addRow(new String[]{bibkey, au});
                    }
                    break;
            }
        }
        // select first entry
        jTablePreview.getSelectionModel().setSelectionInterval(0, 0);
        // set label text
        rowcounter = jTablePreview.getRowCount();
        jLabel1.setText(resourceMap.getString("entryTxt", String.valueOf(rowcounter)));
    }

    /**
     * This class sets up a selection listener for the tables. each table which
     * shall react on selections, e.g. by showing an entry, gets this
     * selectionlistener in the method
     * {@link #initSelectionListeners() initSelectionListeners()}.
     */
    private class SelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            // get list selection model
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            // set value-adjusting to true, so we don't fire multiple value-changed events...
            lsm.setValueIsAdjusting(true);
            // check for any values
            if (null == selectedAuthors || selectedAuthors.isEmpty()) {
                jButtonApply.setEnabled(false);
                return;
            }
            // en- or disable apply-button
            if (!jRadioButtonManualBibkey.isSelected()) {
                // else when file-option is selected, en- or disable depending on table-selection
                jButtonApply.setEnabled(jTablePreview.getSelectedRow() != -1);
            }
        }
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    // Generated using JFormDesigner Evaluation license - Ralf Barkow
    private void initComponents() {
        ResourceBundle bundle = ResourceBundle.getBundle("de.danielluedecke.zettelkasten.resources.CSetBibKey");
        jRadioButtonManualBibkey = new JRadioButton();
        jLabelManualBibkey = new JLabel();
        jTextFieldManualBibkey = new JTextField();
        jRadioButtonFileBibkey = new JRadioButton();
        jButtonCancel = new JButton();
        jButtonApply = new JButton();
        jScrollPane1 = new JScrollPane();
        jTablePreview = (settingsObj.isMacStyle()) ? MacWidgetFactory.createITunesTable(null) : new javax.swing.JTable();
        jLabelTitle = new JLabel();
        jButtonRefreshView = new JButton();
        jTextFieldFilterTable = new JTextField();
        jComboBoxShowBibTex = new JComboBox<>();
        jLabel1 = new JLabel();
        buttonGroup1 = new ButtonGroup();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(bundle.getString("CFormSetBibKey.title"));
        setName("CFormSetBibKey");
        Container contentPane = getContentPane();

        //---- jRadioButtonManualBibkey ----
        jRadioButtonManualBibkey.setSelected(true);
        jRadioButtonManualBibkey.setText(bundle.getString("jRadioButtonManualBibkey.text"));
        jRadioButtonManualBibkey.setToolTipText(bundle.getString("jRadioButtonManualBibkey.toolTipText"));
        jRadioButtonManualBibkey.setName("jRadioButtonManualBibkey");

        //---- jLabelManualBibkey ----
        jLabelManualBibkey.setText(bundle.getString("jLabelManualBibkey.text"));
        jLabelManualBibkey.setName("jLabelManualBibkey");

        //---- jTextFieldManualBibkey ----
        jTextFieldManualBibkey.setText(bundle.getString("jTextFieldManualBibkey.text"));
        jTextFieldManualBibkey.setName("jTextFieldManualBibkey");

        //---- jRadioButtonFileBibkey ----
        jRadioButtonFileBibkey.setText(bundle.getString("jRadioButtonFileBibkey.text"));
        jRadioButtonFileBibkey.setToolTipText(bundle.getString("jRadioButtonFileBibkey.toolTipText"));
        jRadioButtonFileBibkey.setName("jRadioButtonFileBibkey");

        //---- jButtonCancel ----
        jButtonCancel.setName("jButtonCancel");

        //---- jButtonApply ----
        jButtonApply.setName("jButtonApply");

        //======== jScrollPane1 ========
        {
            jScrollPane1.setName("jScrollPane1");
            jScrollPane1.setPreferredSize(new Dimension(100, 100));

            //---- jTablePreview ----
            jTablePreview.setModel(new DefaultTableModel(
                new Object[][] {
                    {null, null},
                    {null, null},
                    {null, null},
                    {null, null},
                },
                new String[] {
                    "Bibkey", "Eintrag"
                }
            ) {
                Class<?>[] columnTypes = new Class<?>[] {
                    String.class, String.class
                };
                boolean[] columnEditable = new boolean[] {
                    false, false
                };
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return columnTypes[columnIndex];
                }
                @Override
                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return columnEditable[columnIndex];
                }
            });
            jTablePreview.setName("jTablePreview");
            jTablePreview.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            jScrollPane1.setViewportView(jTablePreview);
        }

        //---- jLabelTitle ----
        jLabelTitle.setText(bundle.getString("jLabelTitle.text"));
        jLabelTitle.setName("jLabelTitle");

        //---- jButtonRefreshView ----
        jButtonRefreshView.setBorderPainted(false);
        jButtonRefreshView.setContentAreaFilled(false);
        jButtonRefreshView.setFocusPainted(false);
        jButtonRefreshView.setName("jButtonRefreshView");
        jButtonRefreshView.setPreferredSize(new Dimension(20, 20));

        //---- jTextFieldFilterTable ----
        jTextFieldFilterTable.setToolTipText(bundle.getString("jTextFieldFilterTable.toolTipText"));
        jTextFieldFilterTable.setName("jTextFieldFilterTable");

        //---- jComboBoxShowBibTex ----
        jComboBoxShowBibTex.setModel(new DefaultComboBoxModel<>(new String[] {
            "Nur neue BibTex-Eintr\u00e4ge anzeigen",
            "Nur vorhandene BibTex-Eintr\u00e4ge anzeigen",
            "Alle BibTex-Eintr\u00e4ge anzeigen"
        }));
        jComboBoxShowBibTex.setName("jComboBoxShowBibTex");

        //---- jLabel1 ----
        jLabel1.setText(bundle.getString("jLabel1.text"));
        jLabel1.setName("jLabel1");

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
            contentPaneLayout.createParallelGroup()
                .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                            .addGap(21, 21, 21)
                            .addGroup(contentPaneLayout.createParallelGroup()
                                .addGroup(contentPaneLayout.createSequentialGroup()
                                    .addComponent(jComboBoxShowBibTex, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabel1)
                                    .addGap(0, 209, Short.MAX_VALUE))
                                .addGroup(contentPaneLayout.createSequentialGroup()
                                    .addComponent(jLabelManualBibkey)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jTextFieldManualBibkey))))
                        .addGroup(contentPaneLayout.createSequentialGroup()
                            .addComponent(jTextFieldFilterTable)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButtonRefreshView, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButtonCancel)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButtonApply))
                        .addGroup(contentPaneLayout.createSequentialGroup()
                            .addGroup(contentPaneLayout.createParallelGroup()
                                .addComponent(jRadioButtonManualBibkey)
                                .addComponent(jLabelTitle)
                                .addComponent(jRadioButtonFileBibkey))
                            .addGap(0, 0, Short.MAX_VALUE)))
                    .addContainerGap())
        );
        contentPaneLayout.setVerticalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jLabelTitle)
                    .addGap(18, 18, 18)
                    .addComponent(jRadioButtonManualBibkey)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextFieldManualBibkey, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabelManualBibkey))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(jRadioButtonFileBibkey)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(jComboBoxShowBibTex, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel1))
                    .addGap(18, 18, 18)
                    .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 225, Short.MAX_VALUE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                        .addComponent(jTextFieldFilterTable, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonCancel, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonApply, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonRefreshView, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGap(3, 3, 3))
        );
        pack();
        setLocationRelativeTo(getOwner());

        //---- buttonGroup1 ----
        buttonGroup1.add(jRadioButtonManualBibkey);
        buttonGroup1.add(jRadioButtonFileBibkey);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Ralf Barkow
    private JRadioButton jRadioButtonManualBibkey;
    private JLabel jLabelManualBibkey;
    private JTextField jTextFieldManualBibkey;
    private JRadioButton jRadioButtonFileBibkey;
    private JButton jButtonCancel;
    private JButton jButtonApply;
    private JScrollPane jScrollPane1;
    private JTable jTablePreview;
    private JLabel jLabelTitle;
    private JButton jButtonRefreshView;
    private JTextField jTextFieldFilterTable;
    private JComboBox<String> jComboBoxShowBibTex;
    private JLabel jLabel1;
    private ButtonGroup buttonGroup1;
    // End of variables declaration//GEN-END:variables

}
