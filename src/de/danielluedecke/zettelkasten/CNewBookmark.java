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

import de.danielluedecke.zettelkasten.database.Bookmarks;
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.util.ColorUtil;
import de.danielluedecke.zettelkasten.util.Constants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.application.Action;

/**
 *
 * @author danielludecke
 */
public class CNewBookmark extends javax.swing.JDialog {

    /**
     * Reference to the bookmarks-class
     */
    private final Bookmarks bookmarksObj;
    /**
     * Indicates whether the dialog was cancelled or not
     */
    private boolean cancelled = false;

    private final int[] entrynumbers;
    private final boolean editbookmark;

    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap
            = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
            getContext().getResourceMap(CNewBookmark.class);

    /**
     *
     * @param parent
     * @param bm
     * @param nrs
     * @param edit
     * @param settingsObj
     */
    public CNewBookmark(java.awt.Frame parent, Bookmarks bm, int[] nrs, boolean edit, Settings settingsObj) {
        super(parent);
        initComponents();
        // set application icon
        setIconImage(Constants.zknicon.getImage());

        bookmarksObj = bm;
        entrynumbers = nrs;
        editbookmark = edit;

        // init combobox with all existing categories
        initComboBox();
        initListeners();
        initBorders(settingsObj);
        if (settingsObj.isSeaGlass()) {
            setupSeaGlassStyle();
        }
        // when the user wants to edit a bookmark, init fields
        if (editbookmark) {
            // get the bookmark
            // when changing a bookmark, we assume that we only have one bookmark
            int pos = bookmarksObj.getBookmarkPosition(entrynumbers[0]);
            // get category name
            String catname = bookmarksObj.getBookmarkCategoryAsString(pos);
            // and select it, if available
            if (catname != null) {
                jComboBoxCats.setSelectedItem(catname);
            }
            // get the comment
            String comment = bookmarksObj.getComment(pos);
            // replace br-tags with newlines
            comment = comment.replace("[br]", System.lineSeparator());
            // and set text to textfield
            jTextAreaComment.setText(comment);
            // change title
            setTitle(resourceMap.getString("frametitleEdit"));
            // finally, reset modified state
            setModified(false);
        }
    }

    private void setupSeaGlassStyle() {
        jButtonApply.putClientProperty("JComponent.sizeVariant", "small");
        jButtonCancel.putClientProperty("JComponent.sizeVariant", "small");
        jButtonAddCat.putClientProperty("JComponent.sizeVariant", "small");
    }

