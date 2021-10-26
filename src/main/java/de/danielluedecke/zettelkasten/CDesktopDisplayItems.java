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
        savesettingok = settingsObj.saveSettings();
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
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jCheckBoxRemarks = new javax.swing.JCheckBox();
        jCheckBoxAuthors = new javax.swing.JCheckBox();
        jCheckBoxAttachments = new javax.swing.JCheckBox();
        jCheckBoxKeywords = new javax.swing.JCheckBox();
        jButtonApply = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getResourceMap(CDesktopDisplayItems.class);
        setTitle(resourceMap.getString("FormDesktopDisplayItems.title")); // NOI18N
        setModal(true);
        setName("FormDesktopDisplayItems"); // NOI18N
        setResizable(false);

        jPanel1.setName("jPanel1"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jCheckBoxRemarks.setText(resourceMap.getString("jCheckBoxRemarks.text")); // NOI18N
        jCheckBoxRemarks.setName("jCheckBoxRemarks"); // NOI18N
        jCheckBoxRemarks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxClicked(evt);
            }
        });

        jCheckBoxAuthors.setText(resourceMap.getString("jCheckBoxAuthors.text")); // NOI18N
        jCheckBoxAuthors.setName("jCheckBoxAuthors"); // NOI18N
        jCheckBoxAuthors.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxClicked(evt);
            }
        });

        jCheckBoxAttachments.setText(resourceMap.getString("jCheckBoxAttachments.text")); // NOI18N
        jCheckBoxAttachments.setName("jCheckBoxAttachments"); // NOI18N
        jCheckBoxAttachments.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxClicked(evt);
            }
        });

        jCheckBoxKeywords.setText(resourceMap.getString("jCheckBoxKeywords.text")); // NOI18N
        jCheckBoxKeywords.setName("jCheckBoxKeywords"); // NOI18N
        jCheckBoxKeywords.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxClicked(evt);
            }
        });

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getActionMap(CDesktopDisplayItems.class, this);
        jButtonApply.setAction(actionMap.get("applyChanges")); // NOI18N
        jButtonApply.setText(resourceMap.getString("jButtonApply.text")); // NOI18N
        jButtonApply.setName("jButtonApply"); // NOI18N

        jButtonCancel.setAction(actionMap.get("cancelWindow")); // NOI18N
        jButtonCancel.setName("jButtonCancel"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jCheckBoxRemarks)
            .addComponent(jCheckBoxAuthors)
            .addComponent(jCheckBoxAttachments)
            .addComponent(jCheckBoxKeywords)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(146, Short.MAX_VALUE)
                .addComponent(jButtonCancel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonApply))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxRemarks)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxAuthors)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxAttachments)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxKeywords)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonApply)
                    .addComponent(jButtonCancel)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
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
    private javax.swing.JButton jButtonApply;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JCheckBox jCheckBoxAttachments;
    private javax.swing.JCheckBox jCheckBoxAuthors;
    private javax.swing.JCheckBox jCheckBoxKeywords;
    private javax.swing.JCheckBox jCheckBoxRemarks;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

}
