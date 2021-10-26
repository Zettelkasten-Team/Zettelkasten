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
import de.danielluedecke.zettelkasten.database.StenoData;
import de.danielluedecke.zettelkasten.util.Constants;
import com.explodingpixels.macwidgets.MacWidgetFactory;
import com.explodingpixels.widgets.TableUtils;
import de.danielluedecke.zettelkasten.util.ColorUtil;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.application.Action;

/**
 *
 * @author danielludecke
 */
public class CStenoEdit extends javax.swing.JDialog {

    private final StenoData stenoObj;
    private final Settings settingsObj;

    private boolean modified = false;
    public boolean isModified() {
        return modified;
    }
    /**
     *
     */
    private DefaultTableModel tm;
    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap =
        org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
        getContext().getResourceMap(CStenoEdit.class);


    /**
     * 
     * @param parent
     * @param stn
     * @param st 
     */
    public CStenoEdit(java.awt.Frame parent, StenoData stn, Settings st) {
        super(parent);
        stenoObj=stn;
        settingsObj = st;
        initComponents();
        initTheRest(null);
    }
    /**
     * 
     * @param parent
     * @param stn
     * @param st
     * @param inivalue 
     */
    public CStenoEdit(java.awt.Frame parent, StenoData stn, Settings st, String inivalue) {
        super(parent);
        stenoObj=stn;
        settingsObj = st;
        initComponents();
        initTheRest(inivalue);
    }

    
    /**
     * 
     * @param initvalue 
     */
    private void initTheRest(String initvalue) {
        // disable apply-button
        jButtonApply.setEnabled(false);
        // set application icon
        setIconImage(Constants.zknicon.getImage());
        /*
         * Constructor for Matte Border
         * public MatteBorder(int top, int left, int bottom, int right, Color matteColor)
         */
        jScrollPane1.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ColorUtil.getBorderGray(settingsObj)));        if (settingsObj.isSeaGlass()) {
            jButtonApply.putClientProperty("JComponent.sizeVariant", "small");
            jButtonCancel.putClientProperty("JComponent.sizeVariant", "small");
        }
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
        // get the default fontsize for tables and lists
        int defaultsize = settingsObj.getTableFontSize();
        // only set new fonts, when fontsize differs from the initial value
        if (defaultsize>0) {
            // get current font
            Font f = jTableSteno.getFont();
            // create new font, add fontsize-value
            f = new Font(f.getName(), f.getStyle(), f.getSize()+defaultsize);
            // set new font
            jTableSteno.setFont(f);
        }
        // create auto-sorter for tabel
        jTableSteno.setAutoCreateRowSorter(true);
        jTableSteno.setGridColor(settingsObj.getTableGridColor());
        if (settingsObj.isMacAqua()) {
            TableUtils.SortDelegate sortDelegate = new TableUtils.SortDelegate() {
                @Override
                public void sort(int columnModelIndex, TableUtils.SortDirection sortDirection) {
                }
            };
            TableUtils.makeSortable(jTableSteno, sortDelegate);
            // change back default column-resize-behaviour when we have itunes-tables,
            // since the default for those is "auto resize off"
            jTableSteno.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        }
        // init the table, i.e. fill it with all existing data
        tm = (DefaultTableModel)jTableSteno.getModel();
        tm.setRowCount(0);
        // add all spellchecking-entries to linked list
        for (int cnt=0; cnt<stenoObj.getCount(); cnt++) {
            String[] value = stenoObj.getElement(cnt);
            if (value!=null) tm.addRow(value);
        }
        // add one row for editing...
        tm.setRowCount(tm.getRowCount()+1);
        // when we have a parameter, set this valus as initial value into the table
        // we do this before we init the change-event, so this does not trigger any changes
        // that will be tracked...
        if (initvalue!=null) {
            // get selected row...
            int row = tm.getRowCount()-1;
            // set initial value, which typically is a word where
            // the user wants to have a short-form for
            tm.setValueAt(initvalue, row, 1);
            // select the previous cell, so the user can start editing the short form.
            jTableSteno.setColumnSelectionInterval(0, 0);
            jTableSteno.setRowSelectionInterval(row, row);
            // and scroll rectangle to visible area
            jTableSteno.scrollRectToVisible(jTableSteno.getCellRect(row, 0, false));
        }
        // add table model listener. in case we have edited a new value at the last
        // column or row, automatically add a new column/row
        tm.addTableModelListener(new TableModelListener() {
            @Override public void tableChanged(TableModelEvent e) {
                // only react on updates, i.e. the user edited/inserted/changed new values
                if (TableModelEvent.UPDATE == e.getType()) {
                    // if edited row was last row, add one row
                    if (e.getLastRow()==(tm.getRowCount()-1)) tm.setRowCount(tm.getRowCount()+1);
                }
                // enable apply-button
                jButtonApply.setEnabled(true);
            }
        });
        // create action which should be executed when the user presses
        // the delete/backspace-key
        AbstractAction a_delete = new AbstractAction(){
            @Override public void actionPerformed(ActionEvent e) {
                // retrieve selected row and column
                int row = jTableSteno.getSelectedRow();
                int col = jTableSteno.getSelectedColumn();
                // remove row
                tm.removeRow(row);
                // adjust row if last row was selected
                if (row>=tm.getRowCount()) row--;
                // select new cell
                jTableSteno.setColumnSelectionInterval(col, col);
                jTableSteno.setRowSelectionInterval(row, row);
                // enable apply-button
                jButtonApply.setEnabled(true);
            }
        };
        jTableSteno.getActionMap().put("DeleteKeyPressed",a_delete);
        // check for os, and use appropriate controlKey
        KeyStroke ks = KeyStroke.getKeyStroke((System.getProperty("os.name").toLowerCase().startsWith("mac os"))?"meta BACK_SPACE":"ctrl DELETE");
        jTableSteno.getInputMap().put(ks, "DeleteKeyPressed");
        // disable reordering of columns
        jTableSteno.getTableHeader().setReorderingAllowed(false);
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
        stenoObj.clear();
        // retrieve all table rows
        for (int row=0; row<tm.getRowCount(); row++) {
            // get table-cell-values
            Object o1 = tm.getValueAt(row, 0);
            Object o2 = tm.getValueAt(row, 1);
            // check whether we have any valid values...
            if (o1!=null && o2!=null) {
                // retrieve string-content of values
                String abbr = tm.getValueAt(row, 0).toString();
                String longword = tm.getValueAt(row, 1).toString();
                // if we have valid values, add them to the file
                if (abbr!=null && !abbr.isEmpty() && longword!=null && !longword.isEmpty()) stenoObj.addElement(abbr, longword);
            }
        }
        // now check whether everything is ok
        if (!stenoObj.isDocumentOK()) {
            // log error
            Constants.zknlogger.log(Level.WARNING,"Warning! Could not save stenography data! The original XML document has been restored!");
            // if not, restore document and tell user about problem
            stenoObj.restoreDocument();
            // tell user about problem
            JOptionPane.showMessageDialog(null,resourceMap.getString("errSavingDataMsg"),
                                          resourceMap.getString("errSavingDataTitle"),
                                          JOptionPane.PLAIN_MESSAGE);
            // no modifications have been made
            modified = false;
        }
        else {
            modified = true;
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
        jTableSteno = (settingsObj.isMacStyle()) ? MacWidgetFactory.createITunesTable(null) : new javax.swing.JTable();
        jButtonCancel = new javax.swing.JButton();
        jButtonApply = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getResourceMap(CStenoEdit.class);
        setTitle(resourceMap.getString("FormStenoEdit.title")); // NOI18N
        setModal(true);
        setName("FormStenoEdit"); // NOI18N

        jScrollPane1.setBorder(null);
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTableSteno.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Abkürzung", "Original"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jTableSteno.setCellSelectionEnabled(true);
        jTableSteno.setName("jTableSteno"); // NOI18N
        jTableSteno.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(jTableSteno);
        jTableSteno.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("jTableSteno.columnModel.title0")); // NOI18N
        jTableSteno.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("jTableSteno.columnModel.title1")); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getActionMap(CStenoEdit.class, this);
        jButtonCancel.setAction(actionMap.get("cancel")); // NOI18N
        jButtonCancel.setName("jButtonCancel"); // NOI18N

        jButtonApply.setAction(actionMap.get("applyChanges")); // NOI18N
        jButtonApply.setName("jButtonApply"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(156, Short.MAX_VALUE)
                .addComponent(jButtonCancel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonApply)
                .addContainerGap())
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonApply)
                    .addComponent(jButtonCancel))
                .addGap(3, 3, 3))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonApply;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTableSteno;
    // End of variables declaration//GEN-END:variables

}
