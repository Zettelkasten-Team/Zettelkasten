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
import de.danielluedecke.zettelkasten.database.BibTeX;
import de.danielluedecke.zettelkasten.util.Tools;
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
 * This dialog displays various export settings that are defined by the user. In this dialog, the
 * user can specify the export-format, which elements of entries should be exported and so on.
 *
 * When the user clicks the apply-button, the dialog will be closed and the settings can be
 * retrieved via the various getter-methods.
 *
 * Usually, this method is called from the main window.
 *
 * @author danielludecke
 */
public class CExport extends javax.swing.JDialog {

    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap
            = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
            getContext().getResourceMap(CExport.class);
    /**
     * A reference to the settings class
     */
    private final Settings settingsObj;
    /**
     * A reference to the bibtex class
     */
    private final BibTeX bibtexObj;
    /**
     * file path to export file
     */
    private File filepath;
    /**
     * indicates which type of data format should be exported to. Use following constants:<br>
     * - CConstants.EXP_TYPE_DOCX<br>
     * - CConstants.EXP_TYPE_ODT<br>
     * - CConstants.EXP_TYPE_RTF<br>
     * - CConstants.EXP_TYPE_XML<br>
     * - CConstants.EXP_TYPE_CSV<br>
     * - CConstants.EXP_TYPE_HTML<br>
     * - CConstants.EXP_TYPE_TXT<br>
     * - CConstants.EXP_TYPE_TEX<br>
     * - CConstants.EXP_TYPE_ZKN3<br>
     */
    private int exportType;
    private static final int TYPE_DOCX = 1;
    private static final int TYPE_ODT = 2;
    private static final int TYPE_RTF = 3;
    private static final int TYPE_XML = 4;
    private static final int TYPE_CSV = 5;
    private static final int TYPE_HTML = 6;
    private static final int TYPE_MD = 7;
    private static final int TYPE_TXT = 8;
    private static final int TYPE_TEX = 9;
    private static final int TYPE_ZKN3 = 10;
    /**
     * This variable indicates whether the author- and keyword-file should be exported as separate
     * files (just like the typical storage-system we use) or whether the author- and
     * keyword-index-numbers should be replaced with the related string-values, so the author- and
     * keyword-information are all in one file
     */
    private boolean separateFileForNotes;
    /**
     *
     */
    private boolean titlePrefix;
    /**
     *
     */
    private boolean highlightkeywords;
    /**
     * Indicates wheher the UBB-Fomattags should be removed and the entries should be exported in
     * plain text, without format-tags.
     */
    private boolean removeformattags;
    /**
     *
     */
    private boolean exportbibtex;
    /**
     *
     */
    private char csvseparator = ',';
    /**
     * return value which indicates whether the dialog was closed correcty or if a the action was
     * cancelled
     * <br>-1 = cancel action
     * <br>0 = close action
     * <br>1 = valid start of import including correct file path
     */
    private int retval;
    /**
     * This variable stores the parts which should be exported. It's a mix of ORed constants, see
     * above
     */
    private int exportparts;
    /**
     * Here we set up the available amount of different export-formats. currently, the programme
     * supports 8 different export-formats.
     */
    private static final int EXPORTTYPECOUNT = 10;

