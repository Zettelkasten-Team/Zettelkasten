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
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import org.jdesktop.application.Action;


/**
 * Opens a find and replace dialog, where the user can specify find- and replace terms, as well as
 * the place where to do the replacement (i.e. replacing in content, auhtor values, keywords etc.).
 * 
 * @author danielludecke
 */
public class CReplaceDialog extends javax.swing.JDialog {

    /**
     * indicates whether the user cancelled the search request or not...
     */
    private boolean cancelled = true;
    /**
     * Whether we have a wholeword search or not.
     */
    private boolean wholeword;
    /**
     * Whether we have a regular expression as find-term or not
     */
    private boolean regex;
    /**
     * Whether the search is case-sensitive or not
     */
    private boolean matchcase;
    /**
     * Indicates in which parts of an entry the search should be applied to. E.g.
     * the user can search in titles only, or in authors and keywords etc...
     */
    private int where;
    /**
     *
     */
    private String findTerm;
    /**
     *
     */
    private String replaceTerm;
    private final Settings settingsObj;
    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap =
        org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
        getContext().getResourceMap(CReplaceDialog.class);
    /**
     * Opens a find and replace dialog, where the user can specify find- and replace terms, as well as
     * the place where to do the replacement (i.e. replacing in content, auhtor values, keywords etc.)
     * 
     * @param parent the parent frame of this dialog
     * @param s a reference to the CSettings-class
     * @param initialSearchTerm an initial value that will be used as search term. Use {@code null} to leave search term text field empty.
     * @param filteredSearch {@code true} when the search is a filter-request started from the CSearchResultsWindow. In this case,
     * no replacements in author nor keyword values are accepted. Usually, use {@code false} for a normal replace request.
     */
    public CReplaceDialog(java.awt.Frame parent, Settings s, String initialSearchTerm, boolean filteredSearch) {
        super(parent);
        settingsObj = s;
        initComponents();
        // set application icon
        setIconImage(Constants.zknicon.getImage());
        initListeners();
        initMnemonics();
        // init the checkboxes
        int w = settingsObj.getReplaceWhere();
        jCheckBoxReplaceTitles.setSelected((w&Constants.SEARCH_TITLE)!=0);
        jCheckBoxReplaceContent.setSelected((w&Constants.SEARCH_CONTENT)!=0);
        jCheckBoxReplaceAuthors.setSelected((w&Constants.SEARCH_AUTHOR)!=0);
        jCheckBoxReplaceKeywords.setSelected((w&Constants.SEARCH_KEYWORDS)!=0);
        jCheckBoxReplaceRemarks.setSelected((w&Constants.SEARCH_REMARKS)!=0);
        jCheckBoxReplaceAttachments.setSelected((w&Constants.SEARCH_LINKS)!=0);
        // init option-checkboxes
        w = settingsObj.getReplaceOptions();
        jCheckBoxWholeWord.setSelected((w&Constants.SEARCH_OPTION_WHOLEWORD)!=0);
        jCheckBoxMatchCase.setSelected((w&Constants.SEARCH_OPTION_MATCHCASE)!=0);
        // init textfield
        jTextFieldReplace.setText(settingsObj.getReplaceWhat());
        if (initialSearchTerm!=null) jTextFieldFind.setText(initialSearchTerm);
        // when we have a "filtered search", i.e. a find&replace-request from
        // the search window, this only applies to entry's data - not to the keyword-
        // nor author-list
        if (filteredSearch) {
            // so we have to de-select the checkboxex for replacing in authors and keywords
            jCheckBoxReplaceAuthors.setSelected(false);
            jCheckBoxReplaceKeywords.setSelected(false);
            // and disable the checkboxex for replacing in authors and keywords
            jCheckBoxReplaceAuthors.setEnabled(false);
            jCheckBoxReplaceKeywords.setEnabled(false);
        }
        if (settingsObj.isSeaGlass()) {
            jButtonCancel.putClientProperty("JComponent.sizeVariant", "small");
            jButtonReplace.putClientProperty("JComponent.sizeVariant", "small");
        }
    }

