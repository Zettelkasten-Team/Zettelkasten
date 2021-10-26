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


package de.danielluedecke.zettelkasten.util.classes;

import de.danielluedecke.zettelkasten.ZettelkastenView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.Timer;
import org.jdesktop.application.TaskMonitor;

/**
 *
 * @author danielludecke
 */
public class InitStatusbarForTasks {

    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[30];
    private int busyIconIndex = 0;

    private final org.jdesktop.application.ResourceMap resourceMap =
        org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
        getContext().getResourceMap(ZettelkastenView.class);
    
    /**
     * Initiates the status bar for background tasks.
     * Catches messages from the doInBackground task
     * and changes the progressbar state, the busy icon animation
     * and - if necessary - the status message.
     * @param statusAnimationLabel
     * @param progressBar
     * @param statusMessageLabel
     */
    public InitStatusbarForTasks( final javax.swing.JLabel statusAnimationLabel,
                    final javax.swing.JProgressBar progressBar,
                    final javax.swing.JLabel statusMessageLabel) {
        /**
         * This is pre-defined code taken from the NetBeans IDE
         * Initiates some basic things for background tasks, like
         * associating a statusbar and busy-icon to a background thread
         */

        // initiate animated busy-icons, which are animated when the thread is running
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        // and create a busy-icon-timer
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                if (statusAnimationLabel!=null) {
                    statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
                }
            }
        });
        // initiate the idle icon and make it visible
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        if (statusAnimationLabel!=null) {
            statusAnimationLabel.setIcon(idleIcon);
        }
        // hide progressbar, if there is one
        /*if (progressBar!=null) progressBar.setVisible(false);*/

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext());

        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if (null != propertyName) // when a background thread starts, start the busy icon animation
                switch (propertyName) {
                    case "started":
                        if (statusAnimationLabel!=null && !busyIconTimer.isRunning()) {
                            statusAnimationLabel.setIcon(busyIcons[0]);
                            busyIconIndex = 0;
                            busyIconTimer.start();
                        }   // and make the progressbar visible, if we have one
                        if (progressBar!=null) {
                            progressBar.setVisible(true);
                            progressBar.setIndeterminate(true);
                        }   break;
                    case "done":
                        busyIconTimer.stop();
                        if (statusAnimationLabel!=null) {
                            statusAnimationLabel.setIcon(idleIcon);
                        }   if (progressBar!=null) {
                            /*progressBar.setVisible(false);*/
                            progressBar.setValue(0);
                    }   break;
                    case "message":
                        if (statusMessageLabel!=null) {
                            String text = (String) (evt.getNewValue());
                            statusMessageLabel.setText((text == null) ? "" : text);
                    }   break;
                    case "progress":
                        if (progressBar!=null) {
                            int value = (Integer)(evt.getNewValue());
                            progressBar.setVisible(true);
                            progressBar.setIndeterminate(false);
                            progressBar.setValue(value);
                    }   break;
                }
            }
        });                
    }
}
