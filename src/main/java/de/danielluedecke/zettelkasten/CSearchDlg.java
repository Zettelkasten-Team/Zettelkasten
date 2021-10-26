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
import de.danielluedecke.zettelkasten.database.SearchRequests;
import de.danielluedecke.zettelkasten.util.Constants;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import org.jdesktop.application.Action;

/**
 *
 * @author  danielludecke
 */
public class CSearchDlg extends javax.swing.JDialog {    
    /**
     * indicates whether the user cancelled the search request or not...
     */
    private boolean cancelled = true;
    public boolean isCancelled() {
        return cancelled;
    }
    /**
     * Here we store the searchterms that are retrieved from the jTextFieldSearchterms. Since
     * searchterms can be separated with commas, we might have several searchterms, thus using
     * an array to store them...
     */
    private String[] searchterms = null;
    /**
     * Here we store the searchterms that are retrieved from the jTextFieldSearchterms. Since
     * searchterms can be separated with commas, we might have several searchterms, thus using
     * an array to store them...
     * 
     * @return a String-array that contains all search terms, or {@code null} if
     * the search request was cancelled.
     */
    public String[] getSearchTerms() {
        return searchterms;
    }
    /**
     * Here we tell the program, whether the user wants logical-or, logical-and or
     * logical-not-search.
     */
    private int logical;
    /**
     * This method retrieves the logical of the combination of the search, i.e.
     * if the {@link #searchterms} only match when all search terms are found within
     * an entry (logical-and), or only one of them (logical-or) etc.
     * 
     * @return One of the following constants:
     * <ul>
     * <li>Constants.LOG_AND</li>
     * <li>Constants.LOG_OR</li>
     * <li>Constants.LOG_NOT</li>
     * </ul>
     */
    public int getLogical() {
        return logical;
    }
    /**
     * Whether we have a wholeword search or not.
     */
    private boolean wholeword;
    /**
     * Whether we have a wholeword search or not.
     * 
     * @return {@code true} when the search should match whole words only, {@code false}
     * when search terms also may match only parts within words.
     */
    public boolean isWholeWord() {
        return wholeword;
    }
    /**
     * Whether the search is case-sensitive or not
     */
    private boolean matchcase;
    /**
     * Whether the search is case-sensitive or not
     * 
     * @return {@code true} when the search should be case-sensitive, {@code false}
     * when the case can be ignored.
     */
    public boolean isMatchCase() {
        return matchcase;
    }
    /**
     * Whether synonyms should be included into the search or not
     */
    private boolean synonyms;
    /**
     * Whether synonyms should be included into the search or not. In case
     * synonyms should be included, not only the direct search terms, but also
     * possible relates synonyms (see {@code synonyms} class in the database-package).
     * 
     * @return {@code true} when synonyms should be included into the search,
     * {@code false} otherwise.
     */
    public boolean isSynonymsIncluded() {
        return synonyms;
    }
    /**
     * 
     */
    private boolean regex;
    /**
     * Indicates whether the search term is a <i>regular expression</i> or
     * a "simple" search term.
     * 
     * @return {@code true} when the search should use regular expression patterns,
     * {@code false} if the user wants to conduct a normal search.
     */
    public boolean isRegExSearch() {
        return regex;
    }
    /**
     * Indicates in which parts of an entry the search should be applied to. E.g.
     * the user can search in titles only, or in authors and keywords etc...
     */
    private int where;
    /**
     * Indicates in which parts of an entry the search should be applied to. E.g.
     * the user can search in titles only, or in authors and keywords etc...
     * 
     * @return a logical combination of following constants:
     * <ul>
     * <li>Constants.SEARCH_TITLE</li>
     * <li>Constants.SEARCH_CONTENT</li>
     * <li>Constants.SEARCH_AUTHOR</li>
     * <li>Constants.SEARCH_KEYWORDS</li>
     * <li>Constants.SEARCH_REMARKS</li>
     * <li>Constants.SEARCH_LINKS</li>
     * <li>Constants.SEARCH_LINKCONTENT</li>
     * </ul>
     */
    public int getWhereToSearch() {
        return where;
    }
    /**
     * Indicates whether the user ticked the heckbox which starts a search for entries
     * within a certain period
     */
    private boolean istimesearch;
    /**
     * Indicates whether the user wants to search for entries within a certain time
     * period, i.e. whether the search results should be limited to a certain time
     * period according to there creation or modfied date (timestamp-attriute of entries).
     * 
     * @return {@code true} when the user wants to limit the scope of the search to entries
     * that have been modified or created within a certain time period, {@code false}
     * if the search should include all entries, independent from their timestamp-attribute.
     */
    public boolean isTimestampSearch() {
        return istimesearch;
    }
    private int timestampindex;
    /**
     * Indicates whether the user, when starting a search within a certain time period
     * (see {@link #isTimestampSearch() isTimestampSearch()}), wants to search for
     * entries with a certain
     * <ul>
     * <li>creation timestamp</li>
     * <li>modified timestamp</li>
     * <li>both creation and modification date</li>
     * </ul>
     * 
     * @return One of the following constants:
     * <ul>
     * <li>Constants.TIMESTAMP_CREATED</li>
     * <li>Constants.TIMESTAMP_EDITED</li>
     * <li>Constants.TIMESTAMP_BOTH</li>
     * </ul>
     */
    public int getTimestampIndex() {
        return timestampindex;
    }
    /**
     * The beginning of the timesearch's period.
     */
    private String datefrom;
    public String getDateFromValue() {
        return datefrom;
    }
    /**
     * The end of the timesearch's period.
     */
    private String dateto;
    public String getDateToValue() {
        return dateto;
    }
    /**
     * Reference to the settings class. needed for storing the find-settings, like
     * where to search and which search term was entered.
     */
    private Settings settingsObj;
    /**
     * Reference to the search requesr class.
     */
    private SearchRequests searchObj;
    /**
     * get the strings for file descriptions from the resource map
     */
    private org.jdesktop.application.ResourceMap resourceMap =
        org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
        getContext().getResourceMap(CSearchDlg.class);
    
    
    /**
     * 
     * @param parent
     * @param sr
     * @param s
     * @param initialSearchTerm 
     */
    public CSearchDlg(java.awt.Frame parent, SearchRequests sr, Settings s, String initialSearchTerm) {
        super(parent);
        settingsObj = s;
        searchObj = sr;
        initComponents();
        initComboBox();
        initListener();
        // set application icon
        setIconImage(Constants.zknicon.getImage());
        if (settingsObj.isSeaGlass()) {
            jButtonSearch.putClientProperty("JComponent.sizeVariant", "small");
            jButtonCancel.putClientProperty("JComponent.sizeVariant", "small");
        }
        // init the checkboxes
        int w = settingsObj.getSearchWhere();
        jCheckBoxSTitles.setSelected((w&Constants.SEARCH_TITLE)!=0);
        jCheckBoxSContent.setSelected((w&Constants.SEARCH_CONTENT)!=0);
        jCheckBoxSAuthors.setSelected((w&Constants.SEARCH_AUTHOR)!=0);
        jCheckBoxSKeywords.setSelected((w&Constants.SEARCH_KEYWORDS)!=0);
        jCheckBoxSRemarks.setSelected((w&Constants.SEARCH_REMARKS)!=0);
        jCheckBoxSLinks.setSelected((w&Constants.SEARCH_LINKS)!=0);
        jCheckBoxSLinksContent.setSelected((w&Constants.SEARCH_LINKCONTENT)!=0);
        // init option-checkboxes
        w = settingsObj.getSearchOptions();
        jCheckBoxWholeWord.setSelected((w&Constants.SEARCH_OPTION_WHOLEWORD)!=0);
        jCheckBoxMatchCase.setSelected((w&Constants.SEARCH_OPTION_MATCHCASE)!=0);
        jCheckBoxSynonyms.setSelected((w&Constants.SEARCH_OPTION_SYNONYMS)!=0);
        // init textfield
        jTextFieldSearchTerm.setText((null==initialSearchTerm) ? settingsObj.getSearchWhat() : initialSearchTerm);
        // get settings for the logical combination
        w = settingsObj.getSearchLog();
        switch (w) {
            case 0: jRadioButtonLogAnd.setSelected(true); break;
            case 1: jRadioButtonLogOr.setSelected(true); break;
            case 2: jRadioButtonLogNot.setSelected(true); break;
            default: jRadioButtonLogAnd.setSelected(true); break;
        }
        // check whether the time-search is enabled or not
        boolean timesearch = settingsObj.getSearchTime();
        jCheckBoxTimeSearch.setSelected(timesearch);
        jFormattedTextFieldTimeFrom.setEnabled(timesearch);
        jFormattedTextFieldTimeTo.setEnabled(timesearch);
        jLabelTimeSearch.setEnabled(timesearch);
        jComboBoxTimeSearch.setEnabled(timesearch);
        // init combobox
        jComboBoxTimeSearch.setSelectedIndex(settingsObj.getSearchComboTime());
        // init date fields
        String[] dates = settingsObj.getSearchDateTime().split(",");
        // if we have valid values, init the formatted text fields
        if (dates!=null && dates.length>1) {
            if (!dates[0].isEmpty()) jFormattedTextFieldTimeFrom.setText(dates[0]);
            if (!dates[1].isEmpty()) jFormattedTextFieldTimeTo.setText(dates[1]);
        }
        setMnemonicKeys();
    }

    
    private void setMnemonicKeys() {
        if (!settingsObj.isMacAqua()) {
            // init the variables
            String text;
            char mkey;
            // set mnemonic key
            text = jLabel1.getText();
            mkey = text.charAt(1);
            jLabel1.setDisplayedMnemonic(mkey);
            jLabel1.setLabelFor(jTextFieldSearchTerm);
            // set mnemonic key
            jCheckBoxRegEx.setDisplayedMnemonicIndex(0);
            jCheckBoxSTitles.setDisplayedMnemonicIndex(1);
            jCheckBoxSContent.setDisplayedMnemonicIndex(0);
            jCheckBoxSKeywords.setDisplayedMnemonicIndex(0);
            jCheckBoxSAuthors.setDisplayedMnemonicIndex(0);
            jCheckBoxWholeWord.setDisplayedMnemonicIndex(0);
            jCheckBoxMatchCase.setDisplayedMnemonicIndex(0);
            jCheckBoxSynonyms.setDisplayedMnemonicIndex(1);
        }
    }
    

