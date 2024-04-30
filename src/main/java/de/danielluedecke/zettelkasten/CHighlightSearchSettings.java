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
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Hashtable;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import org.jdesktop.application.Action;

/**
 *
 * @author  danielludecke
 */
public class CHighlightSearchSettings extends javax.swing.JDialog {

    private Settings settingsObj;
    private int highlightstyle;
    private boolean cancelled = false;
    public boolean isCancelled() {
        return cancelled;
    }
    /**
     * get the strings for file descriptions from the resource map
     */
    private org.jdesktop.application.ResourceMap resourceMap = 
        org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
        getContext().getResourceMap(CHighlightSearchSettings.class);
    
    
    /**
     * 
     * @param parent
     * @param s 
     */
    public CHighlightSearchSettings(java.awt.Frame parent, Settings s, int style) {
        super(parent);
        initComponents();
        
        settingsObj = s;
        highlightstyle = style;
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
        // set default button
        getRootPane().setDefaultButton(jButtonApply);
        // init the slider for the default table and list fontsize
//        Map<Integer, JLabel> labelTable = new HashMap<Integer, JLabel>();
//        labelTable.put(new Integer(0), new JLabel(resourceMap.getString("smallFontSize")));
//        labelTable.put(new Integer(5), new JLabel(resourceMap.getString("bigFontSize")));
//        jSliderSize.setLabelTable((Dictionary)labelTable);
        Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
        labelTable.put(new Integer(0), new JLabel(resourceMap.getString("smallFontSize")));
        labelTable.put(new Integer(5), new JLabel(resourceMap.getString("bigFontSize")));
        jSliderSize.setLabelTable(labelTable);
        jSliderSize.setValue(Integer.parseInt(settingsObj.getHighlightSearchStyle(Settings.FONTSIZE, highlightstyle)));

        jCheckBoxBold.setSelected(settingsObj.getHighlightSearchStyle(Settings.FONTWEIGHT, highlightstyle).equalsIgnoreCase("bold"));
        jCheckBoxItalic.setSelected(settingsObj.getHighlightSearchStyle(Settings.FONTSTYLE, highlightstyle).equalsIgnoreCase("italic"));
        jCheckBoxShowHighlight.setSelected(settingsObj.getShowHighlightBackground(highlightstyle));

        jLabelColor.setForeground(new Color(Integer.parseInt(settingsObj.getHighlightSearchStyle(Settings.FONTCOLOR, highlightstyle), 16)));
        if (settingsObj.getShowHighlightBackground(highlightstyle)) jLabelColor.setBackground(new Color(Integer.parseInt(settingsObj.getHighlightBackgroundColor(highlightstyle), 16)));
        if (settingsObj.isSeaGlass()) {
            jButtonApply.putClientProperty("JComponent.sizeVariant", "small");
            jButtonBackgroundColor.putClientProperty("JComponent.sizeVariant", "small");
            jButtonCancel.putClientProperty("JComponent.sizeVariant", "small");
            jButtonChangeColor.putClientProperty("JComponent.sizeVariant", "small");
        }
    }

    private String chooseColor(String col) {
        // first, show an color-chooser-dialog and let the user choose the color
        Color color = JColorChooser.showDialog(this, resourceMap.getString("chooseColorMsg"), new Color(Integer.parseInt(col, 16)));
        // if the user chose a color, proceed
        if (color!=null) {
            // convert the color-rgb-values into a hexa-decimal-string
            StringBuilder output = new StringBuilder("");
            // we need the format option to keep the leeding zero of hex-values
            // from 00 to 0F.
            output.append(String.format("%02x", color.getRed()));
            output.append(String.format("%02x", color.getGreen()));
            output.append(String.format("%02x", color.getBlue()));
            // convert the color-rgb-values into a hexa-decimal-string and save the new font color
            return output.toString();
        }
        return col;
    }

    @Action
    public void cancel() {
        cancelled = true;
        dispose();
        setVisible(false);
    }