    private void initMnemonics() {
        if (!settingsObj.isMacStyle()) {
            jLabel1.setDisplayedMnemonic(0);        
            jLabel2.setDisplayedMnemonic(0);        
            jCheckBoxRegEx.setDisplayedMnemonicIndex(0);
            jCheckBoxMatchCase.setDisplayedMnemonicIndex(0);
            jCheckBoxWholeWord.setDisplayedMnemonicIndex(0);
        }
    }

    private void initListeners() {
        // these code lines add an escape-listener to the dialog. so, when the user
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
        // set default button
        getRootPane().setDefaultButton(jButtonReplace);
        // init action and other listeners
        jCheckBoxRegEx.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent evt) {
                boolean selected = jCheckBoxRegEx.isSelected();
                if (selected) {
                    checkRegExPattern();
                }
                else {
                    jTextFieldFind.setForeground(Color.BLACK);
                }
                jCheckBoxMatchCase.setEnabled(!selected);
                jCheckBoxWholeWord.setEnabled(!selected);
            }
        });
        jTextFieldFind.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent evt) {
                if (jCheckBoxRegEx.isSelected()) {
                    checkRegExPattern();
                }
                else {
                    jTextFieldFind.setForeground(Color.BLACK);
                }
            }
        });
        jTextFieldReplace.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger() && !jPopupMenuCCP.isVisible()) {
                    jPopupMenuCCP.show(jTextFieldReplace, evt.getPoint().x, evt.getPoint().y);
                }
            }
            @Override public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger() && !jPopupMenuCCP.isVisible()) {
                    jPopupMenuCCP.show(jTextFieldReplace, evt.getPoint().x, evt.getPoint().y);
                }
            }
        });
        jTextFieldFind.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger() && !jPopupMenuCCP.isVisible()) {
                    jPopupMenuCCP.show(jTextFieldFind, evt.getPoint().x, evt.getPoint().y);
                }
            }
            @Override public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger() && !jPopupMenuCCP.isVisible()) {
                    jPopupMenuCCP.show(jTextFieldFind, evt.getPoint().x, evt.getPoint().y);
                }
            }
        });
        // finally, we have to manuall init the actions for the popup-menu, since the gui-builder always
        // puts the menu-items before the line where the action-map is initialised. we cannot change
        // this because it is in the protected area, and when changing it from outside, it will
        // always be re-arranged by the gui-designer
        // get the action map
        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.
            getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().
            getActionMap(CReplaceDialog.class, this);
        popupCCPcut.setAction(actionMap.get("cut"));
        popupCCPcopy.setAction(actionMap.get("copy"));
        popupCCPpaste.setAction(actionMap.get("paste"));

    }


    @Action
    public void cancel() {
        cancelled = true;
        dispose();
        setVisible(false);
    }

    @Action
    public void replace() {
        // first of all, check the reg-ex-pattern, in case we have a regular expression
        regex = jCheckBoxRegEx.isSelected();
        if (regex && !checkRegExPattern()) {
            // display error message box
            JOptionPane.showMessageDialog(null,resourceMap.getString("errWrongRegExMsg"),resourceMap.getString("errWrongRegExTitle"),JOptionPane.PLAIN_MESSAGE);
            // set focus back to textfield
            jTextFieldFind.requestFocusInWindow();
            // leave method
            return;
        }
        // get find and replace terms
        findTerm = jTextFieldFind.getText();
        replaceTerm = jTextFieldReplace.getText();
        // no cancel-operation, search is ok
        cancelled = false;
        // get wholeword and matchcase settings
        wholeword = jCheckBoxWholeWord.isSelected();
        matchcase = jCheckBoxMatchCase.isSelected();
        // check out where to replace
        where = 0;
        // now check where the user wants to search in...
        if (jCheckBoxReplaceTitles.isSelected()) where = where | Constants.SEARCH_TITLE;
        if (jCheckBoxReplaceContent.isSelected()) where = where | Constants.SEARCH_CONTENT;
        if (jCheckBoxReplaceAuthors.isSelected()) where = where | Constants.SEARCH_AUTHOR;
        if (jCheckBoxReplaceKeywords.isSelected()) where = where | Constants.SEARCH_KEYWORDS;
        if (jCheckBoxReplaceRemarks.isSelected()) where = where | Constants.SEARCH_REMARKS;
        if (jCheckBoxReplaceAttachments.isSelected()) where = where | Constants.SEARCH_LINKS;
        // save user settings
        settingsObj.setReplaceWhere(where);
        settingsObj.setReplaceWhat(replaceTerm);
        // save the checked search options
        int options = 0;
        if (wholeword) options = options | Constants.SEARCH_OPTION_WHOLEWORD;
        if (matchcase) options = options | Constants.SEARCH_OPTION_MATCHCASE;

        settingsObj.setReplaceOptions(options);
        // hide window
        dispose();
        setVisible(false);
    }


    private boolean checkRegExPattern() {
        try {
            Pattern.compile(jTextFieldFind.getText());
        }
        catch (PatternSyntaxException e) {
            jTextFieldFind.setForeground(Color.RED);
            return false;
        }
        jTextFieldFind.setForeground(Color.BLACK);
        return true;
    }


    /**
     * 
     * @return 
     */
    public boolean isCancelled() {
        return cancelled;
    }
    /**
     * 
     * @return 
     */
    public boolean isWholeWord() {
        return wholeword;
    }
    /**
     * 
     * @return 
     */
    public boolean isRegEx() {
        return regex;
    }
    /**
     * 
     * @return 
     */
    public boolean isMatchCase() {
        return matchcase;
    }
    /**
     * 
     * @return 
     */
    public int getWhereToSearch() {
        return where;
    }
    /**
     * 
     * @return 
     */
    public String getFindTerm() {
        return findTerm;
    }
    /**
     * 
     * @return 
     */
    public String getReplaceTerm() {
        return replaceTerm;
    }
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    // Generated using JFormDesigner Evaluation license - Ralf Barkow
    private void initComponents() {
        ResourceBundle bundle = ResourceBundle.getBundle("de.danielluedecke.zettelkasten.resources.CReplaceDialog");
        jPanel1 = new JPanel();
        jLabel1 = new JLabel();
        jTextFieldFind = new JTextField();
        jLabel2 = new JLabel();
        jTextFieldReplace = new JTextField();
        jPanel2 = new JPanel();
        jCheckBoxMatchCase = new JCheckBox();
        jCheckBoxWholeWord = new JCheckBox();
        jPanel3 = new JPanel();
        jCheckBoxReplaceTitles = new JCheckBox();
        jCheckBoxReplaceContent = new JCheckBox();
        jCheckBoxReplaceRemarks = new JCheckBox();
        jCheckBoxReplaceKeywords = new JCheckBox();
        jCheckBoxReplaceAuthors = new JCheckBox();
        jCheckBoxReplaceAttachments = new JCheckBox();
        jButtonCancel = new JButton();
        jButtonReplace = new JButton();
        jCheckBoxRegEx = new JCheckBox();
        jPopupMenuCCP = new JPopupMenu();
        popupCCPcut = new JMenuItem();
        popupCCPcopy = new JMenuItem();
        popupCCPpaste = new JMenuItem();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(bundle.getString("FormReplaceDialog.title"));
        setModal(true);
        setName("FormReplaceDialog");
        setResizable(false);
        Container contentPane = getContentPane();

        //======== jPanel1 ========
        {
            jPanel1.setName("jPanel1");
            jPanel1.setBorder (new javax. swing. border. CompoundBorder( new javax .swing .border .TitledBorder (new javax. swing. border.
            EmptyBorder( 0, 0, 0, 0) , "JF\u006frmD\u0065sig\u006eer \u0045val\u0075ati\u006fn", javax. swing. border. TitledBorder. CENTER, javax. swing
            . border. TitledBorder. BOTTOM, new java .awt .Font ("Dia\u006cog" ,java .awt .Font .BOLD ,12 ),
            java. awt. Color. red) ,jPanel1. getBorder( )) ); jPanel1. addPropertyChangeListener (new java. beans. PropertyChangeListener( )
            { @Override public void propertyChange (java .beans .PropertyChangeEvent e) {if ("\u0062ord\u0065r" .equals (e .getPropertyName () ))
            throw new RuntimeException( ); }} );

            //---- jLabel1 ----
            jLabel1.setLabelFor(jTextFieldFind);
            jLabel1.setText(bundle.getString("jLabel1.text"));
            jLabel1.setName("jLabel1");

            //---- jTextFieldFind ----
            jTextFieldFind.setName("jTextFieldFind");

            //---- jLabel2 ----
            jLabel2.setLabelFor(jTextFieldReplace);
            jLabel2.setText(bundle.getString("jLabel2.text"));
            jLabel2.setName("jLabel2");

            //---- jTextFieldReplace ----
            jTextFieldReplace.setName("jTextFieldReplace");

            //======== jPanel2 ========
            {
                jPanel2.setBorder(new TitledBorder("Suchoptionen"));
                jPanel2.setName("jPanel2");

                //---- jCheckBoxMatchCase ----
                jCheckBoxMatchCase.setText(bundle.getString("jCheckBoxMatchCase.text"));
                jCheckBoxMatchCase.setName("jCheckBoxMatchCase");

                //---- jCheckBoxWholeWord ----
                jCheckBoxWholeWord.setText(bundle.getString("jCheckBoxWholeWord.text"));
                jCheckBoxWholeWord.setName("jCheckBoxWholeWord");

                GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
                jPanel2.setLayout(jPanel2Layout);
                jPanel2Layout.setHorizontalGroup(
                    jPanel2Layout.createParallelGroup()
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(jPanel2Layout.createParallelGroup()
                                .addComponent(jCheckBoxMatchCase)
                                .addComponent(jCheckBoxWholeWord))
                            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
                jPanel2Layout.setVerticalGroup(
                    jPanel2Layout.createParallelGroup()
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jCheckBoxMatchCase)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jCheckBoxWholeWord)
                            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
            }

            //======== jPanel3 ========
            {
                jPanel3.setBorder(new TitledBorder("Ersetzen in"));
                jPanel3.setName("jPanel3");

                //---- jCheckBoxReplaceTitles ----
                jCheckBoxReplaceTitles.setText(bundle.getString("jCheckBoxReplaceTitles.text"));
                jCheckBoxReplaceTitles.setName("jCheckBoxReplaceTitles");

                //---- jCheckBoxReplaceContent ----
                jCheckBoxReplaceContent.setText(bundle.getString("jCheckBoxReplaceContent.text"));
                jCheckBoxReplaceContent.setName("jCheckBoxReplaceContent");

                //---- jCheckBoxReplaceRemarks ----
                jCheckBoxReplaceRemarks.setText(bundle.getString("jCheckBoxReplaceRemarks.text"));
                jCheckBoxReplaceRemarks.setName("jCheckBoxReplaceRemarks");

                //---- jCheckBoxReplaceKeywords ----
                jCheckBoxReplaceKeywords.setText(bundle.getString("jCheckBoxReplaceKeywords.text"));
                jCheckBoxReplaceKeywords.setName("jCheckBoxReplaceKeywords");

                //---- jCheckBoxReplaceAuthors ----
                jCheckBoxReplaceAuthors.setText(bundle.getString("jCheckBoxReplaceAuthors.text"));
                jCheckBoxReplaceAuthors.setName("jCheckBoxReplaceAuthors");

                //---- jCheckBoxReplaceAttachments ----
                jCheckBoxReplaceAttachments.setText(bundle.getString("jCheckBoxReplaceAttachments.text"));
                jCheckBoxReplaceAttachments.setName("jCheckBoxReplaceAttachments");

                GroupLayout jPanel3Layout = new GroupLayout(jPanel3);
                jPanel3.setLayout(jPanel3Layout);
                jPanel3Layout.setHorizontalGroup(
                    jPanel3Layout.createParallelGroup()
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(jPanel3Layout.createParallelGroup()
                                .addComponent(jCheckBoxReplaceRemarks)
                                .addComponent(jCheckBoxReplaceTitles)
                                .addComponent(jCheckBoxReplaceContent))
                            .addGap(18, 18, 18)
                            .addGroup(jPanel3Layout.createParallelGroup()
                                .addComponent(jCheckBoxReplaceAuthors)
                                .addComponent(jCheckBoxReplaceKeywords)
                                .addComponent(jCheckBoxReplaceAttachments))
                            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
                jPanel3Layout.setVerticalGroup(
                    jPanel3Layout.createParallelGroup()
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(jPanel3Layout.createParallelGroup()
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addComponent(jCheckBoxReplaceKeywords)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jCheckBoxReplaceAuthors)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jCheckBoxReplaceAttachments))
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addComponent(jCheckBoxReplaceTitles)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jCheckBoxReplaceContent)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jCheckBoxReplaceRemarks)))
                            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
            }

            //---- jButtonCancel ----
            jButtonCancel.setName("jButtonCancel");

            //---- jButtonReplace ----
            jButtonReplace.setName("jButtonReplace");

            //---- jCheckBoxRegEx ----
            jCheckBoxRegEx.setText(bundle.getString("jCheckBoxRegEx.text"));
            jCheckBoxRegEx.setToolTipText(bundle.getString("jCheckBoxRegEx.toolTipText"));
            jCheckBoxRegEx.setName("jCheckBoxRegEx");

            GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
            jPanel1.setLayout(jPanel1Layout);
            jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup()
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup()
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup()
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jCheckBoxRegEx)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jTextFieldFind)
                            .addComponent(jTextFieldReplace))
                        .addContainerGap())
                    .addGroup(GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonCancel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonReplace)
                        .addGap(6, 6, 6))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jPanel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jPanel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
            );
            jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup()
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jTextFieldFind, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, 0)
                        .addComponent(jCheckBoxRegEx)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jTextFieldReplace, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(jButtonCancel)
                            .addComponent(jButtonReplace)))
            );
        }

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        contentPaneLayout.setVerticalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addGap(3, 3, 3))
        );
        pack();
        setLocationRelativeTo(getOwner());

        //======== jPopupMenuCCP ========
        {
            jPopupMenuCCP.setName("jPopupMenuCCP");

            //---- popupCCPcut ----
            popupCCPcut.setName("popupCCPcut");
            jPopupMenuCCP.add(popupCCPcut);

            //---- popupCCPcopy ----
            popupCCPcopy.setName("popupCCPcopy");
            jPopupMenuCCP.add(popupCCPcopy);

            //---- popupCCPpaste ----
            popupCCPpaste.setName("popupCCPpaste");
            jPopupMenuCCP.add(popupCCPpaste);
        }
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Ralf Barkow
    private JPanel jPanel1;
    private JLabel jLabel1;
    private JTextField jTextFieldFind;
    private JLabel jLabel2;
    private JTextField jTextFieldReplace;
    private JPanel jPanel2;
    private JCheckBox jCheckBoxMatchCase;
    private JCheckBox jCheckBoxWholeWord;
    private JPanel jPanel3;
    private JCheckBox jCheckBoxReplaceTitles;
    private JCheckBox jCheckBoxReplaceContent;
    private JCheckBox jCheckBoxReplaceRemarks;
    private JCheckBox jCheckBoxReplaceKeywords;
    private JCheckBox jCheckBoxReplaceAuthors;
    private JCheckBox jCheckBoxReplaceAttachments;
    private JButton jButtonCancel;
    private JButton jButtonReplace;
    private JCheckBox jCheckBoxRegEx;
    private JPopupMenu jPopupMenuCCP;
    private JMenuItem popupCCPcut;
    private JMenuItem popupCCPcopy;
    private JMenuItem popupCCPpaste;
    // End of variables declaration//GEN-END:variables

}
