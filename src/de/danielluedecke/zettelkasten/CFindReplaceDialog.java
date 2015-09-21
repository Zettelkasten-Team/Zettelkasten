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
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.application.Action;

/**
 *
 * @author danielludecke
 */
public class CFindReplaceDialog extends javax.swing.JDialog {

    private final javax.swing.JTextArea textarea;
    private int findpos = -1;
    private final LinkedList<Integer[]> findselections = new LinkedList<>();
    private static final int REPLACE_TEXT = 0;
    private static final int FIND_TEXT = 1;
    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap =
        org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
        getContext().getResourceMap(CFindReplaceDialog.class);

    /** 
     * Creates new form CFindReplaceDialog
     * @param parent 
     * @param ta 
     * @param settingsObj 
     */
    public CFindReplaceDialog(java.awt.Frame parent, javax.swing.JTextArea ta, Settings settingsObj) {
        super(parent);
        initComponents();
        // set application icon
        setIconImage(Constants.zknicon.getImage());
        textarea = ta;
        initListeners();
        if (settingsObj.isSeaGlass()) {
            jButtonFindNext.putClientProperty("JComponent.sizeVariant", "small");
            jButtonFindPrev.putClientProperty("JComponent.sizeVariant", "small");
            jButtonReplace.putClientProperty("JComponent.sizeVariant", "small");
            jButtonReplaceAll.putClientProperty("JComponent.sizeVariant", "small");
        }
    }


