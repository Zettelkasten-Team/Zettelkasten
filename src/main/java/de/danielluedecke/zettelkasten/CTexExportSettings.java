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
import de.danielluedecke.zettelkasten.util.Constants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.logging.Level;

import javax.swing.JComboBox;
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    // Generated using JFormDesigner Evaluation license - Ralf Barkow
    private void initComponents() {
        ResourceBundle bundle = ResourceBundle.getBundle("de.danielluedecke.zettelkasten.resources.CTexExportSettings");
        jPanel1 = new JPanel();
        jCheckBoxUseFootnote = new JCheckBox();
        jCheckBoxCreateFormTags = new JCheckBox();
        jCheckBoxCenterForm = new JCheckBox();
        jButtonCancel = new JButton();
        jButtonApply = new JButton();
        jCheckBoxConvertQuotes = new JCheckBox();
        jCheckBoxRemoveTag = new JCheckBox();
        jCheckBoxStatisticsTablesStyle = new JCheckBox();
        jCheckBoxPreamble = new JCheckBox();
        jCheckBoxNoUmlautConvert = new JCheckBox();
        jPanel2 = new JPanel();
        jLabel2 = new JLabel();
        jTextFieldAuthor = new JTextField();
        jComboBoxDocClass = new JComboBox();
        jTextFieldMail = new JTextField();
        jLabel1 = new JLabel();
        jCheckBoxShowMail = new JCheckBox();
        jComboBoxCiteStyle = new JComboBox();
        jCheckBoxShowAuthor = new JCheckBox();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(bundle.getString("FormLatexExportSettings.title"));
        setModal(true);
        setName("FormLatexExportSettings");
        setResizable(false);
        Container contentPane = getContentPane();

        //======== jPanel1 ========
        {
            jPanel1.setBorder(new TitledBorder("LaTex-Exportoptionen"));
            jPanel1.setName("jPanel1");
            jPanel1.setBorder (new javax. swing. border. CompoundBorder( new javax .swing .border .TitledBorder (new javax. swing.
            border. EmptyBorder( 0, 0, 0, 0) , "JFor\u006dDesi\u0067ner \u0045valu\u0061tion", javax. swing. border. TitledBorder. CENTER
            , javax. swing. border. TitledBorder. BOTTOM, new java .awt .Font ("Dia\u006cog" ,java .awt .Font
            .BOLD ,12 ), java. awt. Color. red) ,jPanel1. getBorder( )) ); jPanel1. addPropertyChangeListener (
            new java. beans. PropertyChangeListener( ){ @Override public void propertyChange (java .beans .PropertyChangeEvent e) {if ("bord\u0065r"
            .equals (e .getPropertyName () )) throw new RuntimeException( ); }} );

            //---- jCheckBoxUseFootnote ----
            jCheckBoxUseFootnote.setText(bundle.getString("jCheckBoxUseFootnote.text"));
            jCheckBoxUseFootnote.setName("jCheckBoxUseFootnote");

            //---- jCheckBoxCreateFormTags ----
            jCheckBoxCreateFormTags.setText(bundle.getString("jCheckBoxCreateFormTags.text"));
            jCheckBoxCreateFormTags.setName("jCheckBoxCreateFormTags");

            //---- jCheckBoxCenterForm ----
            jCheckBoxCenterForm.setText(bundle.getString("jCheckBoxCenterForm.text"));
            jCheckBoxCenterForm.setName("jCheckBoxCenterForm");

            //---- jButtonCancel ----
            jButtonCancel.setName("jButtonCancel");

            //---- jButtonApply ----
            jButtonApply.setName("jButtonApply");

            //---- jCheckBoxConvertQuotes ----
            jCheckBoxConvertQuotes.setText(bundle.getString("jCheckBoxConvertQuotes.text"));
            jCheckBoxConvertQuotes.setToolTipText(bundle.getString("jCheckBoxConvertQuotes.toolTipText"));
            jCheckBoxConvertQuotes.setName("jCheckBoxConvertQuotes");

            //---- jCheckBoxRemoveTag ----
            jCheckBoxRemoveTag.setText(bundle.getString("jCheckBoxRemoveTag.text"));
            jCheckBoxRemoveTag.setName("jCheckBoxRemoveTag");

            //---- jCheckBoxStatisticsTablesStyle ----
            jCheckBoxStatisticsTablesStyle.setText(bundle.getString("jCheckBoxStatisticsTablesStyle.text"));
            jCheckBoxStatisticsTablesStyle.setName("jCheckBoxStatisticsTablesStyle");

            //---- jCheckBoxPreamble ----
            jCheckBoxPreamble.setText(bundle.getString("jCheckBoxPreamble.text"));
            jCheckBoxPreamble.setName("jCheckBoxPreamble");

            //---- jCheckBoxNoUmlautConvert ----
            jCheckBoxNoUmlautConvert.setText(bundle.getString("jCheckBoxNoUmlautConvert.text"));
            jCheckBoxNoUmlautConvert.setName("jCheckBoxNoUmlautConvert");

            //======== jPanel2 ========
            {
                jPanel2.setBorder(new TitledBorder("Pr\u00e4ambel / Meta-Informationen"));
                jPanel2.setName("jPanel2");

                //---- jLabel2 ----
                jLabel2.setText(bundle.getString("jLabel2.text"));
                jLabel2.setName("jLabel2");

                //---- jTextFieldAuthor ----
                jTextFieldAuthor.setName("jTextFieldAuthor");

                //---- jComboBoxDocClass ----
                jComboBoxDocClass.setName("jComboBoxDocClass");

                //---- jTextFieldMail ----
                jTextFieldMail.setName("jTextFieldMail");

                //---- jLabel1 ----
                jLabel1.setText(bundle.getString("jLabel1.text"));
                jLabel1.setName("jLabel1");

                //---- jCheckBoxShowMail ----
                jCheckBoxShowMail.setText(bundle.getString("jCheckBoxShowMail.text"));
                jCheckBoxShowMail.setName("jCheckBoxShowMail");

                //---- jComboBoxCiteStyle ----
                jComboBoxCiteStyle.setName("jComboBoxCiteStyle");

                //---- jCheckBoxShowAuthor ----
                jCheckBoxShowAuthor.setText(bundle.getString("jCheckBoxShowAuthor.text"));
                jCheckBoxShowAuthor.setName("jCheckBoxShowAuthor");

                GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
                jPanel2.setLayout(jPanel2Layout);
                jPanel2Layout.setHorizontalGroup(
                    jPanel2Layout.createParallelGroup()
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(jPanel2Layout.createParallelGroup()
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addComponent(jCheckBoxShowMail)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jTextFieldMail))
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addComponent(jCheckBoxShowAuthor)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jTextFieldAuthor))
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addComponent(jLabel2)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jComboBoxDocClass, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addComponent(jLabel1)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jComboBoxCiteStyle, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                            .addContainerGap())
                );
                jPanel2Layout.setVerticalGroup(
                    jPanel2Layout.createParallelGroup()
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel2)
                                .addComponent(jComboBoxDocClass, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jCheckBoxShowAuthor)
                                .addComponent(jTextFieldAuthor, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jCheckBoxShowMail)
                                .addComponent(jTextFieldMail, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel1)
                                .addComponent(jComboBoxCiteStyle, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addContainerGap())
                );
            }

            GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
            jPanel1.setLayout(jPanel1Layout);
            jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup()
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup()
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup()
                                    .addComponent(jCheckBoxNoUmlautConvert)
                                    .addComponent(jCheckBoxPreamble)
                                    .addGroup(jPanel1Layout.createParallelGroup()
                                        .addComponent(jCheckBoxUseFootnote)
                                        .addComponent(jCheckBoxRemoveTag)
                                        .addGroup(GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                            .addComponent(jButtonCancel)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(jButtonApply))
                                        .addComponent(jCheckBoxCreateFormTags)
                                        .addComponent(jCheckBoxConvertQuotes)
                                        .addComponent(jCheckBoxCenterForm)
                                        .addComponent(jCheckBoxStatisticsTablesStyle)))
                                .addGap(0, 0, 0))
                            .addComponent(jPanel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())
            );
            jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup()
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxPreamble)
                        .addGap(18, 18, 18)
                        .addComponent(jCheckBoxUseFootnote)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxCreateFormTags)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxCenterForm)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxStatisticsTablesStyle)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxConvertQuotes)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxNoUmlautConvert)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxRemoveTag)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(jButtonCancel)
                            .addComponent(jButtonApply))
                        .addContainerGap())
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
                .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap())
        );
        pack();
        setLocationRelativeTo(getOwner());
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Ralf Barkow
    private JPanel jPanel1;
    private JCheckBox jCheckBoxUseFootnote;
    private JCheckBox jCheckBoxCreateFormTags;
    private JCheckBox jCheckBoxCenterForm;
    private JButton jButtonCancel;
    private JButton jButtonApply;
    private JCheckBox jCheckBoxConvertQuotes;
    private JCheckBox jCheckBoxRemoveTag;
    private JCheckBox jCheckBoxStatisticsTablesStyle;
    private JCheckBox jCheckBoxPreamble;
    private JCheckBox jCheckBoxNoUmlautConvert;
    private JPanel jPanel2;
    private JLabel jLabel2;
    private JTextField jTextFieldAuthor;
    private JComboBox jComboBoxDocClass;
    private JTextField jTextFieldMail;
    private JLabel jLabel1;
    private JCheckBox jCheckBoxShowMail;
    private JComboBox jComboBoxCiteStyle;
    private JCheckBox jCheckBoxShowAuthor;
    // End of variables declaration//GEN-END:variables
}
