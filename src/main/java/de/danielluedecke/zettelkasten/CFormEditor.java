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
public class CFormEditor extends javax.swing.JDialog {
    /**
     *
     */
    private boolean modified = false;
    /**
     * 
     * @return 
     */
    public boolean isModified() {
        return modified;
    }
    /**
     * 
     */
    private String formtag = "";
    /**
     * 
     * @return 
     */
    public String getFormTag() {
        return formtag;
    }


    /**
     * 
     * @param parent 
     */
    public CFormEditor(java.awt.Frame parent, Settings settingsObj) {
        super(parent);
        initComponents();
        // set application icon
        setIconImage(Constants.zknicon.getImage());
        // disable apply-button
        jButtonApply.setEnabled(false);
        // set default button
        getRootPane().setDefaultButton(jButtonApply);
        // init listeners
        initListeners();
        // init textfield
        jTextField4.setEnabled(jCheckBoxReentry.isSelected());        
        if (settingsObj.isSeaGlass()) {
            jButtonApply.putClientProperty("JComponent.sizeVariant", "small");
            jButtonCancel.putClientProperty("JComponent.sizeVariant", "small");
        }
    }
    
    
    private void initListeners() {
        // These code lines add an escape-listener to the dialog. So, when the user
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
        // if the user inputs text into the textfield, check whether we have at least 
        // the two necessary textfield filled with data
        jTextField1.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void changedUpdate(DocumentEvent e) { enableApplyButton(); }
            @Override public void insertUpdate(DocumentEvent e) { enableApplyButton(); }
            @Override public void removeUpdate(DocumentEvent e) { enableApplyButton(); }
        });
        jTextField2.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void changedUpdate(DocumentEvent e) { enableApplyButton(); }
            @Override public void insertUpdate(DocumentEvent e) { enableApplyButton(); }
            @Override public void removeUpdate(DocumentEvent e) { enableApplyButton(); }
        });
        jCheckBoxReentry.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                jTextField4.setEnabled(jCheckBoxReentry.isSelected());
            }
        });

    }
    /**
     * 
     */
    private void enableApplyButton() {
        // check whether we have text in first textfield
        boolean check1 = jTextField1.getText()!=null && !jTextField1.getText().isEmpty();
        // check whether we have text in second textfield
        boolean check2 = jTextField2.getText()!=null && !jTextField2.getText().isEmpty();
        // enable apply-button only if we have text in the obligatory text fields
        jButtonApply.setEnabled(check1 && check2);
    }
    /**
     * 
     */
    @Action
    public void cancel() {
        modified = false;
        // Close Window
        setVisible(false);
        dispose();
    }
    /**
     * 
     */
    @Action
    public void insertForm() {
        // create string builder
        StringBuilder sb = new StringBuilder(Constants.FORMAT_FORM_TAG);
        // check whether reentry-hook is requested
        if (jCheckBoxReentry.isSelected()) sb.append("#");
        sb.append(" ");
        // check whether we have a description
        if (jTextFieldModel.getText()!=null && !jTextFieldModel.getText().isEmpty()) {
            // append description, i.e. left side of equal sign
            sb.append(jTextFieldModel.getText()).append("=");
        }
        // add first distinction
        sb.append(jTextField1.getText()).append("|");
        // add secont distinction
        sb.append(jTextField2.getText());
        // get third distinction
        String thirdDist = jTextField3.getText();
        // if we have third distinction, append it
        if (thirdDist!=null && !thirdDist.isEmpty()) {
            sb.append("|").append(thirdDist);
        }
        // get unmarked space
        String unmarkedSpace = jTextField4.getText();
        // if we have third distinction, append it
        if (jCheckBoxReentry.isSelected() && unmarkedSpace!=null && !unmarkedSpace.isEmpty() && !unmarkedSpace.equals("unmarked Space")) {
            sb.append("^").append(unmarkedSpace);
        }
        // close tag
        sb.append("]").append(System.lineSeparator());
        // copy result to global variable
        formtag = sb.toString();
        modified = true;
        dispose();
        setVisible(false);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    // Generated using JFormDesigner Evaluation license - Ralf Barkow
    private void initComponents() {
        ResourceBundle bundle = ResourceBundle.getBundle("de.danielluedecke.zettelkasten.resources.CFormEditor");
        jPanel1 = new JPanel();
        jTextFieldModel = new JTextField();
        jLabel1 = new JLabel();
        jTextField1 = new JTextField();
        jLabel2 = new JLabel();
        jTextField2 = new JTextField();
        jTextField3 = new JTextField();
        jTextField4 = new JTextField();
        jCheckBoxReentry = new JCheckBox();
        jButtonApply = new JButton();
        jButtonCancel = new JButton();
        jLabel3 = new JLabel();
        jLabel4 = new JLabel();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(bundle.getString("Form.title"));
        setModal(true);
        setName("Form");
        setResizable(false);
        Container contentPane = getContentPane();

        //======== jPanel1 ========
        {
            jPanel1.setName("jPanel1");
            jPanel1.setBorder (new javax. swing. border. CompoundBorder( new javax .swing .border .TitledBorder (
            new javax. swing. border. EmptyBorder( 0, 0, 0, 0) , "JFor\u006dDesi\u0067ner \u0045valu\u0061tion"
            , javax. swing. border. TitledBorder. CENTER, javax. swing. border. TitledBorder. BOTTOM
            , new java .awt .Font ("Dia\u006cog" ,java .awt .Font .BOLD ,12 )
            , java. awt. Color. red) ,jPanel1. getBorder( )) ); jPanel1. addPropertyChangeListener (
            new java. beans. PropertyChangeListener( ){ @Override public void propertyChange (java .beans .PropertyChangeEvent e
            ) {if ("bord\u0065r" .equals (e .getPropertyName () )) throw new RuntimeException( )
            ; }} );

            //---- jTextFieldModel ----
            jTextFieldModel.setName("jTextFieldModel");

            //---- jLabel1 ----
            jLabel1.setText(bundle.getString("jLabel1.text"));
            jLabel1.setName("jLabel1");

            //---- jTextField1 ----
            jTextField1.setName("jTextField1");

            //---- jLabel2 ----
            jLabel2.setName("jLabel2");

            //---- jTextField2 ----
            jTextField2.setName("jTextField2");

            //---- jTextField3 ----
            jTextField3.setName("jTextField3");

            //---- jTextField4 ----
            jTextField4.setText(bundle.getString("jTextField4.text"));
            jTextField4.setToolTipText(bundle.getString("jTextField4.toolTipText"));
            jTextField4.setName("jTextField4");

            //---- jCheckBoxReentry ----
            jCheckBoxReentry.setText(bundle.getString("jCheckBoxReentry.text"));
            jCheckBoxReentry.setName("jCheckBoxReentry");

            //---- jButtonApply ----
            jButtonApply.setName("jButtonApply");

            //---- jButtonCancel ----
            jButtonCancel.setName("jButtonCancel");

            //---- jLabel3 ----
            jLabel3.setName("jLabel3");

            //---- jLabel4 ----
            jLabel4.setText(bundle.getString("jLabel4.text"));
            jLabel4.setName("jLabel4");

            GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
            jPanel1.setLayout(jPanel1Layout);
            jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup()
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup()
                            .addComponent(jCheckBoxReentry)
                            .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jButtonCancel)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jButtonApply))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jTextFieldModel, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabel1)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jTextField1, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabel2)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jTextField2, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabel3)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jTextField3, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabel4)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jTextField4, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE))))
                        .addContainerGap())
            );
            jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup()
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel4, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
                                .addComponent(jTextField4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                .addComponent(jTextField2, GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel2, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jTextFieldModel, GroupLayout.Alignment.TRAILING)
                                .addComponent(jTextField1, GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jTextField3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel3, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxReentry)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(jButtonApply)
                            .addComponent(jButtonCancel))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
        }

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
            contentPaneLayout.createParallelGroup()
                .addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        contentPaneLayout.setVerticalGroup(
            contentPaneLayout.createParallelGroup()
                .addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        pack();
        setLocationRelativeTo(getOwner());
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Ralf Barkow
    private JPanel jPanel1;
    private JTextField jTextFieldModel;
    private JLabel jLabel1;
    private JTextField jTextField1;
    private JLabel jLabel2;
    private JTextField jTextField2;
    private JTextField jTextField3;
    private JTextField jTextField4;
    private JCheckBox jCheckBoxReentry;
    private JButton jButtonApply;
    private JButton jButtonCancel;
    private JLabel jLabel3;
    private JLabel jLabel4;
    // End of variables declaration//GEN-END:variables
}