    private void initListeners() {
        // these codelines add an escape-listener to the dialog. so, when the user
        // presses the escape-key, the same action is performed as if the user
        // presses the cancel button...
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        ActionListener cancelAction = new java.awt.event.ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                dispose();
                setVisible(false);
            }
        };
        getRootPane().registerKeyboardAction(cancelAction, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        // set default button
        getRootPane().setDefaultButton(jButtonFindNext);
        jTextFieldFind.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void changedUpdate(DocumentEvent e) { initValues(); }
            @Override public void insertUpdate(DocumentEvent e) { initValues(); }
            @Override public void removeUpdate(DocumentEvent e) { initValues(); }
        });
        jCheckBoxMatchCase.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetAll();
            }
        });
        jCheckBoxWholeWord.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetAll();
            }
        });
        jCheckBoxRegEx.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetAll();
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
            getActionMap(CFindReplaceDialog.class, this);
        popupCCPcut.setAction(actionMap.get("cut"));
        popupCCPcopy.setAction(actionMap.get("copy"));
        popupCCPpaste.setAction(actionMap.get("paste"));
    }

    private void resetAll() {
        initValues();
        if (initmatcher()) findNext();
    }


    private void initValues() {
        // init list where we store the start/end-positions of the found terms
        findselections.clear();
        // disable buttons
        jButtonReplace.setEnabled(false);
        jButtonFindPrev.setEnabled(false);
        findpos = -1;
    }

    private boolean initmatcher() {
        return initmatcher(true);
    }


    private boolean initmatcher(boolean resetFindPos) {
        Matcher findmatcher;
        // retrieve findtext
        String text = jTextFieldFind.getText();
        // if we have no findtext, reset buttons
        if (text.isEmpty()) {
            initValues();
            return false;
        }
        // check whether the user wants to find a regular expression or not
        // if not, prepare findterm and surround it with the regular expressions
        // for whole word and matchcase.
        if (!jCheckBoxRegEx.isSelected()) {
            // if the findterm contains meta-chars of a regular expression, although no regular
            // expression search is requested, escape all these meta-chars...
            text = Pattern.quote(text);
            // when we have a whole-word-find&replace, surround findterm with
            // the regular expression that indicates word beginning and ending (i.e. whole word)
            if (jCheckBoxWholeWord.isSelected()) text = "\\b"+text+"\\b";
            // when the find & replace is *not* case-sensitive, set regular expression
            // to ignore the case...
            if (!jCheckBoxMatchCase.isSelected()) text = "(?i)"+text;
            // the final findterm now might look like this:
            // "(?i)\\b<findterm>\\b", in case we ignore case and have whole word search
        }
        try {
            // create a pattern from the first search term. if it fails, go on
            // to the catch-block, else contiue here.
            Pattern p = Pattern.compile(text);
            // now we know we have a valid regular expression. we now want to
            // retrieve all matching groups
            findmatcher = p.matcher(textarea.getText());
            // init findpos...
            if (resetFindPos) findpos = 0;
            // init list where we store the start/end-positions of the found terms
            findselections.clear();
            // set textcolor of textfield to black to indicate the regular expression
            // is a valid term
            jTextFieldFind.setForeground(Color.BLACK);
            // find all matches and copy the start/end-positions to our arraylist
            // we now can easily retrieve the found terms and their positions via this
            // array, thus navigation with find-next and find-prev-buttons is simple
            while (findmatcher.find()) findselections.add(new Integer[] {findmatcher.start(),findmatcher.end()});
            // update match-label
            updateMatchLabel(FIND_TEXT);
        }
        catch (PatternSyntaxException e) {
            // set textcolor of textfield to red to indicate the regular expression
            // is an invalid syntax
            jTextFieldFind.setForeground(Color.RED);
            // init list where we store the start/end-positions of the found terms
            findselections.clear();
            // disable buttons
            jButtonReplace.setEnabled(false);
            jButtonFindPrev.setEnabled(false);
            // and leave method
            return false;
        }
        return (findselections.size()>0);
    }

    
    private void updateMatchLabel(int val) {
        switch (val) {
            case REPLACE_TEXT:
                // set status label to tell the user about the amounf of found matches
                jLabelMatches.setText(resourceMap.getString("replacedTermsText",String.valueOf(findselections.size())));
                jLabelMatches.setForeground(resourceMap.getColor("jLabelMatches.foreground"));
                break;
            default:
                // set status label to tell the user about the amounf of found matches
                jLabelMatches.setText(findselections.size()>0 ? 
                        resourceMap.getString("matchesText",String.valueOf(findselections.size())) :
                        resourceMap.getString("noMatchText"));
                // switch color according to match count
                jLabelMatches.setForeground(findselections.size()>0 ? resourceMap.getColor("jLabelMatches.foreground") : new Color(160,51,51));
                break;
        }
    }
    

    @Action
    public void findNext() {
        // when we have no founds, init matcher
        if (findselections.isEmpty()) {
            initmatcher();
        }
        // check whether we have any found at all
        if (findselections.size()>0) {
            // as long as we haven't reached the last match...
            if (findpos<findselections.size()) {
                // when we have a negative index (might be possible, when
                // using the "findPrev"-method and the first match was found.
                // in this case, findpos was zero and by "findpos--" it was decreased to -1
                if (findpos<0) findpos=0;
                // select next occurence of find term
                textarea.setSelectionStart(findselections.get(findpos)[0]);
                textarea.moveCaretPosition(findselections.get(findpos)[1]);
                // increase our find-counter
                findpos++;
                // enable buttons
                jButtonReplace.setEnabled(true);
                jButtonFindPrev.setEnabled(true);
            }
            else {
                findpos=0;
                // select next occurence of find term
                textarea.setSelectionStart(findselections.get(findpos)[0]);
                textarea.moveCaretPosition(findselections.get(findpos)[1]);
                // increase our find-counter
                findpos++;
            }
        }
        else {
            initValues();
        }
    }

    @Action
    public void findPrev() {
        // check whether we have any found at all
        if (findselections.size()>0) {
            // as long as we havem't reached the last match...
            if (findpos>=0) {
                // when we have a larger index that array-size (might be possible, when
                // using the "findNext"-method and the last match was found.
                // in this case, findpos is equal to the array-size and by "findpos++"
                // it was increased to a larger index than array size
                if (findpos>=findselections.size()) findpos=findselections.size()-1;
                // select next occurence of find term
                textarea.setSelectionStart(findselections.get(findpos)[0]);
                textarea.moveCaretPosition(findselections.get(findpos)[1]);
                // decrease our find-counter
                findpos--;
                // enable buttons
                jButtonReplace.setEnabled(true);
                jButtonFindPrev.setEnabled(true);
            }
            // when we reached the first match, start from end again
            else {
                findpos = findselections.size()-1;
                // select next occurence of find term
                textarea.setSelectionStart(findselections.get(findpos)[0]);
                textarea.moveCaretPosition(findselections.get(findpos)[1]);
                // decrease our find-counter
                findpos--;
            }
        }
        else {
            initValues();
        }
    }

    @Action
    public void replace() {
        if (textarea.getSelectedText()!=null) {
            textarea.replaceSelection(jTextFieldReplace.getText());
        }
        if (initmatcher(false)) {
            findNext();
        }
        else {
            initValues();
        }
    }

    @Action
    public void replaceAll() {
        if (initmatcher()) {
            for (int cnt=findselections.size()-1;cnt>=0; cnt--) {
                textarea.setSelectionStart(findselections.get(cnt)[0]);
                textarea.moveCaretPosition(findselections.get(cnt)[1]);
                if (textarea.getSelectedText()!=null) textarea.replaceSelection(jTextFieldReplace.getText());
            }
        }
        if (findselections.size()>0) {
            textarea.setCaretPosition(findselections.get(findselections.size()-1)[1]);
            // update match-label
            updateMatchLabel(REPLACE_TEXT);
            // reset values
            initValues();
        }
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
        jCheckBoxMatchCase = new javax.swing.JCheckBox();
        jCheckBoxWholeWord = new javax.swing.JCheckBox();
        jButtonFindNext = new javax.swing.JButton();
        jButtonFindPrev = new javax.swing.JButton();
        jButtonReplace = new javax.swing.JButton();
        jButtonReplaceAll = new javax.swing.JButton();
        jCheckBoxRegEx = new javax.swing.JCheckBox();
        jLabelMatches = new javax.swing.JLabel();

        jPopupMenuCCP.setName("jPopupMenuCCP"); // NOI18N

        popupCCPcut.setName("popupCCPcut"); // NOI18N
        jPopupMenuCCP.add(popupCCPcut);

        popupCCPcopy.setName("popupCCPcopy"); // NOI18N
        jPopupMenuCCP.add(popupCCPcopy);

        popupCCPpaste.setName("popupCCPpaste"); // NOI18N
        jPopupMenuCCP.add(popupCCPpaste);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getResourceMap(CFindReplaceDialog.class);
        setTitle(resourceMap.getString("CFindReplaceDialog.title")); // NOI18N
        setAlwaysOnTop(true);
        setName("CFindReplaceDialog"); // NOI18N
        setResizable(false);

        jPanel1.setName("jPanel1"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jTextFieldFind.setName("jTextFieldFind"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jTextFieldReplace.setName("jTextFieldReplace"); // NOI18N

        jCheckBoxMatchCase.setText(resourceMap.getString("jCheckBoxMatchCase.text")); // NOI18N
        jCheckBoxMatchCase.setName("jCheckBoxMatchCase"); // NOI18N

        jCheckBoxWholeWord.setText(resourceMap.getString("jCheckBoxWholeWord.text")); // NOI18N
        jCheckBoxWholeWord.setName("jCheckBoxWholeWord"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getActionMap(CFindReplaceDialog.class, this);
        jButtonFindNext.setAction(actionMap.get("findNext")); // NOI18N
        jButtonFindNext.setName("jButtonFindNext"); // NOI18N

        jButtonFindPrev.setAction(actionMap.get("findPrev")); // NOI18N
        jButtonFindPrev.setName("jButtonFindPrev"); // NOI18N

        jButtonReplace.setAction(actionMap.get("replace")); // NOI18N
        jButtonReplace.setName("jButtonReplace"); // NOI18N

        jButtonReplaceAll.setAction(actionMap.get("replaceAll")); // NOI18N
        jButtonReplaceAll.setName("jButtonReplaceAll"); // NOI18N

        jCheckBoxRegEx.setText(resourceMap.getString("jCheckBoxRegEx.text")); // NOI18N
        jCheckBoxRegEx.setName("jCheckBoxRegEx"); // NOI18N

        jLabelMatches.setForeground(resourceMap.getColor("jLabelMatches.foreground")); // NOI18N
        jLabelMatches.setText(resourceMap.getString("jLabelMatches.text")); // NOI18N
        jLabelMatches.setName("jLabelMatches"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonReplaceAll)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonReplace)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonFindPrev)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonFindNext))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabelMatches)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(jCheckBoxRegEx)
                            .addContainerGap())
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jCheckBoxMatchCase)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBoxWholeWord)
                                .addContainerGap())
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jTextFieldFind, javax.swing.GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE)
                                .addComponent(jTextFieldReplace, javax.swing.GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE))))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldFind, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextFieldReplace, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxMatchCase)
                    .addComponent(jCheckBoxWholeWord))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxRegEx)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelMatches)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonFindNext)
                    .addComponent(jButtonFindPrev)
                    .addComponent(jButtonReplace)
                    .addComponent(jButtonReplaceAll)))
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
    private javax.swing.JButton jButtonFindNext;
    private javax.swing.JButton jButtonFindPrev;
    private javax.swing.JButton jButtonReplace;
    private javax.swing.JButton jButtonReplaceAll;
    private javax.swing.JCheckBox jCheckBoxMatchCase;
    private javax.swing.JCheckBox jCheckBoxRegEx;
    private javax.swing.JCheckBox jCheckBoxWholeWord;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelMatches;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPopupMenu jPopupMenuCCP;
    private javax.swing.JTextField jTextFieldFind;
    private javax.swing.JTextField jTextFieldReplace;
    private javax.swing.JMenuItem popupCCPcopy;
    private javax.swing.JMenuItem popupCCPcut;
    private javax.swing.JMenuItem popupCCPpaste;
    // End of variables declaration//GEN-END:variables

}
