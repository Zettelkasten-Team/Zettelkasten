/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.danielluedecke.zettelkasten;

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
        // these codelines add an escape-listener to the dialog. so, when the user
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
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelDesc = new javax.swing.JLabel();
        jTextFieldDesc = new javax.swing.JTextField();
        jLabelNr = new javax.swing.JLabel();
        jTextFieldNr = new javax.swing.JTextField();
        jButtonCancel = new javax.swing.JButton();
        jButtonApply = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getResourceMap(CInsertManualLink.class);
        setTitle(resourceMap.getString("FormInsertQuerverweis.title")); // NOI18N
        setModal(true);
        setName("FormInsertQuerverweis"); // NOI18N
        setResizable(false);

        jLabelDesc.setText(resourceMap.getString("jLabelDesc.text")); // NOI18N
        jLabelDesc.setName("jLabelDesc"); // NOI18N

        jTextFieldDesc.setToolTipText(resourceMap.getString("jTextFieldDesc.toolTipText")); // NOI18N
        jTextFieldDesc.setName("jTextFieldDesc"); // NOI18N

        jLabelNr.setText(resourceMap.getString("jLabelNr.text")); // NOI18N
        jLabelNr.setName("jLabelNr"); // NOI18N

        jTextFieldNr.setToolTipText(resourceMap.getString("jTextFieldNr.toolTipText")); // NOI18N
        jTextFieldNr.setName("jTextFieldNr"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getActionMap(CInsertManualLink.class, this);
        jButtonCancel.setAction(actionMap.get("cancel")); // NOI18N
        jButtonCancel.setName("jButtonCancel"); // NOI18N

        jButtonApply.setAction(actionMap.get("insertManlink")); // NOI18N
        jButtonApply.setName("jButtonApply"); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabelDesc)
                            .add(jLabelNr))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jTextFieldNr)
                            .add(jTextFieldDesc)))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(0, 262, Short.MAX_VALUE)
                        .add(jButtonCancel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonApply)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelDesc)
                    .add(jTextFieldDesc, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelNr)
                    .add(jTextFieldNr, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButtonCancel)
                    .add(jButtonApply))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonApply;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JLabel jLabelDesc;
    private javax.swing.JLabel jLabelNr;
    private javax.swing.JTextField jTextFieldDesc;
    private javax.swing.JTextField jTextFieldNr;
    // End of variables declaration//GEN-END:variables
}
