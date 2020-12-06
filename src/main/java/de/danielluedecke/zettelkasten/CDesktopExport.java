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

import de.danielluedecke.zettelkasten.database.BibTeX;
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.util.Tools;
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.util.FileOperationsUtil;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import org.jdesktop.application.Action;

/**
 *
 * @author danielludecke
 */
public class CDesktopExport extends javax.swing.JDialog {
    /**
     * get the strings for file descriptions from the resource map
     */
    private org.jdesktop.application.ResourceMap resourceMap =
        org.jdesktop.application.Application.getInstance(ZettelkastenApp.class).
        getContext().getResourceMap(CDesktopExport.class);
    /**
     *
     */
    private Settings settingsObj;
    /**
     *
     */
    private BibTeX bibtexObj;
    /**
     * file path to export file
     */
    private File filepath;
    /**
     * Here we set up the available amount of different export-formats. currently, the
     * programme supports 8 different export-formats.
     */
    private static final int EXPORTTYPECOUNT = 8;
    /**
     * indicates which type of data format should be exported to.
     * Use following constants:<br>
     * - CConstants.EXP_TYPE_DESKTOP_DOCX<br>
     * - CConstants.EXP_TYPE_DESKTOP_ODT<br>
     * - CConstants.EXP_TYPE_DESKTOP_RTF<br>
     * - CConstants.EXP_TYPE_DESKTOP_HTML<br>
     * - CConstants.EXP_TYPE_DESKTOP_MD<br>
     * - CConstants.EXP_TYPE_DESKTOP_TXT<br>
     * - CConstants.EXP_TYPE_DESKTOP_TEX<br>
     * - CConstants.EXP_TYPE_DESKTOP_EPUB<br>
     */
    private int exportType;
    /**
     *
     */
    private boolean exportbibtex;
    /**
     * Indicates, whether a table of contents should be created or not. applies only to
     * the HTML-export-format
     */
    private boolean exportTOC;
    /**
     * return value which indicates whether the dialog was closed correcty or
     * if a the action was cancelled
     * <br>-1 = cancel action
     * <br>0 = close action
     * <br>1 = valid start of import including correct file path
     */
    private int retval;
    
