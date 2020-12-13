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
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.util.FileOperationsUtil;
import java.awt.FileDialog;
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
 * This is an import dialog which organises the import of data which
 * is not provided in the standard file format. With this dialog it
 * should be possible to import data in other xml-format than the 
 * programme's typical format, or csv-files, text-files etc.
 * as well as Zettelkasten-data files from older programme versions
 * 
 * Parameters:
 * reference to the parent window
 * the modal state of the dialog
 * and the data objetct "CDaten" which adds new entry to the "Zettelkasten"
 * 
 * @author  danielludecke
 */
public class CImport extends javax.swing.JDialog {
    
    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap = 
        org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
        getContext().getResourceMap(CImport.class);
    /**
     * file path to import file
     */
    private File filepath;
    private final Settings settingsObj;
    private final BibTeX bibtexObj;
    /**
     * indicates which type of data format should be imported.
     * refer to the Zettelkasten.view properties file (resources) to see
     * which number is which file type.
     */
    private int importType;
    /**
     * old zettelkasten data from windows are provided in ascii format,
     * so it might be that ascii chars have to be converted to unicode
     */
    private boolean asciitounicode;
    /**
     * indicates whether the data should be appended to an already opened zettelkasten
     * or whether the old zettelkasten-data-file should be closed (and saved) before and
     * a new data-file should be created from the imported data
     */
    private boolean append;
    /**
     * indicates whether the we have any data at all, and thus appending is
     * possible or not. if we have no data, we disable the radiobuttons and simply
     * create a new data file.
     */
    private final boolean appendingrestore;
    private boolean appendingpossible;
    /**
     * Indicates whether we have a currently opened dataset or not...
     */
    private final boolean dataavailable;
    private String separatorchar;
    /**
     * return value which indicates whether the dialog was closed correcty or
     * if a the action was cancelled
     * <br>-1 = cancel action
     * <br>0 = close action
     * <br>1 = valid start of import including correct file path
     */
    private int retval;
    
    
    /**
     * 
     * @param parent
     * @param st
     * @param bt
     * @param ap 
     */
    public CImport(java.awt.Frame parent, Settings st, BibTeX bt, boolean ap) {
        super(parent);
        initComponents();
        // set application icon
        setIconImage(Constants.zknicon.getImage());
        // reset variables
        settingsObj = st;
        bibtexObj = bt;
        retval = 0;
        importType = 0;
        asciitounicode = false;
        append = false;
        appendingpossible = appendingrestore = dataavailable = ap;
        // make all compononents invisible. the second parameter
        // indicates, how many components are invisible. the components
        // are "grouped" and can be selectivly made (in-)visible        
        setComponentsVisible(false,3);
        jCheckBox1.setEnabled(false);
        // initiate the combo box with items
        initComboBox();
        // when we have no data, there is no need for appending the data
        if (!appendingpossible) {
            jRadioButton1.setSelected(true);
            jRadioButton1.setEnabled(false);
            jRadioButton2.setEnabled(false);
            jLabel3.setEnabled(false);
        }
        // these codelines add an escape-listener to the dialog. so, when the user
        // presses the escape-key, the same action is performed as if the user
        // presses the cancel button...
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        ActionListener cancelAction = new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                cancelImport();
            }
        };
        getRootPane().registerKeyboardAction(cancelAction, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);                
        if (settingsObj.isSeaGlass()) {
            jButton1.putClientProperty("JComponent.sizeVariant", "small");
            jButton2.putClientProperty("JComponent.sizeVariant", "small");
            jButton3.putClientProperty("JComponent.sizeVariant", "small");
        }
    }


    /**
     * Finish the import dialog. Check for valid file path
     * before closing the window
     */
    @Action
    public void startImport() {
        // get the filepath from the textfield
        filepath = new File(jTextField1.getText());
        // if no file exists, send error msg
        if (null == filepath || !filepath.exists()) {
            // display error message box
            JOptionPane.showMessageDialog(null,resourceMap.getString("importWindowErrMsg1"),resourceMap.getString("errMsgTitle"),JOptionPane.PLAIN_MESSAGE);
            jTextField1.requestFocusInWindow();
            return;
        }
        // set the data format which should be imported
        importType = jComboBox_importType.getSelectedIndex();
        // check whether a conversion from ascii to unicode is necessary
        asciitounicode = jCheckBox1.isSelected();
        // check whether the data should be appended to the opened data-file
        append = jRadioButton2.isSelected();
        // separator char?
        separatorchar = jTextFieldCsvSeparator.getText();
        // Close Window
        retval = 1;
        setVisible(false);
        dispose();
    }
    
    
    
    /**
     * Action which stops the import
     */
    @Action
    public void cancelImport() {
        // Close Window
        retval = -1;
        setVisible(false);
        dispose();
    }

    
    
    /**
     * Action which opens the file chooser
     */
    @Action
    public void open() {
        String ext;
        switch (jComboBox_importType.getSelectedIndex()) {
            case 1: ext = resourceMap.getString("ImportType1Ext");
                    break;
            case 2: ext = resourceMap.getString("ImportType2Ext");
                    break;
            case 3: ext = resourceMap.getString("ImportType3Ext");
                    break;
            case 4: ext = resourceMap.getString("ImportTypeBibTexExt");
                    break;
            case 5: ext = resourceMap.getString("ImportType4Ext");
                    break;
            default: ext = resourceMap.getString("ImportType1Ext");
                     break;
        }
        String desc;
        switch (jComboBox_importType.getSelectedIndex()) {
            case 1: desc = resourceMap.getString("ImportType1");
                    break;
            case 2: desc = resourceMap.getString("ImportType2");
                    break;
            case 3: desc = resourceMap.getString("ImportType3");
                    break;
            case 4: desc = resourceMap.getString("ImportTypeBibTex");
                    break;
            case 5: desc = resourceMap.getString("ImportType4");
                    break;
            default: desc = resourceMap.getString("ImportType1");
                     break;
        }
        // retrieve last used importdirectory
        File importdir = settingsObj.getLastOpenedImportDir();
        // let user choose filepath
        filepath = FileOperationsUtil.chooseFile(this,
                                          (settingsObj.isMacAqua())?FileDialog.LOAD:JFileChooser.OPEN_DIALOG,
                                          JFileChooser.FILES_ONLY,
                                          (null==importdir)?null:importdir.getPath(),
                                          (null==importdir)?null:importdir.getName(),
                                          resourceMap.getString("importUseOpenTitle"),
                                          ext.split(";"),
                                          desc,
                                          settingsObj);
        // check whether we have any file...
        if (filepath!=null) {
            // set the string into the textfield.
            jTextField1.setText(filepath.toString());
            // if we have a bibtex-file, directly open next window here...
            if (Constants.TYPE_BIB==jComboBox_importType.getSelectedIndex()) {
                // set new bibtex-filepath
                bibtexObj.setFilePath(filepath);
                bibtexObj.detachCurrentlyAttachedFile();
                // make next components visible
                setComponentsVisible(true,3);
                // and give focus to the next button
                jButton2.requestFocusInWindow();
            }
            else {
                // save as standard-directory
                settingsObj.setLastOpenedImportDir(filepath);
                // if the data can be appended, set focus to radio button
                if (appendingpossible) {
                    // make next components visible
                    setComponentsVisible(true,2);
                    // and give focus to the next button
                    jRadioButton1.requestFocusInWindow();
                }
                // else to the finish button
                else {
                    // make next components visible
                    setComponentsVisible(true,3);
                    // and give focus to the next button
                    jButton2.requestFocusInWindow();
                }
            }
        }
    }

    
    
    /**
     * Action which shows the last components after choosing whether a file
     * should be appended to the existing data or a new
     * zettelkasten should be created...
     */
    public void showLastStep() {
        // make next components visible
        setComponentsVisible(true,3);
        // and give focus to the button
        jButton2.requestFocusInWindow();
    }


    
    /**
     * Sets components visible or invisible. Used to show the buttons and labels
     * step by step, as the user gets to the next function
     */    
    private void setComponentsVisible(boolean wert, int howmuch) {
        // show/hide second step (choose file from directory)
        jLabel2.setEnabled(wert);
        jTextField1.setEnabled(wert);
        jButton1.setEnabled(wert);
        // show/hide third step (append or open file)
        if (howmuch > 1 && appendingpossible) {
            jLabel3.setEnabled(wert);
            jRadioButton1.setEnabled(wert);
            jRadioButton2.setEnabled(wert);
        }
        // show/hide fourth step (finish choices and start import)
        if (howmuch > 2) {
            jButton2.setEnabled(wert);
        }
    }

    
    /**
     * Initiation of the combo box. Sets possible import types (file types)
     * into the combo box.
     */    
    private void initComboBox() {
        // empyt combo box
        jComboBox_importType.removeAllItems();
        // add file types, which can be importet
        jComboBox_importType.addItem(resourceMap.getString("ComboItemChoose"));
        // get file descriptions and extenstions from the resource map
        // and add add them to the combo box
        String comboItem;
        comboItem = resourceMap.getString("ImportType1")+" ("+resourceMap.getString("ImportType1Ext")+")";
        jComboBox_importType.addItem(comboItem);
        comboItem = resourceMap.getString("ImportType2")+" ("+resourceMap.getString("ImportType2Ext")+")";
        jComboBox_importType.addItem(comboItem);
        comboItem = resourceMap.getString("ImportType3")+" ("+resourceMap.getString("ImportType3Ext")+")";
        jComboBox_importType.addItem(comboItem);
        comboItem = resourceMap.getString("ImportTypeBibTex")+" ("+resourceMap.getString("ImportTypeBibTexExt")+")";
        jComboBox_importType.addItem(comboItem);

        // TODO implement import-options for csv and txt later...

        // these lines are for importing text and csv-data. this feature will not come with version
        // 3.0 of this program, so we comment them...
//        comboItem = resourceMap.getString("ImportType4")+" ("+resourceMap.getString("ImportType4Ext")+")";
//        jComboBox_importType.addItem(makeObj(comboItem));
//        comboItem = resourceMap.getString("ImportType5")+" ("+resourceMap.getString("ImportType5Ext")+")";
//        jComboBox_importType.addItem(makeObj(comboItem));
                
        // set an action listener which reacts on item choices.
        // cannot be done earlier, because adding items to the
        // combo box would fire an action each time,
        // although the combo box is still being initiated
        jComboBox_importType.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                int selected = jComboBox_importType.getSelectedIndex();
                // if no valid item chose, do nothing
                if (selected < 1) {
                    return;
                }
                // when the user selected to import new data, although we have no currently opened
                // data, tell user to use the open-menuitem instead
                if (Constants.TYPE_ZKN3==selected && !dataavailable) {
                    // how warning message
                    JOptionPane.showMessageDialog(null,resourceMap.getString("importUseOpenMsg"),resourceMap.getString("importUseOpenTitle"),JOptionPane.PLAIN_MESSAGE);
                    // disable button again
                    setComponentsVisible(false,3);
                    // reset selection
                    jComboBox_importType.setSelectedIndex(0);
                    return;
                }

                // else show next components
                setComponentsVisible(true,1);
                // re-init appending-possible value. this value might be changed
                // when the user selected to import meta-data
                appendingpossible = appendingrestore;
                // en-/disable checkbox for ascii format only if
                // it applies. choosing the new zkn-format (zkn3) will automatically
                // disable the checkbox because ascii-to-unicode conversion is not necessary.
                // choosing the old zkn-format will enable it by default
                switch(selected) {
                    case Constants.TYPE_BIB:
                    case Constants.TYPE_ZKN3:
                            jCheckBox1.setEnabled(false);
                            jCheckBox1.setSelected(false);
                            // set focus on the file choose button...
                            jButton1.requestFocusInWindow();
                            break;
                    case Constants.TYPE_ZKN:        
                            jCheckBox1.setEnabled(false);
                            jCheckBox1.setSelected(true);
                            // set focus on the file choose button...
                            jButton1.requestFocusInWindow();
                            break;
                    case Constants.TYPE_CSV:
                            jCheckBox1.setEnabled(false);
                            jCheckBox1.setSelected(false);
                            // set focus on the file choose button...
                            jTextFieldCsvSeparator.requestFocusInWindow();
                    case Constants.TYPE_XML:
                            jCheckBox1.setEnabled(true);
                            // set focus on the checkbox
                            jCheckBox1.requestFocusInWindow();
                            break;
                }
                if (Constants.TYPE_ZKN3==selected) {
                    appendingpossible = false;
                    jRadioButton1.setEnabled(false);
                    jRadioButton2.setEnabled(false);
                    jRadioButton2.setSelected(true);
                    jLabel3.setEnabled(false);
                }
                else if (Constants.TYPE_BIB==selected) {
                    appendingpossible = false;
                    jRadioButton1.setEnabled(false);
                    jRadioButton2.setEnabled(false);
                    jRadioButton2.setSelected(true);
                    jLabel3.setEnabled(false);
                }
                else {
                    // when we want to import foreign-word or synonyms etc.,
                    // we don't need append-option
                    jRadioButton1.setEnabled(appendingpossible);
                    jRadioButton2.setEnabled(appendingpossible);
                    jLabel3.setEnabled(appendingpossible);
                }
            }
        });
    }
            
    
    /**
     * File pathes of the files that have to be imported.
     * 
     * @return
     */
    public File getFilePath() {
        return filepath;
    }
    /**
     * indicates which type of data format should be imported.
     * refer to the Zettelkasten.view properties file (resources) to see
     * which number is which file type.
     * 
     * @return
     * <ul>
     * <li>TYPE_ZKN3 (new zettelkasten-files)</li>
     * <li>TYPE_ZKN (older zettelkasten-file)(</li>
     * <li>TYPE_META (foreign-words-list from the old zettelkasten)</li>
     * <li>TYPE_BIB (bibtex-file)</li>
     * <li>TYPE_XML</li>
     * <li>TYPE_CSV</li>
     * <li>TYPE_ZKS (synonyms-file from the old zettelkasten)</li>
     * <li>TYPE_ZKA (auto-correction-file from the old zettelkasten)</li>
     * <li>TYPE_ZKL (bookmark-file from the old zettelkasten)</li>
     * <li>TYPE_ZKD (desktop-file from the old zettelkasten(</li>
     * <li>TYPE_ZKF (foreign-words-list from the old zettelkasten)</li>
     * <li>TYPE_ZKT (steno-list from the old zettelkasten)</li>
     * </ul>
     */
    public int getImportType() {
        return importType;
    }
    /**
     * Old zettelkasten data from windows are provided in ascii format,
     * so it might be that ascii chars have to be converted to unicode.
     * 
     * @return
     */
    public boolean getAsciiToUnicode() {
        return asciitounicode;
    }
    /**
     * indicates whether the data should be appended to an already opened zettelkasten
     * or whether the old zettelkasten-data-file should be closed (and saved) before and
     * a new data-file should be created from the imported data
     * 
     * @return
     */
    public boolean getAppend() {
        return append;
    }
    /**
     * Returns the separator char for CSV files.
     * 
     * @return The separator char for CSV files
     */
    public String getSeparatorChar() {
        return separatorchar;
    }
    /**
     * indicates whether the we have any data at all, and thus appending is
     * possible or not. if we have no data, we disable the radiobuttons and simply
     * create a new data file.
     * 
     * @return
     */
    public boolean getAppendingPossible() {
        return appendingpossible;
    }