    /**
     *
     *
     * @param parent
     * @param s
     * @param bt
     */
    public CExport(java.awt.Frame parent, Settings s, BibTeX bt) {
        super(parent);
        settingsObj = s;
        bibtexObj = bt;
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
        jCheckBoxSeparateFile.setEnabled(false);
        jCheckBoxExportBibTex.setEnabled(false);
        jCheckBoxRemoveFormatTags.setEnabled(false);
        jComboBoxCSVSeparator.setEnabled(false);
        jCheckBox2.setEnabled(false);
        jCheckBox3.setEnabled(false);
        jCheckBox4.setEnabled(false);
        jCheckBox5.setEnabled(false);
        jCheckBox6.setEnabled(false);
        jCheckBox7.setEnabled(false);
        jCheckBox8.setEnabled(false);
        jCheckBox9.setEnabled(false);
        jCheckBox10.setEnabled(false);
        // now we have to check which checkboxes have to be selected. the
        // value of the export-parts are ORed together, so the variable
        // "exportparts" finally contains all set bits for the parts of the data
        // which the user wants to export.
        int exportp = settingsObj.getExportParts();
        jCheckBox2.setSelected((exportp & Constants.EXPORT_TITLE) != 0);
        jCheckBox3.setSelected((exportp & Constants.EXPORT_CONTENT) != 0);
        jCheckBox4.setSelected((exportp & Constants.EXPORT_AUTHOR) != 0);
        jCheckBox5.setSelected((exportp & Constants.EXPORT_KEYWORDS) != 0);
        jCheckBox6.setSelected((exportp & Constants.EXPORT_LINKS) != 0);
        jCheckBox7.setSelected((exportp & Constants.EXPORT_REMARKS) != 0);
        jCheckBox8.setSelected((exportp & Constants.EXPORT_TIMESTAMP) != 0);
        jCheckBox9.setSelected((exportp & Constants.EXPORT_MANLINKS) != 0);
        jCheckBox10.setSelected((exportp & Constants.EXPORT_LUHMANN) != 0);
        jButtonBrowse.setEnabled(false);
        jButtonApply.setEnabled(false);
        jTextFieldFilepath.setEnabled(false);
        jLabelBrowseDir.setEnabled(false);
        jLabelCSVSeparator.setEnabled(false);
        initListeners();
        int sel = settingsObj.getExportFormat();
        jComboBox_exportType.setSelectedIndex((sel >= 0 && sel < jComboBox_exportType.getItemCount()) ? sel : 0);
        // jPanel2.setVisible(showexportparts);
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
                jLabelBrowseDir.setEnabled(selectedIndex > 0);
                jCheckBoxExportBibTex.setEnabled(selectedIndex > 0);
                jButtonBrowse.setEnabled(selectedIndex > 0);
                jTextFieldFilepath.setEnabled(selectedIndex > 0);
                jCheckBoxRemoveFormatTags.setEnabled(selectedIndex > 0);
                jCheckBoxSetTitleNumber.setEnabled(selectedIndex > 0);
                // show or hide checkboxes
                enableCheckBoxes();
                // if no valid item chose, do nothing
                if (selectedIndex < 1) {
                    return;
                }
                //
                // here we enable/disbale components depending on the XML-export-fomat
                //
                // en-/disable checkbox for "all-in-one-file" only if
                // it applies. choosing the xml-format will automatically
                // enable the checkbox. all other formats disable this checkbox
                jCheckBoxSeparateFile.setEnabled(TYPE_MD == selectedIndex || TYPE_TXT == selectedIndex || TYPE_TEX == selectedIndex);
                //
                // here we enable/disbale components depending on the CSV-export-fomat
                //
                // csv-checkboxes only apply to csv-export-format
                jLabelCSVSeparator.setEnabled(TYPE_CSV == selectedIndex);
                jComboBoxCSVSeparator.setEnabled(TYPE_CSV == selectedIndex);
                //
                // here we enable/disbale components depending on the ZKN3-export-fomat
                //
                // when we export data to the zettelkasten format (.zkn3), we have do disable
                // several components, since most options just don't apply to .zkn3-format
                jCheckBoxExportBibTex.setEnabled(TYPE_ZKN3 != selectedIndex);
                //
                // here we go on with further stuff...
                //
                // format tags removable
                switch (selectedIndex) {
                    case TYPE_DOCX:
                    case TYPE_ODT:
                    case TYPE_HTML:
                    case TYPE_RTF:
                    case TYPE_MD:
                    case TYPE_TEX:
                        jCheckBoxSetTitleNumber.setEnabled(true);
                    case TYPE_ZKN3:
                        jCheckBoxRemoveFormatTags.setEnabled(false);
                        jCheckBoxRemoveFormatTags.setSelected(false);
                        break;
                    case TYPE_TXT:
                        jCheckBoxRemoveFormatTags.setEnabled(true);
                        jCheckBoxSetTitleNumber.setEnabled(true);
                        break;
                    case TYPE_CSV:
                        jCheckBoxRemoveFormatTags.setEnabled(true);
                        jCheckBoxSetTitleNumber.setSelected(false);
                        jCheckBoxSetTitleNumber.setEnabled(false);
                        break;
                    case TYPE_XML:
                        jCheckBoxRemoveFormatTags.setEnabled(true);
                        jCheckBoxSetTitleNumber.setSelected(false);
                        jCheckBoxSetTitleNumber.setEnabled(false);
                        break;
                    default:
                        jCheckBoxRemoveFormatTags.setEnabled(true);
                        jCheckBoxSetTitleNumber.setEnabled(true);
                        break;
                }
                if (TYPE_DOCX == selectedIndex || TYPE_ODT == selectedIndex || TYPE_HTML == selectedIndex) {
                    jCheckBoxHighlightKeywords.setEnabled(jCheckBox5.isEnabled());
                } else {
                    jCheckBoxHighlightKeywords.setEnabled(false);
                    jCheckBoxHighlightKeywords.setSelected(false);
                }
                // set keyboard focus input
                if (TYPE_MD == selectedIndex || TYPE_TXT == selectedIndex || TYPE_TEX == selectedIndex) {
                    jCheckBoxSeparateFile.requestFocusInWindow();
                } // when the user selectes csv-format as export-type,
                // enable the combobox for the csv-separator
                else if (TYPE_CSV == selectedIndex) {
                    jComboBoxCSVSeparator.requestFocusInWindow();
                } else {
                    jButtonBrowse.requestFocusInWindow();
                }
                // check whetehr the user changes the fileformat, then apply new file-extenstion
                String filext = jTextFieldFilepath.getText();
                if (filext != null && !filext.isEmpty()) {
                    String newext = resourceMap.getString("ExportType" + String.valueOf(jComboBox_exportType.getSelectedIndex()) + "Ext");
                    filepath = new File(filext.substring(0, filext.lastIndexOf(".")) + newext);
                    jTextFieldFilepath.setText(filepath.toString());
                }
            }
        });
        jComboBoxCSVSeparator.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                switch (jComboBoxCSVSeparator.getSelectedIndex()) {
                    case 0:
                        csvseparator = ',';
                        break;
                    case 1:
                        csvseparator = ';';
                        break;
                    case 2:
                        csvseparator = '\t';
                        break;
                    default:
                        csvseparator = ',';
                        break;
                }
            }
        });
        jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showHideStartButton();
            }
        });
        jCheckBox3.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showHideStartButton();
            }
        });
        jCheckBox4.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showHideStartButton();
            }
        });
        jCheckBox5.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showHideStartButton();
                // retrieve selected index of combobox
                int selectedIndex = jComboBox_exportType.getSelectedIndex();
                if (TYPE_DOCX == selectedIndex || TYPE_ODT == selectedIndex || TYPE_HTML == selectedIndex) {
                    jCheckBoxHighlightKeywords.setEnabled(jCheckBox5.isSelected());
                }
            }
        });
        jCheckBox6.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showHideStartButton();
            }
        });
        jCheckBox7.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showHideStartButton();
            }
        });
        jCheckBox8.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showHideStartButton();
            }
        });
        jCheckBox9.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showHideStartButton();
            }
        });
        jCheckBox10.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showHideStartButton();
            }
        });
        jCheckBoxExportBibTex.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boolean selected = jCheckBoxExportBibTex.isSelected();
                if (selected) {
                    JOptionPane.showMessageDialog(null, resourceMap.getString("bibtexInformationMsg"), resourceMap.getString("bibtexInformationTitle"), JOptionPane.PLAIN_MESSAGE);
                }
            }
        });
    }

    /**
     * Initiation of the combo box. Sets possible Export types (file types) into the combo box.
     */
    private void initComboBox() {
        // empyt combo box
        jComboBox_exportType.removeAllItems();
        // add file types, which can be importet
        jComboBox_exportType.addItem(resourceMap.getString("ComboItemChoose"));
        // get file descriptions and extenstions from the resource map
        // and add add them to the combo box
        for (int cnt = 1; cnt <= EXPORTTYPECOUNT; cnt++) {
            jComboBox_exportType.addItem(resourceMap.getString("ExportType" + String.valueOf(cnt)) + " (" + resourceMap.getString("ExportType" + String.valueOf(cnt) + "Ext") + ")");
        }
        // make drop-down-list fit item-size
        jComboBox_exportType.setMaximumRowCount(EXPORTTYPECOUNT + 1);
    }

    private void enableCheckBoxes() {
        // retrieve selected index of combobox
        int selectedIndex = jComboBox_exportType.getSelectedIndex();
        // get enabled-status
        boolean enabled = TYPE_ZKN3 != selectedIndex && selectedIndex > 0;
        // enable the next components
        jCheckBox2.setEnabled(enabled);
        jCheckBox3.setEnabled(enabled);
        jCheckBox4.setEnabled(enabled);
        jCheckBox5.setEnabled(enabled);
        jCheckBox6.setEnabled(enabled);
        jCheckBox7.setEnabled(enabled);
        jCheckBox8.setEnabled(enabled);
        jCheckBox9.setEnabled(enabled);
        jCheckBox10.setEnabled(enabled);
        jCheckBoxHighlightKeywords.setEnabled(jCheckBox5.isSelected() && TYPE_ZKN3 != selectedIndex && enabled);
    }

    @Action
    public void save() {
        // retrieve last used importdirectory
        File exportdir = settingsObj.getLastOpenedExportDir();
        // here we open a swing filechooser, in case the os ist no mac aqua
        if (jCheckBoxSeparateFile.isSelected()) {
            filepath = FileOperationsUtil.chooseDirectory(resourceMap.getString("fileDialogTitleChooseDir"),
                    resourceMap.getString("LocaleFolder"),
                    (null == exportdir) ? null : new File(exportdir.getPath()));
            
        } else {
            filepath = FileOperationsUtil.chooseFile(this,
                    (settingsObj.isMacAqua()) ? FileDialog.SAVE : JFileChooser.SAVE_DIALOG,
                    JFileChooser.FILES_ONLY,
                    (null == exportdir) ? null : exportdir.getPath(),
                    (null == exportdir) ? null : exportdir.getName(),
                    resourceMap.getString("fileDialogTitleSave"),
                    new String[]{resourceMap.getString("ExportType" + String.valueOf(jComboBox_exportType.getSelectedIndex()) + "Ext")},
                    resourceMap.getString("ExportType" + String.valueOf(jComboBox_exportType.getSelectedIndex())),
                    settingsObj);
        }
        if (filepath != null) {
            // save last used path
            settingsObj.setLastOpenedExportDir(filepath);
            // retrieve extenstion
            String cb_choice = resourceMap.getString("ExportType" + String.valueOf(jComboBox_exportType.getSelectedIndex()) + "Ext");
            // if the entered filename has no extension, add one
            // but only, if user wants to save all notes to a single file
            if (!jCheckBoxSeparateFile.isSelected() && !filepath.getName().toLowerCase().endsWith(cb_choice)) {
                filepath = new File(filepath.getPath() + cb_choice);
            }
            // set the filepath to the textfield
            jTextFieldFilepath.setText(filepath.toString());
            // enables checkboxes
            enableCheckBoxes();
            // enable apply-button
            jButtonApply.setEnabled(true);
            // and set cursor focus
            jButtonApply.requestFocusInWindow();
        } else {
            // enable apply-button
            jButtonApply.setEnabled(false);
        }
    }

    /**
     * This method is called whenever the user ticks a checkbox for those elements which should be
     * exportet. if we have no selection, i.e. no element should be exportet, the export-button will
     * be disabled. if at least one checkbox is ticked, we can enable the excort-button.
     */
    private void showHideStartButton() {
        // check wether user has already chosen a filepath...
        if (!jTextFieldFilepath.getText().isEmpty()) {
            // if yes, enable apply-button.
            jButtonApply.setEnabled(jCheckBox2.isSelected()
                    || jCheckBox3.isSelected()
                    || jCheckBox4.isSelected()
                    || jCheckBox5.isSelected()
                    || jCheckBox6.isSelected()
                    || jCheckBox7.isSelected()
                    || jCheckBox8.isSelected()
                    || jCheckBox9.isSelected()
                    || jCheckBox10.isSelected());
        }
    }

    private int getSelectedExportType(int sel) {
        int selt = -1;
        // set the data format which should be imported
        switch (sel) {
            case 0:
                break;
            case TYPE_DOCX:
                selt = Constants.EXP_TYPE_DOCX;
                break;
            case TYPE_ODT:
                selt = Constants.EXP_TYPE_ODT;
                break;
            case TYPE_RTF:
                selt = Constants.EXP_TYPE_RTF;
                break;
            case TYPE_HTML:
                selt = Constants.EXP_TYPE_HTML;
                break;
            case TYPE_TXT:
                selt = Constants.EXP_TYPE_TXT;
                break;
            case TYPE_TEX:
                selt = Constants.EXP_TYPE_TEX;
                break;
            case TYPE_MD:
                selt = Constants.EXP_TYPE_MD;
                break;
            case TYPE_ZKN3:
                selt = Constants.EXP_TYPE_ZKN3;
                break;
            case TYPE_XML:
                selt = Constants.EXP_TYPE_XML;
                break;
            case TYPE_CSV:
                selt = Constants.EXP_TYPE_CSV;
                break;
        }
        return selt;
    }

    @Action
    public void startExport() {
        // set the data format which should be imported
        exportType = getSelectedExportType(jComboBox_exportType.getSelectedIndex());
        if (-1 == exportType) {
            return;
        }
        // check whether pandoc is available
        if (Tools.isPandocMissing(settingsObj, exportType)) {
            // Close Window
            retval = -1;
            setVisible(false);
            dispose();
            return;
        }
        // check whether the xml-file should be exported in one file, or separated
        separateFileForNotes = jCheckBoxSeparateFile.isSelected();
        // check whether the xml-file should be exported in one file, or separated
        removeformattags = jCheckBoxRemoveFormatTags.isSelected();
        // check whether a bibtex-file should be created or not
        exportbibtex = jCheckBoxExportBibTex.isSelected();
        // check whether entry-numbers should be used as title-prefix
        titlePrefix = jCheckBoxSetTitleNumber.isSelected();
        // when the user chose to export bibtex-file and we have no attached file, tell this to user
        if (exportbibtex && bibtexObj.getCount() < 1) {
            JOptionPane.showMessageDialog(null, resourceMap.getString("noAttachedBibtexMsg"), resourceMap.getString("noAttachedBibtexTitle"), JOptionPane.PLAIN_MESSAGE);
            return;
        }
        highlightkeywords = jCheckBoxHighlightKeywords.isSelected() && jCheckBoxHighlightKeywords.isEnabled();
        // reset the exportpatrts variable
        exportparts = 0;
        // now we have to check which checkboxes have been selected. the
        // value of the export-parts are ORed together, so the variable
        // "exportparts" finally contains all set bits for the parts of the data
        // which the user wants to export
        if (jCheckBox2.isSelected()) {
            exportparts = exportparts | Constants.EXPORT_TITLE;
        }
        if (jCheckBox3.isSelected()) {
            exportparts = exportparts | Constants.EXPORT_CONTENT;
        }
        if (jCheckBox4.isSelected()) {
            exportparts = exportparts | Constants.EXPORT_AUTHOR;
        }
        if (jCheckBox5.isSelected()) {
            exportparts = exportparts | Constants.EXPORT_KEYWORDS;
        }
        if (jCheckBox6.isSelected()) {
            exportparts = exportparts | Constants.EXPORT_LINKS;
        }
        if (jCheckBox7.isSelected()) {
            exportparts = exportparts | Constants.EXPORT_REMARKS;
        }
        if (jCheckBox8.isSelected()) {
            exportparts = exportparts | Constants.EXPORT_TIMESTAMP;
        }
        if (jCheckBox9.isSelected()) {
            exportparts = exportparts | Constants.EXPORT_MANLINKS;
        }
        if (jCheckBox10.isSelected()) {
            exportparts = exportparts | Constants.EXPORT_LUHMANN;
        }
        // save settings
        settingsObj.setExportParts(exportparts);
        settingsObj.setExportFormat(jComboBox_exportType.getSelectedIndex());
        // Close Window
        retval = 1;
        setVisible(false);
        dispose();
    }

    @Action
    public void cancel() {
        // Close Window
        retval = -1;
        setVisible(false);
        dispose();
    }

    /**
     * Retrieves the export-type, i.e. in which data format the entries should be exported.<br>
     * Use following constants:<br>
     * - CConstants.EXP_TYPE_PDF<br>
     * - CConstants.EXP_TYPE_RTF<br>
     * - CConstants.EXP_TYPE_XML<br>
     * - CConstants.EXP_TYPE_CSV<br>
     * - CConstants.EXP_TYPE_HTML<br>
     * - CConstants.EXP_TYPE_TXT<br>
     * - CConstants.EXP_TYPE_TEX<br>
     * - CConstants.EXP_TYPE_ZKN3<br>
     *
     * @return The export type, as integer constant.
     */
    public int getExportType() {
        return exportType;
    }

    /**
     * This variable indicates whether the author- and keyword-file should be exported as separate
     * files (just like the typical storage-system we use) or whether the author- and
     * keyword-index-numbers should be replaced with the related string-values, so the author- and
     * keyword-information are all in one file
     *
     * @return {@code true} if author- and keyword-file shoud be included in the export-file,
     * {@code false} if they should be exported as separate files.
     */
    public boolean isSeparateFileForNotes() {
        return separateFileForNotes;
    }

    /**
     *
     * @return
     */
    public boolean hasTitlePrefix() {
        return titlePrefix;
    }

    /**
     * Indicates wheher the UBB-Fomattags should be removed and the entries should be exported in
     * plain text, without format-tags.
     *
     * @return
     */
    public boolean getFormatTagsRemoved() {
        return removeformattags;
    }

    /**
     *
     * @return
     */
    public boolean getKeywordsHighlighted() {
        return highlightkeywords;
    }

    /**
     *
     * @return
     */
    public char getCSVSeparator() {
        return csvseparator;
    }

    /**
     *
     * @return
     */
    public boolean getExportBibTex() {
        return exportbibtex;
    }

    /**
     *
     * @return
     */
    public File getFilePath() {
        return filepath;
    }

    /**
     *
     * @return
     */
    public int getExportParts() {
        return exportparts;
    }

    /**
     * return value which indicates whether the dialog was closed correcty or if a the action was
     * cancelled.
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
        jCheckBoxSeparateFile = new javax.swing.JCheckBox();
        jLabelBrowseDir = new javax.swing.JLabel();
        jButtonBrowse = new javax.swing.JButton();
        jTextFieldFilepath = new javax.swing.JTextField();
        jButtonApply = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jCheckBox4 = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jCheckBox6 = new javax.swing.JCheckBox();
        jCheckBox7 = new javax.swing.JCheckBox();
        jCheckBox8 = new javax.swing.JCheckBox();
        jPanel5 = new javax.swing.JPanel();
        jCheckBox5 = new javax.swing.JCheckBox();
        jCheckBox10 = new javax.swing.JCheckBox();
        jCheckBox9 = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        jCheckBoxHighlightKeywords = new javax.swing.JCheckBox();
        jCheckBoxSetTitleNumber = new javax.swing.JCheckBox();
        jCheckBoxRemoveFormatTags = new javax.swing.JCheckBox();
        jLabelCSVSeparator = new javax.swing.JLabel();
        jComboBoxCSVSeparator = new javax.swing.JComboBox();
        jCheckBoxExportBibTex = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getResourceMap(CExport.class);
        setTitle(resourceMap.getString("FormExportDialog.title")); // NOI18N
        setModal(true);
        setName("FormExportDialog"); // NOI18N
        setResizable(false);

        jPanel1.setName("jPanel1"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jComboBox_exportType.setName("jComboBox_exportType"); // NOI18N

        jCheckBoxSeparateFile.setText(resourceMap.getString("jCheckBoxSeparateFile.text")); // NOI18N
        jCheckBoxSeparateFile.setToolTipText(resourceMap.getString("jCheckBoxSeparateFile.toolTipText")); // NOI18N
        jCheckBoxSeparateFile.setName("jCheckBoxSeparateFile"); // NOI18N

        jLabelBrowseDir.setText(resourceMap.getString("jLabelBrowseDir.text")); // NOI18N
        jLabelBrowseDir.setName("jLabelBrowseDir"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getActionMap(CExport.class, this);
        jButtonBrowse.setAction(actionMap.get("save")); // NOI18N
        jButtonBrowse.setName("jButtonBrowse"); // NOI18N

        jTextFieldFilepath.setEditable(false);
        jTextFieldFilepath.setText(resourceMap.getString("jTextFieldFilepath.text")); // NOI18N
        jTextFieldFilepath.setName("jTextFieldFilepath"); // NOI18N

        jButtonApply.setAction(actionMap.get("startExport")); // NOI18N
        jButtonApply.setName("jButtonApply"); // NOI18N

        jButtonCancel.setAction(actionMap.get("cancel")); // NOI18N
        jButtonCancel.setName("jButtonCancel"); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        jPanel3.setName("jPanel3"); // NOI18N

        jCheckBox2.setText(resourceMap.getString("jCheckBox2.text")); // NOI18N
        jCheckBox2.setName("jCheckBox2"); // NOI18N

        jCheckBox3.setText(resourceMap.getString("jCheckBox3.text")); // NOI18N
        jCheckBox3.setName("jCheckBox3"); // NOI18N

        jCheckBox4.setText(resourceMap.getString("jCheckBox4.text")); // NOI18N
        jCheckBox4.setName("jCheckBox4"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jCheckBox2)
            .addComponent(jCheckBox3)
            .addComponent(jCheckBox4)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jCheckBox2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox4)
                .addContainerGap())
        );

        jPanel4.setName("jPanel4"); // NOI18N

        jCheckBox6.setText(resourceMap.getString("jCheckBox6.text")); // NOI18N
        jCheckBox6.setName("jCheckBox6"); // NOI18N

        jCheckBox7.setText(resourceMap.getString("jCheckBox7.text")); // NOI18N
        jCheckBox7.setName("jCheckBox7"); // NOI18N

        jCheckBox8.setText(resourceMap.getString("jCheckBox8.text")); // NOI18N
        jCheckBox8.setName("jCheckBox8"); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jCheckBox6)
            .addComponent(jCheckBox8)
            .addComponent(jCheckBox7)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jCheckBox6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox8)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setName("jPanel5"); // NOI18N

        jCheckBox5.setText(resourceMap.getString("jCheckBox5.text")); // NOI18N
        jCheckBox5.setName("jCheckBox5"); // NOI18N

        jCheckBox10.setText(resourceMap.getString("jCheckBox10.text")); // NOI18N
        jCheckBox10.setName("jCheckBox10"); // NOI18N

        jCheckBox9.setText(resourceMap.getString("jCheckBox9.text")); // NOI18N
        jCheckBox9.setName("jCheckBox9"); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox5)
                    .addComponent(jCheckBox10)
                    .addComponent(jCheckBox9))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jCheckBox5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox9)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSeparator1.setName("jSeparator1"); // NOI18N

        jCheckBoxHighlightKeywords.setText(resourceMap.getString("jCheckBoxHighlightKeywords.text")); // NOI18N
        jCheckBoxHighlightKeywords.setToolTipText(resourceMap.getString("jCheckBoxHighlightKeywords.toolTipText")); // NOI18N
        jCheckBoxHighlightKeywords.setName("jCheckBoxHighlightKeywords"); // NOI18N

        jCheckBoxSetTitleNumber.setText(resourceMap.getString("jCheckBoxSetTitleNumber.text")); // NOI18N
        jCheckBoxSetTitleNumber.setToolTipText(resourceMap.getString("jCheckBoxSetTitleNumber.toolTipText")); // NOI18N
        jCheckBoxSetTitleNumber.setName("jCheckBoxSetTitleNumber"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jSeparator1)
                    .addComponent(jCheckBoxHighlightKeywords)
                    .addComponent(jCheckBoxSetTitleNumber))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxHighlightKeywords)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxSetTitleNumber)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jCheckBoxRemoveFormatTags.setText(resourceMap.getString("jCheckBoxRemoveFormatTags.text")); // NOI18N
        jCheckBoxRemoveFormatTags.setToolTipText(resourceMap.getString("jCheckBoxRemoveFormatTags.toolTipText")); // NOI18N
        jCheckBoxRemoveFormatTags.setName("jCheckBoxRemoveFormatTags"); // NOI18N

        jLabelCSVSeparator.setText(resourceMap.getString("jLabelCSVSeparator.text")); // NOI18N
        jLabelCSVSeparator.setToolTipText(resourceMap.getString("jLabelCSVSeparator.toolTipText")); // NOI18N
        jLabelCSVSeparator.setName("jLabelCSVSeparator"); // NOI18N

        jComboBoxCSVSeparator.setModel(new javax.swing.DefaultComboBoxModel(new String[] { ",", ";", "Tabulator" }));
        jComboBoxCSVSeparator.setName("jComboBoxCSVSeparator"); // NOI18N

        jCheckBoxExportBibTex.setText(resourceMap.getString("jCheckBoxExportBibTex.text")); // NOI18N
        jCheckBoxExportBibTex.setToolTipText(resourceMap.getString("jCheckBoxExportBibTex.toolTipText")); // NOI18N
        jCheckBoxExportBibTex.setName("jCheckBoxExportBibTex"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jCheckBoxSeparateFile)
                    .addComponent(jCheckBoxRemoveFormatTags)
                    .addComponent(jComboBox_exportType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jCheckBoxExportBibTex)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(jLabelCSVSeparator)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jComboBoxCSVSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jLabelBrowseDir)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(jButtonBrowse)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jTextFieldFilepath))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jButtonCancel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButtonApply))
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox_exportType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxSeparateFile)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxRemoveFormatTags)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxExportBibTex)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelCSVSeparator)
                    .addComponent(jComboBoxCSVSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabelBrowseDir)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonBrowse)
                    .addComponent(jTextFieldFilepath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonApply)
                    .addComponent(jButtonCancel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonApply;
    private javax.swing.JButton jButtonBrowse;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JCheckBox jCheckBox10;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JCheckBox jCheckBox5;
    private javax.swing.JCheckBox jCheckBox6;
    private javax.swing.JCheckBox jCheckBox7;
    private javax.swing.JCheckBox jCheckBox8;
    private javax.swing.JCheckBox jCheckBox9;
    private javax.swing.JCheckBox jCheckBoxExportBibTex;
    private javax.swing.JCheckBox jCheckBoxHighlightKeywords;
    private javax.swing.JCheckBox jCheckBoxRemoveFormatTags;
    private javax.swing.JCheckBox jCheckBoxSeparateFile;
    private javax.swing.JCheckBox jCheckBoxSetTitleNumber;
    private javax.swing.JComboBox jComboBoxCSVSeparator;
    private javax.swing.JComboBox jComboBox_exportType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelBrowseDir;
    private javax.swing.JLabel jLabelCSVSeparator;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField jTextFieldFilepath;
    // End of variables declaration//GEN-END:variables

}
