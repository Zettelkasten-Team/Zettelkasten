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
        Hashtable labelTable = new Hashtable();
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
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jButtonChangeColor = new javax.swing.JButton();
        jCheckBoxBold = new javax.swing.JCheckBox();
        jCheckBoxItalic = new javax.swing.JCheckBox();
        jCheckBoxShowHighlight = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        jSliderSize = new javax.swing.JSlider();
        jLabel2 = new javax.swing.JLabel();
        jButtonBackgroundColor = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabelColor = new javax.swing.JLabel();
        jButtonCancel = new javax.swing.JButton();
        jButtonApply = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getResourceMap(CHighlightSearchSettings.class);
        setTitle(resourceMap.getString("FormHighlightSearchSettings.title")); // NOI18N
        setModal(true);
        setName("FormHighlightSearchSettings"); // NOI18N
        setResizable(false);

        jPanel1.setName("jPanel1"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jButtonChangeColor.setText(resourceMap.getString("jButtonChangeColor.text")); // NOI18N
        jButtonChangeColor.setName("jButtonChangeColor"); // NOI18N
        jButtonChangeColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonChangeColorActionPerformed(evt);
            }
        });

        jCheckBoxBold.setText(resourceMap.getString("jCheckBoxBold.text")); // NOI18N
        jCheckBoxBold.setName("jCheckBoxBold"); // NOI18N
        jCheckBoxBold.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxBoldActionPerformed(evt);
            }
        });

        jCheckBoxItalic.setText(resourceMap.getString("jCheckBoxItalic.text")); // NOI18N
        jCheckBoxItalic.setName("jCheckBoxItalic"); // NOI18N
        jCheckBoxItalic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxItalicActionPerformed(evt);
            }
        });

        jCheckBoxShowHighlight.setText(resourceMap.getString("jCheckBoxShowHighlight.text")); // NOI18N
        jCheckBoxShowHighlight.setName("jCheckBoxShowHighlight"); // NOI18N
        jCheckBoxShowHighlight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxShowHighlightActionPerformed(evt);
            }
        });

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jSliderSize.setMajorTickSpacing(1);
        jSliderSize.setMaximum(5);
        jSliderSize.setMinorTickSpacing(1);
        jSliderSize.setPaintLabels(true);
        jSliderSize.setPaintTicks(true);
        jSliderSize.setSnapToTicks(true);
        jSliderSize.setName("jSliderSize"); // NOI18N
        jSliderSize.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderSizeStateChanged(evt);
            }
        });

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jButtonBackgroundColor.setText(resourceMap.getString("jButtonBackgroundColor.text")); // NOI18N
        jButtonBackgroundColor.setName("jButtonBackgroundColor"); // NOI18N
        jButtonBackgroundColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBackgroundColorActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        jLabelColor.setText(resourceMap.getString("jLabelColor.text")); // NOI18N
        jLabelColor.setName("jLabelColor"); // NOI18N
        jLabelColor.setOpaque(true);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelColor)
                .addContainerGap(241, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelColor)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getActionMap(CHighlightSearchSettings.class, this);
        jButtonCancel.setAction(actionMap.get("cancel")); // NOI18N
        jButtonCancel.setName("jButtonCancel"); // NOI18N

        jButtonApply.setAction(actionMap.get("apply")); // NOI18N
        jButtonApply.setName("jButtonApply"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jCheckBoxBold)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBoxItalic)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBoxShowHighlight))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel2))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jButtonBackgroundColor)
                                    .addComponent(jButtonChangeColor)))
                            .addComponent(jSliderSize, javax.swing.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                            .addComponent(jLabel3))
                        .addGap(6, 6, 6))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jButtonCancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonApply)
                        .addContainerGap())))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jButtonChangeColor))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonBackgroundColor)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxBold)
                    .addComponent(jCheckBoxItalic)
                    .addComponent(jCheckBoxShowHighlight))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSliderSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonCancel)
                    .addComponent(jButtonApply)))
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
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
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
    private javax.swing.JButton jButtonApply;
    private javax.swing.JButton jButtonBackgroundColor;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonChangeColor;
    private javax.swing.JCheckBox jCheckBoxBold;
    private javax.swing.JCheckBox jCheckBoxItalic;
    private javax.swing.JCheckBox jCheckBoxShowHighlight;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabelColor;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSlider jSliderSize;
    // End of variables declaration//GEN-END:variables

}
