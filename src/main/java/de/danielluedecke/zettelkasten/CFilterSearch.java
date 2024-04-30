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
import javax.swing.border.*;
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.mac.MacSourceList;
import de.danielluedecke.zettelkasten.util.ColorUtil;
import de.danielluedecke.zettelkasten.util.Tools;
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.util.ListUtil;
import de.danielluedecke.zettelkasten.util.classes.Comparer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.jdesktop.application.Action;

/**
 *
 * @author  danielludecke
 */
public class CFilterSearch extends javax.swing.JDialog {

    /**
     * create a variable for a list model. this list model is used for
     * the JList-component which displays the search terms which should be filtered
     */
    private final DefaultListModel<String> filterListModel = new DefaultListModel<String>();
    /**
     * The list of available keywords or authors from the current search results that
     * should be filtered
     */
    private LinkedList<String> terms = new LinkedList<>();
    /**
     * This variable stores the data of the terms-list when this list is filtered.
     * All changes to a fitered table-list are also applied to this linked list. When
     * the list is being refreshed, we don't need to run the time-consuming task; instead
     * we simply iterate this list and set the values to the table
     */
    private LinkedList<String> linkedtermslist = null;
    /**
     * The selected entries which are used for filtering
     */
    private String [] filterterms = null;
    public String[] getFilterTerms() {
        return filterterms;
    }
    /**
     * The logical-combination of the filter-request
     */
    private int logical = -1;
    public int getLogical() {
        return logical;
    }