    private void initComboBox() {
        String[] hist = searchObj.getHistory();
        jComboBoxHistory.removeAllItems();
        jComboBoxHistory.addItem(resourceMap.getString("pleaseChooseText"));
        if (hist!=null) {
            for (String h : hist) jComboBoxHistory.addItem(h);
        }
    }


    private void initListener() {
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
        getRootPane().setDefaultButton(jButtonSearch);
        jComboBoxHistory.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (jComboBoxHistory.getSelectedIndex()>0) jTextFieldSearchTerm.setText(jComboBoxHistory.getSelectedItem().toString());
            }
        });
        jCheckBoxTimeSearch.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent evt) {
                // check whether the time-search is enabled or not
                boolean timesearch = jCheckBoxTimeSearch.isSelected();
                settingsObj.setSearchTime(timesearch);
                jCheckBoxTimeSearch.setSelected(timesearch);
                jFormattedTextFieldTimeFrom.setEnabled(timesearch);
                jFormattedTextFieldTimeTo.setEnabled(timesearch);
                jLabelTimeSearch.setEnabled(timesearch);
                jComboBoxTimeSearch.setEnabled(timesearch);
            }
        });
        jCheckBoxRegEx.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonLogAnd.setEnabled(!jCheckBoxRegEx.isSelected());
                jRadioButtonLogOr.setEnabled(!jCheckBoxRegEx.isSelected());
                jRadioButtonLogNot.setEnabled(!jCheckBoxRegEx.isSelected());
                jCheckBoxWholeWord.setEnabled(!jCheckBoxRegEx.isSelected());
                jCheckBoxMatchCase.setEnabled(!jCheckBoxRegEx.isSelected());
                jCheckBoxSynonyms.setEnabled(!jCheckBoxRegEx.isSelected());
                jLabel2.setText(resourceMap.getString(jCheckBoxRegEx.isSelected()?"jLabel2.textRegEx":"jLabel2.text"));
                if (jCheckBoxRegEx.isSelected()) checkRegExPattern();
                else jTextFieldSearchTerm.setForeground(Color.BLACK);
            }
        });
        jTextFieldSearchTerm.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent evt) {
                if (jCheckBoxRegEx.isSelected()) checkRegExPattern();
                else jTextFieldSearchTerm.setForeground(Color.BLACK);
            }
        });
        jTextFieldSearchTerm.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger() && !jPopupMenuCCP.isVisible()) {
                    jPopupMenuCCP.show(jTextFieldSearchTerm, evt.getPoint().x, evt.getPoint().y);
                }
            }
            @Override public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger() && !jPopupMenuCCP.isVisible()) {
                    jPopupMenuCCP.show(jTextFieldSearchTerm, evt.getPoint().x, evt.getPoint().y);
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
            getActionMap(CSearchDlg.class, this);
        popupCCPcut.setAction(actionMap.get("cut"));
        popupCCPcopy.setAction(actionMap.get("copy"));
        popupCCPpaste.setAction(actionMap.get("paste"));
    }

    private boolean checkRegExPattern() {
        try {
            Pattern.compile(jTextFieldSearchTerm.getText());
        }
        catch (PatternSyntaxException e) {
            jTextFieldSearchTerm.setForeground(Color.RED);
            return false;
        }
        jTextFieldSearchTerm.setForeground(Color.BLACK);
        return true;
    }


    @Action
    public void cancel() {
        cancelled = true;
        dispose();
        setVisible(false);
    }

    
    @Action
    public void startSearch() {
        // first of all, check the reg-ex-pattern, in case we have a regular expression
        regex = jCheckBoxRegEx.isSelected();
        // if we have an invalid regular expression, tell user to correct it
        if (regex && !checkRegExPattern()) {
            // display error message box
            JOptionPane.showMessageDialog(null,resourceMap.getString("errWrongRegExMsg"),resourceMap.getString("errWrongRegExTitle"),JOptionPane.PLAIN_MESSAGE);
            // set focus back to textfield
            jTextFieldSearchTerm.requestFocusInWindow();
            // leave method
            return;
        }
        // get search terms and split them at each comma (comma seperates several searchterms)
        String st = jTextFieldSearchTerm.getText();
        // here we go on when we have no regular expression as search term
        if (!regex) {
            // create a regular expression, that separates the input at each comma.
            // furthermore, commas within double-quotes ("") are not treated as separator-char,
            // so the user can search for sentences that include commas as well. and finally, the
            // quotes are removed, since we don't need them...
            Matcher mat = Pattern.compile("(\"(.*?)\"|([^,]+)),?").matcher(st);
            // create a new list that will contain each found pattern (i.e. searchterm)
            List<String> result = new ArrayList<>();
            while (mat.find()) result.add(mat.group(2) == null ? mat.group(3).trim() : mat.group(2).trim());
            // and copy the list to our array...
            searchterms = result.toArray(new String[result.size()]);
        }
        // else, if we have a regular expression, we do not split the search term after each comma,
        // but keep the whole expression as one search term...
        else {
            searchterms = new String[] {st};
        }
        // no cancel-operation, search is ok
        cancelled = false;
        // check which logical combination of the search is requested
        if (jRadioButtonLogAnd.isSelected()) logical = Constants.LOG_AND;
        if (jRadioButtonLogOr.isSelected()) logical = Constants.LOG_OR;
        if (jRadioButtonLogNot.isSelected()) logical = Constants.LOG_NOT;
        
        wholeword = jCheckBoxWholeWord.isSelected();
        matchcase = jCheckBoxMatchCase.isSelected();
        synonyms = jCheckBoxSynonyms.isSelected();
        
        where = 0;
        // now check where the user wants to search in...
        if (jCheckBoxSTitles.isSelected()) where = where | Constants.SEARCH_TITLE;
        if (jCheckBoxSContent.isSelected()) where = where | Constants.SEARCH_CONTENT;
        if (jCheckBoxSAuthors.isSelected()) where = where | Constants.SEARCH_AUTHOR;
        if (jCheckBoxSKeywords.isSelected()) where = where | Constants.SEARCH_KEYWORDS;
        if (jCheckBoxSRemarks.isSelected()) where = where | Constants.SEARCH_REMARKS;
        if (jCheckBoxSLinks.isSelected()) where = where | Constants.SEARCH_LINKS;
        if (jCheckBoxSLinksContent.isSelected()) where = where | Constants.SEARCH_LINKCONTENT;
        
        // save user settings
        settingsObj.setSearchWhere(where);
        settingsObj.setSearchWhat(st);
        
        // save the checked search options
        int options = 0;
        if (wholeword) options = options | Constants.SEARCH_OPTION_WHOLEWORD;
        if (matchcase) options = options | Constants.SEARCH_OPTION_MATCHCASE;
        if (synonyms) options = options | Constants.SEARCH_OPTION_SYNONYMS;
        
        settingsObj.setSearchOptions(options);
        
        // save logical combination
        if (jRadioButtonLogAnd.isSelected()) settingsObj.setSearchLog(0);
        else if (jRadioButtonLogOr.isSelected()) settingsObj.setSearchLog(1);
        else if (jRadioButtonLogNot.isSelected()) settingsObj.setSearchLog(2);

        // save time-search settings
        istimesearch = jCheckBoxTimeSearch.isSelected();
        timestampindex = jComboBoxTimeSearch.getSelectedIndex();
        settingsObj.setSearchTime(istimesearch);
        settingsObj.setSearchComboTime(timestampindex);
        // get the date from the time-search
        String date1 = jFormattedTextFieldTimeFrom.getText();
        String date2 = jFormattedTextFieldTimeTo.getText();
        // if we have two valid values, store them
        if (!date1.isEmpty()&&!date2.isEmpty()) settingsObj.setSearchDateTime(date1+","+date2);
        // check whether the date-period was entered correctly, so we don't have for instance a later
        // start date than the end of the period
        if (istimesearch) {
            try {
                datefrom = date1.substring(6)+date1.substring(3,5)+date1.substring(0,2);
                dateto = date2.substring(6)+date2.substring(3,5)+date2.substring(0,2);
            }
            catch (IndexOutOfBoundsException e) {
                // display error message box
                JOptionPane.showMessageDialog(null,resourceMap.getString("errWrongDateMsg"),resourceMap.getString("errWrongDateTitle"),JOptionPane.PLAIN_MESSAGE);
                // set focus back to textfield
                jFormattedTextFieldTimeFrom.requestFocusInWindow();
                // leave method
                return;
            }
            // if "from"-date is later than "to"-date, display error message
            if (datefrom.compareTo(dateto)>0) {
                // display error message box
                JOptionPane.showMessageDialog(null,resourceMap.getString("errWrongDateMsg"),resourceMap.getString("errWrongDateTitle"),JOptionPane.PLAIN_MESSAGE);
                // set focus back to textfield
                jFormattedTextFieldTimeFrom.requestFocusInWindow();
                // leave method
                return;
            }
        }

        searchObj.addToHistory(jTextFieldSearchTerm.getText());

        // hide window
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPopupMenuCCP = new javax.swing.JPopupMenu();
        popupCCPcut = new javax.swing.JMenuItem();
        popupCCPcopy = new javax.swing.JMenuItem();
        popupCCPpaste = new javax.swing.JMenuItem();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldSearchTerm = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jCheckBoxSTitles = new javax.swing.JCheckBox();
        jCheckBoxSContent = new javax.swing.JCheckBox();
        jCheckBoxSAuthors = new javax.swing.JCheckBox();
        jCheckBoxSKeywords = new javax.swing.JCheckBox();
        jCheckBoxSRemarks = new javax.swing.JCheckBox();
        jCheckBoxSLinks = new javax.swing.JCheckBox();
        jCheckBoxSLinksContent = new javax.swing.JCheckBox();
        jPanel5 = new javax.swing.JPanel();
        jRadioButtonLogAnd = new javax.swing.JRadioButton();
        jRadioButtonLogOr = new javax.swing.JRadioButton();
        jRadioButtonLogNot = new javax.swing.JRadioButton();
        jCheckBoxWholeWord = new javax.swing.JCheckBox();
        jCheckBoxMatchCase = new javax.swing.JCheckBox();
        jCheckBoxSynonyms = new javax.swing.JCheckBox();
        jButtonCancel = new javax.swing.JButton();
        jButtonSearch = new javax.swing.JButton();
        jComboBoxHistory = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jCheckBoxTimeSearch = new javax.swing.JCheckBox();
        jFormattedTextFieldTimeFrom = new javax.swing.JFormattedTextField();
        jLabelTimeSearch = new javax.swing.JLabel();
        jFormattedTextFieldTimeTo = new javax.swing.JFormattedTextField();
        jComboBoxTimeSearch = new javax.swing.JComboBox();
        jCheckBoxRegEx = new javax.swing.JCheckBox();

        jPopupMenuCCP.setName("jPopupMenuCCP"); // NOI18N

        popupCCPcut.setName("popupCCPcut"); // NOI18N
        jPopupMenuCCP.add(popupCCPcut);

        popupCCPcopy.setName("popupCCPcopy"); // NOI18N
        jPopupMenuCCP.add(popupCCPcopy);

        popupCCPpaste.setName("popupCCPpaste"); // NOI18N
        jPopupMenuCCP.add(popupCCPpaste);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getResourceMap(CSearchDlg.class);
        setTitle(resourceMap.getString("FormSearchDialog.title")); // NOI18N
        setModal(true);
        setName("FormSearchDialog"); // NOI18N
        setResizable(false);

        jPanel1.setName("jPanel1"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jTextFieldSearchTerm.setText(resourceMap.getString("jTextFieldSearchTerm.text")); // NOI18N
        jTextFieldSearchTerm.setName("jTextFieldSearchTerm"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        jCheckBoxSTitles.setSelected(true);
        jCheckBoxSTitles.setText(resourceMap.getString("jCheckBoxSTitles.text")); // NOI18N
        jCheckBoxSTitles.setName("jCheckBoxSTitles"); // NOI18N

        jCheckBoxSContent.setSelected(true);
        jCheckBoxSContent.setText(resourceMap.getString("jCheckBoxSContent.text")); // NOI18N
        jCheckBoxSContent.setName("jCheckBoxSContent"); // NOI18N

        jCheckBoxSAuthors.setText(resourceMap.getString("jCheckBoxSAuthors.text")); // NOI18N
        jCheckBoxSAuthors.setName("jCheckBoxSAuthors"); // NOI18N

        jCheckBoxSKeywords.setSelected(true);
        jCheckBoxSKeywords.setText(resourceMap.getString("jCheckBoxSKeywords.text")); // NOI18N
        jCheckBoxSKeywords.setName("jCheckBoxSKeywords"); // NOI18N

        jCheckBoxSRemarks.setText(resourceMap.getString("jCheckBoxSRemarks.text")); // NOI18N
        jCheckBoxSRemarks.setName("jCheckBoxSRemarks"); // NOI18N

        jCheckBoxSLinks.setText(resourceMap.getString("jCheckBoxSLinks.text")); // NOI18N
        jCheckBoxSLinks.setName("jCheckBoxSLinks"); // NOI18N

        jCheckBoxSLinksContent.setText(resourceMap.getString("jCheckBoxSLinksContent.text")); // NOI18N
        jCheckBoxSLinksContent.setEnabled(false);
        jCheckBoxSLinksContent.setName("jCheckBoxSLinksContent"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBoxSTitles)
                            .addComponent(jCheckBoxSContent)
                            .addComponent(jCheckBoxSAuthors))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBoxSRemarks)
                            .addComponent(jCheckBoxSLinks)
                            .addComponent(jCheckBoxSLinksContent)))
                    .addComponent(jCheckBoxSKeywords))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jCheckBoxSRemarks)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxSLinks)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxSLinksContent))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jCheckBoxSTitles)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxSContent)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxSAuthors)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxSKeywords)
                .addContainerGap())
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel5.border.title"))); // NOI18N
        jPanel5.setName("jPanel5"); // NOI18N

        buttonGroup1.add(jRadioButtonLogAnd);
        jRadioButtonLogAnd.setSelected(true);
        jRadioButtonLogAnd.setText(resourceMap.getString("jRadioButtonLogAnd.text")); // NOI18N
        jRadioButtonLogAnd.setName("jRadioButtonLogAnd"); // NOI18N

        buttonGroup1.add(jRadioButtonLogOr);
        jRadioButtonLogOr.setText(resourceMap.getString("jRadioButtonLogOr.text")); // NOI18N
        jRadioButtonLogOr.setName("jRadioButtonLogOr"); // NOI18N

        buttonGroup1.add(jRadioButtonLogNot);
        jRadioButtonLogNot.setText(resourceMap.getString("jRadioButtonLogNot.text")); // NOI18N
        jRadioButtonLogNot.setName("jRadioButtonLogNot"); // NOI18N

        jCheckBoxWholeWord.setText(resourceMap.getString("jCheckBoxWholeWord.text")); // NOI18N
        jCheckBoxWholeWord.setName("jCheckBoxWholeWord"); // NOI18N

        jCheckBoxMatchCase.setText(resourceMap.getString("jCheckBoxMatchCase.text")); // NOI18N
        jCheckBoxMatchCase.setName("jCheckBoxMatchCase"); // NOI18N

        jCheckBoxSynonyms.setText(resourceMap.getString("jCheckBoxSynonyms.text")); // NOI18N
        jCheckBoxSynonyms.setName("jCheckBoxSynonyms"); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButtonLogNot)
                    .addComponent(jRadioButtonLogOr)
                    .addComponent(jRadioButtonLogAnd))
                .addGap(18, 18, 18)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBoxWholeWord)
                    .addComponent(jCheckBoxMatchCase)
                    .addComponent(jCheckBoxSynonyms))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jCheckBoxWholeWord)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxMatchCase)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxSynonyms))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jRadioButtonLogAnd)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButtonLogOr)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButtonLogNot)))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getActionMap(CSearchDlg.class, this);
        jButtonCancel.setAction(actionMap.get("cancel")); // NOI18N
        jButtonCancel.setName("jButtonCancel"); // NOI18N

        jButtonSearch.setAction(actionMap.get("startSearch")); // NOI18N
        jButtonSearch.setName("jButtonSearch"); // NOI18N

        jComboBoxHistory.setName("jComboBoxHistory"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jCheckBoxTimeSearch.setText(resourceMap.getString("jCheckBoxTimeSearch.text")); // NOI18N
        jCheckBoxTimeSearch.setName("jCheckBoxTimeSearch"); // NOI18N

        jFormattedTextFieldTimeFrom.setColumns(6);
        jFormattedTextFieldTimeFrom.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.DateFormatter(java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT))));
        jFormattedTextFieldTimeFrom.setName("jFormattedTextFieldTimeFrom"); // NOI18N

        jLabelTimeSearch.setText(resourceMap.getString("jLabelTimeSearch.text")); // NOI18N
        jLabelTimeSearch.setName("jLabelTimeSearch"); // NOI18N

        jFormattedTextFieldTimeTo.setColumns(6);
        jFormattedTextFieldTimeTo.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.DateFormatter(java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT))));
        jFormattedTextFieldTimeTo.setName("jFormattedTextFieldTimeTo"); // NOI18N

        jComboBoxTimeSearch.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Erstelldatum", "Änderungsdatum", "Beides" }));
        jComboBoxTimeSearch.setName("jComboBoxTimeSearch"); // NOI18N

        jCheckBoxRegEx.setText(resourceMap.getString("jCheckBoxRegEx.text")); // NOI18N
        jCheckBoxRegEx.setName("jCheckBoxRegEx"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jCheckBoxTimeSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jFormattedTextFieldTimeFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelTimeSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jFormattedTextFieldTimeTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxTimeSearch, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel3))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jComboBoxHistory, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jTextFieldSearchTerm)
                                    .addComponent(jCheckBoxRegEx)))
                            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(6, 6, 6))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jButtonCancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonSearch)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextFieldSearchTerm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addComponent(jCheckBoxRegEx)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jComboBoxHistory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxTimeSearch)
                    .addComponent(jFormattedTextFieldTimeFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelTimeSearch)
                    .addComponent(jFormattedTextFieldTimeTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBoxTimeSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, 11, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonCancel)
                    .addComponent(jButtonSearch)))
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
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonSearch;
    private javax.swing.JCheckBox jCheckBoxMatchCase;
    private javax.swing.JCheckBox jCheckBoxRegEx;
    private javax.swing.JCheckBox jCheckBoxSAuthors;
    private javax.swing.JCheckBox jCheckBoxSContent;
    private javax.swing.JCheckBox jCheckBoxSKeywords;
    private javax.swing.JCheckBox jCheckBoxSLinks;
    private javax.swing.JCheckBox jCheckBoxSLinksContent;
    private javax.swing.JCheckBox jCheckBoxSRemarks;
    private javax.swing.JCheckBox jCheckBoxSTitles;
    private javax.swing.JCheckBox jCheckBoxSynonyms;
    private javax.swing.JCheckBox jCheckBoxTimeSearch;
    private javax.swing.JCheckBox jCheckBoxWholeWord;
    private javax.swing.JComboBox jComboBoxHistory;
    private javax.swing.JComboBox jComboBoxTimeSearch;
    private javax.swing.JFormattedTextField jFormattedTextFieldTimeFrom;
    private javax.swing.JFormattedTextField jFormattedTextFieldTimeTo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabelTimeSearch;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPopupMenu jPopupMenuCCP;
    private javax.swing.JRadioButton jRadioButtonLogAnd;
    private javax.swing.JRadioButton jRadioButtonLogNot;
    private javax.swing.JRadioButton jRadioButtonLogOr;
    private javax.swing.JTextField jTextFieldSearchTerm;
    private javax.swing.JMenuItem popupCCPcopy;
    private javax.swing.JMenuItem popupCCPcut;
    private javax.swing.JMenuItem popupCCPpaste;
    // End of variables declaration//GEN-END:variables

}
