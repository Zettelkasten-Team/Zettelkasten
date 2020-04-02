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
import de.danielluedecke.zettelkasten.util.*;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

/**
 *
 * @author danielludecke
 */
public class CErrorLog extends javax.swing.JDialog {

    /**
     * get the strings for file descriptions from the resource map
     */
    private org.jdesktop.application.ResourceMap resourceMap
            = org.jdesktop.application.Application.getInstance(ZettelkastenApp.class).
            getContext().getResourceMap(CErrorLog.class);
    /**
     * Reference to the main frame.
     */
    private ZettelkastenView mainframe;

    /**
     *
     * @param parent
     * @param zkn
     * @param settingsObj
     */
    public CErrorLog(java.awt.Frame parent, ZettelkastenView zkn, Settings settingsObj) {
        super(parent);
        mainframe = zkn;
        initComponents();
        // set application icon
        setIconImage(Constants.zknicon.getImage());
        initBorders(settingsObj);
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
        if (settingsObj.isSeaGlass()) {
            jButton1.putClientProperty("JComponent.sizeVariant", "small");
            jButton2.putClientProperty("JComponent.sizeVariant", "small");
        }
        // add change listener to tabbed pane
        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                if (1 == jTabbedPane1.getSelectedIndex()) {
                    // set internal session-log-text to 2. textarea
                    Tools.flushSessionLog();
                    jTextArea2.setText("------------------------------"
                            + System.lineSeparator()
                            + "Zettelkasten-Version: " + Version.get().getVersionString()
                            + System.lineSeparator()
                            + System.lineSeparator()
                            + Tools.getSystemInformation()
                            + System.lineSeparator()
                            + "------------------------------"
                            + System.lineSeparator()
                            + System.lineSeparator()
                            + mainframe.baos_log.toString());
                    jTextArea2.setCaretPosition(0);
                }
            }
        });
        InputStream is = null;
        // now we load the error log and display it to the user. but first, we put some useful
        // information in front of the log
        StringBuilder sb = new StringBuilder("");
        try {
            // some text for the user
            sb.append(resourceMap.getString("errorMsg")).append(System.lineSeparator()).append(System.lineSeparator());
            // a separator line for a better overview
            sb.append("------------------------------").append(System.lineSeparator());
            // first, show programme-version
            sb.append("Zettelkasten-Version: " + Version.get().getVersionString()).append(System.lineSeparator()).append(System.lineSeparator());
            // now show system-information (jre, os etc.)
            sb.append("System-Information:").append(System.lineSeparator()).append(Tools.getSystemInformation()).append(System.lineSeparator());
            // a separator line for a better overview
            sb.append("------------------------------").append(System.lineSeparator()).append(System.lineSeparator());
            int c = 0;
            // header for log 1
            sb.append("Log 1").append(System.lineSeparator()).append("-----").append(System.lineSeparator());
            // now, load the log-file and append it to the final error message as well
            is = new FileInputStream(new File(FileOperationsUtil.getZettelkastenHomeDir() + "zknerror0.log"));
            while (c != -1) {
                c = is.read();
                if (c != -1) {
                    sb.append((char) c);
                }
            }
        } catch (IOException e) {
            Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
            }
        }
        try {
            int c = 0;
            // header for log 2
            sb.append(System.lineSeparator()).append(System.lineSeparator()).append("Log 2").append(System.lineSeparator()).append("-----").append(System.lineSeparator());
            // now, load the log-file and append it to the final error message as well
            is = new FileInputStream(new File(FileOperationsUtil.getZettelkastenHomeDir() + "zknerror1.log"));
            while (c != -1) {
                c = is.read();
                if (c != -1) {
                    sb.append((char) c);
                }
            }
        } catch (IOException e) {
            Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
            }
        }
        try {
            int c = 0;
            // header for log 3
            sb.append(System.lineSeparator()).append(System.lineSeparator()).append("Log 3").append(System.lineSeparator()).append("-----").append(System.lineSeparator());
            // now, load the log-file and append it to the final error message as well
            is = new FileInputStream(new File(FileOperationsUtil.getZettelkastenHomeDir() + "zknerror2.log"));
            while (c != -1) {
                c = is.read();
                if (c != -1) {
                    sb.append((char) c);
                }
            }
        } catch (IOException e) {
            Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
            }
        }
        // finally, set the text to the text area
        jTextArea1.setText(sb.toString());
        jTextArea1.setCaretPosition(0);
        // focus on button
        jButton1.requestFocusInWindow();
    }

    private void initBorders(Settings settingsObj) {
        /*
         * Constructor for Matte Border
         * public MatteBorder(int top, int left, int bottom, int right, Color matteColor)
         */
        jScrollPane1.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ColorUtil.getBorderGray(settingsObj)));
        jScrollPane2.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ColorUtil.getBorderGray(settingsObj)));
    }

    private void cancel() {
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

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ZettelkastenApp.class).getContext().getResourceMap(CErrorLog.class);
        setTitle(resourceMap.getString("FormErrorLog.title")); // NOI18N
        setModal(true);
        setName("FormErrorLog"); // NOI18N

        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jTabbedPane1.setMinimumSize(new java.awt.Dimension(60, 60));
        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jPanel1.setMinimumSize(new java.awt.Dimension(25, 25));
        jPanel1.setName("jPanel1"); // NOI18N

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setMinimumSize(new java.awt.Dimension(25, 25));
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTextArea1.setLineWrap(true);
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setMinimumSize(new java.awt.Dimension(25, 25));
        jTextArea1.setName("jTextArea1"); // NOI18N
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        jPanel2.setMinimumSize(new java.awt.Dimension(25, 25));
        jPanel2.setName("jPanel2"); // NOI18N

        jScrollPane2.setBorder(null);
        jScrollPane2.setMinimumSize(new java.awt.Dimension(25, 25));
        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jTextArea2.setLineWrap(true);
        jTextArea2.setWrapStyleWord(true);
        jTextArea2.setMinimumSize(new java.awt.Dimension(25, 25));
        jTextArea2.setName("jTextArea2"); // NOI18N
        jScrollPane2.setViewportView(jTextArea2);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addGap(3, 3, 3))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // tell user what to do
        JOptionPane.showMessageDialog(null,resourceMap.getString("infoMsg"),
                                      resourceMap.getString("infoTitle"),
                                      JOptionPane.PLAIN_MESSAGE);
        // select which text to copy to clipboard
        switch(jTabbedPane1.getSelectedIndex()) {
            case -1:
            case 0:  jTextArea1.selectAll();
                     jTextArea1.copy();
                     break;
            case 1:  jTextArea2.selectAll();
                     jTextArea2.copy();
                     break;
        }
        
        try {
            Desktop.getDesktop().mail(new URI("mailto:mail@danielluedecke.de?SUBJECT=Logfile%20ZKN3"));
        } catch (URISyntaxException | IOException ex) {
            Constants.zknlogger.log(Level.SEVERE,ex.getLocalizedMessage());
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        cancel();
    }//GEN-LAST:event_jButton2ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    // End of variables declaration//GEN-END:variables

}