    @Action
    public void apply() {
        cancelled = false;
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
        ResourceBundle bundle = ResourceBundle.getBundle("de.danielluedecke.zettelkasten.resources.CHighlightSearchSettings");
        jPanel1 = new JPanel();
        jLabel1 = new JLabel();
        jButtonChangeColor = new JButton();
        jCheckBoxBold = new JCheckBox();
        jCheckBoxItalic = new JCheckBox();
        jCheckBoxShowHighlight = new JCheckBox();
        jLabel3 = new JLabel();
        jSliderSize = new JSlider();
        jLabel2 = new JLabel();
        jButtonBackgroundColor = new JButton();
        jPanel2 = new JPanel();
        jLabelColor = new JLabel();
        jButtonCancel = new JButton();
        jButtonApply = new JButton();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(bundle.getString("FormHighlightSearchSettings.title"));
        setModal(true);
        setName("FormHighlightSearchSettings");
        setResizable(false);
        Container contentPane = getContentPane();

        //======== jPanel1 ========
        {
            jPanel1.setName("jPanel1");
            jPanel1.setBorder(new javax.swing.border.CompoundBorder(new javax.swing.border.TitledBorder(new javax.swing
            .border.EmptyBorder(0,0,0,0), "JF\u006frmD\u0065sig\u006eer \u0045val\u0075ati\u006fn",javax.swing.border.TitledBorder
            .CENTER,javax.swing.border.TitledBorder.BOTTOM,new java.awt.Font("Dia\u006cog",java.
            awt.Font.BOLD,12),java.awt.Color.red),jPanel1. getBorder()))
            ;jPanel1. addPropertyChangeListener(new java.beans.PropertyChangeListener(){@Override public void propertyChange(java.beans.PropertyChangeEvent e
            ){if("\u0062ord\u0065r".equals(e.getPropertyName()))throw new RuntimeException();}})
            ;

            //---- jLabel1 ----
            jLabel1.setText(bundle.getString("jLabel1.text"));
            jLabel1.setName("jLabel1");

            //---- jButtonChangeColor ----
            jButtonChangeColor.setText(bundle.getString("jButtonChangeColor.text"));
            jButtonChangeColor.setName("jButtonChangeColor");
            jButtonChangeColor.addActionListener(e -> jButtonChangeColorActionPerformed(e));

            //---- jCheckBoxBold ----
            jCheckBoxBold.setText(bundle.getString("jCheckBoxBold.text"));
            jCheckBoxBold.setName("jCheckBoxBold");
            jCheckBoxBold.addActionListener(e -> jCheckBoxBoldActionPerformed(e));

            //---- jCheckBoxItalic ----
            jCheckBoxItalic.setText(bundle.getString("jCheckBoxItalic.text"));
            jCheckBoxItalic.setName("jCheckBoxItalic");
            jCheckBoxItalic.addActionListener(e -> jCheckBoxItalicActionPerformed(e));

            //---- jCheckBoxShowHighlight ----
            jCheckBoxShowHighlight.setText(bundle.getString("jCheckBoxShowHighlight.text"));
            jCheckBoxShowHighlight.setName("jCheckBoxShowHighlight");
            jCheckBoxShowHighlight.addActionListener(e -> jCheckBoxShowHighlightActionPerformed(e));

            //---- jLabel3 ----
            jLabel3.setText(bundle.getString("jLabel3.text"));
            jLabel3.setName("jLabel3");

            //---- jSliderSize ----
            jSliderSize.setMajorTickSpacing(1);
            jSliderSize.setMaximum(5);
            jSliderSize.setMinorTickSpacing(1);
            jSliderSize.setPaintLabels(true);
            jSliderSize.setPaintTicks(true);
            jSliderSize.setSnapToTicks(true);
            jSliderSize.setName("jSliderSize");
            jSliderSize.addChangeListener(e -> jSliderSizeStateChanged(e));

            //---- jLabel2 ----
            jLabel2.setText(bundle.getString("jLabel2.text"));
            jLabel2.setName("jLabel2");

            //---- jButtonBackgroundColor ----
            jButtonBackgroundColor.setText(bundle.getString("jButtonBackgroundColor.text"));
            jButtonBackgroundColor.setName("jButtonBackgroundColor");
            jButtonBackgroundColor.addActionListener(e -> jButtonBackgroundColorActionPerformed(e));

            //======== jPanel2 ========
            {
                jPanel2.setBorder(new TitledBorder("Vorschau Farbeinstellungen"));
                jPanel2.setName("jPanel2");

                //---- jLabelColor ----
                jLabelColor.setText(bundle.getString("jLabelColor.text"));
                jLabelColor.setName("jLabelColor");
                jLabelColor.setOpaque(true);

                GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
                jPanel2.setLayout(jPanel2Layout);
                jPanel2Layout.setHorizontalGroup(
                    jPanel2Layout.createParallelGroup()
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jLabelColor)
                            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
                jPanel2Layout.setVerticalGroup(
                    jPanel2Layout.createParallelGroup()
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jLabelColor)
                            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
            }

            //---- jButtonCancel ----
            jButtonCancel.setName("jButtonCancel");

            //---- jButtonApply ----
            jButtonApply.setName("jButtonApply");

            GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
            jPanel1.setLayout(jPanel1Layout);
            jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup()
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup()
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup()
                                    .addComponent(jPanel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jCheckBoxBold)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jCheckBoxItalic)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jCheckBoxShowHighlight))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel1)
                                            .addComponent(jLabel2))
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel1Layout.createParallelGroup()
                                            .addComponent(jButtonBackgroundColor)
                                            .addComponent(jButtonChangeColor)))
                                    .addComponent(jSliderSize, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel3))
                                .addGap(6, 6, 6))
                            .addGroup(GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jButtonCancel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButtonApply)
                                .addContainerGap())))
            );
            jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup()
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jButtonChangeColor))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(jButtonBackgroundColor)
                            .addComponent(jLabel2))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(jCheckBoxBold)
                            .addComponent(jCheckBoxItalic)
                            .addComponent(jCheckBoxShowHighlight))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel3)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSliderSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(jButtonCancel)
                            .addComponent(jButtonApply)))
            );
        }

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap())
        );
        contentPaneLayout.setVerticalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pack();
        setLocationRelativeTo(getOwner());
    }// </editor-fold>//GEN-END:initComponents

