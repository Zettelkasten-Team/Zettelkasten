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
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.database.Daten;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.jdesktop.application.Action;

/**
 *
 * @author danielludecke
 */
public class CRateEntry extends javax.swing.JDialog {

    private final Daten dataObj;
    private boolean cancelled = false;
    private final int entrynr;

    private static final int RATING_VALUE_NONE = 1;
    private static final int RATING_VALUE_HALF = 2;
    private static final int RATING_VALUE_FULL = 3;

    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap =
        org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
        getContext().getResourceMap(CRateEntry.class);

    /**
     * 
     * @param parent
     * @param d
     * @param nr 
     */
    public CRateEntry(java.awt.Frame parent, Daten d, int nr) {
        super(parent);
        dataObj = d;
        entrynr = nr;
        initComponents();
        // set application icon
        setIconImage(Constants.zknicon.getImage());
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
        // init entry data
        initEntryData();
        // add listener for slider
        jSlider1.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override public void stateChanged(javax.swing.event.ChangeEvent evt) {
                updateImages();
                jButtonApply.setEnabled(true);
            }
        });
        // make button mac style
        // if (isMacStyle) jButtonResetRating.putClientProperty("JButton.buttonType","roundRect");
        // disable button
        jButtonApply.setEnabled(false);
    }


    /**
     * This method creates the image-tag for a rating-point
     *
     * @param ratingvalue the ratingvalue, i.e. whether the image-tag should contain a full, half or no value-point-image.
     * use following constants:<br>
     * <ul>
     * <li>{@link #RATING_VALUE_FULL RATING_VALUE_FULL}</li>
     * <li>{@link #RATING_VALUE_FULL RATING_VALUE_HALF}</li>
     * <li>{@link #RATING_VALUE_FULL RATING_VALUE_NONE}</li>
     * </ul>
     * @return the HTML-formatted image-tag with the requested rating-symbol
     */
    private Icon getRatingSymbol(int ratingvalue) {
        // create value for img-name
        URL imgURL = null;
        // check which image to choose
        switch (ratingvalue) {
            // no rating point
            case RATING_VALUE_NONE: imgURL = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getClass().getResource("/de/danielluedecke/zettelkasten/resources/icons/bullet_black.png"); break;
            // half rating point
            case RATING_VALUE_HALF: imgURL = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getClass().getResource("/de/danielluedecke/zettelkasten/resources/icons/bullet_yellow.png"); break;
            // full rating point
            case RATING_VALUE_FULL: imgURL = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getClass().getResource("/de/danielluedecke/zettelkasten/resources/icons/bullet_green.png"); break;
        }
        // return result
        return new ImageIcon(imgURL);
    }


    private void updateImages() {
        // get slider position
        int value = jSlider1.getValue();
        // set images for first star
        if (value>=2) jLabelRate1.setIcon(getRatingSymbol(RATING_VALUE_FULL));
        else if (value>=1) jLabelRate1.setIcon(getRatingSymbol(RATING_VALUE_HALF));
        else jLabelRate1.setIcon(getRatingSymbol(RATING_VALUE_NONE));
        // set images for second star
        if (value>=4) jLabelRate2.setIcon(getRatingSymbol(RATING_VALUE_FULL));
        else if (value>=3) jLabelRate2.setIcon(getRatingSymbol(RATING_VALUE_HALF));
        else jLabelRate2.setIcon(getRatingSymbol(RATING_VALUE_NONE));
        // set images for third star
        if (value>=6) jLabelRate3.setIcon(getRatingSymbol(RATING_VALUE_FULL));
        else if (value>=5) jLabelRate3.setIcon(getRatingSymbol(RATING_VALUE_HALF));
        else jLabelRate3.setIcon(getRatingSymbol(RATING_VALUE_NONE));
        // set images for fourth star
        if (value>=8) jLabelRate4.setIcon(getRatingSymbol(RATING_VALUE_FULL));
        else if (value>=7) jLabelRate4.setIcon(getRatingSymbol(RATING_VALUE_HALF));
        else jLabelRate4.setIcon(getRatingSymbol(RATING_VALUE_NONE));
        // set images for fifth star
        if (value>=10) jLabelRate5.setIcon(getRatingSymbol(RATING_VALUE_FULL));
        else if (value>=9) jLabelRate5.setIcon(getRatingSymbol(RATING_VALUE_HALF));
        else jLabelRate5.setIcon(getRatingSymbol(RATING_VALUE_NONE));
    }


    private void initEntryData() {
        // get default text for label
        StringBuilder deftext = new StringBuilder(resourceMap.getString("jLabel1.text"));
        // add entrynumber and colon
        deftext.append(" ").append(String.valueOf(entrynr)).append(":");
        // update label title
        jLabel1.setText(deftext.toString());
        // get current rating value
        jLabelCurrentRating.setText(dataObj.getZettelRatingAsString(entrynr));
        // get rating count, as string
        String ratingcount = String.valueOf(dataObj.getZettelRatingCount(entrynr));
        // create numbers
        jLabelRatingCount.setText(ratingcount);
    }


    /**
     * When the user presses the cancel button, no update needed, close window
     */
    @Action
    public void cancel() {
        cancelled = true;
        dispose();
        setVisible(false);
    }


    @Action
    public void rateEntry() {
        // retrieve rate-value
        int rating = jSlider1.getValue();
        // add rating to entry
        dataObj.addZettelRating(entrynr, (float) (rating/2.0));
        cancelled = false;
        dispose();
        setVisible(false);
    }

    
    @Action
    public void resetRating() {
        // reset rating
        dataObj.resetZettelRating(entrynr);
        cancelled = false;
        dispose();
        setVisible(false);
    }

    
    public boolean isCancelled() {
        return cancelled;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    // Generated using JFormDesigner Evaluation license - Ralf Barkow
    private void initComponents() {
        ResourceBundle bundle = ResourceBundle.getBundle("de.danielluedecke.zettelkasten.resources.CRateEntry");
        jPanel1 = new JPanel();
        jButtonApply = new JButton();
        jButtonCancel = new JButton();
        jPanel2 = new JPanel();
        jLabel1 = new JLabel();
        jLabelRate2 = new JLabel();
        jLabelRate1 = new JLabel();
        jLabelRate4 = new JLabel();
        jLabelRate5 = new JLabel();
        jSlider1 = new JSlider();
        jLabelRate3 = new JLabel();
        jPanel3 = new JPanel();
        jLabelCurrentRating = new JLabel();
        jLabelCR = new JLabel();
        jButtonResetRating = new JButton();
        jLabel2 = new JLabel();
        jLabelRatingCount = new JLabel();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(bundle.getString("FormRateEntry.title"));
        setModal(true);
        setName("FormRateEntry");
        setResizable(false);
        Container contentPane = getContentPane();

        //======== jPanel1 ========
        {
            jPanel1.setName("jPanel1");
            jPanel1.setBorder(new javax.swing.border.CompoundBorder(new javax.swing.border.TitledBorder(new javax
            .swing.border.EmptyBorder(0,0,0,0), "JF\u006frmD\u0065sig\u006eer \u0045val\u0075ati\u006fn",javax.swing
            .border.TitledBorder.CENTER,javax.swing.border.TitledBorder.BOTTOM,new java.awt.
            Font("Dia\u006cog",java.awt.Font.BOLD,12),java.awt.Color.red
            ),jPanel1. getBorder()));jPanel1. addPropertyChangeListener(new java.beans.PropertyChangeListener(){@Override
            public void propertyChange(java.beans.PropertyChangeEvent e){if("\u0062ord\u0065r".equals(e.getPropertyName(
            )))throw new RuntimeException();}});

            //---- jButtonApply ----
            jButtonApply.setName("jButtonApply");

            //---- jButtonCancel ----
            jButtonCancel.setName("jButtonCancel");

            //======== jPanel2 ========
            {
                jPanel2.setBorder(new EtchedBorder());
                jPanel2.setName("jPanel2");

                //---- jLabel1 ----
                jLabel1.setText(bundle.getString("jLabel1.text"));
                jLabel1.setName("jLabel1");

                //---- jLabelRate2 ----
                jLabelRate2.setName("jLabelRate2");

                //---- jLabelRate1 ----
                jLabelRate1.setName("jLabelRate1");

                //---- jLabelRate4 ----
                jLabelRate4.setName("jLabelRate4");

                //---- jLabelRate5 ----
                jLabelRate5.setName("jLabelRate5");

                //---- jSlider1 ----
                jSlider1.setMaximum(10);
                jSlider1.setSnapToTicks(true);
                jSlider1.setValue(0);
                jSlider1.setName("jSlider1");

                //---- jLabelRate3 ----
                jLabelRate3.setName("jLabelRate3");

                GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
                jPanel2.setLayout(jPanel2Layout);
                jPanel2Layout.setHorizontalGroup(
                    jPanel2Layout.createParallelGroup()
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jSlider1, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                    .addComponent(jLabel1)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabelRate1)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabelRate2)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabelRate3)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabelRate4)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabelRate5)))
                            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
                jPanel2Layout.setVerticalGroup(
                    jPanel2Layout.createParallelGroup()
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(jPanel2Layout.createParallelGroup()
                                .addComponent(jLabelRate5, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabelRate4, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabelRate3, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
                                .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabelRate1, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE))
                                .addComponent(jLabelRate2, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jSlider1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
            }

            //======== jPanel3 ========
            {
                jPanel3.setBorder(new EtchedBorder());
                jPanel3.setName("jPanel3");

                //---- jLabelCurrentRating ----
                jLabelCurrentRating.setText(bundle.getString("jLabelCurrentRating.text"));
                jLabelCurrentRating.setName("jLabelCurrentRating");

                //---- jLabelCR ----
                jLabelCR.setText(bundle.getString("jLabelCR.text"));
                jLabelCR.setName("jLabelCR");

                //---- jButtonResetRating ----
                jButtonResetRating.setName("jButtonResetRating");

                //---- jLabel2 ----
                jLabel2.setText(bundle.getString("jLabel2.text"));
                jLabel2.setName("jLabel2");

                //---- jLabelRatingCount ----
                jLabelRatingCount.setText(bundle.getString("jLabelRatingCount.text"));
                jLabelRatingCount.setName("jLabelRatingCount");

                GroupLayout jPanel3Layout = new GroupLayout(jPanel3);
                jPanel3.setLayout(jPanel3Layout);
                jPanel3Layout.setHorizontalGroup(
                    jPanel3Layout.createParallelGroup()
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(jPanel3Layout.createParallelGroup()
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addComponent(jLabelCR)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabelCurrentRating)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jButtonResetRating))
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addComponent(jLabel2)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabelRatingCount)))
                            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
                jPanel3Layout.setVerticalGroup(
                    jPanel3Layout.createParallelGroup()
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                .addComponent(jLabelCurrentRating)
                                .addComponent(jLabelCR)
                                .addComponent(jButtonResetRating))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel2)
                                .addComponent(jLabelRatingCount))
                            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
            }

            GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
            jPanel1.setLayout(jPanel1Layout);
            jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup()
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup()
                            .addComponent(jPanel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jButtonCancel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButtonApply)))
                        .addContainerGap())
            );
            jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup()
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(jButtonCancel)
                            .addComponent(jButtonApply))
                        .addContainerGap())
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
                    .addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap())
        );
        pack();
        setLocationRelativeTo(getOwner());
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Ralf Barkow
    private JPanel jPanel1;
    private JButton jButtonApply;
    private JButton jButtonCancel;
    private JPanel jPanel2;
    private JLabel jLabel1;
    private JLabel jLabelRate2;
    private JLabel jLabelRate1;
    private JLabel jLabelRate4;
    private JLabel jLabelRate5;
    private JSlider jSlider1;
    private JLabel jLabelRate3;
    private JPanel jPanel3;
    private JLabel jLabelCurrentRating;
    private JLabel jLabelCR;
    private JButton jButtonResetRating;
    private JLabel jLabel2;
    private JLabel jLabelRatingCount;
    // End of variables declaration//GEN-END:variables

}
