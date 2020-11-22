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

import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.util.ExtractFormInformation;
import de.danielluedecke.zettelkasten.util.FileOperationsUtil;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

/**
 *
 * @author danielludecke
 */
public class CMakeFormImage {
    private BufferedImage formimage;
    private final Settings settingsObj;
    private final Daten dataObj;
    private final String formtag;
    private final File formImageDir;
    private boolean saveImgOk;
    private final File formImageFilepath;
    private final File formLargeImageFilepath;
    private static final int IMG_SIZE_SMALL = 1;
    private static final int IMG_SIZE_LARGE = 2;
    private static final int MARK_DISTANCE = 6;
    private static final int MARK_THICKNESS = 2;
    private static final int IMG_PADDING = 2;
    public boolean isSaveImgOk() {
        return saveImgOk;
    }
    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap = 
        org.jdesktop.application.Application.getInstance(ZettelkastenApp.class).
        getContext().getResourceMap(EditorFrame.class);
    
    public CMakeFormImage(Daten dat, Settings set, String ftag) {
        settingsObj = set;
        dataObj = dat;
        formtag = ftag;
        saveImgOk = false;
        // retrieve user defined or default path to form-image-directory
        formImageDir = new File(settingsObj.getFormImagePath(dataObj.getUserImagePath(), false));
        // check whether image file already exists
        formImageFilepath = new File(formImageDir+String.valueOf(File.separatorChar)+FileOperationsUtil.convertFormtagToImagepath(formtag, true, false));
        // create large image for export
        formLargeImageFilepath = new File(formImageDir+String.valueOf(File.separatorChar)+FileOperationsUtil.convertFormtagToImagepath(formtag, true, true));
    }
    public void createFormImage() {
        // check whether this form-image has already been created before
        if (!formImageFilepath.exists()) {
            // if not, draw image
            drawForms(IMG_SIZE_SMALL);
            // save image to disk
            saveImage(formImageFilepath);
            // also draw large image
            drawForms(IMG_SIZE_LARGE);
            // save image to disk
            saveImage(formLargeImageFilepath);
        }
        else {
            saveImgOk = true;
        }
    }
    private void drawForms(int size) {
        // determine mark-distance
        int markdist = size * MARK_DISTANCE;
        // first, extract form-information
        ExtractFormInformation extractedforms = new ExtractFormInformation(formtag);
        // retrieve default font-size
        int fsize = Integer.parseInt(settingsObj.getMainfont(Settings.FONTSIZE));
        // create font, with double font size (for printing resolution)
//        Font font = new Font(settingsObj.getMainfont(Settings.FONTNAME),Font.PLAIN,3*fsize);
        Font font = new Font(settingsObj.getMainfont(Settings.FONTNAME),Font.PLAIN,(int)(size*fsize*1.3));
        // create new image. we use very large dimensions here, and crop the image later
        formimage = new BufferedImage(3000, 1000, BufferedImage.TYPE_INT_RGB);
        Graphics2D g =(Graphics2D) formimage.getGraphics();
        // set font
        g.setFont(font);
        // fill color white 
        // fill white background
        g.fillRect(0, 0, 3000, 1000);
        // draw color black
        g.setPaint(Color.BLACK);
        // line-thickness 2px
        g.setStroke(new BasicStroke(MARK_THICKNESS*size));
        // get font metrics. needed to calculate width of text-portions, so
        // we know where to draw the vertically form-lines
        FontMetrics fm = g.getFontMetrics(font);
        // x-pos. of textstart
        int textStartX = size*IMG_PADDING;
        // x-pos. of marks
        int markStartX = size*IMG_PADDING;
        // calclulate y-position for text. depending on the amount of
        // marks we have, we increase the y-position of text-start by the mark's pixel-distance
        // per mark. finally, we need to add the line height, since the y-pos
        // of text is depending on the font's baseline
        int textStartY = extractedforms.getMarkCount()*markdist + fm.getHeight()+(size*IMG_PADDING);
        // if we also have a reentry-hook, we increase it by one more mark's pixel-distance
        if (extractedforms.hasReentry()) textStartY += markdist;
        // y-start-pos. of marks. we take the text-y-position, substract the lineheight (so we have
        // the y-position of the text-top-line and substract one mark-distance, so the mark-line
        // does not cross the text
        int markStartY = textStartY - markdist - fm.getHeight();
        // create array which contains the end-x-position of each mark
        int[] markEndX = new int[extractedforms.getMarkCount()];
        // y-end-pos. of marks. these end at the bottom-line of the drawn text strings
        int markEndY = textStartY;
        // x-end for reentry-mark
        int reentryEndX = -1;
        //
        // draw description here
        //
        // check whether we have a description
        String desc = extractedforms.getDescription();
        if (desc!=null && !desc.isEmpty()) {
            // draw description-text
            g.drawString(desc+" = ", 0, textStartY);
            // retrieve width of textstring and calculate the new textstart-x-pos.
            // of the text behind the = sign (the distinctions)
            textStartX = markStartX = fm.stringWidth(desc+" = ");
        }
        //
        // draw distinctions here
        //
        // retrieve distinctions
        String[] dist = extractedforms.getDistinctions();
        // get unmarked space text
        String unmarkedSpace = extractedforms.getUnmarkedSpace();
        // check whether we have any distincions
        if (dist!=null && dist.length>0) {
            // draw all distinction strings
            for (int cnt=0; cnt<dist.length; cnt++) {
                // draw distinction text
                g.drawString(dist[cnt], textStartX, textStartY);
                // we have less marks than distinctions, so keep array index bounds in mind
                if (cnt<markEndX.length) {
                    // save mark's end-x-position, which is exact one mark-distance behind
                    // the end of the text-string
                    markEndX[cnt] = textStartX + markdist + fm.stringWidth(dist[cnt]);
                }
                // reentry-mark x-pos ends after last text-string
                reentryEndX = textStartX + markdist + fm.stringWidth(dist[cnt]);
                // determine new x-pos of textstart, which starts exact one mark-distance
                // behind the mark (which means: two mark-distances behind the end of the
                // previous text string)
                textStartX = textStartX + 2*markdist + fm.stringWidth(dist[cnt]);
            }
            // check for valid value
            if (unmarkedSpace!=null && !unmarkedSpace.isEmpty()) {
                // draw text
                g.drawString(unmarkedSpace, textStartX, textStartY);
            }
        }
        //
        // draw marks here
        //
        // go through all marks
        for (int cnt=0; cnt<markEndX.length; cnt++) {
            // draw mark, horizontal line
            g.drawLine(markStartX, markStartY, markEndX[cnt], markStartY);
            // draw mark, vertical line
            g.drawLine(markEndX[cnt], markStartY, markEndX[cnt], markEndY);
            // determine new y-start
            markStartY -= markdist;
        }
        //
        // draw reentry-mark here
        //
        // draw reentry-hook, if requested
        if (extractedforms.hasReentry()) {
            // draw reentry, horizontal line
            g.drawLine(markStartX, markStartY, reentryEndX, markStartY);
            // draw reentry, vertical line
            g.drawLine(reentryEndX, markStartY, reentryEndX, markEndY+(int)(2.5*markdist));
            // draw reentry, horizontal line
            g.drawLine(markStartX, markEndY+(int)(2.5*markdist), reentryEndX, markEndY+(int)(2.5*markdist));
            // draw reentry, vertical reentry
            g.drawLine(markStartX, markEndY+(int)(2.5*markdist), markStartX, markEndY+(int)(1.3*markdist));
        }
        //
        // crop image
        //
        // determine final width
        int width = reentryEndX+(size*IMG_PADDING);
        if (!extractedforms.hasReentry()) width -= markdist;
        if (unmarkedSpace!=null && !unmarkedSpace.isEmpty()) width += (fm.stringWidth(unmarkedSpace) + 2*markdist);
        // determine final height
        int height = markEndY+(size*IMG_PADDING);
        height += (extractedforms.hasReentry()) ? (int)(2.5*markdist) : size*IMG_PADDING;
        // crop image to necessary size
        formimage = formimage.getSubimage(0, 0, width, height);
    }
    private void saveImage(File fp) {
        // check whether image dir exists. if not, create image dir
        if (createFormImageDir()) {
            // when image dir exists or was successfully created, continue
            // and save file
            try {
                // write file image
                ImageIO.write(formimage, "png", fp);
                // saving the image succeeded
                saveImgOk = true;
            }   
            catch(IOException ex) {
                Constants.zknlogger.log(Level.SEVERE, ex.getLocalizedMessage());
            }
        }
    }
    private boolean createFormImageDir() {
        // first, check whether we already have a form image directory
        // create the file-object with the necessary directory path
        // if the directory does not exist, create it
        if (!formImageDir.exists()) {
            // create directory
            try {
                if (!formImageDir.mkdir()) {
                    // if it fails, show warning message and leave method
                    // create a message string including the filepath of the directory
                    // which could not be created
                    JOptionPane.showMessageDialog(null,resourceMap.getString("errMsgCreateImgDirMsg",formImageDir.toString()),resourceMap.getString("errMsgCreateDirTitle"),JOptionPane.PLAIN_MESSAGE);
                    return false;
                }
            }
            catch (SecurityException e) {
                Constants.zknlogger.log(Level.SEVERE,e.getLocalizedMessage());
                // if it fails, show warning message and leave method
                // create a message string including the filepath of the directory
                // which could not be created
                JOptionPane.showMessageDialog(null,resourceMap.getString("errMsgCreateImgDirMsg",formImageDir.toString()),resourceMap.getString("errMsgCreateDirTitle"),JOptionPane.PLAIN_MESSAGE);
                return false;
            }
        }
        return true;
    }
}
