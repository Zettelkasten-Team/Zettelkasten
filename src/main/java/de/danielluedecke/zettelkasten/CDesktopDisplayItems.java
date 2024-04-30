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
import de.danielluedecke.zettelkasten.util.Constants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.jdesktop.application.Action;

/**
 *
 * @author danielludecke
 */
public class CDesktopDisplayItems extends javax.swing.JDialog {

    /**
     * return value for the main window so we know whether we have to update the
     * display
     */
    private boolean needsupdate = false;

    /**
     *
     * @return
     */
    public boolean isNeedsUpdate() {
        return needsupdate;
    }
    /**
     *
     */
    private boolean savesettingok = true;

    /**
     *
     * @return
     */
    public boolean isSaveSettingsOk() {
        return savesettingok;
    }
    /**
     * Reference to the settings class
     */
    private final Settings settingsObj;

    /**
     * wm,
     *
     * @param parent
     * @param s
     */
    public CDesktopDisplayItems(java.awt.Frame parent, Settings s) {
        super(parent);
        initComponents();
        // set application icon
        setIconImage(Constants.zknicon.getImage());

        settingsObj = s;
        if (settingsObj.isSeaGlass()) {
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
                cancelWindow();
            }
        };
        getRootPane().registerKeyboardAction(cancelAction, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

        // init option-checkboxes
        int items = settingsObj.getDesktopDisplayItems();
        jCheckBoxRemarks.setSelected((items & Constants.DESKTOP_SHOW_REMARKS) != 0);
        jCheckBoxAuthors.setSelected((items & Constants.DESKTOP_SHOW_AUTHORS) != 0);
        jCheckBoxAttachments.setSelected((items & Constants.DESKTOP_SHOW_ATTACHMENTS) != 0);
        jCheckBoxKeywords.setSelected((items & Constants.DESKTOP_SHOW_KEYWORDS) != 0);
    }

    /**
     * Finally, when the user presses the apply-button, all settings are saved.
     * this is done in this method. when all changes have been saved, the window
     * will be closed and disposed.
     */
    @Action(enabledProperty = "modified")
    public void applyChanges() {
        // reset indicator
        int items = 0;
        // check which items should be displayed
        if (jCheckBoxRemarks.isSelected()) {
            items = items | Constants.DESKTOP_SHOW_REMARKS;
        }
        if (jCheckBoxAuthors.isSelected()) {
            items = items | Constants.DESKTOP_SHOW_AUTHORS;
        }
        if (jCheckBoxAttachments.isSelected()) {
            items = items | Constants.DESKTOP_SHOW_ATTACHMENTS;
        }
        if (jCheckBoxKeywords.isSelected()) {
            items = items | Constants.DESKTOP_SHOW_KEYWORDS;
        }
        // save user settings
        settingsObj.setDesktopDisplayItems(items);
        // save the changes to the settings-file
        savesettingok = settingsObj.saveSettingsToFiles();
        // tell programm that we need to update the display
        needsupdate = true;
        // close window
        closeWindow();
    }

    /**
     * When the user presses the cancel button, no update needed, close window
     */
    @Action
    public void cancelWindow() {
        needsupdate = false;
        closeWindow();
    }

