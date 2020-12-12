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
import de.danielluedecke.zettelkasten.util.Constants;
import com.explodingpixels.macwidgets.MacWidgetFactory;
import de.danielluedecke.zettelkasten.util.ColorUtil;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.application.Action;

/**
 *
 * @author danielludecke
 */
public class CInsertTable extends javax.swing.JDialog {
    /**
     *
     */
    private final DefaultTableModel tm;    
    /**
     *
     */
    private boolean modified = false;
    public boolean isModified() {
        return modified;
    }
    private final Settings settingsObj;
    /**
     * 
     */
    private String tabletag = "";
    public String getTableTag() {
        return tabletag;
    }

    /**
     * 
     * @param parent
     * @param s
     * @param etable 
     */
    public CInsertTable(java.awt.Frame parent, Settings s, String etable) {
        super(parent);
        settingsObj = s;
        initComponents();
        // set application icon
        setIconImage(Constants.zknicon.getImage());
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
        jTable1.setGridColor(settingsObj.getTableGridColor());
        // init table
        tm = (DefaultTableModel)jTable1.getModel();
        // check whether we have an edit-string
        if (null==etable) {
            // delete all rows and columns
            tm.setRowCount(2);
            tm.setColumnCount(2);
        }
        else {
            tm.setRowCount(0);
            tm.setColumnCount(0);
            // retrieve all table rows
            String[] tablerows = etable.split("\\n");
            // iterate all tablerows
            for (String trow : tablerows) {
                // if the row is not empty, go on
                if (!trow.isEmpty()) {
                    // check whether we have a caption
                    if (trow.startsWith(Constants.FORMAT_TABLECAPTION_OPEN)) {
                        try {
                            // if so, set text in textfield
                            jTextFieldCaption.setText(trow.substring(Constants.FORMAT_TABLECAPTION_OPEN.length(), trow.length()-Constants.FORMAT_TABLECAPTION_CLOSE.length()));
                        }
                        catch (IndexOutOfBoundsException ex) {
                        }
                    }
                    else {
                        // check whether row is table header or a simple data-row. therefore,
                        // look for occurences of "|", which is a cell-separator, or for "^",
                        // whih is the separator for the tableheader
                        boolean isheader = trow.contains("^");
                        // use approprate split-char: | for cells, ^ for header-rows
                        String[] tablecells = trow.split((isheader)?"\\^":"\\|");
                        // add as much columns as needed
                        while (tm.getColumnCount()<tablecells.length) tm.addColumn(null);
                        // add table data
                        tm.addRow(tablecells);
                    }
                }
            }
            tm.setRowCount(tm.getRowCount()+1);
            tm.setColumnCount(tm.getColumnCount()+1);
        }
        // get the default fontsize for tables and lists
        int defaultsize = settingsObj.getTableFontSize();
        // only set new fonts, when fontsize differs from the initial value
        if (defaultsize>0) {
            // get current font
            Font f = jTable1.getFont();
            // create new font, add fontsize-value
            f = new Font(f.getName(), f.getStyle(), f.getSize()+defaultsize);
            // set new font
            jTable1.setFont(f);
        }
        // create auto-sorter for tabel
        jTable1.setAutoCreateRowSorter(false);
        // init Listeners and action maps
        initListeners();
        initBorders(settingsObj);
    }

    
    private void initBorders(Settings settingsObj) {
        /*
         * Constructor for Matte Border
         * public MatteBorder(int top, int left, int bottom, int right, Color matteColor)
         */
        jScrollPane1.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ColorUtil.getBorderGray(settingsObj)));
    }
    
    private void initListeners() {
        // add table model listener. in case we have edited a new value at the last
        // column or row, automatically add a new column/row
        tm.addTableModelListener(new TableModelListener() {
            @Override public void tableChanged(TableModelEvent e) {
                // only react on updates, i.e. the user edited/inserted/changed new values
                if (TableModelEvent.UPDATE == e.getType()) {
                    // if edited row was last row, add one row
                    if (e.getLastRow()==(tm.getRowCount()-1)) tm.setRowCount(tm.getRowCount()+1);
                    // if edited column was last column, add one column
                    if (e.getColumn()==(tm.getColumnCount()-1)) tm.setColumnCount(tm.getColumnCount()+1);
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
                int row = jTable1.getSelectedRow();
                int col = jTable1.getSelectedColumn();
                // remove row
                tm.removeRow(row);
                // adjust row if last row was selected
                if (row>=tm.getRowCount()) row--;
                // select new cell
                jTable1.setColumnSelectionInterval(col, col);
                jTable1.setRowSelectionInterval(row, row);
                // enable apply-button
                jButtonApply.setEnabled(true);
            }
        };
        jTable1.getActionMap().put("DeleteKeyPressed",a_delete);
        // check for os, and use appropriate controlKey
        KeyStroke ks = KeyStroke.getKeyStroke((System.getProperty("os.name").toLowerCase().startsWith("mac os"))?"meta BACK_SPACE":"ctrl DELETE");
        jTable1.getInputMap().put(ks, "DeleteKeyPressed");
        // init text field change listener
        jTextFieldCaption.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void changedUpdate(DocumentEvent e) { modified=true; jButtonApply.setEnabled(true); }
            @Override public void insertUpdate(DocumentEvent e) { modified=true; jButtonApply.setEnabled(true); }
            @Override public void removeUpdate(DocumentEvent e) { modified=true; jButtonApply.setEnabled(true); }
        });
        jCheckBoxTableHeader.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent evt) {
                modified=true;
                // enable apply-button
                jButtonApply.setEnabled(true);
            }
        });
    }
    
    
    @Action
    public void insertTable() {
        // this variable stores the values of each table-row, so we can
        // parse them into a single string.
        ArrayList<String> tablerow = new ArrayList<>();
        // prepare stringbuilder that will contain the complete table-data including format-tags.
        StringBuilder tabledata = new StringBuilder("[table]");
        // append caption
        tabledata.append(Constants.FORMAT_TABLECAPTION_OPEN);
        tabledata.append(jTextFieldCaption.getText());
        tabledata.append(Constants.FORMAT_TABLECAPTION_CLOSE);
        // and append a new line
        tabledata.append(System.lineSeparator());
        // retrieve all table rows
        for (int row=0; row<tm.getRowCount()-1; row++) {
            // clear tablerow
            tablerow.clear();
            // go through all columns
            for (int col=0; col<tm.getColumnCount()-1; col++) {
                // get data from each cell
                Object o = tm.getValueAt(row, col);
                // check whether we had any data in that cell
                tablerow.add((o!=null)?o.toString():" ");
            }
            // this is the table-cell separator-char
            String tablesep = "|";
            // if the user wants to format the first row as headline, use the
            // related table-separator-char instead
            if (0==row && jCheckBoxTableHeader.isSelected()) tablesep="^";
            for (String tablerow1 : tablerow) {
                tabledata.append(tablerow1);
                tabledata.append(tablesep);
            }
            // if we have any data, remove last separator char
            if (tabledata.length()>1) tabledata.setLength(tabledata.length()-1);
            // and append a new line
            tabledata.append(System.lineSeparator());
        }
        // if we have any data, remove last new line
        if (tabledata.length()>1) tabledata.setLength(tabledata.length()-System.lineSeparator().length());
        // close tag
        tabledata.append("[/table]");
        // copy stringbuilder to return variable
        tabletag = tabledata.toString();
        modified = true;
        dispose();
        setVisible(false);
    }

    @Action
    public void cancel() {
        // no changes made
        modified = false;
        setVisible(false);
        dispose();
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
        jTable1 = (settingsObj.isMacStyle()) ? MacWidgetFactory.createITunesTable(null) : new javax.swing.JTable();
        jCheckBoxTableHeader = new javax.swing.JCheckBox();
        jButtonApply = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldCaption = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getResourceMap(CInsertTable.class);
        setTitle(resourceMap.getString("FormCInsertTable.title")); // NOI18N
        setModal(true);
        setName("FormCInsertTable"); // NOI18N

        jScrollPane1.setBorder(null);
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTable1.setCellSelectionEnabled(true);
        jTable1.setName("jTable1"); // NOI18N
        jTable1.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(jTable1);

        jCheckBoxTableHeader.setText(resourceMap.getString("jCheckBoxTableHeader.text")); // NOI18N
        jCheckBoxTableHeader.setName("jCheckBoxTableHeader"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getActionMap(CInsertTable.class, this);
        jButtonApply.setAction(actionMap.get("insertTable")); // NOI18N
        jButtonApply.setName("jButtonApply"); // NOI18N

        jButtonCancel.setAction(actionMap.get("cancel")); // NOI18N
        jButtonCancel.setName("jButtonCancel"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jTextFieldCaption.setName("jTextFieldCaption"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 605, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jCheckBoxTableHeader)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 240, Short.MAX_VALUE)
                        .addComponent(jButtonCancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonApply))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldCaption, javax.swing.GroupLayout.DEFAULT_SIZE, 461, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextFieldCaption, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxTableHeader)
                    .addComponent(jButtonApply)
                    .addComponent(jButtonCancel))
                .addGap(3, 3, 3))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonApply;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JCheckBox jCheckBoxTableHeader;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextFieldCaption;
    // End of variables declaration//GEN-END:variables

}