    private void initBorders(Settings settingsObj) {
        /*
         * Constructor for Matte Border
         * public MatteBorder(int top, int left, int bottom, int right, Color matteColor)
         */
        jScrollPane1.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, ColorUtil.getBorderGray(settingsObj)));
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
        // if the document is ever edited, assume that it needs to be saved
        // so we add some document listeners here
        jTextFieldNewCat.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                changeUpdateState();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                changeUpdateState();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changeUpdateState();
            }
        });
        // if the document is ever edited, assume that it needs to be saved
        // so we add some document listeners here
        jTextAreaComment.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                changeModifiedState();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                changeModifiedState();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changeModifiedState();
            }
        });
        jTextFieldNewCat.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                if (KeyEvent.VK_ENTER == evt.getKeyCode()) {
                    addCat();
                }
            }
        });
    }

    /**
     * This method checks whether we have changes to the textfield. if so, and
     * we have text (i.e. textfield is not empty), enable the button (action).
     */
    private void changeUpdateState() {
        setTextUpdated(!jTextFieldNewCat.getText().isEmpty());
    }

    /**
     * This method indicates whether we have changes that the user can apply.
     * when no category is selected, the apply-button should not be
     * activated/enabled.
     */
    private void changeModifiedState() {
        setModified(jComboBoxCats.getSelectedIndex() != -1);
    }

    /**
     * Inits or updates the combobox with all available bookmark-categories.
     * when a new category is added, it is selected and the modified state is
     * changed so the apply-button becomes enabled.
     */
    private void initComboBox() {
        // clear combobox
        jComboBoxCats.removeAllItems();
        // get sorted categories
        String[] cats = bookmarksObj.getSortedCategories();
        // if we have any categories, go on...
        if ((cats != null) && (cats.length > 0)) {
            for (String cat : cats) {
                jComboBoxCats.addItem(cat);
            }
        }
        // add action listener to combo box
        jComboBoxCats.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                setModified(true);
            }
        });
        // select first item
        if (jComboBoxCats.getItemCount() > 0) {
            jComboBoxCats.setSelectedIndex(0);
        }
    }

    /**
     * When the user presses the cancel button, no update needed, close window
     */
    @Action
    public void cancelWindow() {
        cancelled = true;
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
     * This method adds a new category to the bookmark-categories. the
     * category-description is retrieved from the textfield.
     */
    @Action(enabledProperty = "textUpdated")
    public void addCat() {
        // get text
        String text = jTextFieldNewCat.getText();
        // check whether category already exists
        if (-1 == bookmarksObj.getCategoryPosition(text)) {
            // if not, add it to bookmark-data
            bookmarksObj.addCategory(text);
            // and update combobox
            initComboBox();
            // select the new created value
            jComboBoxCats.setSelectedItem(text);
            // reset textfield
            jTextFieldNewCat.setText("");
        } else {
            // category-name already existed, so desktop was not added...
            JOptionPane.showMessageDialog(null, 
                    resourceMap.getString("errCatExistsMsg"), 
                    resourceMap.getString("errCatExistsTitle"), 
                    JOptionPane.PLAIN_MESSAGE);
        }
    }

    /**
     * This method applies the changes made from the user. then, if we have
     * edited a bookmark, its changes are saved. if we have a new bookmark, it
     * is added to the bookmark-data-file.
     */
    @Action(enabledProperty = "modified")
    public void applyChanges() {
        // get the comment-text
        String comment = jTextAreaComment.getText();
        // and re-convert all new lines to br's. this is necessary for converting
        // them into <br>'s because the entry is displayed as html-content. simple
        // new lines without "<br>" command would not be shown as new lines
        //
        // but first, we habe to remove all carriage-returns (\r), which are part of the
        // line-seperator in windows. somehow, the replace-command does *not* work, when
        // we replace "System.lineSeparator()" with "[br]", but only when
        // a "\n" is replaced by [br]. So, in case the system's line-separator also contains a
        // "\r", it is replaced by nothing, to clean the content.
        if (System.lineSeparator().contains("\r")) {
            comment = comment.replace("\r", "");
        }
        comment = comment.replace("\n", "[br]");
        // get the category
        String cat = jComboBoxCats.getSelectedItem().toString();
        // add or change bookmark
        if (editbookmark) {
            // when changing a bookmark, we assume that we only have one bookmark
            bookmarksObj.changeBookmark(entrynumbers[0], cat, comment);
        } else {
            // whenn adding bookmarks, one or more bookmarks might be passed as parameter,
            // e.g. if we get several entries from the search results window...
            for (int bm : entrynumbers) {
                bookmarksObj.addBookmark(bm, cat, comment);
            }
        }
        // finally, close window
        closeWindow();
    }

    /**
     * This variable indicates whether the displayed entry is already
     * bookmarked, so we can en- or disable the bookmark-action.
     */
    private boolean textUpdated = false;

    public boolean isTextUpdated() {
        return textUpdated;
    }

    public void setTextUpdated(boolean b) {
        boolean old = isTextUpdated();
        this.textUpdated = b;
        firePropertyChange("textUpdated", old, isTextUpdated());
    }
    /**
     * This variable indicates whether changes have been made that should be
     * saved....
     */
    private boolean modified = false;

    public boolean isModified() {
        return modified;
    }

    public final void setModified(boolean b) {
        boolean old = isModified();
        this.modified = b;
        firePropertyChange("modified", old, isModified());
    }

    /**
     *
     * @return
     */
    public boolean isCancelled() {
        return cancelled;
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jComboBoxCats = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldNewCat = new javax.swing.JTextField();
        jButtonAddCat = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextAreaComment = new javax.swing.JTextArea();
        jButtonApply = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getResourceMap(CNewBookmark.class);
        setTitle(resourceMap.getString("FormNewBookmark.title")); // NOI18N
        setModal(true);
        setName("FormNewBookmark"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jComboBoxCats.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxCats.setName("jComboBoxCats"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jTextFieldNewCat.setText(resourceMap.getString("jTextFieldNewCat.text")); // NOI18N
        jTextFieldNewCat.setName("jTextFieldNewCat"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getActionMap(CNewBookmark.class, this);
        jButtonAddCat.setAction(actionMap.get("addCat")); // NOI18N
        jButtonAddCat.setName("jButtonAddCat"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTextAreaComment.setLineWrap(true);
        jTextAreaComment.setWrapStyleWord(true);
        jTextAreaComment.setName("jTextAreaComment"); // NOI18N
        jScrollPane1.setViewportView(jTextAreaComment);

        jButtonApply.setAction(actionMap.get("applyChanges")); // NOI18N
        jButtonApply.setName("jButtonApply"); // NOI18N

        jButtonCancel.setAction(actionMap.get("cancelWindow")); // NOI18N
        jButtonCancel.setName("jButtonCancel"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldNewCat)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonAddCat))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBoxCats, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel3))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButtonCancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonApply)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jComboBoxCats, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jButtonAddCat)
                    .addComponent(jTextFieldNewCat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonApply)
                    .addComponent(jButtonCancel))
                .addGap(3, 3, 3))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAddCat;
    private javax.swing.JButton jButtonApply;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JComboBox jComboBoxCats;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextAreaComment;
    private javax.swing.JTextField jTextFieldNewCat;
    // End of variables declaration//GEN-END:variables

}
