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
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.util.Constants;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.application.Action;

/**
 *
 * @author danielludecke
 */
public class CInsertHyperlink extends javax.swing.JDialog {

    private String hyperlink;
    /**
     * 
     * @param parent
     * @param selection
     * @param settingsObj 
     */
    public CInsertHyperlink(java.awt.Frame parent, String selection, Settings settingsObj) {
        super(parent);
        initComponents();
        if (settingsObj.isSeaGlass()) {
            jButtonApply.putClientProperty("JComponent.sizeVariant", "small");
            jButtonCancel.putClientProperty("JComponent.sizeVariant", "small");
        }
        // set application icon
        setIconImage(Constants.zknicon.getImage());
        // disbale apply button
        jButtonApply.setEnabled(false);
        getRootPane().setDefaultButton(jButtonApply);
        // check for param
        if (selection!=null && !selection.isEmpty()) {
            // trim spaces
            selection = selection.trim();
            // set text
            jTextFieldDescription.setText(selection);
        }
        initListeners();
        hyperlink = null;
        // check clipboard
        checkClipboard();
    }

    
    private void checkClipboard() {
        // get the clipbaord
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            // retrieve clipboard content
            Transferable content = clipboard.getContents(null);
            // if we have any content, create new entry...
            if (content!=null) {
                // first, copy clipboard to string
                String text = content.getTransferData(DataFlavor.stringFlavor).toString().trim();
                // check whether text starts with http
                if (text.startsWith("http") && text.contains(".")) {
                    jTextFieldURL.setText(text);
                }
            }
        }
        catch(IllegalStateException e) {
            Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
        }
        catch(IOException e) {
            Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
        }
        catch(UnsupportedFlavorException e) {
            Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
        }
    }
    
    
    private void initListeners() {
        // these code lines add an escape-listener to the dialog. so, when the user
        // presses the escape-key, the same action is performed as if the user
        // presses the cancel button...
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        ActionListener cancelAction = new java.awt.event.ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                cancel();
            }
        };
        getRootPane().registerKeyboardAction(cancelAction, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        // if the user inputs text into the textfield, check whether we have at least 
        // the two necessary textfield filled with data
        jTextFieldURL.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void changedUpdate(DocumentEvent e) { enableApplyButton(); }
            @Override public void insertUpdate(DocumentEvent e) { enableApplyButton(); }
            @Override public void removeUpdate(DocumentEvent e) { enableApplyButton(); }
        });
        jTextFieldDescription.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void changedUpdate(DocumentEvent e) { enableApplyButton(); }
            @Override public void insertUpdate(DocumentEvent e) { enableApplyButton(); }
            @Override public void removeUpdate(DocumentEvent e) { enableApplyButton(); }
        });
    }
    
    
    @Action
    public void cancel() {
        hyperlink = null;
        dispose();
        setVisible(false);
    }
    @Action
    public void insertHyperlink() {
        // get url
        String url = jTextFieldURL.getText();
        // check if it starts with http...
        if (!url.startsWith("http")) url = "http://"+url;
        hyperlink = "["+jTextFieldDescription.getText()+"]("+url+")";
        dispose();
        setVisible(false);
    }
    
    public String getHyperlink() {
        return hyperlink;
    }
    
    
    private void enableApplyButton() {
        // check whether we have text in first textfield
        boolean check1 = jTextFieldURL.getText()!=null && !jTextFieldURL.getText().isEmpty();
        // check whether we have text in second textfield
        boolean check2 = jTextFieldDescription.getText()!=null && !jTextFieldDescription.getText().isEmpty();
        // enable apply-button only if we have text in the obligatory text fields
        jButtonApply.setEnabled(check1 && check2);
    }
    
    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    // Generated using JFormDesigner Evaluation license - Ralf Barkow
    private void initComponents() {
        ResourceBundle bundle = ResourceBundle.getBundle("de.danielluedecke.zettelkasten.resources.CInsertHyperlink");
        jLabel1 = new JLabel();
        jTextFieldURL = new JTextField();
        jLabel2 = new JLabel();
        jTextFieldDescription = new JTextField();
        jButtonApply = new JButton();
        jButtonCancel = new JButton();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(bundle.getString("FormInsertHyperlink.title"));
        setModal(true);
        setName("FormInsertHyperlink");
        setResizable(false);
        Container contentPane = getContentPane();

        //---- jLabel1 ----
        jLabel1.setText(bundle.getString("jLabel1.text"));
        jLabel1.setName("jLabel1");

        //---- jTextFieldURL ----
        jTextFieldURL.setText(bundle.getString("jTextFieldURL.text"));
        jTextFieldURL.setName("jTextFieldURL");

        //---- jLabel2 ----
        jLabel2.setText(bundle.getString("jLabel2.text"));
        jLabel2.setName("jLabel2");

        //---- jTextFieldDescription ----
        jTextFieldDescription.setText(bundle.getString("jTextFieldDescription.text"));
        jTextFieldDescription.setName("jTextFieldDescription");

        //---- jButtonApply ----
        jButtonApply.setName("jButtonApply");

        //---- jButtonCancel ----
        jButtonCancel.setName("jButtonCancel");

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                            .addGroup(contentPaneLayout.createParallelGroup()
                                .addComponent(jLabel1)
                                .addComponent(jLabel2))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(contentPaneLayout.createParallelGroup()
                                .addComponent(jTextFieldURL, GroupLayout.PREFERRED_SIZE, 360, GroupLayout.PREFERRED_SIZE)
                                .addComponent(jTextFieldDescription, GroupLayout.PREFERRED_SIZE, 360, GroupLayout.PREFERRED_SIZE)))
                        .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                            .addGap(0, 0, Short.MAX_VALUE)
                            .addComponent(jButtonCancel)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButtonApply)))
                    .addContainerGap())
        );
        contentPaneLayout.setVerticalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(jTextFieldURL, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(jTextFieldDescription, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(contentPaneLayout.createParallelGroup()
                        .addComponent(jButtonCancel)
                        .addComponent(jButtonApply))
                    .addContainerGap())
        );
        pack();
        setLocationRelativeTo(getOwner());
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Ralf Barkow
    private JLabel jLabel1;
    private JTextField jTextFieldURL;
    private JLabel jLabel2;
    private JTextField jTextFieldDescription;
    private JButton jButtonApply;
    private JButton jButtonCancel;
    // End of variables declaration//GEN-END:variables
}
