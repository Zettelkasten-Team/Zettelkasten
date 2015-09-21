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
        // if (isMacAqua) jButtonResetRating.putClientProperty("JButton.buttonType","roundRect");
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
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jButtonApply = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabelRate2 = new javax.swing.JLabel();
        jLabelRate1 = new javax.swing.JLabel();
        jLabelRate4 = new javax.swing.JLabel();
        jLabelRate5 = new javax.swing.JLabel();
        jSlider1 = new javax.swing.JSlider();
        jLabelRate3 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabelCurrentRating = new javax.swing.JLabel();
        jLabelCR = new javax.swing.JLabel();
        jButtonResetRating = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabelRatingCount = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getResourceMap(CRateEntry.class);
        setTitle(resourceMap.getString("FormRateEntry.title")); // NOI18N
        setModal(true);
        setName("FormRateEntry"); // NOI18N
        setResizable(false);

        jPanel1.setName("jPanel1"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getActionMap(CRateEntry.class, this);
        jButtonApply.setAction(actionMap.get("rateEntry")); // NOI18N
        jButtonApply.setName("jButtonApply"); // NOI18N

        jButtonCancel.setAction(actionMap.get("cancel")); // NOI18N
        jButtonCancel.setName("jButtonCancel"); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setName("jPanel2"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabelRate2.setIcon(resourceMap.getIcon("jLabelRate2.icon")); // NOI18N
        jLabelRate2.setName("jLabelRate2"); // NOI18N

        jLabelRate1.setIcon(resourceMap.getIcon("jLabelRate1.icon")); // NOI18N
        jLabelRate1.setName("jLabelRate1"); // NOI18N

        jLabelRate4.setIcon(resourceMap.getIcon("jLabelRate4.icon")); // NOI18N
        jLabelRate4.setName("jLabelRate4"); // NOI18N

        jLabelRate5.setIcon(resourceMap.getIcon("jLabelRate5.icon")); // NOI18N
        jLabelRate5.setName("jLabelRate5"); // NOI18N

        jSlider1.setMaximum(10);
        jSlider1.setSnapToTicks(true);
        jSlider1.setValue(0);
        jSlider1.setName("jSlider1"); // NOI18N

        jLabelRate3.setIcon(resourceMap.getIcon("jLabelRate3.icon")); // NOI18N
        jLabelRate3.setName("jLabelRate3"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jSlider1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelRate1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelRate2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelRate3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelRate4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelRate5)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelRate5, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelRate4, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelRate3, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(jLabelRate1, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabelRate2, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel3.setName("jPanel3"); // NOI18N

        jLabelCurrentRating.setText(resourceMap.getString("jLabelCurrentRating.text")); // NOI18N
        jLabelCurrentRating.setName("jLabelCurrentRating"); // NOI18N

        jLabelCR.setText(resourceMap.getString("jLabelCR.text")); // NOI18N
        jLabelCR.setName("jLabelCR"); // NOI18N

        jButtonResetRating.setAction(actionMap.get("resetRating")); // NOI18N
        jButtonResetRating.setName("jButtonResetRating"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jLabelRatingCount.setText(resourceMap.getString("jLabelRatingCount.text")); // NOI18N
        jLabelRatingCount.setName("jLabelRatingCount"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabelCR)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelCurrentRating)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonResetRating))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelRatingCount)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabelCurrentRating)
                    .addComponent(jLabelCR)
                    .addComponent(jButtonResetRating))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabelRatingCount))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jButtonCancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonApply)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonCancel)
                    .addComponent(jButtonApply))
                .addContainerGap())
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
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonApply;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonResetRating;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelCR;
    private javax.swing.JLabel jLabelCurrentRating;
    private javax.swing.JLabel jLabelRate1;
    private javax.swing.JLabel jLabelRate2;
    private javax.swing.JLabel jLabelRate3;
    private javax.swing.JLabel jLabelRate4;
    private javax.swing.JLabel jLabelRate5;
    private javax.swing.JLabel jLabelRatingCount;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSlider jSlider1;
    // End of variables declaration//GEN-END:variables

}
