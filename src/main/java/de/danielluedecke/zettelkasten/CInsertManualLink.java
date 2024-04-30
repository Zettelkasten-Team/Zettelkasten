/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.danielluedecke.zettelkasten;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.GroupLayout;
import javax.swing.LayoutStyle;
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.util.Constants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.application.Action;

/**
 *
 * @author danielludecke
 */
public class CInsertManualLink extends javax.swing.JDialog {
    private String manlink;
    /**
     * 
     * @param parent
     * @param selection
     * @param settingsObj 
     */
    public CInsertManualLink(java.awt.Frame parent, String selection, Settings settingsObj) {
        super(parent);
        initComponents();
        // set application icon
        setIconImage(Constants.zknicon.getImage());
        // disbale apply button
        jButtonApply.setEnabled(false);
        getRootPane().setDefaultButton(jButtonApply);
        if (settingsObj.isSeaGlass()) {
            jButtonApply.putClientProperty("JComponent.sizeVariant", "small");
            jButtonCancel.putClientProperty("JComponent.sizeVariant", "small");
        }
        // check for param
        if (selection!=null && !selection.isEmpty()) {
            // trim spaces
            selection = selection.trim();
            // set text
            jTextFieldDesc.setText(selection);
            // set focus to next textfield
            jTextFieldNr.requestFocusInWindow();
        }
        else {
            jTextFieldDesc.requestFocusInWindow();
        }
        initListeners();
        manlink = null;
    }

    @Action
    public void cancel() {
        manlink = null;
        dispose();
        setVisible(false);
    }
    @Action
    public void insertManlink() {
        // get desc and manlink nr.
        String desc = jTextFieldDesc.getText();
        String link = jTextFieldNr.getText();
        // check for valid values
        if (desc!=null && link!=null && !link.isEmpty()) {
            // check for valid nr input
            try {
                Integer.parseInt(link);
                manlink = Constants.FORMAT_MANLINK_OPEN+" "+link+"]"+desc+Constants.FORMAT_MANLINK_CLOSE;
            }
            catch (NumberFormatException ex) {
                manlink = null;
            }
        }
        else {
            manlink = null;
        }
        dispose();
        setVisible(false);
    }
    
    public String getManlink() {
        return manlink;
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
        jTextFieldDesc.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void changedUpdate(DocumentEvent e) { enableApplyButton(); }
            @Override public void insertUpdate(DocumentEvent e) { enableApplyButton(); }
            @Override public void removeUpdate(DocumentEvent e) { enableApplyButton(); }
        });
        jTextFieldNr.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void changedUpdate(DocumentEvent e) { enableApplyButton(); }
            @Override public void insertUpdate(DocumentEvent e) { enableApplyButton(); }
            @Override public void removeUpdate(DocumentEvent e) { enableApplyButton(); }
        });
    }
    private void enableApplyButton() {
        // check whether we have text in first textfield
        boolean check1 = jTextFieldDesc.getText()!=null && !jTextFieldDesc.getText().isEmpty();
        // check whether we have text in second textfield
        boolean check2 = jTextFieldNr.getText()!=null && !jTextFieldNr.getText().isEmpty();
        // third check, correct nr (int)
        boolean check3 = false;
        // check for valid nr input
        if (check2) {
            try {
                Integer.parseInt(jTextFieldNr.getText());
                check3 = true;
            }
            catch (NumberFormatException ex) {
            }
        }
        // enable apply-button only if we have text in the obligatory text fields
        jButtonApply.setEnabled(check1 && check2 && check3);
    }
    
    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    // Generated using JFormDesigner Evaluation license - Ralf Barkow
    private void initComponents() {
        ResourceBundle bundle = ResourceBundle.getBundle("de.danielluedecke.zettelkasten.resources.CInsertManualLink");
        jLabelDesc = new JLabel();
        jTextFieldDesc = new JTextField();
        jLabelNr = new JLabel();
        jTextFieldNr = new JTextField();
        jButtonCancel = new JButton();
        jButtonApply = new JButton();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(bundle.getString("FormInsertQuerverweis.title"));
        setModal(true);
        setName("FormInsertQuerverweis");
        setResizable(false);
        Container contentPane = getContentPane();

        //---- jLabelDesc ----
        jLabelDesc.setText(bundle.getString("jLabelDesc.text"));
        jLabelDesc.setName("jLabelDesc");

        //---- jTextFieldDesc ----
        jTextFieldDesc.setToolTipText(bundle.getString("jTextFieldDesc.toolTipText"));
        jTextFieldDesc.setName("jTextFieldDesc");

        //---- jLabelNr ----
        jLabelNr.setText(bundle.getString("jLabelNr.text"));
        jLabelNr.setName("jLabelNr");

        //---- jTextFieldNr ----
        jTextFieldNr.setToolTipText(bundle.getString("jTextFieldNr.toolTipText"));
        jTextFieldNr.setName("jTextFieldNr");

        //---- jButtonCancel ----
        jButtonCancel.setName("jButtonCancel");

        //---- jButtonApply ----
        jButtonApply.setName("jButtonApply");

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                            .addGroup(contentPaneLayout.createParallelGroup()
                                .addComponent(jLabelDesc)
                                .addComponent(jLabelNr))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(contentPaneLayout.createParallelGroup()
                                .addComponent(jTextFieldNr)
                                .addComponent(jTextFieldDesc)))
                        .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                            .addGap(0, 262, Short.MAX_VALUE)
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
                        .addComponent(jLabelDesc)
                        .addComponent(jTextFieldDesc, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabelNr)
                        .addComponent(jTextFieldNr, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonCancel)
                        .addComponent(jButtonApply))
                    .addContainerGap())
        );
        pack();
        setLocationRelativeTo(getOwner());
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Ralf Barkow
    private JLabel jLabelDesc;
    private JTextField jTextFieldDesc;
    private JLabel jLabelNr;
    private JTextField jTextFieldNr;
    private JButton jButtonCancel;
    private JButton jButtonApply;
    // End of variables declaration//GEN-END:variables
}
