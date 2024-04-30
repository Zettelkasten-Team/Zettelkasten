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

package de.danielluedecke.zettelkasten.tasks;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.GroupLayout;
import javax.swing.LayoutStyle;
import com.explodingpixels.macwidgets.MacWidgetFactory;
import com.explodingpixels.widgets.TableUtils;
import de.danielluedecke.zettelkasten.ZettelkastenView;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.util.classes.TasksStatusBar;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;
import org.jdesktop.application.TaskService;

/**
 *
 * @author danielludecke
 */
public class FindDoubleEntriesTask extends javax.swing.JDialog {
    /**
     *
     */
    private FindDoubleEntryTask doubleEntryTask = null;
    /**
     * Reference to the main frame.
     */
    private ZettelkastenView mainframe;
    /**
     * 
     */
    private final Daten dataObj;
    private final Settings settingsObj;
    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap =
        org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
        getContext().getResourceMap(FindDoubleEntriesTask.class);

    /**
     * 
     * @param parent
     * @param zkn
     * @param data
     * @param set 
     */
    public FindDoubleEntriesTask(java.awt.Frame parent, ZettelkastenView zkn, Daten data, Settings set) {
        super(parent);
        dataObj = data;
        mainframe = zkn;
        settingsObj = set;
        initComponents();
        // set application icon
        setIconImage(Constants.zknicon.getImage());
        new TasksStatusBar(statusAnimationLabel, progressBar, null);
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
        
        // Set font.
        Font settingsTableFont = settingsObj.getTableFont();
        jTable1.setFont(settingsTableFont);
        
        // create auto-sorter for tabel
        jTable1.setAutoCreateRowSorter(false);
        jTable1.setGridColor(settingsObj.getTableGridColor());
        // make extra table-sorter for itunes-tables
        if (settingsObj.isMacStyle()) {
            TableUtils.SortDelegate sortDelegate = new TableUtils.SortDelegate() {
                @Override
                public void sort(int columnModelIndex, TableUtils.SortDirection sortDirection) {
                }
            };
            TableUtils.makeSortable(jTable1, sortDelegate);
        }
        
        // disable table while thread is running
        jTable1.setEnabled(false);
        // add mouse-listener, so a doubleclick displays the entry
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (2==evt.getClickCount()) {
                    Object val = jTable1.getValueAt(jTable1.getSelectedRow(), jTable1.getSelectedColumn());
                    if (val!=null) {
                        mainframe.setNewActivatedEntryAndUpdateDisplay(Integer.parseInt(val.toString()));
                    }
                }
            }
        });
        // create action for delete-key. by doing this instead of using a key-event, hitting
        // the enter-key does not select a new cell.
        AbstractAction a_enter = new AbstractAction(){
            @Override public void actionPerformed(ActionEvent e) {
                Object val = jTable1.getValueAt(jTable1.getSelectedRow(), jTable1.getSelectedColumn());
                    if (val!=null) {
                        mainframe.setNewActivatedEntryAndUpdateDisplay(Integer.parseInt(val.toString()));
                    }
            }
        };
        jTable1.getActionMap().put("EnterKeyPressed",a_enter);
        // associate enter-keystroke with that action
        KeyStroke ks = KeyStroke.getKeyStroke("ENTER");
        jTable1.getInputMap().put(ks, "EnterKeyPressed");
        // create action which should be executed when the user presses
        // the delete/backspace-key
        AbstractAction a_delete = new AbstractAction(){
            @Override public void actionPerformed(ActionEvent e) {
                // retrieve selected row and column
                int row = jTable1.getSelectedRow();
                int col = jTable1.getSelectedColumn();
                // check for valid values
                if (row!=-1 && col!=-1) {
                    // retrieve selected value
                    Object val = jTable1.getValueAt(row, col);
                    // check for valid content
                    if (val!=null) {
                        try {
                            // convert string to integer value
                            int selectedEntry = Integer.parseInt(val.toString());
                            // show entry that should be deleted, so the user knows which entry
                            // is to be deleted
                            mainframe.setNewActivatedEntryAndUpdateDisplay(selectedEntry);
                            // and delete entry
                            mainframe.deleteEntries(new int[] {selectedEntry});
                        }
                        catch (NumberFormatException ex) {

                        }
                    }
                }
            }
        };
        jTable1.getActionMap().put("DeleteKeyPressed",a_delete);
        // check for os, and use appropriate controlKey
        ks = KeyStroke.getKeyStroke((System.getProperty("os.name").toLowerCase().startsWith("mac os"))?"BACK_SPACE":"DELETE");
        jTable1.getInputMap().put(ks, "DeleteKeyPressed");
    }


    public void startTask() {
        // start the background task manually
        Task<?, ?> fdeT = findDoubleEntries();
        // get the application's context...
        ApplicationContext appC = Application.getInstance().getContext();
        // ...to get the TaskMonitor and TaskService
        TaskMonitor tM = appC.getTaskMonitor();
        TaskService tS = appC.getTaskService();
        // with these we can execute the task and bring it to the foreground
        // i.e. making the animated progressbar and busy icon visible
        tS.execute(fdeT);
        tM.setForegroundTask(fdeT);
    }


    @Action
    public Task<?, ?> findDoubleEntries() {
        // initiate the "statusbar" (the loading splash screen), giving visiual
        // feedback during open and save operations
        return new FindDoubleEntryTask(org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class));
    }

    private class FindDoubleEntryTask extends org.jdesktop.application.Task<Object, Void> {
        // create list that will contain all double entries
        private final ArrayList<Integer[]> doubleentries = new ArrayList<>();
        
        FindDoubleEntryTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to ImportFileTask fields, here.
            super(app);
            doubleEntryTask = this;
        }
        @Override
        protected Object doInBackground() {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.

            // create array with all available entries.
            int[] entries = new int[dataObj.getCount(Daten.ZKNCOUNT)];
            // copy all entry-numbers to that array. empty entries will have the index "-1"
            for (int cnt=1; cnt<=dataObj.getCount(Daten.ZKNCOUNT); cnt++) {
                // if we have an entry, add that entry-numbers to the array. else, in
                // case the entry was deleted, add -1 as number.
                entries[cnt-1] = (dataObj.isEmpty(cnt))?-1:cnt;
            }
            // create array list that will contain possible multiple occurences of
            // the entry with the index-number "entrynr"
            ArrayList<Integer> founddoubles = new ArrayList<>();
            // go through all entries. We have the entry-numbers saves in an array. each
            // previously found double entry's index-number will be replaced with a "-1",
            // so already found double entries will not appear multiple times in the final list
            for (int cnt=0; cnt<entries.length; cnt++) {
                // get current entry to check
                int entrynr = entries[cnt];
                // check whether entry-index-number is a valid entry-number
                if (entrynr!=-1) {
                    // clear list
                    founddoubles.clear();
                    // retrieve content of the entry "entrynr" so we can compare that content
                    // will all other entries
                    String zettelContent = dataObj.getZettelContent(entrynr);
                    String cleanZettelContent = dataObj.getCleanZettelContent(entrynr);
                    // go through all entries, starting with the next entry after "entrynr". all
                    // previous entries have already been check for multiple occurences in a former
                    // iteration
                    for (int counter=entrynr+1;counter<=dataObj.getCount(Daten.ZKNCOUNT);counter++) {
                        // compare zettelcontent with the following entries.
                        if (zettelContent.equalsIgnoreCase(dataObj.getZettelContent(counter)) ||
                            cleanZettelContent.equalsIgnoreCase(dataObj.getCleanZettelContent(counter))) {
                            // if we found a multiple occurence, set that entry-index-number to "-1", so
                            // this entry will not be checked again.
                            entries[counter-1] = -1;
                            // if "entrynr", which is a double entry, is not already in our list,
                            // add it now
                            if (!founddoubles.contains(entrynr)) {
                                    founddoubles.add(entrynr);
                                }
                            // add index-number of found double entries
                            founddoubles.add(counter);
                        }
                    }
                    // if we found any double entries, add those to our final list
                    if (!founddoubles.isEmpty()) {
                        doubleentries.add(founddoubles.toArray(new Integer[founddoubles.size()]));
                    }
                }
                // update progressbar
                setProgress(cnt,0,entries.length);
            }

            return null;  // return your result
        }
        @Override
        protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
        }
        @Override
        protected void finished()
        {
            super.finished();
            // when the task is finished, clear it
            doubleEntryTask = null;
            // hide task-progress bar
            jPanel1.setVisible(false);
            // create tablemodel for the table data, which is not editable
            DefaultTableModel tm = new DefaultTableModel(new Object[] {}, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
            // apply model to table
            jTable1.setModel(tm);
            // and delete all rows and columns
            tm.setRowCount(0);
            tm.setColumnCount(0);
            // now fill the table
            // check whether we have any multiple entries at all.
            if (!doubleentries.isEmpty()) {
                // create iterator for the list
                Iterator<Integer[]> it = doubleentries.iterator();
                // iterate all multiple entries
                while (it.hasNext()) {
                    // retrieve the row data
                    Integer[] rowdata = it.next();
                    // if the rowdate has more columns than the table model, adjust
                    // column count
                    while (tm.getColumnCount()<rowdata.length) {
                        tm.addColumn(String.valueOf(tm.getColumnCount()+1));
                    }
                    // add it to the table model
                    tm.addRow((Object[])rowdata);
                }
                // finally, enable table
                jTable1.setEnabled(true);
            }
            else {
                JOptionPane.showMessageDialog(null,resourceMap.getString("noMultipleEntriesFoundMsg"),resourceMap.getString("noMultipleEntriesFoundTitle"),JOptionPane.PLAIN_MESSAGE);
                setVisible(false);
                dispose();
            }
        }
    }


    @Action
    public void cancel() {
        if (doubleEntryTask!=null && !doubleEntryTask.isDone()) {
            // cancel task
            doubleEntryTask.cancel(true);
        }
        dispose();
        setVisible(false);
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    // Generated using JFormDesigner Evaluation license - Ralf Barkow
    private void initComponents() {
        ResourceBundle bundle = ResourceBundle.getBundle("de.danielluedecke.zettelkasten.tasks.resources.FindDoubleEntriesTask");
        jScrollPane1 = new JScrollPane();
        jTable1 = (settingsObj.isMacStyle()) ? MacWidgetFactory.createITunesTable(null) : new javax.swing.JTable();
        jPanel1 = new JPanel();
        progressBar = new JProgressBar();
        statusAnimationLabel = new JLabel();
        msgLabel = new JLabel();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(bundle.getString("FormFindDoubleEntries.title"));
        setName("FormFindDoubleEntries");
        Container contentPane = getContentPane();

        //======== jScrollPane1 ========
        {
            jScrollPane1.setBorder(null);
            jScrollPane1.setName("jScrollPane1");

            //---- jTable1 ----
            jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            jTable1.setCellSelectionEnabled(true);
            jTable1.setName("jTable1");
            jScrollPane1.setViewportView(jTable1);
        }

        //======== jPanel1 ========
        {
            jPanel1.setName("jPanel1");
            jPanel1.setBorder (new javax. swing. border. CompoundBorder( new javax .swing .border .TitledBorder (new
            javax. swing. border. EmptyBorder( 0, 0, 0, 0) , "JF\u006frmDesi\u0067ner Ev\u0061luatio\u006e", javax
            . swing. border. TitledBorder. CENTER, javax. swing. border. TitledBorder. BOTTOM, new java
            .awt .Font ("Dialo\u0067" ,java .awt .Font .BOLD ,12 ), java. awt
            . Color. red) ,jPanel1. getBorder( )) ); jPanel1. addPropertyChangeListener (new java. beans.
            PropertyChangeListener( ){ @Override public void propertyChange (java .beans .PropertyChangeEvent e) {if ("borde\u0072" .
            equals (e .getPropertyName () )) throw new RuntimeException( ); }} );

            //---- progressBar ----
            progressBar.setName("progressBar");

            //---- statusAnimationLabel ----
            statusAnimationLabel.setName("statusAnimationLabel");

            //---- msgLabel ----
            msgLabel.setText(bundle.getString("msgLabel.text"));
            msgLabel.setName("msgLabel");

            GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
            jPanel1.setLayout(jPanel1Layout);
            jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup()
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup()
                            .addComponent(msgLabel, GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(progressBar, GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(statusAnimationLabel)))
                        .addContainerGap())
            );
            jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup()
                    .addGroup(GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(msgLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                            .addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(statusAnimationLabel))
                        .addContainerGap())
            );
        }

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
            contentPaneLayout.createParallelGroup()
                .addComponent(jPanel1, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        contentPaneLayout.setVerticalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                    .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );
        pack();
        setLocationRelativeTo(getOwner());
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Ralf Barkow
    private JScrollPane jScrollPane1;
    private JTable jTable1;
    private JPanel jPanel1;
    private JProgressBar progressBar;
    private JLabel statusAnimationLabel;
    private JLabel msgLabel;
    // End of variables declaration//GEN-END:variables

}
