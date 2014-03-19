/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.danielluedecke.zettelkasten;

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
     * Creates new form CInsertHyperlink
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
                if (text.startsWith("http") && text.indexOf(".")!=-1) {
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
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jTextFieldURL = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldDescription = new javax.swing.JTextField();
        jButtonApply = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getResourceMap(CInsertHyperlink.class);
        setTitle(resourceMap.getString("FormInsertHyperlink.title")); // NOI18N
        setModal(true);
        setName("FormInsertHyperlink"); // NOI18N
        setResizable(false);

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jTextFieldURL.setText(resourceMap.getString("jTextFieldURL.text")); // NOI18N
        jTextFieldURL.setName("jTextFieldURL"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jTextFieldDescription.setText(resourceMap.getString("jTextFieldDescription.text")); // NOI18N
        jTextFieldDescription.setName("jTextFieldDescription"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getActionMap(CInsertHyperlink.class, this);
        jButtonApply.setAction(actionMap.get("insertHyperlink")); // NOI18N
        jButtonApply.setName("jButtonApply"); // NOI18N

        jButtonCancel.setAction(actionMap.get("cancel")); // NOI18N
        jButtonCancel.setName("jButtonCancel"); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel1)
                            .add(jLabel2))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jTextFieldURL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 360, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jTextFieldDescription, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 360, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(0, 0, Short.MAX_VALUE)
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
                    .add(jLabel1)
                    .add(jTextFieldURL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(jTextFieldDescription, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jButtonCancel)
                    .add(jButtonApply))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonApply;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField jTextFieldDescription;
    private javax.swing.JTextField jTextFieldURL;
    // End of variables declaration//GEN-END:variables
}
