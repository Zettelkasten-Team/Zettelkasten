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
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.jdesktop.application.Action;

/**
 *
 * @author danielludecke
 */
public class CTexExportSettings extends javax.swing.JDialog {
    /**
     * 
     */
    private final Settings settingsObj;
    /**
     * 
     */
    private boolean cancelled = false;
    public boolean isCancelled() {
        return cancelled;
    }

    /** 
     * Creates new form CTexExportSettings
     * @param parent 
     * @param s 
     */
    public CTexExportSettings(java.awt.Frame parent, Settings s) {
        super(parent);
        settingsObj = s;
        
        initComponents();
        // set application icon
        setIconImage(Constants.zknicon.getImage());
        // init comboboxes with default values
        initComboBox();
        // init listeners
        initListeners();
        if (settingsObj.isSeaGlass()) {
            jButtonApply.putClientProperty("JComponent.sizeVariant", "small");
            jButtonCancel.putClientProperty("JComponent.sizeVariant", "small");
        }
        // set last user settings
        jCheckBoxCreateFormTags.setSelected(settingsObj.getLatexExportCreateFormTags());
        jCheckBoxUseFootnote.setSelected(settingsObj.getLatexExportFootnoteRef());
        jCheckBoxShowAuthor.setSelected(settingsObj.getLatexExportShowAuthor());
        jCheckBoxShowMail.setSelected(settingsObj.getLatexExportShowMail());
        jCheckBoxConvertQuotes.setSelected(settingsObj.getLatexExportConvertQuotes());
        jCheckBoxCenterForm.setSelected(settingsObj.getLatexExportCenterForm());
        jCheckBoxStatisticsTablesStyle.setSelected(settingsObj.getLatexExportStatisticTableStyle());
        jCheckBoxRemoveTag.setSelected(settingsObj.getLatexExportRemoveNonStandardTags());
        jTextFieldAuthor.setText(settingsObj.getLatexExportAuthorValue());
        jTextFieldMail.setText(settingsObj.getLatexExportMailValue());
        jCheckBoxNoUmlautConvert.setSelected(!settingsObj.getLatexExportConvertUmlaut());
        jCheckBoxPreamble.setSelected(settingsObj.getLatexExportNoPreamble());
        // show preamble?
        enablePreamble();
    }
    private void initListeners() {
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
        jCheckBoxShowAuthor.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldAuthor.setEnabled(jCheckBoxShowAuthor.isSelected());
                if (jCheckBoxShowAuthor.isSelected()) jTextFieldAuthor.requestFocusInWindow();
            }
        });
        jCheckBoxShowMail.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldMail.setEnabled(jCheckBoxShowMail.isSelected());
                if (jCheckBoxShowMail.isSelected()) jTextFieldMail.requestFocusInWindow();
            }
        });
        jCheckBoxPreamble.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsObj.setLatexExportNoPreamble(jCheckBoxPreamble.isSelected());
                enablePreamble();
            }
        });
    }
    
    private void enablePreamble() {
        boolean isPreambleVisible = !settingsObj.getLatexExportNoPreamble();
        jTextFieldAuthor.setEnabled(isPreambleVisible);
        jComboBoxDocClass.setEnabled(isPreambleVisible);
        jTextFieldMail.setEnabled(isPreambleVisible);
        jCheckBoxShowMail.setEnabled(isPreambleVisible);
        jComboBoxCiteStyle.setEnabled(isPreambleVisible);
        jCheckBoxShowAuthor.setEnabled(isPreambleVisible);
    }
    
    /**
     * Here we set all available character-encodings for the bibtex-file. each reference-manager
     * (jabref, refworks, citavi) has its own character-encoding, so we have to take this
     * into account when importing bibtex-files.
     */
    private void initComboBox() {
        // reset combobox
        jComboBoxCiteStyle.removeAllItems();
        // add items that show the bibtex-encodings
        for (String s : Constants.LATEX_BIB_STYLES) {
            jComboBoxCiteStyle.addItem(s);
        }
        try {
            // auto-select last used format, when we auto-load the last used bibtex-file
            jComboBoxCiteStyle.setSelectedIndex(settingsObj.getLastUsedLatexBibStyle());
        }
        catch (IllegalArgumentException e) {
            Constants.zknlogger.log(Level.WARNING,e.getLocalizedMessage());
        }
        // reset combobox
        jComboBoxDocClass.removeAllItems();
        // add items that show the bibtex-encodings
        for (String s : Constants.LATEX_DOCUMENT_CLASS) {
            jComboBoxDocClass.addItem(s);
        }
        try {
            // auto-select last used format, when we auto-load the last used bibtex-file
            jComboBoxDocClass.setSelectedIndex(settingsObj.getLastUsedLatexDocClass());
        }
        catch (IllegalArgumentException e) {
            Constants.zknlogger.log(Level.WARNING,e.getLocalizedMessage());
        }
    }
    @Action
    public void cancel() {
        cancelled = true;
        setVisible(false);
        dispose();
    }
    @Action
    public void startExport() {
        // check for valid author value
        String dummy = jTextFieldAuthor.getText();
        if (null==dummy || dummy.isEmpty()) jCheckBoxShowAuthor.setSelected(false);
        // check for valid mail value
        dummy = jTextFieldMail.getText();
        if (null==dummy || dummy.isEmpty()) jCheckBoxShowMail.setSelected(false);
        // check whether user wants to use footnotes for references or not
        settingsObj.setLatexExportFootnoteRef(jCheckBoxUseFootnote.isSelected());
        // check whether user wants forms as images or tags
        settingsObj.setLatexExportCreateFormTags(jCheckBoxCreateFormTags.isSelected());
        // check whether user wants to remove nonstandard tags
        settingsObj.setLatexExportRemoveNonStandardTags(jCheckBoxRemoveTag.isSelected());
        // save settings for whether author should be shown or not
        settingsObj.setLatexExportShowAuthor(jCheckBoxShowAuthor.isSelected());
        // save settings for whether mail should be shown or not
        settingsObj.setLatexExportShowMail(jCheckBoxShowMail.isSelected());
        // save settings for whether french quotes should be converted or not
        settingsObj.setLatexExportConvertQuotes(jCheckBoxConvertQuotes.isSelected());
        // save settings for whether form tags should be centred or not
        settingsObj.setLatexExportCenterForm(jCheckBoxCenterForm.isSelected());
        // save settings whether table style should be like statistical results
        settingsObj.setLatexExportStatisticTableStyle(jCheckBoxStatisticsTablesStyle.isSelected());
        // save author-name
        settingsObj.setLatexExportAuthorValue(jTextFieldAuthor.getText());
        // save author email
        settingsObj.setLatexExportMailValue(jTextFieldMail.getText());
        // set cite-style
        settingsObj.setLastUsedLatexBibStyle(jComboBoxCiteStyle.getSelectedIndex());
        // set doc-class
        settingsObj.setLastUsedLatexDocClass(jComboBoxDocClass.getSelectedIndex());
        // set preamble
        settingsObj.setLatexExportNoPreamble(jCheckBoxPreamble.isSelected());
        // set umlaut convert
        settingsObj.setLatexExportConvertUmlaut(!jCheckBoxNoUmlautConvert.isSelected());
        cancelled = false;
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

        jPanel1 = new javax.swing.JPanel();
        jCheckBoxUseFootnote = new javax.swing.JCheckBox();
        jCheckBoxCreateFormTags = new javax.swing.JCheckBox();
        jCheckBoxCenterForm = new javax.swing.JCheckBox();
        jButtonCancel = new javax.swing.JButton();
        jButtonApply = new javax.swing.JButton();
        jCheckBoxConvertQuotes = new javax.swing.JCheckBox();
        jCheckBoxRemoveTag = new javax.swing.JCheckBox();
        jCheckBoxStatisticsTablesStyle = new javax.swing.JCheckBox();
        jCheckBoxPreamble = new javax.swing.JCheckBox();
        jCheckBoxNoUmlautConvert = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldAuthor = new javax.swing.JTextField();
        jComboBoxDocClass = new javax.swing.JComboBox();
        jTextFieldMail = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jCheckBoxShowMail = new javax.swing.JCheckBox();
        jComboBoxCiteStyle = new javax.swing.JComboBox();
        jCheckBoxShowAuthor = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getResourceMap(CTexExportSettings.class);
        setTitle(resourceMap.getString("FormLatexExportSettings.title")); // NOI18N
        setModal(true);
        setName("FormLatexExportSettings"); // NOI18N
        setResizable(false);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jCheckBoxUseFootnote.setText(resourceMap.getString("jCheckBoxUseFootnote.text")); // NOI18N
        jCheckBoxUseFootnote.setName("jCheckBoxUseFootnote"); // NOI18N

        jCheckBoxCreateFormTags.setText(resourceMap.getString("jCheckBoxCreateFormTags.text")); // NOI18N
        jCheckBoxCreateFormTags.setName("jCheckBoxCreateFormTags"); // NOI18N

        jCheckBoxCenterForm.setText(resourceMap.getString("jCheckBoxCenterForm.text")); // NOI18N
        jCheckBoxCenterForm.setName("jCheckBoxCenterForm"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getActionMap(CTexExportSettings.class, this);
        jButtonCancel.setAction(actionMap.get("cancel")); // NOI18N
        jButtonCancel.setName("jButtonCancel"); // NOI18N

        jButtonApply.setAction(actionMap.get("startExport")); // NOI18N
        jButtonApply.setName("jButtonApply"); // NOI18N

        jCheckBoxConvertQuotes.setText(resourceMap.getString("jCheckBoxConvertQuotes.text")); // NOI18N
        jCheckBoxConvertQuotes.setToolTipText(resourceMap.getString("jCheckBoxConvertQuotes.toolTipText")); // NOI18N
        jCheckBoxConvertQuotes.setName("jCheckBoxConvertQuotes"); // NOI18N

        jCheckBoxRemoveTag.setText(resourceMap.getString("jCheckBoxRemoveTag.text")); // NOI18N
        jCheckBoxRemoveTag.setName("jCheckBoxRemoveTag"); // NOI18N

        jCheckBoxStatisticsTablesStyle.setText(resourceMap.getString("jCheckBoxStatisticsTablesStyle.text")); // NOI18N
        jCheckBoxStatisticsTablesStyle.setName("jCheckBoxStatisticsTablesStyle"); // NOI18N

        jCheckBoxPreamble.setText(resourceMap.getString("jCheckBoxPreamble.text")); // NOI18N
        jCheckBoxPreamble.setName("jCheckBoxPreamble"); // NOI18N

        jCheckBoxNoUmlautConvert.setText(resourceMap.getString("jCheckBoxNoUmlautConvert.text")); // NOI18N
        jCheckBoxNoUmlautConvert.setName("jCheckBoxNoUmlautConvert"); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jTextFieldAuthor.setName("jTextFieldAuthor"); // NOI18N

        jComboBoxDocClass.setName("jComboBoxDocClass"); // NOI18N

        jTextFieldMail.setName("jTextFieldMail"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jCheckBoxShowMail.setText(resourceMap.getString("jCheckBoxShowMail.text")); // NOI18N
        jCheckBoxShowMail.setName("jCheckBoxShowMail"); // NOI18N

        jComboBoxCiteStyle.setName("jComboBoxCiteStyle"); // NOI18N

        jCheckBoxShowAuthor.setText(resourceMap.getString("jCheckBoxShowAuthor.text")); // NOI18N
        jCheckBoxShowAuthor.setName("jCheckBoxShowAuthor"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jCheckBoxShowMail)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTextFieldMail))
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jCheckBoxShowAuthor)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTextFieldAuthor))
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jComboBoxDocClass, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jComboBoxCiteStyle, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(jComboBoxDocClass, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jCheckBoxShowAuthor)
                    .add(jTextFieldAuthor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jCheckBoxShowMail)
                    .add(jTextFieldMail, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jComboBoxCiteStyle, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jCheckBoxNoUmlautConvert)
                            .add(jCheckBoxPreamble)
                            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(jCheckBoxUseFootnote)
                                .add(jCheckBoxRemoveTag)
                                .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                                    .add(jButtonCancel)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(jButtonApply))
                                .add(jCheckBoxCreateFormTags)
                                .add(jCheckBoxConvertQuotes)
                                .add(jCheckBoxCenterForm)
                                .add(jCheckBoxStatisticsTablesStyle)))
                        .add(0, 0, 0))
                    .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jCheckBoxPreamble)
                .add(18, 18, 18)
                .add(jCheckBoxUseFootnote)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jCheckBoxCreateFormTags)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jCheckBoxCenterForm)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jCheckBoxStatisticsTablesStyle)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jCheckBoxConvertQuotes)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jCheckBoxNoUmlautConvert)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jCheckBoxRemoveTag)
                .add(18, 18, 18)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButtonCancel)
                    .add(jButtonApply))
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonApply;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JCheckBox jCheckBoxCenterForm;
    private javax.swing.JCheckBox jCheckBoxConvertQuotes;
    private javax.swing.JCheckBox jCheckBoxCreateFormTags;
    private javax.swing.JCheckBox jCheckBoxNoUmlautConvert;
    private javax.swing.JCheckBox jCheckBoxPreamble;
    private javax.swing.JCheckBox jCheckBoxRemoveTag;
    private javax.swing.JCheckBox jCheckBoxShowAuthor;
    private javax.swing.JCheckBox jCheckBoxShowMail;
    private javax.swing.JCheckBox jCheckBoxStatisticsTablesStyle;
    private javax.swing.JCheckBox jCheckBoxUseFootnote;
    private javax.swing.JComboBox jComboBoxCiteStyle;
    private javax.swing.JComboBox jComboBoxDocClass;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField jTextFieldAuthor;
    private javax.swing.JTextField jTextFieldMail;
    // End of variables declaration//GEN-END:variables
}
