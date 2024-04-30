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
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.util.ColorUtil;
import de.danielluedecke.zettelkasten.util.Constants;
import java.awt.AWTKeyStroke;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.jdesktop.application.Action;

/**
 *
 * @author danielludecke
 */
public class CBiggerEditField extends javax.swing.JDialog {

    /**
     * The return value, i.e. the new author
     */
    private String newValue;
    /**
     * The return value of the bibkey textfield, i.e. the new bibkey
     */
    private String newBibKey;

    /**
     * Create a new bigger input dialog for editing new authors etc.
     *
     * @param parent the parent window
     * @param settings a reference to the settings class
     * @param title the dialog's title
     * @param val a default value which should be set to the textfield
     * @param textfieldval an optional value that should be set to the
     * bibkey-textfield. Usually only used when a new author is added / edited
     * and the bibkey is set or changed
     * @param edittype indicates which kind of data is being edited.
     */
    public CBiggerEditField(java.awt.Frame parent, Settings settings, String title, String val, String textfieldval, int edittype) {
        super(parent);
        initComponents();
        // set application icon
        setIconImage(Constants.zknicon.getImage());
        initBorders(settings);
        // usually, the tab key inserts a tab in the textarea-fields, while a ctrl+tab moves
        // the focus to the next component. here we override this setting, changing the tab-key
        // to change the focus.
        if (settings.isSeaGlass()) {
            jButton1.putClientProperty("JComponent.sizeVariant", "small");
            jButton2.putClientProperty("JComponent.sizeVariant", "small");
        }
        // bind our new forward focus traversal keys
        Set<AWTKeyStroke> newForwardKeys = new HashSet<>(1);
        newForwardKeys.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, 0));
        jTextAreaBigEdit.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.unmodifiableSet(newForwardKeys));
        // bind our new backward focus traversal keys
        Set<AWTKeyStroke> newBackwardKeys = new HashSet<>(1);
        newBackwardKeys.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_MASK + KeyEvent.SHIFT_DOWN_MASK));
        jTextAreaBigEdit.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.unmodifiableSet(newBackwardKeys));
        // these codelines add an escape-listener to the dialog. so, when the user
        // presses the escape-key, the same action is performed as if the user
        // presses the cancel button...
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        ActionListener cancelAction = new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                cancelButton();
            }
        };
        getRootPane().registerKeyboardAction(cancelAction, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        // reset return value
        newValue = null;
        newBibKey = null;
        // set dialog's title
        setTitle(title);
        // fill textarea with default value
        if (val != null) {
            jTextAreaBigEdit.setText(val);
        }
        switch (edittype) {
            case Constants.EDIT_AUTHOR:
                jPanel1.setVisible(true);
                if (textfieldval != null) {
                    jTextFieldBibKey.setText(textfieldval);
                } else {
                    jTextFieldBibKey.setText("");
                }
                break;
            default:
                jPanel1.setVisible(false);
                jTextFieldBibKey.setText("");
                break;
        }

    }

    private void initBorders(Settings settingsObj) {
        /*
         * Constructor for Matte Border
         * public MatteBorder(int top, int left, int bottom, int right, Color matteColor)
         */
        jScrollPane1.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ColorUtil.getBorderGray(settingsObj)));
    }

    @Action
    public void okButton() {
        // get the text and leave the dialog
        newValue = jTextAreaBigEdit.getText();
        newBibKey = jTextFieldBibKey.getText().trim();
        setVisible(false);
        dispose();
    }

    @Action
    public void cancelButton() {
        // reset the value and leave dialog
        newValue = null;
        newBibKey = null;
        setVisible(false);
        dispose();
    }

    /**
     *
     * @return
     */
    public String getNewValue() {
        return newValue;
    }

    public String getNewBibKey() {
        return newBibKey;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    // Generated using JFormDesigner Evaluation license - Ralf Barkow
    private void initComponents() {
        ResourceBundle bundle = ResourceBundle.getBundle("de.danielluedecke.zettelkasten.resources.CBiggerEditField");
        jScrollPane1 = new JScrollPane();
        jTextAreaBigEdit = new JTextArea();
        jButton2 = new JButton();
        jButton1 = new JButton();
        jPanel1 = new JPanel();
        jLabel1 = new JLabel();
        jTextFieldBibKey = new JTextField();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(200, 100));
        setModal(true);
        setName("FormBiggerEdit");
        Container contentPane = getContentPane();

        //======== jScrollPane1 ========
        {
            jScrollPane1.setBorder(null);
            jScrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            jScrollPane1.setMinimumSize(new Dimension(100, 50));
            jScrollPane1.setName("jScrollPane1");

            //---- jTextAreaBigEdit ----
            jTextAreaBigEdit.setColumns(20);
            jTextAreaBigEdit.setLineWrap(true);
            jTextAreaBigEdit.setRows(5);
            jTextAreaBigEdit.setWrapStyleWord(true);
            jTextAreaBigEdit.setName("jTextAreaBigEdit");
            jScrollPane1.setViewportView(jTextAreaBigEdit);
        }

        //---- jButton2 ----
        jButton2.setName("jButton2");

        //---- jButton1 ----
        jButton1.setName("jButton1");

        //======== jPanel1 ========
        {
            jPanel1.setName("jPanel1");
            jPanel1.setBorder(new javax.swing.border.CompoundBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EmptyBorder
            (0,0,0,0), "JF\u006frmD\u0065sig\u006eer \u0045val\u0075ati\u006fn",javax.swing.border.TitledBorder.CENTER,javax.swing.border
            .TitledBorder.BOTTOM,new java.awt.Font("Dia\u006cog",java.awt.Font.BOLD,12),java.awt
            .Color.red),jPanel1. getBorder()));jPanel1. addPropertyChangeListener(new java.beans.PropertyChangeListener(){@Override public void
            propertyChange(java.beans.PropertyChangeEvent e){if("\u0062ord\u0065r".equals(e.getPropertyName()))throw new RuntimeException()
            ;}});

            //---- jLabel1 ----
            jLabel1.setText(bundle.getString("jLabel1.text"));
            jLabel1.setName("jLabel1");

            //---- jTextFieldBibKey ----
            jTextFieldBibKey.setName("jTextFieldBibKey");

            GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
            jPanel1.setLayout(jPanel1Layout);
            jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup()
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldBibKey)
                        .addContainerGap())
            );
            jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup()
                    .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(jTextFieldBibKey, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            );
        }

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                    .addContainerGap(274, Short.MAX_VALUE)
                    .addComponent(jButton2)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton1)
                    .addContainerGap())
                .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        contentPaneLayout.setVerticalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                    .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton2)
                        .addComponent(jButton1))
                    .addGap(3, 3, 3))
        );
        pack();
        setLocationRelativeTo(getOwner());
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Ralf Barkow
    private JScrollPane jScrollPane1;
    private JTextArea jTextAreaBigEdit;
    private JButton jButton2;
    private JButton jButton1;
    private JPanel jPanel1;
    private JLabel jLabel1;
    private JTextField jTextFieldBibKey;
    // End of variables declaration//GEN-END:variables

}
