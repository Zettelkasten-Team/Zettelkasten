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
        if (!settingsObj.isMacAqua()) {
            jLabel1.setDisplayedMnemonic(0);        
            jLabel2.setDisplayedMnemonic(0);        
            jCheckBoxRegEx.setDisplayedMnemonicIndex(0);
            jCheckBoxMatchCase.setDisplayedMnemonicIndex(0);
            jCheckBoxWholeWord.setDisplayedMnemonicIndex(0);
        }
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
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenuCCP = new javax.swing.JPopupMenu();
        popupCCPcut = new javax.swing.JMenuItem();
        popupCCPcopy = new javax.swing.JMenuItem();
        popupCCPpaste = new javax.swing.JMenuItem();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldFind = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldReplace = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jCheckBoxMatchCase = new javax.swing.JCheckBox();
        jCheckBoxWholeWord = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jCheckBoxReplaceTitles = new javax.swing.JCheckBox();
        jCheckBoxReplaceContent = new javax.swing.JCheckBox();
        jCheckBoxReplaceRemarks = new javax.swing.JCheckBox();
        jCheckBoxReplaceKeywords = new javax.swing.JCheckBox();
        jCheckBoxReplaceAuthors = new javax.swing.JCheckBox();
        jCheckBoxReplaceAttachments = new javax.swing.JCheckBox();
        jButtonCancel = new javax.swing.JButton();
        jButtonReplace = new javax.swing.JButton();
        jCheckBoxRegEx = new javax.swing.JCheckBox();

        jPopupMenuCCP.setName("jPopupMenuCCP"); // NOI18N

        popupCCPcut.setName("popupCCPcut"); // NOI18N
        jPopupMenuCCP.add(popupCCPcut);

        popupCCPcopy.setName("popupCCPcopy"); // NOI18N
        jPopupMenuCCP.add(popupCCPcopy);

        popupCCPpaste.setName("popupCCPpaste"); // NOI18N
        jPopupMenuCCP.add(popupCCPpaste);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getResourceMap(CReplaceDialog.class);
        setTitle(resourceMap.getString("FormReplaceDialog.title")); // NOI18N
        setModal(true);
        setName("FormReplaceDialog"); // NOI18N
        setResizable(false);

        jPanel1.setName("jPanel1"); // NOI18N

        jLabel1.setLabelFor(jTextFieldFind);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jTextFieldFind.setName("jTextFieldFind"); // NOI18N

        jLabel2.setLabelFor(jTextFieldReplace);
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jTextFieldReplace.setName("jTextFieldReplace"); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        jCheckBoxMatchCase.setText(resourceMap.getString("jCheckBoxMatchCase.text")); // NOI18N
        jCheckBoxMatchCase.setName("jCheckBoxMatchCase"); // NOI18N

        jCheckBoxWholeWord.setText(resourceMap.getString("jCheckBoxWholeWord.text")); // NOI18N
        jCheckBoxWholeWord.setName("jCheckBoxWholeWord"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBoxMatchCase)
                    .addComponent(jCheckBoxWholeWord))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBoxMatchCase)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxWholeWord)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel3.border.title"))); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N

        jCheckBoxReplaceTitles.setText(resourceMap.getString("jCheckBoxReplaceTitles.text")); // NOI18N
        jCheckBoxReplaceTitles.setName("jCheckBoxReplaceTitles"); // NOI18N

        jCheckBoxReplaceContent.setText(resourceMap.getString("jCheckBoxReplaceContent.text")); // NOI18N
        jCheckBoxReplaceContent.setName("jCheckBoxReplaceContent"); // NOI18N

        jCheckBoxReplaceRemarks.setText(resourceMap.getString("jCheckBoxReplaceRemarks.text")); // NOI18N
        jCheckBoxReplaceRemarks.setName("jCheckBoxReplaceRemarks"); // NOI18N

        jCheckBoxReplaceKeywords.setText(resourceMap.getString("jCheckBoxReplaceKeywords.text")); // NOI18N
        jCheckBoxReplaceKeywords.setName("jCheckBoxReplaceKeywords"); // NOI18N

        jCheckBoxReplaceAuthors.setText(resourceMap.getString("jCheckBoxReplaceAuthors.text")); // NOI18N
        jCheckBoxReplaceAuthors.setName("jCheckBoxReplaceAuthors"); // NOI18N

        jCheckBoxReplaceAttachments.setText(resourceMap.getString("jCheckBoxReplaceAttachments.text")); // NOI18N
        jCheckBoxReplaceAttachments.setName("jCheckBoxReplaceAttachments"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBoxReplaceRemarks)
                    .addComponent(jCheckBoxReplaceTitles)
                    .addComponent(jCheckBoxReplaceContent))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBoxReplaceAuthors)
                    .addComponent(jCheckBoxReplaceKeywords)
                    .addComponent(jCheckBoxReplaceAttachments))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jCheckBoxReplaceKeywords)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxReplaceAuthors)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxReplaceAttachments))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jCheckBoxReplaceTitles)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxReplaceContent)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxReplaceRemarks)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getActionMap(CReplaceDialog.class, this);
        jButtonCancel.setAction(actionMap.get("cancel")); // NOI18N
        jButtonCancel.setName("jButtonCancel"); // NOI18N

        jButtonReplace.setAction(actionMap.get("replace")); // NOI18N
        jButtonReplace.setName("jButtonReplace"); // NOI18N

        jCheckBoxRegEx.setText(resourceMap.getString("jCheckBoxRegEx.text")); // NOI18N
        jCheckBoxRegEx.setToolTipText(resourceMap.getString("jCheckBoxRegEx.toolTipText")); // NOI18N
        jCheckBoxRegEx.setName("jCheckBoxRegEx"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jCheckBoxRegEx)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jTextFieldFind)
                    .addComponent(jTextFieldReplace))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonCancel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonReplace)
                .addGap(6, 6, 6))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextFieldFind, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addComponent(jCheckBoxRegEx)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextFieldReplace, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonCancel)
                    .addComponent(jButtonReplace)))
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
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonReplace;
    private javax.swing.JCheckBox jCheckBoxMatchCase;
    private javax.swing.JCheckBox jCheckBoxRegEx;
    private javax.swing.JCheckBox jCheckBoxReplaceAttachments;
    private javax.swing.JCheckBox jCheckBoxReplaceAuthors;
    private javax.swing.JCheckBox jCheckBoxReplaceContent;
    private javax.swing.JCheckBox jCheckBoxReplaceKeywords;
    private javax.swing.JCheckBox jCheckBoxReplaceRemarks;
    private javax.swing.JCheckBox jCheckBoxReplaceTitles;
    private javax.swing.JCheckBox jCheckBoxWholeWord;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPopupMenu jPopupMenuCCP;
    private javax.swing.JTextField jTextFieldFind;
    private javax.swing.JTextField jTextFieldReplace;
    private javax.swing.JMenuItem popupCCPcopy;
    private javax.swing.JMenuItem popupCCPcut;
    private javax.swing.JMenuItem popupCCPpaste;
    // End of variables declaration//GEN-END:variables

}