    /**
     * Occurs when the user closes the window or presses the ok button. the
     * settings-file is then saved and the window disposed.
     */
    private void closeWindow() {
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
        ResourceBundle bundle = ResourceBundle.getBundle("de.danielluedecke.zettelkasten.resources.CDesktopDisplayItems");
        jPanel1 = new JPanel();
        jLabel1 = new JLabel();
        jCheckBoxRemarks = new JCheckBox();
        jCheckBoxAuthors = new JCheckBox();
        jCheckBoxAttachments = new JCheckBox();
        jCheckBoxKeywords = new JCheckBox();
        jButtonApply = new JButton();
        jButtonCancel = new JButton();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(bundle.getString("FormDesktopDisplayItems.title"));
        setModal(true);
        setName("FormDesktopDisplayItems");
        setResizable(false);
        Container contentPane = getContentPane();

        //======== jPanel1 ========
        {
            jPanel1.setName("jPanel1");
            jPanel1.setBorder (new javax. swing. border. CompoundBorder( new javax .swing .border .TitledBorder (new javax. swing
            . border. EmptyBorder( 0, 0, 0, 0) , "JF\u006frm\u0044es\u0069gn\u0065r \u0045va\u006cua\u0074io\u006e", javax. swing. border. TitledBorder
            . CENTER, javax. swing. border. TitledBorder. BOTTOM, new java .awt .Font ("D\u0069al\u006fg" ,java .
            awt .Font .BOLD ,12 ), java. awt. Color. red) ,jPanel1. getBorder( )) )
            ; jPanel1. addPropertyChangeListener (new java. beans. PropertyChangeListener( ){ @Override public void propertyChange (java .beans .PropertyChangeEvent e
            ) {if ("\u0062or\u0064er" .equals (e .getPropertyName () )) throw new RuntimeException( ); }} )
            ;

            //---- jLabel1 ----
            jLabel1.setText(bundle.getString("jLabel1.text"));
            jLabel1.setName("jLabel1");

            //---- jCheckBoxRemarks ----
            jCheckBoxRemarks.setText(bundle.getString("jCheckBoxRemarks.text"));
            jCheckBoxRemarks.setName("jCheckBoxRemarks");
            jCheckBoxRemarks.addActionListener(e -> checkBoxClicked(e));

            //---- jCheckBoxAuthors ----
            jCheckBoxAuthors.setText(bundle.getString("jCheckBoxAuthors.text"));
            jCheckBoxAuthors.setName("jCheckBoxAuthors");
            jCheckBoxAuthors.addActionListener(e -> checkBoxClicked(e));

            //---- jCheckBoxAttachments ----
            jCheckBoxAttachments.setText(bundle.getString("jCheckBoxAttachments.text"));
            jCheckBoxAttachments.setName("jCheckBoxAttachments");
            jCheckBoxAttachments.addActionListener(e -> checkBoxClicked(e));

            //---- jCheckBoxKeywords ----
            jCheckBoxKeywords.setText(bundle.getString("jCheckBoxKeywords.text"));
            jCheckBoxKeywords.setName("jCheckBoxKeywords");
            jCheckBoxKeywords.addActionListener(e -> checkBoxClicked(e));

            //---- jButtonApply ----
            jButtonApply.setText(bundle.getString("jButtonApply.text"));
            jButtonApply.setName("jButtonApply");

            //---- jButtonCancel ----
            jButtonCancel.setName("jButtonCancel");

            GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
            jPanel1.setLayout(jPanel1Layout);
            jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup()
                    .addGroup(GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonCancel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonApply))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup()
                            .addComponent(jCheckBoxRemarks)
                            .addComponent(jCheckBoxAuthors)
                            .addComponent(jCheckBoxAttachments)
                            .addComponent(jCheckBoxKeywords)
                            .addComponent(jLabel1))
                        .addContainerGap())
            );
            jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup()
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxRemarks)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxAuthors)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxAttachments)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxKeywords)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(jButtonApply)
                            .addComponent(jButtonCancel)))
            );
        }

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap())
        );
        contentPaneLayout.setVerticalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pack();
        setLocationRelativeTo(getOwner());
    }// </editor-fold>//GEN-END:initComponents

private void checkBoxClicked(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxClicked
    setModified(true);
}//GEN-LAST:event_checkBoxClicked

    private boolean modified = false;
    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean b) {
        boolean old = isModified();
        this.modified = b;
        firePropertyChange("modified", old, isModified());
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Ralf Barkow
    private JPanel jPanel1;
    private JLabel jLabel1;
    private JCheckBox jCheckBoxRemarks;
    private JCheckBox jCheckBoxAuthors;
    private JCheckBox jCheckBoxAttachments;
    private JCheckBox jCheckBoxKeywords;
    private JButton jButtonApply;
    private JButton jButtonCancel;
    // End of variables declaration//GEN-END:variables

}
