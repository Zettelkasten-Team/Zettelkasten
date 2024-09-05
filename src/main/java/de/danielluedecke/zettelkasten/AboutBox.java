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
import de.danielluedecke.zettelkasten.util.Version;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.event.HyperlinkEvent;

/**
 * Represents an "About" dialog displaying application info and license.
 */
public class AboutBox extends javax.swing.JDialog {

    /**
     * Constructor for AboutBox dialog
     * @param parent The parent frame
     * @param isMacStyle Whether the dialog should use Mac styling
     */
    public AboutBox(java.awt.Frame parent, boolean isMacStyle) {
        super(parent);
        initComponents();
        setIconImage(Constants.zknicon.getImage());
        setTitle("Zettelkasten " + Version.get().getVersionString());
        setUpEscapeKeyListener();
        loadLicenseContent();
    }

    /**
     * Sets up an Escape key listener to close the dialog when pressed.
     */
    private void setUpEscapeKeyListener() {
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        ActionListener cancelAction = (ActionEvent evt) -> dispose();
        getRootPane().registerKeyboardAction(cancelAction, escapeKeyStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    /**
     * Loads the license content into the editor pane.
     */
    private void loadLicenseContent() {
        try {
            // Load the license text HTML
            URL licenseText = getClass().getResource("/de/danielluedecke/zettelkasten/resources/licence.html");
            jEditorPane1.setPage(licenseText);
        } catch (IOException e) {
            handleLoadingError(e);
        }
    }

    /**
     * Handles hyperlink events in the license content.
     * @param evt The hyperlink event
     */
    private void jEditorPane1HyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            try {
                String link = evt.getURL().toString();
                if (link.startsWith("http://") || link.startsWith("https://")) {
                    Desktop.getDesktop().browse(new URI(link));
                } else if (link.startsWith("mailto:")) {
                    Desktop.getDesktop().mail(new URI(link));
                } else if (link.startsWith("#")) {
                    jEditorPane1.scrollToReference(link.substring(1));
                }
            } catch (IOException | URISyntaxException e) {
                Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
            }
        }
    }

    /**
     * Handles errors that occur while loading the license content.
     * @param e the exception thrown
     */
    private void handleLoadingError(Exception e) {
        Constants.zknlogger.log(Level.SEVERE, "Failed to load license content: " + e.getMessage());
        jEditorPane1.setText("<html><body><p>Error loading license information.</p></body></html>");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getResourceMap(AboutBox.class);
        setTitle(resourceMap.getString("FormAboutBox.title")); // NOI18N
        setModal(true);
        setName("FormAboutBox"); // NOI18N

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jEditorPane1.setBorder(null);
        jEditorPane1.setContentType(resourceMap.getString("jEditorPane1.contentType")); // NOI18N
        jEditorPane1.setEditable(false);
        jEditorPane1.setName("jEditorPane1"); // NOI18N
        jEditorPane1.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
            public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
                jEditorPane1HyperlinkUpdate(evt);
            }
        });
        jScrollPane1.setViewportView(jEditorPane1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

}
