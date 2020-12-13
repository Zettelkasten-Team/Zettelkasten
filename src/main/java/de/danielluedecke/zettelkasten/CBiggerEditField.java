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
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextAreaBigEdit = new javax.swing.JTextArea();
        jButton2 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldBibKey = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(200, 100));
        setModal(true);
        setName("FormBiggerEdit"); // NOI18N

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setMinimumSize(new java.awt.Dimension(100, 50));
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTextAreaBigEdit.setColumns(20);
        jTextAreaBigEdit.setLineWrap(true);
        jTextAreaBigEdit.setRows(5);
        jTextAreaBigEdit.setWrapStyleWord(true);
        jTextAreaBigEdit.setName("jTextAreaBigEdit"); // NOI18N
        jScrollPane1.setViewportView(jTextAreaBigEdit);

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getActionMap(CBiggerEditField.class, this);
        jButton2.setAction(actionMap.get("cancelButton")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N

        jButton1.setAction(actionMap.get("okButton")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getResourceMap(CBiggerEditField.class);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jTextFieldBibKey.setName("jTextFieldBibKey"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldBibKey)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel1)
                .addComponent(jTextFieldBibKey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(274, Short.MAX_VALUE)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap())
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jButton1))
                .addGap(3, 3, 3))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextAreaBigEdit;
    private javax.swing.JTextField jTextFieldBibKey;
    // End of variables declaration//GEN-END:variables

}