    /**
     *
     * @param parent the parent window
     * @param se
     * @param t a linked list containing the items that should be display in the jList
     * @param title the dialog's title. use {@code null} for standard-title
     * @param showoptions if true, the search-options for logical-filtering will be shown, if false, this
     * panel will be hidden.     */
    public CFilterSearch(java.awt.Frame parent, Settings se, LinkedList<String> t, String title, boolean showoptions) {
        super(parent);
        initComponents();
        // set application icon
        setIconImage(Constants.zknicon.getImage());
        // if we have a title, set it
        if (title!=null) setTitle(title);
        // show or hide options-panel
        jPanel6.setVisible(showoptions);
        // sort linked list
        terms = t;
        Collections.sort(terms, new Comparer());
        // when we have aqua-style, change scrollbars
        if (se.isMacStyle() || se.isSeaGlass()) {
            jTextFieldFilter.putClientProperty("JTextField.variant", "search");
            if (se.isSeaGlass()) {
                jButtonOk.putClientProperty("JComponent.sizeVariant", "small");
                jButtonCancel.putClientProperty("JComponent.sizeVariant", "small");
            }
        }
        initBorders(se);
        initList();
        initListeners();
    }

    
    private void initBorders(Settings settingsObj) {
        /*
         * Constructor for Matte Border
         * public MatteBorder(int top, int left, int bottom, int right, Color matteColor)
         */
        jScrollPane1.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ColorUtil.getBorderGray(settingsObj)));
    }
    
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
        jButtonOk.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyFilter();
            }
        });
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancel();
            }
        });
        jTextFieldFilter.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent evt) {
                if (Tools.isNavigationKey(evt.getKeyCode())) {
                    // if user pressed navigation key, select next table entry
                    ListUtil.navigateThroughList(jList1, evt.getKeyCode());
                }
                else {
                    // select table-entry live, while the user is typing...
                    ListUtil.selectByTyping(jList1,jTextFieldFilter);
                }
            }
        });
        // create action which should be executed when the user presses
        // the enter-key
        AbstractAction a_enter = new AbstractAction(){
            @Override public void actionPerformed(ActionEvent e) {
                filterList();
            }
        };
        jTextFieldFilter.getActionMap().put("EnterKeyPressed",a_enter);
        // associate enter-keystroke with that action
        KeyStroke ks = KeyStroke.getKeyStroke("ENTER");
        jTextFieldFilter.getInputMap().put(ks, "EnterKeyPressed");
    }
    
    
    /**
     * This method initializes the filter list. When a search request should be filtered,
     * all available items (keywords or authors) from the current search result are
     * passed to this dialog in the parameter {@link #terms terms}.<br><br>
     * This method now lists all these terms in a jList, so the user can select
     * terms for filtering his search.
     */
    private void initList() {
        // clear jList
        filterListModel.clear();
        // create iterator to iterate all terms of the current search
        Iterator<String> i = terms.iterator();
        // and add all terms to the jList
        while (i.hasNext()) filterListModel.addElement(i.next());
        jButtonRefresh.setEnabled(false);
        // enable textfield only if we have more than 1 element in the jtable
        jTextFieldFilter.setEnabled(filterListModel.size()>0);
    }
    
    private void cancel() {
        filterterms = null;
        dispose();
        setVisible(false);
    }
    
    
    /**
     * This method retrieves all selected values from the jList. The current
     * search results will now be filtered according to this selection, i.e. only
     * those search entries remain, that match this search/filter request.
     */
    private void applyFilter() {
        // check which radio button is selected, for the logical-combination of the filter-request
        if (jRadioButtonLogAnd.isSelected()) logical = Constants.LOG_AND;
        else if (jRadioButtonLogOr.isSelected()) logical = Constants.LOG_OR;
        if (jRadioButtonLogNot.isSelected()) logical = Constants.LOG_NOT;
        // retrieve selected values
        List<String> sel = jList1.getSelectedValuesList();
        // create return value
        filterterms = sel.toArray(new String[sel.size()]);
        // close window
        dispose();
        setVisible(false);
    }

    
    /**
     * This method filters the list of filter-itemts, according to the text-input
     * from the textfield. I.e. you can reduce larger filter-lists. In case text
     * is written into the textfield, only those items remain in the filterlist (jList)
     * that match the text in the textfield.
     */
    @Action
    public void filterList() {
        // when we filter the table and want to restore it, we don't need to run the
        // time-consuming task that creates the author-list and related author-frequencies.
        // instead, we simply copy the values from the linkedlist to the table-model, which is
        // much faster. but therefore we have to apply all changes to the filtered-table
        // (like adding/changing values in a filtered list) to the linked list as well.

        // get text from the textfield containing the filter string
        // convert to lowercase, we don't want case-sensitive search
        String text = jTextFieldFilter.getText().toLowerCase();
        // when we have no text, do nothing
        if (text.isEmpty()) return;

        // if we haven't already stored the current complete table data, do this now
        if (null==linkedtermslist) {
            // create new instance of list
            linkedtermslist = new LinkedList<>();
            // go through all table-data
            for (int cnt=0; cnt<filterListModel.getSize(); cnt++) linkedtermslist.add(filterListModel.get(cnt).toString());
        }

        // go through table and delete all rows that don't contain the filter text
        for (int cnt=(filterListModel.size()-1); cnt>=0; cnt--) {
            // get the string (author) value from the table
            // convert to lowercase, we don't want case-sensitive search
            String value = filterListModel.get(cnt).toString().toLowerCase();
            // if the text is *not* part of the author, delete that row
            if (!value.contains(text)) filterListModel.remove(cnt);
        }

        // reset textfield
        jTextFieldFilter.setText("");
        jTextFieldFilter.requestFocusInWindow();
        // enable textfield only if we have more than 1 element in the jtable
        jTextFieldFilter.setEnabled(filterListModel.size()>0);
        // enable refresh button
        jButtonRefresh.setEnabled(true);
    }


    @Action
    public void refreshList() {
        initList();
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    // Generated using JFormDesigner Evaluation license - Ralf Barkow
    private void initComponents() {
        ResourceBundle bundle = ResourceBundle.getBundle("de.danielluedecke.zettelkasten.resources.CFilterSearch");
        jScrollPane1 = new JScrollPane();
        jList1 = MacSourceList.createMacSourceList();
        jButtonOk = new JButton();
        jButtonCancel = new JButton();
        jPanel6 = new JPanel();
        jRadioButtonLogAnd = new JRadioButton();
        jRadioButtonLogOr = new JRadioButton();
        jRadioButtonLogNot = new JRadioButton();
        jTextFieldFilter = new JTextField();
        jButtonRefresh = new JButton();
        buttonGroup1 = new ButtonGroup();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(bundle.getString("FormFilterSearch.title"));
        setModal(true);
        setName("FormFilterSearch");
        Container contentPane = getContentPane();

        //======== jScrollPane1 ========
        {
            jScrollPane1.setBorder(null);
            jScrollPane1.setName("jScrollPane1");

            //---- jList1 ----
            jList1.setModel(filterListModel);
            jList1.setName("jList1");
            jScrollPane1.setViewportView(jList1);
        }

        //---- jButtonOk ----
        jButtonOk.setText(bundle.getString("jButtonOk.text"));
        jButtonOk.setName("jButtonOk");

        //---- jButtonCancel ----
        jButtonCancel.setText(bundle.getString("jButtonCancel.text"));
        jButtonCancel.setName("jButtonCancel");

        //======== jPanel6 ========
        {
            jPanel6.setBorder(new TitledBorder("Suchoptionen"));
            jPanel6.setName("jPanel6");
            jPanel6.setBorder (new javax. swing. border. CompoundBorder( new javax .swing .border .TitledBorder (new javax. swing
            . border. EmptyBorder( 0, 0, 0, 0) , "JFor\u006dDesi\u0067ner \u0045valu\u0061tion", javax. swing. border. TitledBorder
            . CENTER, javax. swing. border. TitledBorder. BOTTOM, new java .awt .Font ("Dia\u006cog" ,java .
            awt .Font .BOLD ,12 ), java. awt. Color. red) ,jPanel6. getBorder( )) )
            ; jPanel6. addPropertyChangeListener (new java. beans. PropertyChangeListener( ){ @Override public void propertyChange (java .beans .PropertyChangeEvent e
            ) {if ("bord\u0065r" .equals (e .getPropertyName () )) throw new RuntimeException( ); }} )
            ;

            //---- jRadioButtonLogAnd ----
            jRadioButtonLogAnd.setSelected(true);
            jRadioButtonLogAnd.setText(bundle.getString("jRadioButtonLogAnd.text"));
            jRadioButtonLogAnd.setName("jRadioButtonLogAnd");

            //---- jRadioButtonLogOr ----
            jRadioButtonLogOr.setText(bundle.getString("jRadioButtonLogOr.text"));
            jRadioButtonLogOr.setName("jRadioButtonLogOr");

            //---- jRadioButtonLogNot ----
            jRadioButtonLogNot.setText(bundle.getString("jRadioButtonLogNot.text"));
            jRadioButtonLogNot.setName("jRadioButtonLogNot");

            GroupLayout jPanel6Layout = new GroupLayout(jPanel6);
            jPanel6.setLayout(jPanel6Layout);
            jPanel6Layout.setHorizontalGroup(
                jPanel6Layout.createParallelGroup()
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel6Layout.createParallelGroup()
                            .addComponent(jRadioButtonLogAnd)
                            .addComponent(jRadioButtonLogOr)
                            .addComponent(jRadioButtonLogNot))
                        .addContainerGap(169, Short.MAX_VALUE))
            );
            jPanel6Layout.setVerticalGroup(
                jPanel6Layout.createParallelGroup()
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jRadioButtonLogAnd)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButtonLogOr)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButtonLogNot)
                        .addContainerGap(10, Short.MAX_VALUE))
            );
        }

        //---- jTextFieldFilter ----
        jTextFieldFilter.setToolTipText(bundle.getString("jTextFieldFilter.toolTipText"));
        jTextFieldFilter.setName("jTextFieldFilter");

        //---- jButtonRefresh ----
        jButtonRefresh.setBorderPainted(false);
        jButtonRefresh.setContentAreaFilled(false);
        jButtonRefresh.setFocusPainted(false);
        jButtonRefresh.setName("jButtonRefresh");

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
            contentPaneLayout.createParallelGroup()
                .addComponent(jScrollPane1, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE)
                .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                    .addContainerGap(188, Short.MAX_VALUE)
                    .addComponent(jButtonCancel)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButtonOk)
                    .addContainerGap())
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addGap(6, 6, 6)
                    .addGroup(contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                            .addComponent(jPanel6, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addContainerGap())
                        .addGroup(contentPaneLayout.createSequentialGroup()
                            .addComponent(jTextFieldFilter, GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButtonRefresh, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
                            .addGap(6, 6, 6))))
        );
        contentPaneLayout.setVerticalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                    .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                        .addComponent(jTextFieldFilter)
                        .addComponent(jButtonRefresh, GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jPanel6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonCancel)
                        .addComponent(jButtonOk))
                    .addGap(3, 3, 3))
        );
        pack();
        setLocationRelativeTo(getOwner());

        //---- buttonGroup1 ----
        buttonGroup1.add(jRadioButtonLogAnd);
        buttonGroup1.add(jRadioButtonLogOr);
        buttonGroup1.add(jRadioButtonLogNot);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Ralf Barkow
    private JScrollPane jScrollPane1;
    private JList jList1;
    private JButton jButtonOk;
    private JButton jButtonCancel;
    private JPanel jPanel6;
    private JRadioButton jRadioButtonLogAnd;
    private JRadioButton jRadioButtonLogOr;
    private JRadioButton jRadioButtonLogNot;
    private JTextField jTextFieldFilter;
    private JButton jButtonRefresh;
    private ButtonGroup buttonGroup1;
    // End of variables declaration//GEN-END:variables

}
