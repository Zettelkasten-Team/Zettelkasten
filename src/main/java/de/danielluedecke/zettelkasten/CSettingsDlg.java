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

import at.jta.Key;
import at.jta.NotSupportedOSException;
import at.jta.RegistryErrorException;
import at.jta.Regor;
import de.danielluedecke.zettelkasten.database.AutoKorrektur;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.database.StenoData;
import de.danielluedecke.zettelkasten.database.Synonyms;
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.util.HtmlUbbUtil;
import de.danielluedecke.zettelkasten.util.PlatformUtil;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.SystemTray;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.application.Action;

/**
 *
 * @author danielludecke
 */
public class CSettingsDlg extends javax.swing.JDialog {

    /**
     * Reference to the settings-class
     */
    private final Settings settings;
    /**
     *
     */
    private final Daten dataObj;
    /**
     * A reference to the auto-correction class
     */
    private final AutoKorrektur autokorrekt;
    /**
     * A reference to the steno-data class
     */
    private final StenoData stenoObj;
    /**
     *
     */
    private final Synonyms synonyms;
    /**
     * Used to retrieve all installed look'n'feels...
     */
    private UIManager.LookAndFeelInfo installed_laf[];
    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap
            = org.jdesktop.application.Application.getInstance(ZettelkastenApp.class).
            getContext().getResourceMap(CSettingsDlg.class);
    /**
     * The different fonts, used to retrieve the font settings and for the
     * font-chooser-dialog
     */
    private Font mainfont;
    private Font quotefont;
    private Font entryheaderfont;
    private Font authorfont;
    private Font remarksfont;
    private Font titlefont;
    private Font headerfont1;
    private Font headerfont2;
    private Font desktopheaderfont;
    private Font desktopcommentfont;
    private Font desktopitemheaderfont;
    private Font desktopitemfont;
    private Font tablefont;
    private Font desktopfont;
    private Font codefont;
    private Font appendixheaderfont;
    /**
     * The different font-colors, used to retrieve the font colors and for the
     * color-chooser-dialog
     */
    private String mainfontcolor;
    private String codefontcolor;
    private String quotefontcolor;
    private String entryheaderfontcolor;
    private String authorfontcolor;
    private String remarksfontcolor;
    private String titlefontcolor;
    private String appendixheaderfontcolor;
    private String headerfont1color;
    private String headerfont2color;
    private String desktopheaderfontcolor;
    private String desktopcommentfontcolor;
    private String desktopitemheaderfontcolor;
    private String desktopitemfontcolor;
    private String tableheadercolor;
    private String tablerowevencolor;
    private String tablerowoddcolor;
    private String linkscolor;
    private String manlinkscolor;
    private String fnlinkscolor;
    private String entryheadingscolor;
    private String reflistbgcolor;
    private String quotecolor;
    private String mainbgcolor;
    private String contentbgcolor;
    /**
     * Stores the current selection of the look'n'feel combobox
     */
    private int lafselection;
    /**
     * return value for the main window so we know whether we have to update the
     * display
     */
    private boolean needsupdate = false;
    /**
     * return value for the main window so we know whether we have to update the
     * display
     */
    private boolean displayupdate = false;
    /**
     * return value for the main window so we know whether we have to update the
     * laf
     */
    private boolean lafupdate = false;
    /**
     *
     */
    private boolean savesettingok = true;
    /**
     *
     */
    private boolean registryChanges = false;
    /**
     *
     */
    private boolean userPathChanges = false;
    private boolean pandocPathChanges = false;

    /**
     *
     * @param parent
     * @param s
     * @param d
     * @param ac
     * @param sy
     * @param stn
     */
    public CSettingsDlg(java.awt.Frame parent, Settings s, Daten d, AutoKorrektur ac, Synonyms sy, StenoData stn) {
        super(parent);
        // set application icon
        setIconImage(Constants.zknicon.getImage());
        // get reference to settings-class
        settings = s;
        dataObj = d;
        autokorrekt = ac;
        synonyms = sy;
        stenoObj = stn;
        initComponents();
        // make extra style for combo-boxes
        if (settings.isSeaGlass()) {
            jButtonApply.putClientProperty("JComponent.sizeVariant", "small");
            jButtonBrowseAttachmentPath.putClientProperty("JComponent.sizeVariant", "small");
            jButtonBrowseBackup.putClientProperty("JComponent.sizeVariant", "small");
            jButtonBrowseImagePath.putClientProperty("JComponent.sizeVariant", "small");
            jButtonBrowsePandoc.putClientProperty("JComponent.sizeVariant", "small");
            jButtonCancel.putClientProperty("JComponent.sizeVariant", "small");
            jButtonDesktopCSS.putClientProperty("JComponent.sizeVariant", "small");
            jButtonEditAutokorrekt.putClientProperty("JComponent.sizeVariant", "small");
            jButtonEditSteno.putClientProperty("JComponent.sizeVariant", "small");
            jButtonEntryCss.putClientProperty("JComponent.sizeVariant", "small");
            jButtonHighlightKeywordStyle.putClientProperty("JComponent.sizeVariant", "small");
            jButtonHighlightLivesearchStyle.putClientProperty("JComponent.sizeVariant", "small");
            jButtonHighlightStyle.putClientProperty("JComponent.sizeVariant", "small");
            jButtonListFont.putClientProperty("JComponent.sizeVariant", "small");
            jButtonDesktopFont.putClientProperty("JComponent.sizeVariant", "small");
            jButtonResetDesktopCSS.putClientProperty("JComponent.sizeVariant", "small");
            jButtonResetEntryCSS.putClientProperty("JComponent.sizeVariant", "small");
            jButtonSynonymEdit.putClientProperty("JComponent.sizeVariant", "small");
        }
        if (!SystemTray.isSupported() || PlatformUtil.isMacOS()) {
            jCheckBoxSystray.setText(jCheckBoxSystray.getText() + resourceMap.getString("systrayNotSupported"));
            jCheckBoxSystray.setEnabled(false);
            jCheckBoxSystray.setSelected(false);
        } else {
            jCheckBoxSystray.setSelected(settings.getMinimizeToTray());
        }

        // en- and disable checkboxes depending on the os
        //TODO: Disabled until #198 is fixed.
        // jCheckBoxRegistry.setEnabled(System.getProperty("os.name").toLowerCase().startsWith("windows"));
        jCheckBoxRegistry.setEnabled(false);

        // init mainfont
        mainfont = settings.getMainFont();
        mainfontcolor = settings.getMainfont(Settings.FONTCOLOR);
        // init code font
        codefont = settings.getCodeFont();
        codefontcolor = settings.getCodeFont(Settings.FONTCOLOR);
        // init quote
        quotefont = settings.getQuoteFont();
        quotefontcolor = settings.getQuoteFont(Settings.FONTCOLOR);
        // init entryheaderfont
        entryheaderfont = settings.getEntryHeaderFont();
        entryheaderfontcolor = settings.getEntryHeaderFont(Settings.FONTCOLOR);
        // init authorfont
        authorfont = settings.getAuthorFont();
        authorfontcolor = settings.getAuthorFont(Settings.FONTCOLOR);
        // init remarksfont
        remarksfont = settings.getRemarksFont();
        remarksfontcolor = settings.getRemarksFont(Settings.FONTCOLOR);
        // init titlefont
        titlefont = settings.getTitleFont();
        titlefontcolor = settings.getTitleFont(Settings.FONTCOLOR);
        // init appendixheaderfont
        appendixheaderfont = settings.getAppendixHeaderFont();
        appendixheaderfontcolor = settings.getAppendixHeaderFont(Settings.FONTCOLOR);
        // init header1-font
        headerfont1 = settings.getHeaderFont1();
        headerfont1color = settings.getHeaderfont1(Settings.FONTCOLOR);
        // init header2-font
        headerfont2 = settings.getHeaderFont2();
        headerfont2color = settings.getHeaderfont2(Settings.FONTCOLOR);
        // init desktopheader-font
        desktopheaderfont = settings.getDesktopHeaderFont();
        desktopheaderfontcolor = settings.getDesktopHeaderfont(Settings.FONTCOLOR);
        // init desktopcomment-font
        desktopcommentfont = settings.getDesktopCommentFont();
        desktopcommentfontcolor = settings.getDesktopCommentfont(Settings.FONTCOLOR);
        // init desktopitemheader-font
        desktopitemheaderfont = settings.getDesktopItemHeaderFont();
        desktopitemheaderfontcolor = settings.getDesktopItemHeaderfont(Settings.FONTCOLOR);
        // init desktopitem-font
        desktopitemfont = settings.getDesktopItemFont();
        desktopitemfontcolor = settings.getDesktopItemfont(Settings.FONTCOLOR);
        // init listviewfont
        tablefont = settings.getTableFont();
        desktopfont = settings.getDesktopOutlineFont();
        // get bg colors
        tableheadercolor = settings.getTableHeaderColor();
        tablerowevencolor = settings.getTableRowEvenColor();
        tablerowoddcolor = settings.getTableRowOddColor();
        linkscolor = settings.getLinkColor();
        manlinkscolor = settings.getManlinkColor();
        fnlinkscolor = settings.getFootnoteLinkColor();
        entryheadingscolor = settings.getEntryHeadingBackgroundColor();
        reflistbgcolor = settings.getAppendixBackgroundColor();
        quotecolor = settings.getQuoteBackgroundColor();
        mainbgcolor = settings.getMainBackgroundColor();
        contentbgcolor = settings.getContentBackgroundColor();
        // init checkboxex
        jCheckBoxEntryCSS.setSelected(settings.getUseCustomCSS(Settings.CUSTOM_CSS_ENTRY));
        jCheckBoxDesktopCSS.setSelected(settings.getUseCustomCSS(Settings.CUSTOM_CSS_DESKTOP));
        jCheckBoxShowHorGrid.setSelected(settings.getShowGridHorizontal());
        jCheckBoxShowVerGrid.setSelected(settings.getShowGridVertical());
        jCheckBoxAutocorrect.setSelected(settings.getSpellCorrect());
        jCheckBoxSteno.setSelected(settings.getStenoActivated());
        jCheckBoxImgResize.setSelected(settings.getImageResize());
        jCheckBoxUseMacBackgroundColor.setSelected(settings.getUseMacBackgroundColor());
        jCheckBoxAutobackup.setSelected(settings.getAutoBackup());
        jCheckBoxFootnote.setSelected(settings.getSupFootnote());
        jCheckBoxJumpToTab.setSelected(settings.getJumpFootnote());
        jCheckBoxSynonym.setSelected(settings.getSearchAlwaysSynonyms());
        jCheckBoxFillNewEntries.setSelected(settings.getInsertNewEntryAtEmpty());
        jCheckBoxExtraBackup.setSelected(settings.getExtraBackup());
        jCheckBoxAllToHist.setSelected(settings.getAddAllToHistory());
        jTextFieldBackupPath.setText((settings.getExtraBackupPath() != null) ? settings.getExtraBackupPath().toString() : "");
        jTextFieldBackupPath.setEnabled(settings.getExtraBackup());
        jTextFieldPandoc.setText(settings.getPandocPath());
        jButtonBrowseBackup.setEnabled(settings.getExtraBackup());
        jCheckBoxIconText.setSelected(settings.getShowIconText());
        jCheckBoxShowToolbar.setSelected(settings.getShowIcons());
        jCheckBoxShowAllIcons.setSelected(settings.getShowAllIcons());
        jCheckBoxAutoUpdate.setSelected(settings.getAutoUpdate());
        jCheckBoxCheckNightly.setSelected(settings.getAutoNightlyUpdate());
        jCheckBoxUseXDGOpen.setSelected(settings.getUseXDGOpen());
        jCheckBoxAutoCompleteTags.setSelected(settings.getAutoCompleteTags());
        jCheckBoxUseMarkdown.setSelected(settings.getMarkdownActivated());
        jCheckBoxShowTableBorder.setSelected(settings.getShowTableBorder());
        jCheckBoxShowEntryHeadline.setSelected(settings.getShowEntryHeadline());
        jCheckBoxFootnoteBraces.setSelected(settings.getFootnoteBraces());
        jCheckBoxSearchWithoutFormatTags.setSelected(!settings.getSearchRemovesFormatTags());
        jCheckBoxLuhmannColSortable.setSelected(settings.getMakeLuhmannColumnSortable());
        // get user attachment and image paths
        File attpath = dataObj.getUserAttachmentPath();
        if (attpath != null) {
            jTextFieldAttachmentPath.setText(attpath.toString());
        }
        File imgpath = dataObj.getUserImagePath();
        if (imgpath != null) {
            jTextFieldImagePath.setText(imgpath.toString());
        }
        // init formatted textfields with resize-preferences
        jFormattedTextFieldImgWidth.setValue(settings.getImageResizeWidth());
        jFormattedTextFieldImgHeight.setValue(settings.getImageResizeHeight());
        jCheckBoxRegistry.setSelected(initRegCheckBox());
        // get value for cell spacing
        Dimension cellspacing = settings.getCellSpacing();
        jSpinnerDistHor.setValue(cellspacing.width);
        jSpinnerDistVer.setValue(cellspacing.height);
        // init the slider for the default table and list fontsize
        jSliderFontSize.setValue(settings.getTableFontSize());
        jSliderDesktopFontSize.setValue(settings.getDesktopOutlineFontSize());
        jSliderTextfields.setValue(settings.getTextfieldFontSize());
        // set language setting
        String lang = settings.getLanguage();
        try {
            if (lang.equalsIgnoreCase("de")) {
                jComboBoxLocale.setSelectedIndex(1);
            } else if (lang.equalsIgnoreCase("es")) {
                jComboBoxLocale.setSelectedIndex(2);
            } else {
                jComboBoxLocale.setSelectedIndex(0);
            }
        } catch (IllegalArgumentException e) {
            // log error
            Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
        }
        // set all look'n'feels to the combobox
        initComboboxLAF();
        // set all font-settings to next combo-box
        initComboboxFonts();
        // init backgrpound colorts
        initComboBoxBackgroundColors();
        // init combobox with manual timestamp-values
        initComboboxManualTimestamp();
        // init icon themes
        initComboBoxIconThemes();
        // init startup cb
        initComboboxStartup();
        // init listener
        initListeners();
        setModified(false);
        lafupdate = false;
    }