    private static final int TYPE_DOCX = 1;
    private static final int TYPE_ODT = 2;
    private static final int TYPE_RTF = 3;
    private static final int TYPE_HTML = 4;
    private static final int TYPE_MD = 5;
    private static final int TYPE_TXT = 6;
    private static final int TYPE_TEX = 7;
    private static final int TYPE_EPUB = 8;

    
    /**
     * 
     * @param parent
     * @param s
     * @param b 
     */
    public CDesktopExport(java.awt.Frame parent, Settings s, BibTeX b) {
        super(parent);
        settingsObj = s;
        bibtexObj = b;
        initComponents();
        // set application icon
        setIconImage(Constants.zknicon.getImage());
        initComboBox();
        if (settingsObj.isSeaGlass()) {
            jButtonApply.putClientProperty("JComponent.sizeVariant", "small");
            jButtonCancel.putClientProperty("JComponent.sizeVariant", "small");
            jButtonBrowse.putClientProperty("JComponent.sizeVariant", "small");
        }
        // first, disable all components, so the user
        // goes through this dialog step by step
        jCheckBoxExportBibTex.setEnabled(false);
        jCheckBoxContent.setEnabled(false);
        jComboBoxComments.setEnabled(false);
        jButtonBrowse.setEnabled(false);
        jTextFieldFilepath.setEnabled(false);
        jLabelBrowseDir.setEnabled(false);
        jButtonApply.setEnabled(false);
        jCheckBoxRemoveParaSpaces.setEnabled(false);
        jCheckBoxContent.setSelected(settingsObj.getTOCForDesktopExport());
        jCheckBoxRemoveParaSpaces.setSelected(settingsObj.getRemoveLinesForDesktopExport());
        initListeners();
        // select initial value for export-format. since the constants start with a value of 8,
        // while the combo-box first index starts with 0, we have to "convert" these values.
        int selection = 0;
        switch (settingsObj.getDesktopExportFormat()) {
            case Constants.EXP_TYPE_DESKTOP_DOCX: selection = TYPE_DOCX; break;
            case Constants.EXP_TYPE_DESKTOP_ODT: selection = TYPE_ODT; break;
            case Constants.EXP_TYPE_DESKTOP_MD: selection = TYPE_MD; break;
            case Constants.EXP_TYPE_DESKTOP_EPUB: selection = TYPE_EPUB; break;
            case Constants.EXP_TYPE_DESKTOP_RTF: selection = TYPE_RTF; break;
            case Constants.EXP_TYPE_DESKTOP_HTML: selection = TYPE_HTML; break;
            case Constants.EXP_TYPE_DESKTOP_TXT: selection = TYPE_TXT; break;
            case Constants.EXP_TYPE_DESKTOP_TEX: selection = TYPE_TEX; break;
        }
        jComboBox_exportType.setSelectedIndex(selection);
        jComboBoxComments.setSelectedIndex(settingsObj.getDesktopCommentExport());
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
        // set an action listener which reacts on item choices.
        // cannot be done earlier, because adding items to the
        // combo box would fire an action each time,
        // although the combo box is still being initiated
        jComboBox_exportType.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                // retrieve selected index of combobox
                int selectedIndex = jComboBox_exportType.getSelectedIndex();
                // show next components, depending on valid choice
                jLabelBrowseDir.setEnabled(selectedIndex>0);
                jCheckBoxExportBibTex.setEnabled(selectedIndex>0);
                jCheckBoxContent.setEnabled(selectedIndex>0 && selectedIndex!=TYPE_TXT); // enable for all except txt
                jButtonBrowse.setEnabled(selectedIndex>0);
                jCheckBoxRemoveParaSpaces.setEnabled(selectedIndex>0);
                jComboBoxComments.setEnabled(selectedIndex>0);
                jTextFieldFilepath.setEnabled(selectedIndex>0);
                // if no valid item chose, do nothing
                if (selectedIndex<1) return;
                // check whetehr the user changes the fileformat, then apply new file-extenstion
                String filext = jTextFieldFilepath.getText();
                if (filext!=null && !filext.isEmpty()) {
                    String newext = resourceMap.getString("ExportType"+String.valueOf(jComboBox_exportType.getSelectedIndex())+"Ext");
                    filepath = new File(filext.substring(0, filext.lastIndexOf("."))+newext);
                    jTextFieldFilepath.setText(filepath.toString());
                }
            }
        });
        jCheckBoxExportBibTex.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent evt) {
                boolean selected = jCheckBoxExportBibTex.isSelected();
                if (selected) {
                    JOptionPane.showMessageDialog(null,resourceMap.getString("bibtexInformationMsg"),resourceMap.getString("bibtexInformationTitle"),JOptionPane.PLAIN_MESSAGE);
                }
            }
        });
    }
    /**
     * Initiation of the combo box. Sets possible Export types (file types)
     * into the combo box.
     */
    private void initComboBox() {
        // empyt combo box
        jComboBox_exportType.removeAllItems();
        // add file types, which can be importet
        jComboBox_exportType.addItem(resourceMap.getString("ComboItemChoose"));
        // get file descriptions and extenstions from the resource map
        // and add add them to the combo box
        for (int cnt=1; cnt<=EXPORTTYPECOUNT; cnt++) {
            jComboBox_exportType.addItem(resourceMap.getString("ExportType"+String.valueOf(cnt))+" ("+resourceMap.getString("ExportType"+String.valueOf(cnt)+"Ext")+")");
        }
        // make drop-down-list fit item-size
        jComboBox_exportType.setMaximumRowCount(EXPORTTYPECOUNT+1);
    }
    /**
     *
     */
    @Action
    public void cancel() {
        // Close Window
        retval = -1;
        setVisible(false);
        dispose();
    }
    /**
     *
     */
    @Action
    public void save() {
        // retrieve last used importdirectory
        File exportdir = settingsObj.getLastOpenedExportDir();
        // here we open a swing filechooser, in case the os ist no mac aqua
        filepath = FileOperationsUtil.chooseFile(this,
                                          JFileChooser.SAVE_DIALOG,
                                          JFileChooser.FILES_ONLY,
                                          (null==exportdir)?null:exportdir.getPath(),
                                          (null==exportdir)?null:exportdir.getName(),
                                          resourceMap.getString("fileDialogTitleSave"),
                                          new String[] {resourceMap.getString("ExportType"+String.valueOf(jComboBox_exportType.getSelectedIndex())+"Ext")},
                                          resourceMap.getString("ExportType"+String.valueOf(jComboBox_exportType.getSelectedIndex())),
                                          settingsObj);
        if (filepath!=null) {
            // save last used path
            settingsObj.setLastOpenedExportDir(filepath);
            // retrieve extenstion
            String cb_choice = resourceMap.getString("ExportType"+String.valueOf(jComboBox_exportType.getSelectedIndex())+"Ext");
            // if the entered filename has no extension, add one
            if (!filepath.getName().toLowerCase().endsWith(cb_choice)) {
                filepath = new File(filepath.getPath()+cb_choice);
            }
            // set the filepath to the textfield
            jTextFieldFilepath.setText(filepath.toString());
            // enable apply-button
            jButtonApply.setEnabled(true);
            // and set cursor focus
            jButtonApply.requestFocusInWindow();
        }
        else {
            // enable apply-button
            jButtonApply.setEnabled(false);
        }
    }
    /**
     *
     */
    @Action
    public void startExport() {
        // set the data format which should be imported
        switch (jComboBox_exportType.getSelectedIndex()) {
            case 0: return;
            case TYPE_DOCX: exportType = Constants.EXP_TYPE_DESKTOP_DOCX; break;
            case TYPE_ODT: exportType = Constants.EXP_TYPE_DESKTOP_ODT; break;
            case TYPE_RTF: exportType = Constants.EXP_TYPE_DESKTOP_RTF; break;
            case TYPE_HTML: exportType = Constants.EXP_TYPE_DESKTOP_HTML; break;
            case TYPE_TXT: exportType = Constants.EXP_TYPE_DESKTOP_TXT; break;
            case TYPE_TEX: exportType = Constants.EXP_TYPE_DESKTOP_TEX; break;
            case TYPE_MD: exportType = Constants.EXP_TYPE_DESKTOP_MD; break;
            case TYPE_EPUB: exportType = Constants.EXP_TYPE_DESKTOP_EPUB; break;
        }
        // check whether pandoc is available
        if (Tools.isPandocMissing(settingsObj, exportType)) {
            // Close Window
            retval = -1;
            setVisible(false);
            dispose();
            return;
        }
        // check whether a bibtex-file should be created or not
        exportbibtex = jCheckBoxExportBibTex.isSelected();
        // check whether the user wants to create a content
        exportTOC = jCheckBoxContent.isSelected();
        // when the user chose to export bibtex-file and we have no attached file, tell this to user
        if (exportbibtex && bibtexObj.getCount()<1) {
            JOptionPane.showMessageDialog(null,resourceMap.getString("noAttachedBibtexMsg"),resourceMap.getString("noAttachedBibtexTitle"),JOptionPane.PLAIN_MESSAGE);
            return;
        }
        settingsObj.setDesktopExportFormat(exportType);
        settingsObj.setDesktopCommentExport(jComboBoxComments.getSelectedIndex());
        settingsObj.setRemoveLinesForDesktopExport(jCheckBoxRemoveParaSpaces.isSelected());
        settingsObj.setTOCForDesktopExport(jCheckBoxContent.isSelected());
        // Close Window
        retval = 1;
        setVisible(false);
        dispose();
    }
    

    public File getFilePath() {
        return filepath;
    }
    /**
     * indicates which type of data format should be exported to.
     * 
     * @return one of following constants:<br>
     * - CConstants.EXP_TYPE_DESKTOP_DOCX<br>
     * - CConstants.EXP_TYPE_DESKTOP_ODT<br>
     * - CConstants.EXP_TYPE_DESKTOP_RTF<br>
     * - CConstants.EXP_TYPE_DESKTOP_HTML<br>
     * - CConstants.EXP_TYPE_DESKTOP_MD<br>
     * - CConstants.EXP_TYPE_DESKTOP_TXT<br>
     * - CConstants.EXP_TYPE_DESKTOP_TEX<br>
     * - CConstants.EXP_TYPE_DESKTOP_EPUB<br>
     */
    public int getExportType() {
        return exportType;
    }
    public boolean getExportBibTex() {
        return exportbibtex;
    }
    /**
     * Indicates, whether a table of contents should be created or not. applies only to
     * the HTML-export-format.
     * 
     * @return
     */
    public boolean getExportToc() {
        return exportTOC;
    }
    /**
     * return value which indicates whether the dialog was closed correcty or
     * if a the action was cancelled.
     * 
     * @return RETURN_VALUE_CANCEL
     * <br>RETURN_VALUE_CLOSE
     * <br>RETURN_VALUE_CONFIRM (valid start of import including correct file path)
     */
    public int getReturnValue() {
        return retval;
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
        jComboBox_exportType = new javax.swing.JComboBox();
        jCheckBoxExportBibTex = new javax.swing.JCheckBox();
        jLabelBrowseDir = new javax.swing.JLabel();
        jButtonBrowse = new javax.swing.JButton();
        jTextFieldFilepath = new javax.swing.JTextField();
        jButtonCancel = new javax.swing.JButton();
        jButtonApply = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jCheckBoxContent = new javax.swing.JCheckBox();
        jComboBoxComments = new javax.swing.JComboBox();
        jCheckBoxRemoveParaSpaces = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ZettelkastenApp.class).getContext().getResourceMap(CDesktopExport.class);
        setTitle(resourceMap.getString("FormDesktopExport.title")); // NOI18N
        setModal(true);
        setName("FormDesktopExport"); // NOI18N
        setResizable(false);

        jPanel1.setName("jPanel1"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jComboBox_exportType.setName("jComboBox_exportType"); // NOI18N

        jCheckBoxExportBibTex.setText(resourceMap.getString("jCheckBoxExportBibTex.text")); // NOI18N
        jCheckBoxExportBibTex.setToolTipText(resourceMap.getString("jCheckBoxExportBibTex.toolTipText")); // NOI18N
        jCheckBoxExportBibTex.setName("jCheckBoxExportBibTex"); // NOI18N

        jLabelBrowseDir.setText(resourceMap.getString("jLabelBrowseDir.text")); // NOI18N
        jLabelBrowseDir.setName("jLabelBrowseDir"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(ZettelkastenApp.class).getContext().getActionMap(CDesktopExport.class, this);
        jButtonBrowse.setAction(actionMap.get("save")); // NOI18N
        jButtonBrowse.setName("jButtonBrowse"); // NOI18N

        jTextFieldFilepath.setEditable(false);
        jTextFieldFilepath.setName("jTextFieldFilepath"); // NOI18N

        jButtonCancel.setAction(actionMap.get("cancel")); // NOI18N
        jButtonCancel.setName("jButtonCancel"); // NOI18N

        jButtonApply.setAction(actionMap.get("startExport")); // NOI18N
        jButtonApply.setName("jButtonApply"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jCheckBoxContent.setText(resourceMap.getString("jCheckBoxContent.text")); // NOI18N
        jCheckBoxContent.setName("jCheckBoxContent"); // NOI18N

        jComboBoxComments.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "keine Kommentare exportieren", "Kommentare mit exportieren", "nur Zettel mit Kommentaren exportieren" }));
        jComboBoxComments.setName("jComboBoxComments"); // NOI18N

        jCheckBoxRemoveParaSpaces.setText(resourceMap.getString("jCheckBoxRemoveParaSpaces.text")); // NOI18N
        jCheckBoxRemoveParaSpaces.setToolTipText(resourceMap.getString("jCheckBoxRemoveParaSpaces.toolTipText")); // NOI18N
        jCheckBoxRemoveParaSpaces.setName("jCheckBoxRemoveParaSpaces"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jComboBox_exportType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jCheckBoxContent)
                    .addComponent(jCheckBoxExportBibTex)
                    .addComponent(jComboBoxComments, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                            .addComponent(jButtonBrowse)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jTextFieldFilepath))
                        .addComponent(jLabelBrowseDir, javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(jButtonCancel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButtonApply)))
                    .addComponent(jCheckBoxRemoveParaSpaces))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox_exportType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxContent)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxRemoveParaSpaces)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxExportBibTex)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxComments, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabelBrowseDir)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonBrowse)
                    .addComponent(jTextFieldFilepath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonCancel)
                    .addComponent(jButtonApply))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonApply;
    private javax.swing.JButton jButtonBrowse;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JCheckBox jCheckBoxContent;
    private javax.swing.JCheckBox jCheckBoxExportBibTex;
    private javax.swing.JCheckBox jCheckBoxRemoveParaSpaces;
    private javax.swing.JComboBox jComboBoxComments;
    private javax.swing.JComboBox jComboBox_exportType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabelBrowseDir;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField jTextFieldFilepath;
    // End of variables declaration//GEN-END:variables

}
