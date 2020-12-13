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

import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.database.Synonyms;
import de.danielluedecke.zettelkasten.util.Constants;
import com.explodingpixels.macwidgets.MacWidgetFactory;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.util.ColorUtil;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.application.Action;

/**
 *
 * @author danielludecke
 */
public class CSynonymsEdit extends javax.swing.JDialog {

    /**
     *
     */
    private final Synonyms synonymsObj;
    private final Settings settingsObj;
    private final Daten dataObj;
    /**
     *
     */
    private final DefaultTableModel tm;
    /**
     *
     */
    private boolean modified = false;

    /**
     *
     * @return
     */
    public boolean isModified() {
        return modified;
    }
    /**
     *
     */
    private int findrow = 0;
    /**
     *
     */
    private int findcol = 0;
    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap
            = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
            getContext().getResourceMap(CSynonymsEdit.class);

    /**
     *
     * @param parent
     * @param sy
     * @param st
     * @param dat
     */
    public CSynonymsEdit(java.awt.Frame parent, Synonyms sy, Settings st, Daten dat) {
        super(parent);
        settingsObj = st;
        dataObj = dat;
        synonymsObj = sy;
        initComponents();
        // set application icon
        setIconImage(Constants.zknicon.getImage());
        /*
         * Constructor for Matte Border
         * public MatteBorder(int top, int left, int bottom, int right, Color matteColor)
         */
        jScrollPane1.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ColorUtil.getBorderGray(settingsObj)));
        // disable apply-button
        jButtonApply.setEnabled(false);
        // these codelines add an escape-listener to the dialog. so, when the user
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
        // when we have aqua-style, change scrollbars
        if (settingsObj.isMacAqua() || settingsObj.isSeaGlass()) {
            jButtonFindNext.putClientProperty("JButton.buttonType", "segmentedRoundRect");
            jButtonFindNext.putClientProperty("JButton.segmentPosition", "only");
            jTextFieldFind.putClientProperty("JTextField.variant", "search");
            if (settingsObj.isSeaGlass()) {
                jButtonCancel.putClientProperty("JComponent.sizeVariant", "small");
                jButtonApply.putClientProperty("JComponent.sizeVariant", "small");
            }
        }
        // get the default fontsize for tables and lists
        int defaultsize = settingsObj.getTableFontSize();
        // only set new fonts, when fontsize differs from the initial value
        if (defaultsize > 0) {
            // get current font
            Font f = jTable1.getFont();
            // create new font, add fontsize-value
            f = new Font(f.getName(), f.getStyle(), f.getSize() + defaultsize);
            // set new font
            jTable1.setFont(f);
        }
        // create auto-sorter for tabel
        jTable1.setAutoCreateRowSorter(true);
        jTable1.setGridColor(settingsObj.getTableGridColor());
        // init the table, i.e. fill it with all existing data
        // therefor retrieve the table model
        tm = (DefaultTableModel) jTable1.getModel();
        // and delete all rows and columns
        tm.setRowCount(0);
        tm.setColumnCount(0);
        // add a initial column
        tm.addColumn(resourceMap.getString("synonymText"));
        // go through al synomyms...
        for (int cnt = 0; cnt < synonymsObj.getCount(); cnt++) {
            // get each synonym-line. a synonym-line is a string array that contains the index-word
            // in the first place of the array, and all synonyms in the following fields of the array
            Object[] rowdata = synonymsObj.getSynonymLine(cnt, true);
            // if we have more values in the synonym-line to add than columns, exists, add new columns
            while (tm.getColumnCount() < rowdata.length) {
                tm.addColumn(String.valueOf(tm.getColumnCount()));
            }
            // finally, add the data to the table model
            tm.addRow(rowdata);
        }
        // add one row and column for editing...
        tm.setRowCount(tm.getRowCount() + 1);
        tm.addColumn(String.valueOf(tm.getColumnCount()));
        // add table model listener. in case we have edited a new value at the last
        // column or row, automatically add a new column/row
        tm.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                // only react on updates, i.e. the user edited/inserted/changed new values
                if (TableModelEvent.UPDATE == e.getType()) {
                    // if edited row was last row, add one row
                    if (e.getLastRow() == (tm.getRowCount() - 1)) {
                        tm.setRowCount(tm.getRowCount() + 1);
                    }
                    // if edited column was last column, add one column
                    if (e.getColumn() == (tm.getColumnCount() - 1)) {
                        tm.addColumn(String.valueOf(tm.getColumnCount()));
                    }
                }
                // enable apply-button
                jButtonApply.setEnabled(true);
            }
        });
        // create action which should be executed when the user presses
        // the delete/backspace-key
        AbstractAction a_delete = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // retrieve selected row and column
                int row = jTable1.getSelectedRow();
                int col = jTable1.getSelectedColumn();
                // remove row
                tm.removeRow(row);
                // adjust row if last row was selected
                if (row >= tm.getRowCount()) {
                    row--;
                }
                // select new cell
                jTable1.setColumnSelectionInterval(col, col);
                jTable1.setRowSelectionInterval(row, row);
                // enable apply-button
                jButtonApply.setEnabled(true);
            }
        };
        jTable1.getActionMap().put("DeleteKeyPressed", a_delete);
        // check for os, and use appropriate controlKey
        KeyStroke ks = KeyStroke.getKeyStroke((System.getProperty("os.name").toLowerCase().startsWith("mac os")) ? "meta BACK_SPACE" : "ctrl DELETE");
        jTable1.getInputMap().put(ks, "DeleteKeyPressed");
        // create action which should be executed when the user presses
        // the delete/backspace-key
        AbstractAction a_enter = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                findrow = 0;
                findcol = 0;
                jTextFieldFind.setForeground((find()) ? Color.BLACK : Color.RED);
            }
        };
        jTextFieldFind.getActionMap().put("EnterKeyPressed", a_enter);
        // check for os, and use appropriate controlKey
        ks = KeyStroke.getKeyStroke("ENTER");
        jTextFieldFind.getInputMap().put(ks, "EnterKeyPressed");
    }

    private boolean find() {
        // get find-text
        String text = jTextFieldFind.getText().toLowerCase();
        // as long as last row is not reached, go on...
        while (findrow < tm.getRowCount()) {
            // as long as last columns is not reached, go on...
            while (findcol < tm.getColumnCount()) {
                // get cell-value
                Object o = tm.getValueAt(findrow, findcol);
                // check whether we had data in that cell
                if (o != null) {
                    // convert value to lowercase string
                    String s = o.toString().toLowerCase();
                    // check whether findtext occurs in the cell-value
                    if (s.contains(text)) {
                        // if yes, select new cell
                        jTable1.setColumnSelectionInterval(findcol, findcol);
                        jTable1.setRowSelectionInterval(findrow, findrow);
                        // make selection visible
                        jTable1.scrollRectToVisible(jTable1.getCellRect(findrow, findcol, false));
                        // and leave method
                        return true;
                    }
                }
                // else go to next cell
                findcol++;
            }
            // reset column counter
            findcol = 0;
            // increase rowcounter
            findrow++;
        }
        return false;
    }

    private void findNext() {
        // go to next cell
        findcol++;
        // if last column is reached...
        if (findcol >= (tm.getColumnCount() - 1)) {
            // reset column-counter
            findcol = 0;
            // increase rowcounter
            findrow++;
        }
        // call find-method
        jTextFieldFind.setForeground((find()) ? Color.BLACK : Color.RED);
    }

    @Action
    public void cancel() {
        modified = false;
        dispose();
        setVisible(false);
    }

    @Action
    public void applyChanges() {
        // clear all synonyms
        synonymsObj.clear();
        // create linked list
        LinkedList<String> synline = new LinkedList<>();
        // retrieve all table rows
        for (int row = 0; row < tm.getRowCount(); row++) {
            // clear list
            synline.clear();
            // go through all columns
            for (int col = 0; col < tm.getColumnCount(); col++) {
                // get data from each cell
                Object o = tm.getValueAt(row, col);
                // check whether we had any data in that cell
                if (o != null) {
                    // convert object to string
                    String val = o.toString();
                    // if it's not null and not empty, add it to the rowdata-variable "synline
                    if (val != null && !val.isEmpty()) {
                        synline.add(val);
                    }
                }
            }
            // if we have a data-row, add it to the datafile
            if (synline.size() > 1) {
                synonymsObj.addSynonym(synline.toArray(new String[synline.size()]));
            }
        }
        // now check whether everything is ok
        if (!synonymsObj.isDocumentOK()) {
            // log error
            Constants.zknlogger.log(Level.WARNING, "Warning! Could not save synonyms data! The original XML document has been restored!");
            // if not, restore document and tell user about problem
            synonymsObj.restoreDocument();
            // tell user about problem
            JOptionPane.showMessageDialog(null, resourceMap.getString("errSavingDataMsg"),
                    resourceMap.getString("errSavingDataTitle"),
                    JOptionPane.PLAIN_MESSAGE);
            // no modifications have been made
            modified = false;
        } else {
            modified = true;
            // if synonyms are displayed in table, set table no longer up to date
            if (settingsObj.getShowSynonymsInTable()) {
                dataObj.setKeywordlistUpToDate(false);
            }
        }
        dispose();
        setVisible(false);
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = (settingsObj.isMacAqua()) ? MacWidgetFactory.createITunesTable(null) : new javax.swing.JTable();
        jButtonCancel = new javax.swing.JButton();
        jButtonApply = new javax.swing.JButton();
        jTextFieldFind = new javax.swing.JTextField();
        jButtonFindNext = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getResourceMap(CSynonymsEdit.class);
        setTitle(resourceMap.getString("FormSynonymsEdit.title")); // NOI18N
        setMinimumSize(new java.awt.Dimension(200, 200));
        setModal(true);
        setName("FormSynonymsEdit"); // NOI18N

        jScrollPane1.setBorder(null);
        jScrollPane1.setMinimumSize(new java.awt.Dimension(30, 30));
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTable1.setCellSelectionEnabled(true);
        jTable1.setName("jTable1"); // NOI18N
        jScrollPane1.setViewportView(jTable1);

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getActionMap(CSynonymsEdit.class, this);
        jButtonCancel.setAction(actionMap.get("cancel")); // NOI18N
        jButtonCancel.setName("jButtonCancel"); // NOI18N

        jButtonApply.setAction(actionMap.get("applyChanges")); // NOI18N
        jButtonApply.setName("jButtonApply"); // NOI18N

        jTextFieldFind.setName("jTextFieldFind"); // NOI18N

        jButtonFindNext.setIcon(resourceMap.getIcon("jButtonFindNext.icon")); // NOI18N
        jButtonFindNext.setToolTipText(resourceMap.getString("jButtonFindNext.toolTipText")); // NOI18N
        jButtonFindNext.setName("jButtonFindNext"); // NOI18N
        jButtonFindNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFindNextActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTextFieldFind, javax.swing.GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonFindNext)
                .addGap(18, 18, 18)
                .addComponent(jButtonCancel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonApply)
                .addContainerGap())
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 359, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonCancel)
                        .addComponent(jTextFieldFind, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonApply))
                    .addComponent(jButtonFindNext))
                .addGap(3, 3, 3))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonFindNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFindNextActionPerformed
        findNext();
    }//GEN-LAST:event_jButtonFindNextActionPerformed



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonApply;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonFindNext;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextFieldFind;
    // End of variables declaration//GEN-END:variables

}