    private void initListeners() {
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
        // init selection
        jComboBoxShowAtStartup.setSelectedIndex(settings.getShowAtStartup());
        // add listener for combobox-showatstartup
        jComboBoxShowAtStartup.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                // whenever the user changes something, set "modifedLook" state to true
                // so the apply-button becomes enabled (this variable is connected to
                // the button's action)
                setModified(true);
            }
        });
        // add listener for combobox-showatstartup
        jComboBoxIconTheme.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                // whenever the user changes something, set "modifedLook" state to true
                // so the apply-button becomes enabled (this variable is connected to
                // the button's action)
                setModified(true);
                needsupdate = true;
            }
        });
        // add listener for combobox-locale
        jComboBoxLocale.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                // whenever the user changes something, set "modifedLook" state to true
                // so the apply-button becomes enabled (this variable is connected to
                // the button's action)
                setModified(true);
                lafupdate = true;
            }
        });
        // add action listener to combo box
        jComboBoxLAF.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                // whenever the user changes something, set "modifedLook" state to true
                // so the apply-button becomes enabled (this variable is connected to
                // the button's action)
                setModified(true);
                lafupdate = true;
            }
        });
        // add listener for combobox-showatstartup
        jComboBoxManualTimestamp.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                // whenever the user changes something, set "modifedLook" state to true
                // so the apply-button becomes enabled (this variable is connected to
                // the button's action)
                setModified(true);
            }
        });
        // add action listener to combo box
        jComboBoxFonts.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                // whenever the user changes something, set "modifedLook" state to true
                // so the apply-button becomes enabled (this variable is connected to
                // the button's action)
                jLabelColor.setBackground(getFontColor());
            }
        });
        jComboBoxFonts.setSelectedIndex(0);
        // add action listener to combo box
        jComboBoxBackgroundColors.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                // whenever the user changes something, set "modifedLook" state to true
                // so the apply-button becomes enabled (this variable is connected to
                // the button's action)
                jLabelTableColor.setBackground(getBackgroundColor());
            }
        });
        jComboBoxBackgroundColors.setSelectedIndex(0);
        jTextFieldAttachmentPath.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                checkPath(jTextFieldAttachmentPath);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                checkPath(jTextFieldAttachmentPath);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkPath(jTextFieldAttachmentPath);
            }
        });
        jTextFieldImagePath.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                checkPath(jTextFieldImagePath);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                checkPath(jTextFieldImagePath);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkPath(jTextFieldImagePath);
            }
        });
        jTextFieldPandoc.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                pandocPathChanges = true;
                setModified(true);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                pandocPathChanges = true;
                setModified(true);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                pandocPathChanges = true;
                setModified(true);
            }
        });
        jCheckBoxAutobackup.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setModified(true);
            }
        });
        jCheckBoxShowTableBorder.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setModified(true);
                displayupdate = true;
            }
        });
        jCheckBoxAutocorrect.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setModified(true);
            }
        });
        jCheckBoxSteno.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setModified(true);
            }
        });
        jCheckBoxShowEntryHeadline.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setModified(true);
                displayupdate = true;
            }
        });
        jCheckBoxSynonym.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setModified(true);
            }
        });
        jCheckBoxSystray.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setModified(true);
            }
        });
        jCheckBoxFillNewEntries.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setModified(true);
            }
        });
        jCheckBoxAutoUpdate.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setModified(true);
                jCheckBoxCheckNightly.setEnabled(jCheckBoxAutoUpdate.isSelected());
            }
        });
        jCheckBoxCheckNightly.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setModified(true);
            }
        });
        jCheckBoxJumpToTab.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setModified(true);
            }
        });
        jCheckBoxUseMarkdown.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayupdate = true;
                setModified(true);
            }
        });
        jCheckBoxUseMacBackgroundColor.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayupdate = true;
                setModified(true);
            }
        });
        jCheckBoxShowHorGrid.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setModified(true);
            }
        });
        jCheckBoxShowVerGrid.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setModified(true);
            }
        });
        jCheckBoxAllToHist.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setModified(true);
            }
        });
        jSpinnerDistHor.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                needsupdate = true;
                setModified(true);
            }
        });
        jSpinnerDistVer.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                needsupdate = true;
                setModified(true);
            }
        });
        jSliderFontSize.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                needsupdate = true;
                setModified(true);
                lafupdate = true;
            }
        });
        jSliderDesktopFontSize.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                needsupdate = true;
                setModified(true);
                lafupdate = true;
            }
        });
        jFormattedTextFieldImgWidth.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                displayupdate = true;
                setModified(true);
            }
        });
        jFormattedTextFieldImgHeight.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                displayupdate = true;
                setModified(true);
            }
        });
        jCheckBoxFootnote.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayupdate = true;
                setModified(true);
            }
        });
        jCheckBoxFootnoteBraces.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setModified(true);
            }
        });
        jCheckBoxSearchWithoutFormatTags.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setModified(true);
            }
        });
        jCheckBoxLuhmannColSortable.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setModified(true);
                dataObj.setTitlelistUpToDate(false);
            }
        });
        jCheckBoxUseXDGOpen.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setModified(true);
            }
        });
        jCheckBoxAutoCompleteTags.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setModified(true);
            }
        });
        jCheckBoxIconText.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                needsupdate = true;
                setModified(true);
            }
        });
        jCheckBoxShowToolbar.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                needsupdate = true;
                setModified(true);
                jCheckBoxShowAllIcons.setEnabled(jCheckBoxShowToolbar.isSelected());
            }
        });
        jCheckBoxShowAllIcons.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                needsupdate = true;
                setModified(true);
            }
        });
        jCheckBoxEntryCSS.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lafupdate = true;
                setModified(true);
            }
        });
        jCheckBoxDesktopCSS.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lafupdate = true;
                setModified(true);
            }
        });
        jButtonEditAutokorrekt.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                // the button for editing the spellchecking-words was pressed,
                // so open the window for edting them...
                if (null == autoKorrektEdit) {
                    // get parent und init window
                    autoKorrektEdit = new CAutoKorrekturEdit(null, autokorrekt, settings);
                    // center window
                    autoKorrektEdit.setLocationRelativeTo(null);
                }
                ZettelkastenApp.getApplication().show(autoKorrektEdit);
                // change modified state and enable apply-button
                if (autoKorrektEdit.isModified()) {
                    setModified(true);
                }
                autoKorrektEdit.dispose();
                autoKorrektEdit = null;
            }
        });
        jButtonListFont.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                // get the selected font
                Font f = tablefont;
                // create font-chooser dialog
                if (null == fontDlg) {
                    fontDlg = new CFontChooser(null, f);
                    fontDlg.setLocationRelativeTo(null);
                }
                ZettelkastenApp.getApplication().show(fontDlg);
                // if the user has chosen a font, set it
                if (fontDlg.selectedFont != null) {
                    tablefont = fontDlg.selectedFont;
                    // whenever the user changes something, set "modifedLook" state to true
                    // so the apply-button becomes enabled (this variable is connected to
                    // the button's action)
                    setModified(true);
                    lafupdate = true;
                }
                // close and dispose the font-dialog
                fontDlg.dispose();
                fontDlg = null;
            }
        });
        jButtonDesktopFont.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                // get the selected font
                Font f = desktopfont;
                // create font-chooser dialog
                if (null == fontDlg) {
                    fontDlg = new CFontChooser(null, f);
                    fontDlg.setLocationRelativeTo(null);
                }
                ZettelkastenApp.getApplication().show(fontDlg);
                // if the user has chosen a font, set it
                if (fontDlg.selectedFont != null) {
                    desktopfont = fontDlg.selectedFont;
                    // whenever the user changes something, set "modifedLook" state to true
                    // so the apply-button becomes enabled (this variable is connected to
                    // the button's action)
                    setModified(true);
                    lafupdate = true;
                }
                // close and dispose the font-dialog
                fontDlg.dispose();
                fontDlg = null;
            }
        });
        jButtonBrowseBackup.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                JFileChooser fc = new JFileChooser();
                // set dialog's title
                fc.setDialogTitle(resourceMap.getString("fileChooserTitle"));
                // restrict all files as choosable
                fc.setAcceptAllFileFilterUsed(false);
                // only directories should be selected
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int option = fc.showOpenDialog(null);
                // if a file was chosen, set the filepath
                if (JFileChooser.APPROVE_OPTION == option) {
                    // get the filepath...
                    jTextFieldBackupPath.setText(fc.getSelectedFile().toString());
                    setModified(true);
                }
            }
        });
        jButtonBrowseAttachmentPath.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                JFileChooser fc = new JFileChooser();
                // set dialog's title
                fc.setDialogTitle(resourceMap.getString("fileChooserTitle"));
                // restrict all files as choosable
                fc.setAcceptAllFileFilterUsed(false);
                // only directories should be selected
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int option = fc.showOpenDialog(null);
                // if a file was chosen, set the filepath
                if (JFileChooser.APPROVE_OPTION == option) {
                    // get the filepath...
                    jTextFieldAttachmentPath.setText(fc.getSelectedFile().toString());
                    setModified(true);
                }
            }
        });
        jButtonBrowsePandoc.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                JFileChooser fc = new JFileChooser();
                // set dialog's title
                fc.setDialogTitle(resourceMap.getString("fileChooserTitle"));
                // restrict all files as choosable
                fc.setAcceptAllFileFilterUsed(false);
                // only directories should be selected
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int option = fc.showOpenDialog(null);
                // if a file was chosen, set the filepath
                if (JFileChooser.APPROVE_OPTION == option) {
                    // get the filepath...
                    jTextFieldPandoc.setText(fc.getSelectedFile().toString());
                    setModified(true);
                }
            }
        });
        jButtonBrowseImagePath.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                JFileChooser fc = new JFileChooser();
                // set dialog's title
                fc.setDialogTitle(resourceMap.getString("fileChooserTitle"));
                // restrict all files as choosable
                fc.setAcceptAllFileFilterUsed(false);
                // only directories should be selected
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int option = fc.showOpenDialog(null);
                // if a file was chosen, set the filepath
                if (JFileChooser.APPROVE_OPTION == option) {
                    // get the filepath...
                    jTextFieldImagePath.setText(fc.getSelectedFile().toString());
                    setModified(true);
                }
            }
        });
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                cancelWindow();
            }
        });
        jButtonEntryCss.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                editCSS(Settings.CUSTOM_CSS_ENTRY);
            }
        });
        jButtonDesktopCSS.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                editCSS(Settings.CUSTOM_CSS_DESKTOP);
            }
        });
        jButtonResetEntryCSS.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                resetCSS(Settings.CUSTOM_CSS_ENTRY);
            }
        });
        jButtonResetDesktopCSS.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                resetCSS(Settings.CUSTOM_CSS_DESKTOP);
            }
        });
        jCheckBoxImgResize.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                setModified(true);
                displayupdate = true;
                jFormattedTextFieldImgWidth.setEnabled(jCheckBoxImgResize.isSelected());
                jFormattedTextFieldImgHeight.setEnabled(jCheckBoxImgResize.isSelected());
            }
        });
        jCheckBoxExtraBackup.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                jTextFieldBackupPath.setEnabled(jCheckBoxExtraBackup.isSelected());
                jButtonBrowseBackup.setEnabled(jCheckBoxExtraBackup.isSelected());
                setModified(true);
            }
        });
        jCheckBoxRegistry.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                setModified(true);
                registryChanges = true;
            }
        });
        jButtonEditSteno.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                // the button for editing the spellchecking-words was pressed,
                // so open the window for edting them...
                if (null == stenoEdit) {
                    // get parent und init window
                    stenoEdit = new CStenoEdit(null, stenoObj, settings);
                    // center window
                    stenoEdit.setLocationRelativeTo(null);
                }
                ZettelkastenApp.getApplication().show(stenoEdit);
                // change modified state and enable apply-button
                if (stenoEdit.isModified()) {
                    setModified(true);
                }
                stenoEdit.dispose();
                stenoEdit = null;
            }
        });
        jButtonHighlightStyle.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (null == highlightSettingsDlg) {
                    highlightSettingsDlg = new CHighlightSearchSettings(null, settings, HtmlUbbUtil.HIGHLIGHT_STYLE_SEARCHRESULTS);
                    highlightSettingsDlg.setLocationRelativeTo(null);
                }
                ZettelkastenApp.getApplication().show(highlightSettingsDlg);
                if (!highlightSettingsDlg.isCancelled()) {
                    setModified(true);
                    lafupdate = true;
                }
                highlightSettingsDlg.dispose();
                highlightSettingsDlg = null;
            }
        });
        jButtonHighlightKeywordStyle.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (null == highlightSettingsDlg) {
                    highlightSettingsDlg = new CHighlightSearchSettings(null, settings, HtmlUbbUtil.HIGHLIGHT_STYLE_KEYWORDS);
                    highlightSettingsDlg.setLocationRelativeTo(null);
                }
                ZettelkastenApp.getApplication().show(highlightSettingsDlg);
                if (!highlightSettingsDlg.isCancelled()) {
                    setModified(true);
                    lafupdate = true;
                }
                highlightSettingsDlg.dispose();
                highlightSettingsDlg = null;
            }
        });
        jButtonHighlightLivesearchStyle.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (null == highlightSettingsDlg) {
                    highlightSettingsDlg = new CHighlightSearchSettings(null, settings, HtmlUbbUtil.HIGHLIGHT_STYLE_LIVESEARCH);
                    highlightSettingsDlg.setLocationRelativeTo(null);
                }
                ZettelkastenApp.getApplication().show(highlightSettingsDlg);
                if (!highlightSettingsDlg.isCancelled()) {
                    setModified(true);
                    lafupdate = true;
                }
                highlightSettingsDlg.dispose();
                highlightSettingsDlg = null;
            }
        });
        jButtonSynonymEdit.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (null == synonymsDlg) {
                    synonymsDlg = new CSynonymsEdit(null, synonyms, settings, dataObj);
                    synonymsDlg.setLocationRelativeTo(null);
                }
                ZettelkastenApp.getApplication().show(synonymsDlg);
                // change modified state and enable apply-button
                if (synonymsDlg.isModified()) {
                    setModified(true);
                    setSynModified(true);
                }
                synonymsDlg.dispose();
                synonymsDlg = null;
            }
        });
    }

    /**
     * This method inits the combobox. all items are removed and then all
     * installed look'n'feels-classnames are addes to the combobox.
     */
    private void initComboboxLAF() {
        // clear combobox
        jComboBoxLAF.removeAllItems();
        // store initial selection
        lafselection = 0;
        // retrieve all available look'n'feels
        installed_laf = UIManager.getInstalledLookAndFeels();
        // and add them to the combobox
        for (int cnt = 0; cnt < installed_laf.length; cnt++) {
            jComboBoxLAF.addItem((installed_laf[cnt].getName()));
            if (installed_laf[cnt].getClassName().equals(settings.getLookAndFeel())) {
                lafselection = cnt;
            }
        }
        jComboBoxLAF.addItem(Constants.seaGlassLookAndFeelClassName);
        if (settings.isSeaGlass()) {
            lafselection = installed_laf.length;
        }
        // select the last active look and feel
        jComboBoxLAF.setSelectedIndex(lafselection);
    }

    /**
     * Initiates the combo-box that hold the values for the manual timestamps
     * that can be inserted when editing new entries (CNewEntry-dialog).
     */
    private void initComboboxManualTimestamp() {
        // remove all items from the combobox
        jComboBoxManualTimestamp.removeAllItems();
        // iterate constant-array that holds all date-format-strings
        for (String item : Constants.manualTimestamp) {
            // create a new dateformat out of that string
            DateFormat df = new SimpleDateFormat(item);
            // and convert it to a string-item
            String timestamp = df.format(new Date());
            // add it to combobox
            jComboBoxManualTimestamp.addItem(timestamp);
        }
        // select initial value
        jComboBoxManualTimestamp.setSelectedIndex(settings.getManualTimestamp());
    }

    private void initComboBoxIconThemes() {
        // clear combobox
        jComboBoxIconTheme.removeAllItems();
        // set themes
        for (String theme : Constants.iconThemesNames) {
            jComboBoxIconTheme.addItem(theme);
        }
        jComboBoxIconTheme.setSelectedIndex(settings.getIconTheme());
    }

    /**
     *
     * @param what Use one of
     * <ul>
     * <li>Settings.CUSTOM_CSS_ENTRY</li>
     * <li>Settings.CUSTOM_CSS_DESKTOP</li>
     * </ul>
     */
    private void editCSS(int what) {
        // get css
        String css = settings.getCustomCSS(what);
        switch (what) {
            case Settings.CUSTOM_CSS_ENTRY:
                if (null == css || css.isEmpty()) {
                    css = HtmlUbbUtil.getCommonStyleDefinition(settings, false, false, false);
                }
                break;
            case Settings.CUSTOM_CSS_DESKTOP:
                if (null == css || css.isEmpty()) {
                    css = HtmlUbbUtil.getCommonStyleDefinition(settings, true, false, false);
                }
                break;
            default:
                if (null == css || css.isEmpty()) {
                    css = HtmlUbbUtil.getCommonStyleDefinition(settings, false, false, false);
                }
                break;
        }
        // open an input-dialog, setting the selected value as default-value
        if (null == biggerEditDlg) {
            // create a new dialog with the bigger edit-field, passing some initial values
            biggerEditDlg = new CBiggerEditField(null, settings, resourceMap.getString("editCSS.text"), css, "", Constants.EDIT_STYLESHEET);
            // center window
            biggerEditDlg.setLocationRelativeTo(this);
        }
        // show window
        ZettelkastenApp.getApplication().show(biggerEditDlg);
        // after closing the window, get the new value
        String newCss = biggerEditDlg.getNewValue();
        // delete the input-dialog
        biggerEditDlg.dispose();
        biggerEditDlg = null;
        // check for valid value
        if (newCss != null && !newCss.isEmpty()) {
            // set custom style sheet
            settings.setCustomCSS(what, newCss);
            lafupdate = true;
            setModified(true);
        }
    }

    /**
     *
     * @param what Use one of
     * <ul>
     * <li>Settings.CUSTOM_CSS_ENTRY</li>
     * <li>Settings.CUSTOM_CSS_DESKTOP</li>
     * </ul>
     */
    private void resetCSS(int what) {
        // remember state
        boolean useCustomCSS = settings.getUseCustomCSS(what);
        // disable custom css, otherwise we would we retrieve just
        // that custom style when resetting it
        settings.setUseCustomCSS(what, false);
        // get css
        String css;
        switch (what) {
            case Settings.CUSTOM_CSS_ENTRY:
                css = HtmlUbbUtil.getCommonStyleDefinition(settings, false, false, false);
                break;
            case Settings.CUSTOM_CSS_DESKTOP:
                css = HtmlUbbUtil.getCommonStyleDefinition(settings, true, false, false);
                break;
            default:
                css = HtmlUbbUtil.getCommonStyleDefinition(settings, false, false, false);
                break;
        }
        // remember state
        settings.setUseCustomCSS(what, useCustomCSS);
        // set custom style sheet
        settings.setCustomCSS(what, css);
        lafupdate = true;
        setModified(true);
    }

    private void initComboBoxBackgroundColors() {
        jComboBoxBackgroundColors.removeAllItems();
        jComboBoxBackgroundColors.addItem(resourceMap.getString("bgCol1"));
        jComboBoxBackgroundColors.addItem(resourceMap.getString("bgCol2"));
        jComboBoxBackgroundColors.addItem(resourceMap.getString("bgCol3"));
        jComboBoxBackgroundColors.addItem(resourceMap.getString("bgCol4"));
        jComboBoxBackgroundColors.addItem(resourceMap.getString("bgCol5"));
        jComboBoxBackgroundColors.addItem(resourceMap.getString("bgCol6"));
        jComboBoxBackgroundColors.addItem(resourceMap.getString("bgCol7"));
        jComboBoxBackgroundColors.addItem(resourceMap.getString("bgCol8"));
        jComboBoxBackgroundColors.addItem(resourceMap.getString("bgCol9"));
        jComboBoxBackgroundColors.addItem(resourceMap.getString("bgCol10"));
        jComboBoxBackgroundColors.addItem(resourceMap.getString("bgCol11"));
        // make all items visible
        jComboBoxBackgroundColors.setMaximumRowCount(jComboBoxBackgroundColors.getItemCount());
    }

    /**
     * This method initiates the combobox with the font settings. here the user
     * can choose which type of font (main-font, font for authors, for lists,
     * for tables etc.) he or she wants to change.
     */
    private void initComboboxFonts() {
        // clear combobox
        jComboBoxFonts.removeAllItems();
        // add all font-items
        jComboBoxFonts.addItem(resourceMap.getString("mainfontText") + ": " + getFontDataForCombobox(mainfont));
        jComboBoxFonts.addItem(resourceMap.getString("authorfontText") + ": " + getFontDataForCombobox(authorfont));
        jComboBoxFonts.addItem(resourceMap.getString("remarksfontText") + ": " + getFontDataForCombobox(remarksfont));
        jComboBoxFonts.addItem(resourceMap.getString("titlefontText") + ": " + getFontDataForCombobox(titlefont));
        jComboBoxFonts.addItem(resourceMap.getString("headerfont1Text") + ": " + getFontDataForCombobox(headerfont1));
        jComboBoxFonts.addItem(resourceMap.getString("headerfont2Text") + ": " + getFontDataForCombobox(headerfont2));
        jComboBoxFonts.addItem(resourceMap.getString("desktopheaderfontText") + ": " + getFontDataForCombobox(desktopheaderfont));
        jComboBoxFonts.addItem(resourceMap.getString("desktopcommentfontText") + ": " + getFontDataForCombobox(desktopcommentfont));
        jComboBoxFonts.addItem(resourceMap.getString("desktopitemheaderfontText") + ": " + getFontDataForCombobox(desktopitemheaderfont));
        jComboBoxFonts.addItem(resourceMap.getString("desktopitemfontText") + ": " + getFontDataForCombobox(desktopitemfont));
        jComboBoxFonts.addItem(resourceMap.getString("quotefontText") + ": " + getFontDataForCombobox(quotefont));
        jComboBoxFonts.addItem(resourceMap.getString("entryheaderfontText") + ": " + getFontDataForCombobox(entryheaderfont));
        jComboBoxFonts.addItem(resourceMap.getString("appendixheaderfontText") + ": " + getFontDataForCombobox(appendixheaderfont));
        jComboBoxFonts.addItem(resourceMap.getString("codefontText") + ": " + getFontDataForCombobox(codefont));
        // make all items visible
        jComboBoxFonts.setMaximumRowCount(jComboBoxFonts.getItemCount());
    }

    private void initComboboxStartup() {
        jComboBoxShowAtStartup.removeAllItems();
        jComboBoxShowAtStartup.addItem(resourceMap.getString("cbStartup1.text"));
        jComboBoxShowAtStartup.addItem(resourceMap.getString("cbStartup2.text"));
        jComboBoxShowAtStartup.addItem(resourceMap.getString("cbStartup3.text"));
    }

    private void checkPath(javax.swing.JTextField tf) {
        // retrieve file path from textfield
        String fps = tf.getText();
        // check whether path exists
        if (!fps.isEmpty()) {
            // create file-variable
            File fp = new File(fps);
            // check for existence
            tf.setForeground((fp.exists()) ? Color.black : Color.red);
        } else {
            // indicate that path is OK
            tf.setForeground(Color.black);
        }
        // enable apply button
        setModified(true);
        // indicate that path has been changed
        userPathChanges = true;
    }

    private boolean initRegCheckBox() {
   /*    try {
            Regor winreg = new Regor();
            return (winreg.openKey(Regor.HKEY_CLASSES_ROOT, ".zkn3") != null && winreg.openKey(Regor.HKEY_CLASSES_ROOT, "zkn3_auto_file\\shell\\Open\\command") != null);
        } catch (RegistryErrorException e) {
            Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
        } catch (NotSupportedOSException e) {
            Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
        }*/
        return false;

    }

    private void registerFileExtension() {
      /*  try {
            Regor winreg = new Regor();
            if (jCheckBoxRegistry.isSelected()) {
                Key regkey = winreg.openKey(Regor.HKEY_CLASSES_ROOT, ".zkn3");
                if (null == regkey) {
                    regkey = winreg.createKey(Regor.HKEY_CLASSES_ROOT, ".zkn3");
                    winreg.closeKey(regkey);
                    regkey = winreg.openKey(Regor.HKEY_CLASSES_ROOT, ".zkn3");

                }
                if (regkey != null) {
                    winreg.saveValue(regkey, "", "zkn3_auto_file");
                    winreg.closeKey(regkey);
                    regkey = winreg.openKey(Regor.HKEY_CLASSES_ROOT, "zkn3_auto_file\\shell\\Open\\command");
                    if (null == regkey) {
                        regkey = winreg.createKey(Regor.HKEY_CLASSES_ROOT, "zkn3_auto_file\\shell\\Open\\command");
                        winreg.closeKey(regkey);
                        regkey = winreg.openKey(Regor.HKEY_CLASSES_ROOT, "zkn3_auto_file\\shell\\Open\\command");
                    }
                    if (regkey != null) {
                        winreg.saveValue(regkey, "", "\"" + System.getProperty("java.class.path") + "\" \"%1\"");
                        winreg.closeKey(regkey);
                    }
                }
            } else {
                winreg.delKey(Regor.HKEY_CLASSES_ROOT, ".zkn3");
                winreg.delKey(Regor.HKEY_CLASSES_ROOT, "zkn3_auto_file\\shell\\Open\\command");
            }
        } catch (RegistryErrorException e) {
            Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
            // show warning message box
            JOptionPane.showMessageDialog(null, resourceMap.getString("errorRegistryMsg"), resourceMap.getString("errorRegistryTitle"), JOptionPane.PLAIN_MESSAGE);
        } catch (NotSupportedOSException e) {
            Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
        }*/
    }

    /**
     * This method creates a string-description for the font-settings, which are
     * used for the font's combobox.
     *
     * @return a prepared string containing a description of the font-settings
     */
    private String getFontDataForCombobox(Font f) {
        StringBuilder item = new StringBuilder("");
        // first, set the font's name
        item.append(f.getFamily());
        // add additional information
        item.append(" (");
        // add the font-size
        item.append(f.getSize());
        item.append("px, ");

        // get the style-information
        switch (f.getStyle()) {
            case Font.PLAIN:
                item.append(resourceMap.getString("fontStylePlain"));
                break;
            case Font.BOLD:
                item.append(resourceMap.getString("fontStyleBold"));
                break;
            case Font.ITALIC:
                item.append(resourceMap.getString("fontStyleItalic"));
                break;
            case Font.BOLD + Font.ITALIC:
                item.append(resourceMap.getString("fontStyleBoldItalic"));
                break;
            default:
                item.append(resourceMap.getString("fontStylePlain"));
                break;
        }
        item.append(")");

        return item.toString();
    }

    /**
     * This methods gets the color-value of the selected font. while the
     * original value is stored as string, this method converts the hex-string
     * into a rgb-Color-value.
     *
     * @return the font-color, converted to Color-format
     */
    private Color getFontColor() {
        // init string
        String c;
        // get the color-value from the selectet font
        switch (jComboBoxFonts.getSelectedIndex()) {
            case 0:
                c = mainfontcolor;
                break;
            case 1:
                c = authorfontcolor;
                break;
            case 2:
                c = remarksfontcolor;
                break;
            case 3:
                c = titlefontcolor;
                break;
            case 4:
                c = headerfont1color;
                break;
            case 5:
                c = headerfont2color;
                break;
            case 6:
                c = desktopheaderfontcolor;
                break;
            case 7:
                c = desktopcommentfontcolor;
                break;
            case 8:
                c = desktopitemheaderfontcolor;
                break;
            case 9:
                c = desktopitemfontcolor;
                break;
            case 10:
                c = quotefontcolor;
                break;
            case 11:
                c = entryheaderfontcolor;
                break;
            case 12:
                c = appendixheaderfontcolor;
                break;
            case 13:
                c = codefontcolor;
                break;
            default:
                c = mainfontcolor;
                break;
        }
        // if we found a color value, go on...
        if (c != null) {
            return new Color(Integer.parseInt(c, 16));
        }

        return Color.BLACK;
    }

    /**
     * This methods gets the color-value of the selected font. while the
     * original value is stored as string, this method converts the hex-string
     * into a rgb-Color-value.
     *
     * @return the font-color, converted to Color-format
     */
    private Color getBackgroundColor() {
        // init string
        String c;
        // get the color-value from the selectet font
        switch (jComboBoxBackgroundColors.getSelectedIndex()) {
            case 0:
                c = tableheadercolor;
                break;
            case 1:
                c = tablerowevencolor;
                break;
            case 2:
                c = tablerowoddcolor;
                break;
            case 3:
                c = entryheadingscolor;
                break;
            case 4:
                c = quotecolor;
                break;
            case 5:
                c = reflistbgcolor;
                break;
            case 6:
                c = mainbgcolor;
                break;
            case 7:
                c = contentbgcolor;
                break;
            case 8:
                c = linkscolor;
                break;
            case 9:
                c = manlinkscolor;
                break;
            case 10:
                c = fnlinkscolor;
                break;
            default:
                c = tableheadercolor;
                break;
        }
        // if we found a color value, go on...
        if (c != null) {
            return new Color(Integer.parseInt(c, 16));
        }

        return Color.BLACK;
    }

    /**
     * Sets the font color for the chosen font
     *
     * @param c the new color value, received from the color-chooser and
     * converted to a hex-string
     */
    private void setFontColor(String c) {
        // get the color-value from the selectet font
        switch (jComboBoxFonts.getSelectedIndex()) {
            case 0:
                mainfontcolor = c;
                break;
            case 1:
                authorfontcolor = c;
                break;
            case 2:
                remarksfontcolor = c;
                break;
            case 3:
                titlefontcolor = c;
                break;
            case 4:
                headerfont1color = c;
                break;
            case 5:
                headerfont2color = c;
                break;
            case 6:
                desktopheaderfontcolor = c;
                break;
            case 7:
                desktopcommentfontcolor = c;
                break;
            case 8:
                desktopitemheaderfontcolor = c;
                break;
            case 9:
                desktopitemfontcolor = c;
                break;
            case 10:
                quotefontcolor = c;
                break;
            case 11:
                entryheaderfontcolor = c;
                break;
            case 12:
                appendixheaderfontcolor = c;
                break;
            case 13:
                codefontcolor = c;
                break;
            default:
                mainfontcolor = c;
                break;
        }
        setModified(true);
        displayupdate = true;
    }

    /**
     * Sets the font color for the chosen font
     *
     * @param c the new color value, received from the color-chooser and
     * converted to a hex-string
     */
    private void setBackgroundColor(String c) {
        // get the color-value from the selectet font
        switch (jComboBoxBackgroundColors.getSelectedIndex()) {
            case 0:
                tableheadercolor = c;
                break;
            case 1:
                tablerowevencolor = c;
                break;
            case 2:
                tablerowoddcolor = c;
                break;
            case 3:
                entryheadingscolor = c;
                break;
            case 4:
                quotecolor = c;
                break;
            case 5:
                reflistbgcolor = c;
                break;
            case 6:
                mainbgcolor = c;
                break;
            case 7:
                contentbgcolor = c;
                break;
            case 8:
                linkscolor = c;
                break;
            case 9:
                manlinkscolor = c;
                break;
            case 10:
                fnlinkscolor = c;
                break;
            default:
                tableheadercolor = c;
                break;
        }
        setModified(true);
        displayupdate = true;
    }

    /**
     * When the user presses the cancel button, no update needed, close window
     */
    private void cancelWindow() {
        needsupdate = false;
        displayupdate = false;
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

    /**
     * This method opens the font-choose-dialog, passing the selected font from
     * the combobox as parameter to set as initial values.
     */
    @Action
    public void chooseMainfont() {
        // get the selected font
        Font f;
        // the order depends on the item-order of the jcombobox
        switch (jComboBoxFonts.getSelectedIndex()) {
            case 0:
                f = mainfont;
                break;
            case 1:
                f = authorfont;
                break;
            case 2:
                f = remarksfont;
                break;
            case 3:
                f = titlefont;
                break;
            case 4:
                f = headerfont1;
                break;
            case 5:
                f = headerfont2;
                break;
            case 6:
                f = desktopheaderfont;
                break;
            case 7:
                f = desktopcommentfont;
                break;
            case 8:
                f = desktopitemheaderfont;
                break;
            case 9:
                f = desktopitemfont;
                break;
            case 10:
                f = quotefont;
                break;
            case 11:
                f = entryheaderfont;
                break;
            case 12:
                f = appendixheaderfont;
                break;
            case 13:
                f = codefont;
                break;
            default:
                f = mainfont;
                break;
        }
        // create font-chooser dialog
        if (null == fontDlg) {
            fontDlg = new CFontChooser(null, f);
            fontDlg.setLocationRelativeTo(this);
        }
        ZettelkastenApp.getApplication().show(fontDlg);

        // if the user has chosen a font, set it
        if (fontDlg.selectedFont != null) {
            // the order depends on the item-order of the jcombobox
            switch (jComboBoxFonts.getSelectedIndex()) {
                case 0:
                    mainfont = fontDlg.selectedFont;
                    break;
                case 1:
                    authorfont = fontDlg.selectedFont;
                    break;
                case 2:
                    remarksfont = fontDlg.selectedFont;
                    break;
                case 3:
                    titlefont = fontDlg.selectedFont;
                    break;
                case 4:
                    headerfont1 = fontDlg.selectedFont;
                    break;
                case 5:
                    headerfont2 = fontDlg.selectedFont;
                    break;
                case 6:
                    desktopheaderfont = fontDlg.selectedFont;
                    break;
                case 7:
                    desktopcommentfont = fontDlg.selectedFont;
                    break;
                case 8:
                    desktopitemheaderfont = fontDlg.selectedFont;
                    break;
                case 9:
                    desktopitemfont = fontDlg.selectedFont;
                    break;
                case 10:
                    quotefont = fontDlg.selectedFont;
                    break;
                case 11:
                    entryheaderfont = fontDlg.selectedFont;
                    break;
                case 12:
                    appendixheaderfont = fontDlg.selectedFont;
                    break;
                case 13:
                    codefont = fontDlg.selectedFont;
                    break;
                default:
                    mainfont = fontDlg.selectedFont;
                    break;
            }
            // and show new item-texts in combobox
            initComboboxFonts();
            // whenever the user changes something, set "modifedLook" state to true
            // so the apply-button becomes enabled (this variable is connected to
            // the button's action)
            setModified(true);
            displayupdate = true;
        }

        // close and dispose the font-dialog
        fontDlg.dispose();
        fontDlg = null;
    }

    /**
     * This method opens a color-chooser-dialog and let's the user choose a font
     * color for the selected font...
     */
    @Action
    public void chooseMainfontColor() {
        // first, show an color-chooser-dialog and let the user choose the color
        Color color = JColorChooser.showDialog(this, resourceMap.getString("chooseColorMsg"), getFontColor());
        // if the user chose a color, proceed
        if (color != null) {
            // set color to jLabel
            jLabelColor.setBackground(color);
            // convert the color-rgb-values into a hexa-decimal-string
            StringBuilder output = new StringBuilder("");
            // we need the format option to keep the leeding zero of hex-values
            // from 00 to 0F.
            output.append(String.format("%02x", color.getRed()));
            output.append(String.format("%02x", color.getGreen()));
            output.append(String.format("%02x", color.getBlue()));
            // setFontColor(Integer.toHexString(color.getRed())+Integer.toHexString(color.getGreen())+Integer.toHexString(color.getBlue()));
            // convert the color-rgb-values into a hexa-decimal-string and save the new font color
            setFontColor(output.toString());
        }
    }

    /**
     * This method opens a color-chooser-dialog and let's the user choose a font
     * color for the selected font...
     */
    @Action
    public void chooseBackgroundColor() {
        // first, show an color-chooser-dialog and let the user choose the color
        Color color = JColorChooser.showDialog(this, resourceMap.getString("chooseColorMsg"), getBackgroundColor());
        // if the user chose a color, proceed
        if (color != null) {
            // set color to jLabel
            jLabelTableColor.setBackground(color);
            // convert the color-rgb-values into a hexa-decimal-string
            StringBuilder output = new StringBuilder("");
            // we need the format option to keep the leeding zero of hex-values
            // from 00 to 0F.
            output.append(String.format("%02x", color.getRed()));
            output.append(String.format("%02x", color.getGreen()));
            output.append(String.format("%02x", color.getBlue()));
            // setFontColor(Integer.toHexString(color.getRed())+Integer.toHexString(color.getGreen())+Integer.toHexString(color.getBlue()));
            // convert the color-rgb-values into a hexa-decimal-string and save the new font color
            setBackgroundColor(output.toString());
        }
    }

    /**
     * Finally, when the user presses the apply-button, all settings are saved.
     * this is done in this method. when all changes have been saved, the window
     * will be closed and disposed.
     */
    @Action(enabledProperty = "modified")
    public void applyChanges() {
        // check for correct settings
        // get the value for the image width and check for valid input
        String val_x = jFormattedTextFieldImgWidth.getValue().toString();
        // get the value for the image height and check for valid input
        String val_y = jFormattedTextFieldImgHeight.getValue().toString();
        // init variables
        int imgresizewidth = 300;
        int imgresizeheight = 300;
        try {
            // convert input-values to integer
            imgresizewidth = Integer.parseInt(val_x);
            imgresizeheight = Integer.parseInt(val_y);
            // check whether values are inside valid boundaries...
            if ((imgresizewidth < 5) || (imgresizewidth > 9999) || (imgresizeheight < 5) || (imgresizeheight > 9999)) {
                // tell the user which setting is wrong
                JOptionPane.showMessageDialog(null, resourceMap.getString("errImgSizeMsg"), resourceMap.getString("errImgSizeTitle"), JOptionPane.PLAIN_MESSAGE);
                // select the appropriate tabbed pane
                jTabbedPane1.setSelectedIndex(1);
                // and set input focus to textfield
                jFormattedTextFieldImgWidth.requestFocusInWindow();
                return;
            }
        } catch (NumberFormatException e) {
        }
        // get the value for the cell spacing and check for valid input
        val_x = jSpinnerDistHor.getValue().toString();
        // get the value for the cell spacing and check for valid input
        val_y = jSpinnerDistVer.getValue().toString();
        // init variables
        int spacinghor = 1;
        int spacingver = 1;
        try {
            // convert input-values to integer
            spacinghor = Integer.parseInt(val_x);
            spacingver = Integer.parseInt(val_y);
            // check whether values are inside valid boundaries...
            if ((spacinghor < 0) || (spacinghor > 25) || (spacingver < 0) || (spacingver > 25)) {
                // tell the user which setting is wrong
                JOptionPane.showMessageDialog(null, resourceMap.getString("errSpacingSizeMsg"), resourceMap.getString("errSpacingSizeTitle"), JOptionPane.PLAIN_MESSAGE);
                // select the appropriate tabbed pane
                jTabbedPane1.setSelectedIndex(1);
                // and set input focus to textfield
                jSpinnerDistHor.requestFocusInWindow();
                return;
            }
        } catch (NumberFormatException e) {
        }
        // check whether changes to winreg have been made
        if (registryChanges) {
            registerFileExtension();
        }
        // save all the settings
        int selectedlaf = jComboBoxLAF.getSelectedIndex();
        String laf = (selectedlaf >= installed_laf.length) ? Constants.seaGlassLookAndFeelClassName : installed_laf[jComboBoxLAF.getSelectedIndex()].getClassName();
        settings.setLookAndFeel(laf);
        settings.setShowAtStartup(jComboBoxShowAtStartup.getSelectedIndex());
        settings.setManualTimestamp(jComboBoxManualTimestamp.getSelectedIndex());
        settings.setShowGridHorizontal(jCheckBoxShowHorGrid.isSelected());
        settings.setShowGridVertical(jCheckBoxShowVerGrid.isSelected());
        settings.setSpellCorrect(jCheckBoxAutocorrect.isSelected());
        settings.setStenoActivated(jCheckBoxSteno.isSelected());
        settings.setImageResize(jCheckBoxImgResize.isSelected());
        settings.setImageResizeHeight(imgresizeheight);
        settings.setImageResizeWidth(imgresizewidth);
        settings.setCellSpacing(spacinghor, spacingver);
        settings.setTableFontSize(jSliderFontSize.getValue());
        settings.setDesktopOutlineFontSize(jSliderDesktopFontSize.getValue());
        settings.setTextfieldFontSize(jSliderTextfields.getValue());
        settings.setAutoBackup(jCheckBoxAutobackup.isSelected());
        settings.setSupFootnote(jCheckBoxFootnote.isSelected());
        settings.setFootnoteBraces(jCheckBoxFootnoteBraces.isSelected());
        settings.setSearchRemovesFormatTags(!jCheckBoxSearchWithoutFormatTags.isSelected());
        settings.setMakeLuhmannColumnSortable(jCheckBoxLuhmannColSortable.isSelected());
        settings.setJumpFootnote(jCheckBoxJumpToTab.isSelected());
        settings.setUseCustomCSS(Settings.CUSTOM_CSS_ENTRY, jCheckBoxEntryCSS.isSelected());
        settings.setUseCustomCSS(Settings.CUSTOM_CSS_DESKTOP, jCheckBoxDesktopCSS.isSelected());
        settings.setShowIcons(jCheckBoxShowToolbar.isSelected());
        settings.setShowAllIcons(jCheckBoxShowAllIcons.isSelected());
        settings.setSearchAlwaysSynonyms(jCheckBoxSynonym.isSelected());
        settings.setInsertNewEntryAtEmpty(jCheckBoxFillNewEntries.isSelected());
        settings.setExtraBackup(jCheckBoxExtraBackup.isSelected());
        settings.setAddAllToHistory(jCheckBoxAllToHist.isSelected());
        settings.setShowIconText(jCheckBoxIconText.isSelected());
        settings.setAutoUpdate(jCheckBoxAutoUpdate.isSelected());
        settings.setAutoNightlyUpdate(jCheckBoxCheckNightly.isSelected());
        settings.setMinimizeToTray(jCheckBoxSystray.isSelected());
        settings.setIconTheme(jComboBoxIconTheme.getSelectedIndex());
        settings.setUseXDGOpen(jCheckBoxUseXDGOpen.isSelected());
        settings.setAutoCompleteTags(jCheckBoxAutoCompleteTags.isSelected());
        settings.setMarkdownActivated(jCheckBoxUseMarkdown.isSelected());
        settings.setTableHeaderColor(tableheadercolor);
        settings.setTableRowEvenColor(tablerowevencolor);
        settings.setShowTableBorder(jCheckBoxShowTableBorder.isSelected());
        settings.setUseMacBackgroundColor(jCheckBoxUseMacBackgroundColor.isSelected());
        settings.setShowEntryHeadline(jCheckBoxShowEntryHeadline.isSelected());
        settings.setEntryHeadingBackgroundColor(entryheadingscolor);
        settings.setReflistBackgroundColor(reflistbgcolor);
        settings.setLinkColor(linkscolor);
        settings.setManlinkColor(manlinkscolor);
        settings.setFootnoteLinkColor(fnlinkscolor);
        settings.setQuoteBackgroundColor(quotecolor);
        settings.setTableRowOddColor(tablerowoddcolor);
        settings.setContentBackgroundColor(contentbgcolor);
        settings.setMainBackgroundColor(mainbgcolor);
        if (jCheckBoxExtraBackup.isSelected()) {
            settings.setExtraBackupPath(jTextFieldBackupPath.getText());
        }
        // only save settings to user paths if changes have been made
        if (userPathChanges) {
            dataObj.setUserAttachmentPath(jTextFieldAttachmentPath.getText());
            dataObj.setUserImagePath(jTextFieldImagePath.getText());
        }
        if (pandocPathChanges) {
            settings.setPandocPath(jTextFieldPandoc.getText());
        }
        switch (jComboBoxLocale.getSelectedIndex()) {
            case 0:
                settings.setLanguage("en");
                break;
            case 1:
                settings.setLanguage("de");
                break;
            case 2:
                settings.setLanguage("es");
                break;
            default:
                settings.setLanguage("en");
                break;
        }
        // save listview font
        settings.setTableFont(tablefont.getFamily());
        settings.setDesktopOutlineFont(desktopfont.getFamily());
        // save mainfont
        String[] styleandweight = getStyleAndWeight(mainfont);
        settings.setMainfont(mainfont.getFamily(), Settings.FONTNAME);
        settings.setMainfont(String.valueOf(mainfont.getSize()), Settings.FONTSIZE);
        settings.setMainfont(styleandweight[0], Settings.FONTSTYLE);
        settings.setMainfont(styleandweight[1], Settings.FONTWEIGHT);
        settings.setMainfont(mainfontcolor, Settings.FONTCOLOR);
        // save quotefont
        settings.setQuoteFont(quotefont.getFamily(), Settings.FONTNAME);
        settings.setQuoteFont(String.valueOf(quotefont.getSize()), Settings.FONTSIZE);
        settings.setQuoteFont(quotefontcolor, Settings.FONTCOLOR);
        // save entryheaderfont
        settings.setEntryHeadeFont(entryheaderfont.getFamily(), Settings.FONTNAME);
        settings.setEntryHeadeFont(String.valueOf(entryheaderfont.getSize()), Settings.FONTSIZE);
        settings.setEntryHeadeFont(entryheaderfontcolor, Settings.FONTCOLOR);
        // save authorfont
        styleandweight = getStyleAndWeight(authorfont);
        settings.setAuthorFont(authorfont.getFamily(), Settings.FONTNAME);
        settings.setAuthorFont(String.valueOf(authorfont.getSize()), Settings.FONTSIZE);
        settings.setAuthorFont(styleandweight[0], Settings.FONTSTYLE);
        settings.setAuthorFont(styleandweight[1], Settings.FONTWEIGHT);
        settings.setAuthorFont(authorfontcolor, Settings.FONTCOLOR);
        // save codefont
        styleandweight = getStyleAndWeight(codefont);
        settings.setCodeFont(codefont.getFamily(), Settings.FONTNAME);
        settings.setCodeFont(String.valueOf(codefont.getSize()), Settings.FONTSIZE);
        settings.setCodeFont(styleandweight[0], Settings.FONTSTYLE);
        settings.setCodeFont(styleandweight[1], Settings.FONTWEIGHT);
        settings.setCodeFont(codefontcolor, Settings.FONTCOLOR);
        // save remarksfont
        styleandweight = getStyleAndWeight(remarksfont);
        settings.setRemarksFont(remarksfont.getFamily(), Settings.FONTNAME);
        settings.setRemarksFont(String.valueOf(remarksfont.getSize()), Settings.FONTSIZE);
        settings.setRemarksFont(styleandweight[0], Settings.FONTSTYLE);
        settings.setRemarksFont(styleandweight[1], Settings.FONTWEIGHT);
        settings.setRemarksFont(remarksfontcolor, Settings.FONTCOLOR);
        // save titlefont
        styleandweight = getStyleAndWeight(titlefont);
        settings.setTitleFont(titlefont.getFamily(), Settings.FONTNAME);
        settings.setTitleFont(String.valueOf(titlefont.getSize()), Settings.FONTSIZE);
        settings.setTitleFont(styleandweight[0], Settings.FONTSTYLE);
        settings.setTitleFont(styleandweight[1], Settings.FONTWEIGHT);
        settings.setTitleFont(titlefontcolor, Settings.FONTCOLOR);
        // save titlefont
        styleandweight = getStyleAndWeight(appendixheaderfont);
        settings.setAppendixHeaderFont(appendixheaderfont.getFamily(), Settings.FONTNAME);
        settings.setAppendixHeaderFont(String.valueOf(appendixheaderfont.getSize()), Settings.FONTSIZE);
        settings.setAppendixHeaderFont(styleandweight[0], Settings.FONTSTYLE);
        settings.setAppendixHeaderFont(styleandweight[1], Settings.FONTWEIGHT);
        settings.setAppendixHeaderFont(appendixheaderfontcolor, Settings.FONTCOLOR);
        // save header1-font
        styleandweight = getStyleAndWeight(headerfont1);
        settings.setHeaderfont1(headerfont1.getFamily(), Settings.FONTNAME);
        settings.setHeaderfont1(String.valueOf(headerfont1.getSize()), Settings.FONTSIZE);
        settings.setHeaderfont1(styleandweight[0], Settings.FONTSTYLE);
        settings.setHeaderfont1(styleandweight[1], Settings.FONTWEIGHT);
        settings.setHeaderfont1(headerfont1color, Settings.FONTCOLOR);
        // save header2-font
        styleandweight = getStyleAndWeight(headerfont2);
        settings.setHeaderfont2(headerfont2.getFamily(), Settings.FONTNAME);
        settings.setHeaderfont2(String.valueOf(headerfont2.getSize()), Settings.FONTSIZE);
        settings.setHeaderfont2(styleandweight[0], Settings.FONTSTYLE);
        settings.setHeaderfont2(styleandweight[1], Settings.FONTWEIGHT);
        settings.setHeaderfont2(headerfont2color, Settings.FONTCOLOR);
        // save desktopheader-font
        styleandweight = getStyleAndWeight(desktopheaderfont);
        settings.setDesktopHeaderfont(desktopheaderfont.getFamily(), Settings.FONTNAME);
        settings.setDesktopHeaderfont(String.valueOf(desktopheaderfont.getSize()), Settings.FONTSIZE);
        settings.setDesktopHeaderfont(styleandweight[0], Settings.FONTSTYLE);
        settings.setDesktopHeaderfont(styleandweight[1], Settings.FONTWEIGHT);
        settings.setDesktopHeaderfont(desktopheaderfontcolor, Settings.FONTCOLOR);
        // save desktopcomment-font
        styleandweight = getStyleAndWeight(desktopcommentfont);
        settings.setDesktopCommentfont(desktopcommentfont.getFamily(), Settings.FONTNAME);
        settings.setDesktopCommentfont(String.valueOf(desktopcommentfont.getSize()), Settings.FONTSIZE);
        settings.setDesktopCommentfont(styleandweight[0], Settings.FONTSTYLE);
        settings.setDesktopCommentfont(styleandweight[1], Settings.FONTWEIGHT);
        settings.setDesktopCommentfont(desktopcommentfontcolor, Settings.FONTCOLOR);
        // save desktopitemheader-font
        styleandweight = getStyleAndWeight(desktopitemheaderfont);
        settings.setDesktopItemHeaderfont(desktopitemheaderfont.getFamily(), Settings.FONTNAME);
        settings.setDesktopItemHeaderfont(String.valueOf(desktopitemheaderfont.getSize()), Settings.FONTSIZE);
        settings.setDesktopItemHeaderfont(styleandweight[0], Settings.FONTSTYLE);
        settings.setDesktopItemHeaderfont(styleandweight[1], Settings.FONTWEIGHT);
        settings.setDesktopItemHeaderfont(desktopitemheaderfontcolor, Settings.FONTCOLOR);
        // save desktopitem-font
        styleandweight = getStyleAndWeight(desktopitemfont);
        settings.setDesktopItemfont(desktopitemfont.getFamily(), Settings.FONTNAME);
        settings.setDesktopItemfont(String.valueOf(desktopitemfont.getSize()), Settings.FONTSIZE);
        settings.setDesktopItemfont(styleandweight[0], Settings.FONTSTYLE);
        settings.setDesktopItemfont(styleandweight[1], Settings.FONTWEIGHT);
        settings.setDesktopItemfont(desktopitemfontcolor, Settings.FONTCOLOR);
        // save the changes to the settings-file
        savesettingok = settings.saveSettings();
        // finally disable button again
        setModified(false);
        // and close window
        closeWindow();
    }

    /**
     * Since the font-properties like plain, bold etc. are different in
     * CSS-definitions, we convert the Font-properties to CSS-properties here.
     * We need this for setting up the HTML-page that displays entries. The
     * formatting is done via style-tags and CSS, so we need the
     * CSS-definitions...
     *
     * @param f the font from which we want to retrieve the style-properties in
     * CSS-values
     * @return a string array with two fields: field one holding the
     * css-font-style-property, and the second field holding the the
     * css-font-weight-property.
     */
    private String[] getStyleAndWeight(Font f) {
        String style = "normal";
        String weight = "normal";
        if (Font.PLAIN == f.getStyle()) {
            style = "normal";
            weight = "normal";
        } else if (Font.BOLD == f.getStyle()) {
            style = "normal";
            weight = "bold";
        } else if (Font.ITALIC == f.getStyle()) {
            style = "italic";
            weight = "normal";
        } else if ((Font.BOLD + Font.ITALIC) == f.getStyle()) {
            style = "italic";
            weight = "bold";
        }
        // prepare return value
        String[] retval = new String[2];
        retval[0] = style;
        retval[1] = weight;

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
        jButtonApply = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jComboBoxShowAtStartup = new javax.swing.JComboBox();
        jCheckBoxAutobackup = new javax.swing.JCheckBox();
        jCheckBoxAutocorrect = new javax.swing.JCheckBox();
        jCheckBoxSteno = new javax.swing.JCheckBox();
        jButtonEditAutokorrekt = new javax.swing.JButton();
        jButtonSynonymEdit = new javax.swing.JButton();
        jCheckBoxSynonym = new javax.swing.JCheckBox();
        jCheckBoxFillNewEntries = new javax.swing.JCheckBox();
        jButtonEditSteno = new javax.swing.JButton();
        jCheckBoxExtraBackup = new javax.swing.JCheckBox();
        jTextFieldBackupPath = new javax.swing.JTextField();
        jButtonBrowseBackup = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        jCheckBoxAllToHist = new javax.swing.JCheckBox();
        jCheckBoxRegistry = new javax.swing.JCheckBox();
        jCheckBoxSystray = new javax.swing.JCheckBox();
        jCheckBoxAutoCompleteTags = new javax.swing.JCheckBox();
        jCheckBoxUseMarkdown = new javax.swing.JCheckBox();
        jCheckBoxSearchWithoutFormatTags = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jComboBoxLAF = new javax.swing.JComboBox();
        jCheckBoxImgResize = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jFormattedTextFieldImgWidth = new javax.swing.JFormattedTextField();
        jFormattedTextFieldImgHeight = new javax.swing.JFormattedTextField();
        jCheckBoxFootnote = new javax.swing.JCheckBox();
        jCheckBoxJumpToTab = new javax.swing.JCheckBox();
        jCheckBoxIconText = new javax.swing.JCheckBox();
        jLabel12 = new javax.swing.JLabel();
        jComboBoxManualTimestamp = new javax.swing.JComboBox();
        jComboBoxLocale = new javax.swing.JComboBox();
        jLabel15 = new javax.swing.JLabel();
        jCheckBoxShowToolbar = new javax.swing.JCheckBox();
        jCheckBoxShowAllIcons = new javax.swing.JCheckBox();
        jLabel10 = new javax.swing.JLabel();
        jComboBoxIconTheme = new javax.swing.JComboBox();
        jCheckBoxShowTableBorder = new javax.swing.JCheckBox();
        jCheckBoxShowEntryHeadline = new javax.swing.JCheckBox();
        jCheckBoxUseMacBackgroundColor = new javax.swing.JCheckBox();
        jCheckBoxFootnoteBraces = new javax.swing.JCheckBox();
        jPanel5 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jComboBoxFonts = new javax.swing.JComboBox();
        jButtonFont = new javax.swing.JButton();
        jLabelColor = new javax.swing.JLabel();
        jButtonFontcolor = new javax.swing.JButton();
        jLabel19 = new javax.swing.JLabel();
        jComboBoxBackgroundColors = new javax.swing.JComboBox();
        jLabelTableColor = new javax.swing.JLabel();
        jButtonTableBackgroundColor = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jButtonHighlightStyle = new javax.swing.JButton();
        jLabel20 = new javax.swing.JLabel();
        jButtonHighlightKeywordStyle = new javax.swing.JButton();
        jLabel21 = new javax.swing.JLabel();
        jButtonHighlightLivesearchStyle = new javax.swing.JButton();
        jCheckBoxEntryCSS = new javax.swing.JCheckBox();
        jButtonEntryCss = new javax.swing.JButton();
        jCheckBoxDesktopCSS = new javax.swing.JCheckBox();
        jButtonDesktopCSS = new javax.swing.JButton();
        jButtonResetEntryCSS = new javax.swing.JButton();
        jButtonResetDesktopCSS = new javax.swing.JButton();
        jPanel11 = new javax.swing.JPanel();
        jCheckBoxShowHorGrid = new javax.swing.JCheckBox();
        jCheckBoxShowVerGrid = new javax.swing.JCheckBox();
        jSpinnerDistHor = new javax.swing.JSpinner();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jSpinnerDistVer = new javax.swing.JSpinner();
        jLabel9 = new javax.swing.JLabel();
        jSliderFontSize = new javax.swing.JSlider();
        jLabel11 = new javax.swing.JLabel();
        jSliderTextfields = new javax.swing.JSlider();
        jLabel16 = new javax.swing.JLabel();
        jButtonListFont = new javax.swing.JButton();
        jLabel22 = new javax.swing.JLabel();
        jButtonDesktopFont = new javax.swing.JButton();
        jLabel23 = new javax.swing.JLabel();
        jSliderDesktopFontSize = new javax.swing.JSlider();
        jCheckBoxLuhmannColSortable = new javax.swing.JCheckBox();
        jPanel7 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jTextFieldAttachmentPath = new javax.swing.JTextField();
        jButtonBrowseAttachmentPath = new javax.swing.JButton();
        jLabel18 = new javax.swing.JLabel();
        jTextFieldImagePath = new javax.swing.JTextField();
        jButtonBrowseImagePath = new javax.swing.JButton();
        jCheckBoxUseXDGOpen = new javax.swing.JCheckBox();
        jLabel14 = new javax.swing.JLabel();
        jTextFieldPandoc = new javax.swing.JTextField();
        jButtonBrowsePandoc = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jCheckBoxAutoUpdate = new javax.swing.JCheckBox();
        jCheckBoxCheckNightly = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ZettelkastenApp.class).getContext().getResourceMap(CSettingsDlg.class);
        setTitle(resourceMap.getString("FormSettingsDlg.title")); // NOI18N
        setModal(true);
        setName("FormSettingsDlg"); // NOI18N
        setResizable(false);

        jPanel1.setName("jPanel1"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(ZettelkastenApp.class).getContext().getActionMap(CSettingsDlg.class, this);
        jButtonApply.setAction(actionMap.get("applyChanges")); // NOI18N
        jButtonApply.setName("jButtonApply"); // NOI18N

        jButtonCancel.setText(resourceMap.getString("jButtonCancel.text")); // NOI18N
        jButtonCancel.setName("jButtonCancel"); // NOI18N

        jTabbedPane1.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jPanel3.setName("jPanel3"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jComboBoxShowAtStartup.setName("jComboBoxShowAtStartup"); // NOI18N

        jCheckBoxAutobackup.setText(resourceMap.getString("jCheckBoxAutobackup.text")); // NOI18N
        jCheckBoxAutobackup.setName("jCheckBoxAutobackup"); // NOI18N

        jCheckBoxAutocorrect.setText(resourceMap.getString("jCheckBoxAutocorrect.text")); // NOI18N
        jCheckBoxAutocorrect.setName("jCheckBoxAutocorrect"); // NOI18N

        jCheckBoxSteno.setText(resourceMap.getString("jCheckBoxSteno.text")); // NOI18N
        jCheckBoxSteno.setName("jCheckBoxSteno"); // NOI18N

        jButtonEditAutokorrekt.setText(resourceMap.getString("jButtonEditAutokorrekt.text")); // NOI18N
        jButtonEditAutokorrekt.setName("jButtonEditAutokorrekt"); // NOI18N

        jButtonSynonymEdit.setText(resourceMap.getString("jButtonSynonymEdit.text")); // NOI18N
        jButtonSynonymEdit.setName("jButtonSynonymEdit"); // NOI18N

        jCheckBoxSynonym.setText(resourceMap.getString("jCheckBoxSynonym.text")); // NOI18N
        jCheckBoxSynonym.setToolTipText(resourceMap.getString("jCheckBoxSynonym.toolTipText")); // NOI18N
        jCheckBoxSynonym.setName("jCheckBoxSynonym"); // NOI18N

        jCheckBoxFillNewEntries.setText(resourceMap.getString("jCheckBoxFillNewEntries.text")); // NOI18N
        jCheckBoxFillNewEntries.setToolTipText(resourceMap.getString("jCheckBoxFillNewEntries.toolTipText")); // NOI18N
        jCheckBoxFillNewEntries.setName("jCheckBoxFillNewEntries"); // NOI18N

        jButtonEditSteno.setText(resourceMap.getString("jButtonEditSteno.text")); // NOI18N
        jButtonEditSteno.setName("jButtonEditSteno"); // NOI18N

        jCheckBoxExtraBackup.setText(resourceMap.getString("jCheckBoxExtraBackup.text")); // NOI18N
        jCheckBoxExtraBackup.setName("jCheckBoxExtraBackup"); // NOI18N

        jTextFieldBackupPath.setName("jTextFieldBackupPath"); // NOI18N

        jButtonBrowseBackup.setText(resourceMap.getString("jButtonBrowseBackup.text")); // NOI18N
        jButtonBrowseBackup.setName("jButtonBrowseBackup"); // NOI18N

        jLabel13.setText(resourceMap.getString("jLabel13.text")); // NOI18N
        jLabel13.setName("jLabel13"); // NOI18N

        jCheckBoxAllToHist.setText(resourceMap.getString("jCheckBoxAllToHist.text")); // NOI18N
        jCheckBoxAllToHist.setToolTipText(resourceMap.getString("jCheckBoxAllToHist.toolTipText")); // NOI18N
        jCheckBoxAllToHist.setName("jCheckBoxAllToHist"); // NOI18N

        jCheckBoxRegistry.setText(resourceMap.getString("jCheckBoxRegistry.text")); // NOI18N
        jCheckBoxRegistry.setName("jCheckBoxRegistry"); // NOI18N

        jCheckBoxSystray.setText(resourceMap.getString("jCheckBoxSystray.text")); // NOI18N
        jCheckBoxSystray.setName("jCheckBoxSystray"); // NOI18N

        jCheckBoxAutoCompleteTags.setText(resourceMap.getString("jCheckBoxAutoCompleteTags.text")); // NOI18N
        jCheckBoxAutoCompleteTags.setName("jCheckBoxAutoCompleteTags"); // NOI18N

        jCheckBoxUseMarkdown.setText(resourceMap.getString("jCheckBoxUseMarkdown.text")); // NOI18N
        jCheckBoxUseMarkdown.setName("jCheckBoxUseMarkdown"); // NOI18N

        jCheckBoxSearchWithoutFormatTags.setText(resourceMap.getString("jCheckBoxSearchWithoutFormatTags.text")); // NOI18N
        jCheckBoxSearchWithoutFormatTags.setToolTipText(resourceMap.getString("jCheckBoxSearchWithoutFormatTags.toolTipText")); // NOI18N
        jCheckBoxSearchWithoutFormatTags.setName("jCheckBoxSearchWithoutFormatTags"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBoxSystray)
                    .addComponent(jCheckBoxRegistry)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBoxSteno)
                            .addComponent(jCheckBoxAutocorrect)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldBackupPath, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButtonEditAutokorrekt, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jButtonEditSteno, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jButtonBrowseBackup, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addComponent(jCheckBoxExtraBackup)
                    .addComponent(jCheckBoxAutobackup)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxShowAtStartup, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jCheckBoxSynonym)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonSynonymEdit))
                    .addComponent(jCheckBoxFillNewEntries)
                    .addComponent(jCheckBoxSearchWithoutFormatTags)
                    .addComponent(jCheckBoxAllToHist)
                    .addComponent(jCheckBoxAutoCompleteTags)
                    .addComponent(jCheckBoxUseMarkdown))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jComboBoxShowAtStartup, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jCheckBoxAutobackup)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxExtraBackup)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldBackupPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13)
                    .addComponent(jButtonBrowseBackup))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxAutocorrect)
                    .addComponent(jButtonEditAutokorrekt))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxSteno)
                    .addComponent(jButtonEditSteno))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxAutoCompleteTags)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxUseMarkdown)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxSynonym)
                    .addComponent(jButtonSynonymEdit))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxSearchWithoutFormatTags)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxFillNewEntries)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxAllToHist)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxRegistry)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxSystray)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N

        jPanel2.setName("jPanel2"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jComboBoxLAF.setName("jComboBoxLAF"); // NOI18N

        jCheckBoxImgResize.setText(resourceMap.getString("jCheckBoxImgResize.text")); // NOI18N
        jCheckBoxImgResize.setName("jCheckBoxImgResize"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        jFormattedTextFieldImgWidth.setColumns(5);
        jFormattedTextFieldImgWidth.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("####"))));
        jFormattedTextFieldImgWidth.setName("jFormattedTextFieldImgWidth"); // NOI18N

        jFormattedTextFieldImgHeight.setColumns(5);
        jFormattedTextFieldImgHeight.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("####"))));
        jFormattedTextFieldImgHeight.setName("jFormattedTextFieldImgHeight"); // NOI18N

        jCheckBoxFootnote.setText(resourceMap.getString("jCheckBoxFootnote.text")); // NOI18N
        jCheckBoxFootnote.setName("jCheckBoxFootnote"); // NOI18N

        jCheckBoxJumpToTab.setText(resourceMap.getString("jCheckBoxJumpToTab.text")); // NOI18N
        jCheckBoxJumpToTab.setName("jCheckBoxJumpToTab"); // NOI18N

        jCheckBoxIconText.setText(resourceMap.getString("jCheckBoxIconText.text")); // NOI18N
        jCheckBoxIconText.setName("jCheckBoxIconText"); // NOI18N

        jLabel12.setText(resourceMap.getString("jLabel12.text")); // NOI18N
        jLabel12.setName("jLabel12"); // NOI18N

        jComboBoxManualTimestamp.setName("jComboBoxManualTimestamp"); // NOI18N

        jComboBoxLocale.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "English", "German (Deutsch)", "Spanish (Espanol)" }));
        jComboBoxLocale.setName("jComboBoxLocale"); // NOI18N

        jLabel15.setText(resourceMap.getString("jLabel15.text")); // NOI18N
        jLabel15.setName("jLabel15"); // NOI18N

        jCheckBoxShowToolbar.setText(resourceMap.getString("jCheckBoxShowToolbar.text")); // NOI18N
        jCheckBoxShowToolbar.setName("jCheckBoxShowToolbar"); // NOI18N

        jCheckBoxShowAllIcons.setText(resourceMap.getString("jCheckBoxShowAllIcons.text")); // NOI18N
        jCheckBoxShowAllIcons.setName("jCheckBoxShowAllIcons"); // NOI18N

        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N

        jComboBoxIconTheme.setName("jComboBoxIconTheme"); // NOI18N

        jCheckBoxShowTableBorder.setText(resourceMap.getString("jCheckBoxShowTableBorder.text")); // NOI18N
        jCheckBoxShowTableBorder.setName("jCheckBoxShowTableBorder"); // NOI18N

        jCheckBoxShowEntryHeadline.setText(resourceMap.getString("jCheckBoxShowEntryHeadline.text")); // NOI18N
        jCheckBoxShowEntryHeadline.setName("jCheckBoxShowEntryHeadline"); // NOI18N

        jCheckBoxUseMacBackgroundColor.setText(resourceMap.getString("jCheckBoxUseMacBackgroundColor.text")); // NOI18N
        jCheckBoxUseMacBackgroundColor.setName("jCheckBoxUseMacBackgroundColor"); // NOI18N

        jCheckBoxFootnoteBraces.setText(resourceMap.getString("jCheckBoxFootnoteBraces.text")); // NOI18N
        jCheckBoxFootnoteBraces.setName("jCheckBoxFootnoteBraces"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBoxFootnoteBraces)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(jCheckBoxShowAllIcons))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxManualTimestamp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jCheckBoxIconText)
                    .addComponent(jCheckBoxShowToolbar)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxIconTheme, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxLAF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxLocale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBoxShowTableBorder)
                            .addComponent(jCheckBoxFootnote)
                            .addComponent(jCheckBoxJumpToTab))
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addGap(27, 27, 27)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel4)
                                .addComponent(jLabel6))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jFormattedTextFieldImgWidth, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jFormattedTextFieldImgHeight, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addComponent(jCheckBoxImgResize))
                    .addComponent(jCheckBoxShowEntryHeadline)
                    .addComponent(jCheckBoxUseMacBackgroundColor))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(jComboBoxLocale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jComboBoxLAF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxUseMacBackgroundColor)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(jComboBoxManualTimestamp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxShowToolbar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxShowAllIcons)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxIconText)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jComboBoxIconTheme, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxImgResize)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jFormattedTextFieldImgWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jFormattedTextFieldImgHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxShowEntryHeadline)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxShowTableBorder)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxFootnote)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxFootnoteBraces)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxJumpToTab)
                .addContainerGap())
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        jPanel5.setName("jPanel5"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jComboBoxFonts.setName("jComboBoxFonts"); // NOI18N

        jButtonFont.setAction(actionMap.get("chooseMainfont")); // NOI18N
        jButtonFont.setText(resourceMap.getString("jButtonFont.text")); // NOI18N
        jButtonFont.setBorderPainted(false);
        jButtonFont.setContentAreaFilled(false);
        jButtonFont.setFocusPainted(false);
        jButtonFont.setName("jButtonFont"); // NOI18N

        jLabelColor.setText(resourceMap.getString("jLabelColor.text")); // NOI18N
        jLabelColor.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabelColor.setName("jLabelColor"); // NOI18N
        jLabelColor.setOpaque(true);

        jButtonFontcolor.setAction(actionMap.get("chooseMainfontColor")); // NOI18N
        jButtonFontcolor.setBorderPainted(false);
        jButtonFontcolor.setContentAreaFilled(false);
        jButtonFontcolor.setFocusPainted(false);
        jButtonFontcolor.setName("jButtonFontcolor"); // NOI18N

        jLabel19.setText(resourceMap.getString("jLabel19.text")); // NOI18N
        jLabel19.setName("jLabel19"); // NOI18N

        jComboBoxBackgroundColors.setName("jComboBoxBackgroundColors"); // NOI18N

        jLabelTableColor.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabelTableColor.setName("jLabelTableColor"); // NOI18N
        jLabelTableColor.setOpaque(true);

        jButtonTableBackgroundColor.setAction(actionMap.get("chooseBackgroundColor")); // NOI18N
        jButtonTableBackgroundColor.setBorderPainted(false);
        jButtonTableBackgroundColor.setContentAreaFilled(false);
        jButtonTableBackgroundColor.setFocusPainted(false);
        jButtonTableBackgroundColor.setName("jButtonTableBackgroundColor"); // NOI18N

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jButtonHighlightStyle.setText(resourceMap.getString("jButtonHighlightStyle.text")); // NOI18N
        jButtonHighlightStyle.setName("jButtonHighlightStyle"); // NOI18N

        jLabel20.setText(resourceMap.getString("jLabel20.text")); // NOI18N
        jLabel20.setName("jLabel20"); // NOI18N

        jButtonHighlightKeywordStyle.setText(resourceMap.getString("jButtonHighlightKeywordStyle.text")); // NOI18N
        jButtonHighlightKeywordStyle.setName("jButtonHighlightKeywordStyle"); // NOI18N

        jLabel21.setText(resourceMap.getString("jLabel21.text")); // NOI18N
        jLabel21.setName("jLabel21"); // NOI18N

        jButtonHighlightLivesearchStyle.setText(resourceMap.getString("jButtonHighlightLivesearchStyle.text")); // NOI18N
        jButtonHighlightLivesearchStyle.setName("jButtonHighlightLivesearchStyle"); // NOI18N

        jCheckBoxEntryCSS.setText(resourceMap.getString("jCheckBoxEntryCSS.text")); // NOI18N
        jCheckBoxEntryCSS.setName("jCheckBoxEntryCSS"); // NOI18N

        jButtonEntryCss.setText(resourceMap.getString("jButtonEntryCss.text")); // NOI18N
        jButtonEntryCss.setName("jButtonEntryCss"); // NOI18N

        jCheckBoxDesktopCSS.setText(resourceMap.getString("jCheckBoxDesktopCSS.text")); // NOI18N
        jCheckBoxDesktopCSS.setName("jCheckBoxDesktopCSS"); // NOI18N

        jButtonDesktopCSS.setText(resourceMap.getString("jButtonDesktopCSS.text")); // NOI18N
        jButtonDesktopCSS.setName("jButtonDesktopCSS"); // NOI18N

        jButtonResetEntryCSS.setText(resourceMap.getString("jButtonResetEntryCSS.text")); // NOI18N
        jButtonResetEntryCSS.setName("jButtonResetEntryCSS"); // NOI18N

        jButtonResetDesktopCSS.setText(resourceMap.getString("jButtonResetDesktopCSS.text")); // NOI18N
        jButtonResetDesktopCSS.setName("jButtonResetDesktopCSS"); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel19)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxBackgroundColors, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelTableColor, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonTableBackgroundColor, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxFonts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelColor, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonFont, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonFontcolor, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel20)
                            .addComponent(jLabel21)
                            .addComponent(jCheckBoxEntryCSS)
                            .addComponent(jCheckBoxDesktopCSS))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButtonDesktopCSS)
                            .addComponent(jButtonEntryCss)
                            .addComponent(jButtonHighlightStyle)
                            .addComponent(jButtonHighlightKeywordStyle)
                            .addComponent(jButtonHighlightLivesearchStyle)
                            .addComponent(jButtonResetEntryCSS)
                            .addComponent(jButtonResetDesktopCSS))))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel2)
                    .addComponent(jComboBoxFonts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonFont)
                    .addComponent(jButtonFontcolor)
                    .addComponent(jLabelColor, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel19)
                        .addComponent(jComboBoxBackgroundColors, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabelTableColor, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonTableBackgroundColor, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jButtonHighlightStyle))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(jButtonHighlightKeywordStyle))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(jButtonHighlightLivesearchStyle))
                .addGap(18, 18, 18)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxEntryCSS)
                    .addComponent(jButtonEntryCss))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonResetEntryCSS)
                .addGap(18, 18, 18)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxDesktopCSS)
                    .addComponent(jButtonDesktopCSS))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonResetDesktopCSS)
                .addContainerGap())
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel5.TabConstraints.tabTitle"), jPanel5); // NOI18N

        jPanel11.setName("jPanel11"); // NOI18N

        jCheckBoxShowHorGrid.setText(resourceMap.getString("jCheckBoxShowHorGrid.text")); // NOI18N
        jCheckBoxShowHorGrid.setName("jCheckBoxShowHorGrid"); // NOI18N

        jCheckBoxShowVerGrid.setText(resourceMap.getString("jCheckBoxShowVerGrid.text")); // NOI18N
        jCheckBoxShowVerGrid.setName("jCheckBoxShowVerGrid"); // NOI18N

        jSpinnerDistHor.setModel(new javax.swing.SpinnerNumberModel(1, 0, 25, 1));
        jSpinnerDistHor.setName("jSpinnerDistHor"); // NOI18N

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        jSpinnerDistVer.setModel(new javax.swing.SpinnerNumberModel(1, 0, 25, 1));
        jSpinnerDistVer.setName("jSpinnerDistVer"); // NOI18N

        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        jSliderFontSize.setMajorTickSpacing(1);
        jSliderFontSize.setMaximum(8);
        jSliderFontSize.setMinorTickSpacing(1);
        jSliderFontSize.setPaintTicks(true);
        jSliderFontSize.setSnapToTicks(true);
        jSliderFontSize.setName("jSliderFontSize"); // NOI18N

        jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N

        jSliderTextfields.setMajorTickSpacing(1);
        jSliderTextfields.setMaximum(8);
        jSliderTextfields.setMinorTickSpacing(1);
        jSliderTextfields.setPaintTicks(true);
        jSliderTextfields.setSnapToTicks(true);
        jSliderTextfields.setName("jSliderTextfields"); // NOI18N

        jLabel16.setText(resourceMap.getString("jLabel16.text")); // NOI18N
        jLabel16.setName("jLabel16"); // NOI18N

        jButtonListFont.setText(resourceMap.getString("jButtonListFont.text")); // NOI18N
        jButtonListFont.setName("jButtonListFont"); // NOI18N

        jLabel22.setText(resourceMap.getString("jLabel22.text")); // NOI18N
        jLabel22.setName("jLabel22"); // NOI18N

        jButtonDesktopFont.setText(resourceMap.getString("jButtonDesktopFont.text")); // NOI18N
        jButtonDesktopFont.setName("jButtonDesktopFont"); // NOI18N

        jLabel23.setText(resourceMap.getString("jLabel23.text")); // NOI18N
        jLabel23.setName("jLabel23"); // NOI18N

        jSliderDesktopFontSize.setMajorTickSpacing(1);
        jSliderDesktopFontSize.setMaximum(8);
        jSliderDesktopFontSize.setMinorTickSpacing(1);
        jSliderDesktopFontSize.setPaintTicks(true);
        jSliderDesktopFontSize.setSnapToTicks(true);
        jSliderDesktopFontSize.setName("jSliderDesktopFontSize"); // NOI18N

        jCheckBoxLuhmannColSortable.setText(resourceMap.getString("jCheckBoxLuhmannColSortable.text")); // NOI18N
        jCheckBoxLuhmannColSortable.setToolTipText(resourceMap.getString("jCheckBoxLuhmannColSortable.toolTipText")); // NOI18N
        jCheckBoxLuhmannColSortable.setName("jCheckBoxLuhmannColSortable"); // NOI18N

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(jLabel23)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSpinnerDistHor, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jSpinnerDistVer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jCheckBoxShowHorGrid)
                    .addComponent(jCheckBoxShowVerGrid)
                    .addComponent(jSliderFontSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSliderTextfields, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonListFont))
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(jLabel22)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonDesktopFont))
                    .addComponent(jLabel11)
                    .addComponent(jSliderDesktopFontSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBoxLuhmannColSortable))
                .addContainerGap())
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBoxShowHorGrid)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxShowVerGrid)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxLuhmannColSortable)
                .addGap(18, 18, 18)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSpinnerDistHor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSpinnerDistVer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel9)
                .addGap(0, 0, 0)
                .addComponent(jSliderFontSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel23)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSliderDesktopFontSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSliderTextfields, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(jButtonListFont))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(jButtonDesktopFont))
                .addGap(17, 17, 17))
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel11.TabConstraints.tabTitle"), jPanel11); // NOI18N

        jPanel7.setName("jPanel7"); // NOI18N

        jLabel17.setText(resourceMap.getString("jLabel17.text")); // NOI18N
        jLabel17.setName("jLabel17"); // NOI18N

        jTextFieldAttachmentPath.setName("jTextFieldAttachmentPath"); // NOI18N

        jButtonBrowseAttachmentPath.setText(resourceMap.getString("jButtonBrowseAttachmentPath.text")); // NOI18N
        jButtonBrowseAttachmentPath.setName("jButtonBrowseAttachmentPath"); // NOI18N

        jLabel18.setText(resourceMap.getString("jLabel18.text")); // NOI18N
        jLabel18.setName("jLabel18"); // NOI18N

        jTextFieldImagePath.setName("jTextFieldImagePath"); // NOI18N

        jButtonBrowseImagePath.setText(resourceMap.getString("jButtonBrowseImagePath.text")); // NOI18N
        jButtonBrowseImagePath.setName("jButtonBrowseImagePath"); // NOI18N

        jCheckBoxUseXDGOpen.setText(resourceMap.getString("jCheckBoxUseXDGOpen.text")); // NOI18N
        jCheckBoxUseXDGOpen.setName("jCheckBoxUseXDGOpen"); // NOI18N

        jLabel14.setText(resourceMap.getString("jLabel14.text")); // NOI18N
        jLabel14.setName("jLabel14"); // NOI18N

        jTextFieldPandoc.setName("jTextFieldPandoc"); // NOI18N

        jButtonBrowsePandoc.setText(resourceMap.getString("jButtonBrowsePandoc.text")); // NOI18N
        jButtonBrowsePandoc.setName("jButtonBrowsePandoc"); // NOI18N

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jCheckBoxUseXDGOpen))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel17)
                            .addComponent(jLabel14)
                            .addComponent(jLabel18))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldAttachmentPath, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
                            .addComponent(jTextFieldImagePath, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jTextFieldPandoc, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jButtonBrowseAttachmentPath, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jButtonBrowseImagePath, javax.swing.GroupLayout.Alignment.TRAILING))
                            .addComponent(jButtonBrowsePandoc))))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldAttachmentPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17)
                    .addComponent(jButtonBrowseAttachmentPath))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldImagePath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18)
                    .addComponent(jButtonBrowseImagePath))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(jTextFieldPandoc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonBrowsePandoc))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxUseXDGOpen)
                .addContainerGap())
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel7.TabConstraints.tabTitle"), jPanel7); // NOI18N

        jPanel4.setName("jPanel4"); // NOI18N

        jCheckBoxAutoUpdate.setText(resourceMap.getString("jCheckBoxAutoUpdate.text")); // NOI18N
        jCheckBoxAutoUpdate.setName("jCheckBoxAutoUpdate"); // NOI18N

        jCheckBoxCheckNightly.setText(resourceMap.getString("jCheckBoxCheckNightly.text")); // NOI18N
        jCheckBoxCheckNightly.setName("jCheckBoxCheckNightly"); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(jCheckBoxCheckNightly))
                    .addComponent(jCheckBoxAutoUpdate))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBoxAutoUpdate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxCheckNightly)
                .addContainerGap())
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel4.TabConstraints.tabTitle"), jPanel4); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButtonCancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonApply)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonCancel)
                    .addComponent(jButtonApply))
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
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private boolean modified = false;
    public boolean isModified() {
        return modified;
    }
    public final void setModified(boolean b) {
        boolean old = isModified();
        this.modified = b;
        firePropertyChange("modified", old, isModified());
    }
    private boolean synmodified = false;
    public boolean isSynModified() {
        return synmodified;
    }
    public final void setSynModified(boolean b) {
        synmodified = b;
    }
    
    /**
     * 
     * @return 
     */
    public boolean isSaveSettingsOk() {
        return savesettingok;
    }
    /**
     * return value for the main window so we know whether we have to update the display.
     * 
     * @return
     */
    public boolean getNeedsUpdate() {
        return needsupdate;
    }
    public boolean getDisplayUpdate() {
        return displayupdate;
    }
    /**
     * 
     * @return 
     */
    public boolean getNeedsLafUpdate() {
        return lafupdate;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonApply;
    private javax.swing.JButton jButtonBrowseAttachmentPath;
    private javax.swing.JButton jButtonBrowseBackup;
    private javax.swing.JButton jButtonBrowseImagePath;
    private javax.swing.JButton jButtonBrowsePandoc;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonDesktopCSS;
    private javax.swing.JButton jButtonDesktopFont;
    private javax.swing.JButton jButtonEditAutokorrekt;
    private javax.swing.JButton jButtonEditSteno;
    private javax.swing.JButton jButtonEntryCss;
    private javax.swing.JButton jButtonFont;
    private javax.swing.JButton jButtonFontcolor;
    private javax.swing.JButton jButtonHighlightKeywordStyle;
    private javax.swing.JButton jButtonHighlightLivesearchStyle;
    private javax.swing.JButton jButtonHighlightStyle;
    private javax.swing.JButton jButtonListFont;
    private javax.swing.JButton jButtonResetDesktopCSS;
    private javax.swing.JButton jButtonResetEntryCSS;
    private javax.swing.JButton jButtonSynonymEdit;
    private javax.swing.JButton jButtonTableBackgroundColor;
    private javax.swing.JCheckBox jCheckBoxAllToHist;
    private javax.swing.JCheckBox jCheckBoxAutoCompleteTags;
    private javax.swing.JCheckBox jCheckBoxAutoUpdate;
    private javax.swing.JCheckBox jCheckBoxAutobackup;
    private javax.swing.JCheckBox jCheckBoxAutocorrect;
    private javax.swing.JCheckBox jCheckBoxCheckNightly;
    private javax.swing.JCheckBox jCheckBoxDesktopCSS;
    private javax.swing.JCheckBox jCheckBoxEntryCSS;
    private javax.swing.JCheckBox jCheckBoxExtraBackup;
    private javax.swing.JCheckBox jCheckBoxFillNewEntries;
    private javax.swing.JCheckBox jCheckBoxFootnote;
    private javax.swing.JCheckBox jCheckBoxFootnoteBraces;
    private javax.swing.JCheckBox jCheckBoxIconText;
    private javax.swing.JCheckBox jCheckBoxImgResize;
    private javax.swing.JCheckBox jCheckBoxJumpToTab;
    private javax.swing.JCheckBox jCheckBoxLuhmannColSortable;
    private javax.swing.JCheckBox jCheckBoxRegistry;
    private javax.swing.JCheckBox jCheckBoxSearchWithoutFormatTags;
    private javax.swing.JCheckBox jCheckBoxShowAllIcons;
    private javax.swing.JCheckBox jCheckBoxShowEntryHeadline;
    private javax.swing.JCheckBox jCheckBoxShowHorGrid;
    private javax.swing.JCheckBox jCheckBoxShowTableBorder;
    private javax.swing.JCheckBox jCheckBoxShowToolbar;
    private javax.swing.JCheckBox jCheckBoxShowVerGrid;
    private javax.swing.JCheckBox jCheckBoxSteno;
    private javax.swing.JCheckBox jCheckBoxSynonym;
    private javax.swing.JCheckBox jCheckBoxSystray;
    private javax.swing.JCheckBox jCheckBoxUseMacBackgroundColor;
    private javax.swing.JCheckBox jCheckBoxUseMarkdown;
    private javax.swing.JCheckBox jCheckBoxUseXDGOpen;
    private javax.swing.JComboBox jComboBoxBackgroundColors;
    private javax.swing.JComboBox jComboBoxFonts;
    private javax.swing.JComboBox jComboBoxIconTheme;
    private javax.swing.JComboBox jComboBoxLAF;
    private javax.swing.JComboBox jComboBoxLocale;
    private javax.swing.JComboBox jComboBoxManualTimestamp;
    private javax.swing.JComboBox jComboBoxShowAtStartup;
    private javax.swing.JFormattedTextField jFormattedTextFieldImgHeight;
    private javax.swing.JFormattedTextField jFormattedTextFieldImgWidth;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelColor;
    private javax.swing.JLabel jLabelTableColor;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JSlider jSliderDesktopFontSize;
    private javax.swing.JSlider jSliderFontSize;
    private javax.swing.JSlider jSliderTextfields;
    private javax.swing.JSpinner jSpinnerDistHor;
    private javax.swing.JSpinner jSpinnerDistVer;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextFieldAttachmentPath;
    private javax.swing.JTextField jTextFieldBackupPath;
    private javax.swing.JTextField jTextFieldImagePath;
    private javax.swing.JTextField jTextFieldPandoc;
    // End of variables declaration//GEN-END:variables

    private CFontChooser fontDlg;
    private CAutoKorrekturEdit autoKorrektEdit;
    private CStenoEdit stenoEdit;
    private CHighlightSearchSettings highlightSettingsDlg;
    private CSynonymsEdit synonymsDlg;
    private CBiggerEditField biggerEditDlg;
}