private void jCheckBoxBoldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxBoldActionPerformed
    settingsObj.setHighlightSearchStyle(jCheckBoxBold.isSelected()?"bold":"normal", Settings.FONTWEIGHT, highlightstyle);
}//GEN-LAST:event_jCheckBoxBoldActionPerformed

private void jCheckBoxItalicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxItalicActionPerformed
    settingsObj.setHighlightSearchStyle(jCheckBoxItalic.isSelected()?"italic":"normal", Settings.FONTSTYLE, highlightstyle);
}//GEN-LAST:event_jCheckBoxItalicActionPerformed

private void jSliderSizeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderSizeStateChanged
    settingsObj.setHighlightSearchStyle(String.valueOf(jSliderSize.getValue()), Settings.FONTSIZE, highlightstyle);
}//GEN-LAST:event_jSliderSizeStateChanged

private void jButtonChangeColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonChangeColorActionPerformed
    // first, show an color-chooser-dialog and let the user choose the color
    settingsObj.setHighlightSearchStyle(chooseColor(settingsObj.getHighlightSearchStyle(Settings.FONTCOLOR, highlightstyle)),Settings.FONTCOLOR, highlightstyle);
    jLabelColor.setForeground(new Color(Integer.parseInt(settingsObj.getHighlightSearchStyle(Settings.FONTCOLOR, highlightstyle),16)));
}//GEN-LAST:event_jButtonChangeColorActionPerformed

private void jCheckBoxShowHighlightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxShowHighlightActionPerformed
    settingsObj.setShowHighlightBackground(jCheckBoxShowHighlight.isSelected(), highlightstyle);
    jLabelColor.setBackground((settingsObj.getShowHighlightBackground(highlightstyle))?new Color(Integer.parseInt(settingsObj.getHighlightBackgroundColor(highlightstyle),16)):null);

}//GEN-LAST:event_jCheckBoxShowHighlightActionPerformed

private void jButtonBackgroundColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBackgroundColorActionPerformed
    settingsObj.setHighlightBackgroundColor(chooseColor(settingsObj.getHighlightBackgroundColor(highlightstyle)), highlightstyle);
    jLabelColor.setBackground((settingsObj.getShowHighlightBackground(highlightstyle))?new Color(Integer.parseInt(settingsObj.getHighlightBackgroundColor(highlightstyle),16)):null);
}//GEN-LAST:event_jButtonBackgroundColorActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Ralf Barkow
    private JPanel jPanel1;
    private JLabel jLabel1;
    private JButton jButtonChangeColor;
    private JCheckBox jCheckBoxBold;
    private JCheckBox jCheckBoxItalic;
    private JCheckBox jCheckBoxShowHighlight;
    private JLabel jLabel3;
    private JSlider jSliderSize;
    private JLabel jLabel2;
    private JButton jButtonBackgroundColor;
    private JPanel jPanel2;
    private JLabel jLabelColor;
    private JButton jButtonCancel;
    private JButton jButtonApply;
    // End of variables declaration//GEN-END:variables

}