//    /**
//     * 
//     * @return 
//     */
//    public boolean getAppendingRestore() {
//        return appendingrestore;
//    }
    /**
     * Indicates whether we have a currently opened dataset or not...
     * 
     * @return
     */
    public boolean isDataAvailable() {
        return dataavailable;
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jComboBox_importType = new javax.swing.JComboBox();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldCsvSeparator = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getResourceMap(CImport.class);
        setTitle(resourceMap.getString("FormImportDialog.title")); // NOI18N
        setModal(true);
        setName("FormImportDialog"); // NOI18N
        setResizable(false);

        jPanel1.setName("jPanel1"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jComboBox_importType.setName("jComboBox_importType"); // NOI18N

        jCheckBox1.setText(resourceMap.getString("jCheckBox1.text")); // NOI18N
        jCheckBox1.setToolTipText(resourceMap.getString("jCheckBox1.toolTipText")); // NOI18N
        jCheckBox1.setName("jCheckBox1"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getActionMap(CImport.class, this);
        jButton1.setAction(actionMap.get("open")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N

        jTextField1.setText(resourceMap.getString("jTextField1.text")); // NOI18N
        jTextField1.setName("jTextField1"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setText(resourceMap.getString("jRadioButton1.text")); // NOI18N
        jRadioButton1.setName("jRadioButton1"); // NOI18N
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setText(resourceMap.getString("jRadioButton2.text")); // NOI18N
        jRadioButton2.setName("jRadioButton2"); // NOI18N
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton2ActionPerformed(evt);
            }
        });

        jButton2.setAction(actionMap.get("startImport")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N

        jButton3.setAction(actionMap.get("cancelImport")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jTextFieldCsvSeparator.setColumns(4);
        jTextFieldCsvSeparator.setText(resourceMap.getString("jTextFieldCsvSeparator.text")); // NOI18N
        jTextFieldCsvSeparator.setToolTipText(resourceMap.getString("jTextFieldCsvSeparator.toolTipText")); // NOI18N
        jTextFieldCsvSeparator.setName("jTextFieldCsvSeparator"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField1))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldCsvSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jRadioButton2)
                            .addComponent(jRadioButton1)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jComboBox_importType, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jCheckBox1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox_importType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jTextFieldCsvSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton2)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton3)
                    .addComponent(jButton2)))
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
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
    showLastStep();
}//GEN-LAST:event_jRadioButton1ActionPerformed

private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
    showLastStep();
}//GEN-LAST:event_jRadioButton2ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JComboBox jComboBox_importType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextFieldCsvSeparator;
    // End of variables declaration//GEN-END:variables

}
