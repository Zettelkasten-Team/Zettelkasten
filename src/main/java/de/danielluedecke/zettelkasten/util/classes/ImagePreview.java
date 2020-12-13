/*
 * This example was copied from the SUN website:
 * http://java.sun.com/docs/books/tutorial/uiswing/components/filechooser.html
 * 
 * This class extends the filechooser, so that images can be previewed
 * @author sun
 * 
 * 
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

package de.danielluedecke.zettelkasten.util.classes;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;

/**
 * This example was copied from the SUN website:
 * http://java.sun.com/docs/books/tutorial/uiswing/components/filechooser.html
 * 
 * This class extends the filechooser, so that images can be previewed
 * @author sun
 */
@SuppressWarnings("LeakingThisInConstructor")
public class ImagePreview extends JComponent implements PropertyChangeListener {
    ImageIcon thumbnail = null;
    File file = null;

    public ImagePreview(JFileChooser fc) {
        setPreferredSize(new Dimension(200, 200));
        fc.addPropertyChangeListener(this);
    }

    public void loadImage() {
        if (file == null) {
            thumbnail = null;
            return;
        }

        //Don't use createImageIcon (which is a wrapper for getResource)
        //because the image we're trying to load is probably not one
        //of this program's own resources.
        ImageIcon tmpIcon = new ImageIcon(file.getPath());
        if (tmpIcon.getIconWidth() > 190) {
            thumbnail = new ImageIcon(tmpIcon.getImage().getScaledInstance(190, -1, Image.SCALE_DEFAULT));
        } else { //no need to miniaturize
            thumbnail = tmpIcon;
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        boolean update = false;
        String prop = e.getPropertyName();

        if (null != prop) { //If the directory changed, don't show an image.
            switch (prop) {
                case JFileChooser.DIRECTORY_CHANGED_PROPERTY:
                    file = null;
                    update = true;

                    //If a file became selected, find out which one.
                    break;
                case JFileChooser.SELECTED_FILE_CHANGED_PROPERTY:
                    file = (File) e.getNewValue();
                    update = true;
                    break;
            }
        }
        //Update the preview accordingly.
        if (update) {
            thumbnail = null;
            if (isShowing()) {
                loadImage();
                repaint();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (thumbnail == null) {
            loadImage();
        }
        if (thumbnail != null) {
            int x = getWidth()/2 - thumbnail.getIconWidth()/2;
            int y = getHeight()/2 - thumbnail.getIconHeight()/2;

            if (y < 0) {
                y = 0;
            }

            if (x < 5) {
                x = 5;
            }
            thumbnail.paintIcon(this, g, x, y);
        }
    }
}
