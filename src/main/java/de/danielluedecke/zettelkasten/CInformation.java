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
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.mac.ZknMacWidgetFactory;
import de.danielluedecke.zettelkasten.util.ColorUtil;
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.util.classes.TasksStatusBar;
import de.danielluedecke.zettelkasten.util.Tools;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;
import org.jdesktop.application.TaskService;


/**
 *
 * @author danielludecke
 */
public class CInformation extends javax.swing.JDialog {

    private Daten dataObj;
    private Settings settingsObj;

    private org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
                                                       getContext().getResourceMap(CInformation.class);

    /**
     * 
     * @param parent
     * @param d
     * @param s 
     */
    public CInformation(java.awt.Frame parent, Daten d, Settings s) {
        super(parent);
        dataObj = d;
        settingsObj = s;
        initComponents();
        // set application icon
        setIconImage(Constants.zknicon.getImage());
        /*
         * Constructor for Matte Border
         * public MatteBorder(int top, int left, int bottom, int right, Color matteColor)
         */
        jScrollPane1.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, ColorUtil.getBorderGray(settingsObj)));
        jScrollPane1.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, ColorUtil.getBorderGray(settingsObj)));
        new TasksStatusBar(statusLabel, null, statusMessageLabel);
        // these codelines add an escape-listener to the dialog. so, when the user
        // presses the escape-key, the same action is performed as if the user
        // presses the cancel button...
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        ActionListener cancelAction = new java.awt.event.ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                closeWindow();
            }
        };
        getRootPane().registerKeyboardAction(cancelAction, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        // set default button
        getRootPane().setDefaultButton(jButtonClose);
        if (settingsObj.isSeaGlass()) {
            jButtonApply.putClientProperty("JComponent.sizeVariant", "small");
            jButtonClose.putClientProperty("JComponent.sizeVariant", "small");
            jButtonBrowseAttachmentPath.putClientProperty("JComponent.sizeVariant", "small");
            jButtonBrowseImagePath.putClientProperty("JComponent.sizeVariant", "small");
            // set new border text
            jTextAreaDescription.setBorder(ZknMacWidgetFactory.getTitledBorder(resourceMap.getString("jTextAreaDescription.border.title"), null, settingsObj));
            jTextAreaSysInfo.setBorder(ZknMacWidgetFactory.getTitledBorder(resourceMap.getString("jTextAreaSysInfo.border.title"), null, settingsObj));
        }
        if (settingsObj.isMacStyle()) {
            jTextAreaDescription.setBorder(ZknMacWidgetFactory.getTitledBorder(resourceMap.getString("jTextAreaDescription.border.title"), ColorUtil.colorJTreeText, settingsObj));
            jTextAreaSysInfo.setBorder(ZknMacWidgetFactory.getTitledBorder(resourceMap.getString("jTextAreaSysInfo.border.title"), ColorUtil.colorJTreeText, settingsObj));
        }
        // retrieve filepath
        String filepath = settingsObj.getMainDataFile().toString();
        try {
            // and path to file as well as file name
            String path = filepath.substring(0, filepath.lastIndexOf(File.separatorChar));
            String name = filepath.substring(filepath.lastIndexOf(File.separatorChar)+1);
            // set file name and path into textfields
            jTextFieldPath.setText(path);
            jLabelName.setText(name);
        }
        catch (IndexOutOfBoundsException ex) {
        }
        // set version info
        String verinfo = dataObj.getVersionInfo();
        if (verinfo!=null) { 
            jLabelVersion.setText(verinfo);
        }
        // and description of the data file
        jTextAreaDescription.setText(dataObj.getZknDescription());
        // get user attachment and image paths
        File attpath = dataObj.getUserAttachmentPath();
        if (attpath!=null) {
            jTextFieldAttachmentPath.setText(attpath.toString());
        }
        File imgpath = dataObj.getUserImagePath();
        if (imgpath!=null) {
            jTextFieldImagePath.setText(imgpath.toString());
        }
        jButtonBrowseAttachmentPath.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                JFileChooser fc = new JFileChooser();
                // set dialog's title
                fc.setDialogTitle(resourceMap.getString("fileChooserTitle"));
                // restrict all files as choosable
                fc.setAcceptAllFileFilterUsed(false);
                // only directories should be selected
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int option = fc.showOpenDialog(null);
                // if a file was chosen, set the filepath
                if (JFileChooser.APPROVE_OPTION == option) {
                    // get the filepath...
                    jTextFieldAttachmentPath.setText(fc.getSelectedFile().toString());
                    dataObj.setUserAttachmentPath(fc.getSelectedFile().toString());
                }
            }
        });
        jButtonBrowseImagePath.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                JFileChooser fc = new JFileChooser();
                // set dialog's title
                fc.setDialogTitle(resourceMap.getString("fileChooserTitle"));
                // restrict all files as choosable
                fc.setAcceptAllFileFilterUsed(false);
                // only directories should be selected
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int option = fc.showOpenDialog(null);
                // if a file was chosen, set the filepath
                if (JFileChooser.APPROVE_OPTION == option) {
                    // get the filepath...
                    jTextFieldImagePath.setText(fc.getSelectedFile().toString());
                    dataObj.setUserImagePath(fc.getSelectedFile().toString());
                }
            }
        });
        jTextFieldAttachmentPath.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void changedUpdate(DocumentEvent e) { checkPath(jTextFieldAttachmentPath); }
            @Override public void insertUpdate(DocumentEvent e) { checkPath(jTextFieldAttachmentPath); }
            @Override public void removeUpdate(DocumentEvent e) { checkPath(jTextFieldAttachmentPath); }
        });
        jTextFieldImagePath.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void changedUpdate(DocumentEvent e) { checkPath(jTextFieldImagePath); }
            @Override public void insertUpdate(DocumentEvent e) { checkPath(jTextFieldImagePath); }
            @Override public void removeUpdate(DocumentEvent e) { checkPath(jTextFieldImagePath); }
        });
        // calculate file-size of data file
        File f = settingsObj.getMainDataFile();
        long length = f.length();
        double lkb = (double)length / 1024.0;
        double lmb = lkb / 1024.0;
        // format file-size into MiB and KiB
        DecimalFormat df = new DecimalFormat("#0.00");
        String size = df.format(lmb)+" Megabyte ("+df.format(lkb)+" Kilobyte)";
        // set filesize
        jLabelSize.setText(size);
        // retrieve system information like used JRE and OS
        jTextAreaSysInfo.setText(Tools.getSystemInformation());
        jButtonApply.setEnabled(false);
        jTextAreaDescription.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void changedUpdate(DocumentEvent e) { jButtonApply.setEnabled(true); }
            @Override public void insertUpdate(DocumentEvent e) { jButtonApply.setEnabled(true); }
            @Override public void removeUpdate(DocumentEvent e) { jButtonApply.setEnabled(true); }
        });

        // start the background task manually
        Task<?, ?> countWordT = countWords();
        // get the application's context...
        ApplicationContext appC = Application.getInstance().getContext();
        // ...to get the TaskMonitor and TaskService
        TaskMonitor tM = appC.getTaskMonitor();
        TaskService tS = appC.getTaskService();
        // with these we can execute the task and bring it to the foreground
        // i.e. making the animated progressbar and busy icon visible
        tS.execute(countWordT);
        tM.setForegroundTask(countWordT);
    }


    private void checkPath(javax.swing.JTextField tf) {
        // retrieve file path from textfield
        String fps = tf.getText();
        // check whether path exists
        if (!fps.isEmpty()) {
            // create file-variable
            File fp = new File(fps);
            // check for existence
            tf.setForeground((fp.exists())?Color.black:Color.red);
        }
        else {
            // indicate that path is OK
            tf.setForeground(Color.black);
        }
        // enable apply button
        jButtonApply.setEnabled(true);
    }


    @Action
    public void openFileDir() {
        try {
            Desktop.getDesktop().open(new File(jTextFieldPath.getText()));
        } catch (IOException ex) {
        }
    }

    @Action
    public void closeWindow() {
        setVisible(false);
        dispose();
    }

    @Action
    public void applyDescription() {
        dataObj.setUserAttachmentPath(jTextFieldAttachmentPath.getText());
        dataObj.setUserImagePath(jTextFieldImagePath.getText());
        dataObj.setZknDescription(jTextAreaDescription.getText());
        closeWindow();
    }


    @Action
    public final Task<?, ?> countWords() {
        return new countWordsTask(org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class));
    }
    private class countWordsTask extends org.jdesktop.application.Task<Object, Void> {
        // init variabls
        int totalWords;
        int totalEntries;

        countWordsTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to createLinksTask fields, here.
            super(app);
        }

        @Override protected Object doInBackground() {
            // init variabls
            totalWords = 0;
            totalEntries = 0;
            // go through all entries and calculate wordcount...
            for (int cnt=1; cnt<=dataObj.getCount(Daten.ZKNCOUNT); cnt++) {
                // check whether entry is not deleted
                if (!dataObj.isDeleted(cnt)) {
                    // increase entry counter
                    totalEntries++;
                    // get complete entry-content, i.e. title and content
                    String wordcoutnstring = dataObj.getZettelTitle(cnt)+" "+dataObj.getCleanZettelContent(cnt);
                    // split complete content at each word
                    String[] words = wordcoutnstring.toLowerCase().
                                                    replace("ä","ae").
                                                    replace("ö","oe").
                                                    replace("ü","ue").
                                                    replace("ß","ss").
                                                    split("\\W");
                    // init wordcounter
                    int wordcount=0;
                    // iterate all words of the entry
                    for (String word : words) {
                        // remove all non-letter-chars and trim spaces
                        word = word.replace("([^A-Za-z0-9]+)", "").trim();
                        // if we have a "word" with more than one char, count it as word...
                        if (!word.isEmpty() /* && word.length()>1 */) {
                            wordcount++;
                        }
                    }
                    // calculate total word-count
                    totalWords = totalWords+wordcount;
                }
                // set status message
                setMessage(resourceMap.getString("wordCountText",String.valueOf(totalWords),String.valueOf(totalEntries)));
            }

            return null;
        }

        @Override protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
        }

        @Override
        protected void finished() {
            // calculate average words per entry
            double wpe = (double)totalWords / totalEntries;
            DecimalFormat df = new DecimalFormat("#0.00");
            String wpeString = df.format(wpe);
            // set results
            statusMessageLabel.setText(resourceMap.getString("wordCountTextFinal",String.valueOf(totalWords),String.valueOf(totalEntries),wpeString));
        }
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    // Generated using JFormDesigner Evaluation license - Ralf Barkow
    private void initComponents() {
        ResourceBundle bundle = ResourceBundle.getBundle("de.danielluedecke.zettelkasten.resources.CInformation");
        jLabel1 = new JLabel();
        jLabelName = new JLabel();
        jLabel2 = new JLabel();
        jTextFieldPath = new JTextField();
        jButtonOpenDir = new JButton();
        jLabel3 = new JLabel();
        jLabelVersion = new JLabel();
        jLabel5 = new JLabel();
        jLabelSize = new JLabel();
        jScrollPane1 = new JScrollPane();
        jTextAreaDescription = new JTextArea();
        jButtonClose = new JButton();
        jButtonApply = new JButton();
        jScrollPane2 = new JScrollPane();
        jTextAreaSysInfo = new JTextArea();
        jLabel4 = new JLabel();
        statusMessageLabel = new JLabel();
        statusLabel = new JLabel();
        jLabel6 = new JLabel();
        jTextFieldAttachmentPath = new JTextField();
        jButtonBrowseAttachmentPath = new JButton();
        jLabel7 = new JLabel();
        jTextFieldImagePath = new JTextField();
        jButtonBrowseImagePath = new JButton();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(bundle.getString("FormCInformation.title"));
        setModal(true);
        setName("FormCInformation");
        Container contentPane = getContentPane();

        //---- jLabel1 ----
        jLabel1.setText(bundle.getString("jLabel1.text"));
        jLabel1.setName("jLabel1");

        //---- jLabelName ----
        jLabelName.setText(bundle.getString("jLabelName.text"));
        jLabelName.setName("jLabelName");

        //---- jLabel2 ----
        jLabel2.setText(bundle.getString("jLabel2.text"));
        jLabel2.setName("jLabel2");

        //---- jTextFieldPath ----
        jTextFieldPath.setName("jTextFieldPath");

        //---- jButtonOpenDir ----
        jButtonOpenDir.setBorderPainted(false);
        jButtonOpenDir.setContentAreaFilled(false);
        jButtonOpenDir.setFocusPainted(false);
        jButtonOpenDir.setName("jButtonOpenDir");

        //---- jLabel3 ----
        jLabel3.setText(bundle.getString("jLabel3.text"));
        jLabel3.setName("jLabel3");

        //---- jLabelVersion ----
        jLabelVersion.setText(bundle.getString("jLabelVersion.text"));
        jLabelVersion.setName("jLabelVersion");

        //---- jLabel5 ----
        jLabel5.setText(bundle.getString("jLabel5.text"));
        jLabel5.setName("jLabel5");

        //---- jLabelSize ----
        jLabelSize.setText(bundle.getString("jLabelSize.text"));
        jLabelSize.setName("jLabelSize");

        //======== jScrollPane1 ========
        {
            jScrollPane1.setName("jScrollPane1");

            //---- jTextAreaDescription ----
            jTextAreaDescription.setLineWrap(true);
            jTextAreaDescription.setWrapStyleWord(true);
            jTextAreaDescription.setBorder(new TitledBorder("Beschreibungstext"));
            jTextAreaDescription.setName("jTextAreaDescription");
            jScrollPane1.setViewportView(jTextAreaDescription);
        }

        //---- jButtonClose ----
        jButtonClose.setName("jButtonClose");

        //---- jButtonApply ----
        jButtonApply.setName("jButtonApply");

        //======== jScrollPane2 ========
        {
            jScrollPane2.setName("jScrollPane2");

            //---- jTextAreaSysInfo ----
            jTextAreaSysInfo.setLineWrap(true);
            jTextAreaSysInfo.setRows(3);
            jTextAreaSysInfo.setWrapStyleWord(true);
            jTextAreaSysInfo.setBorder(new TitledBorder("Systeminformationen"));
            jTextAreaSysInfo.setName("jTextAreaSysInfo");
            jScrollPane2.setViewportView(jTextAreaSysInfo);
        }

        //---- jLabel4 ----
        jLabel4.setText(bundle.getString("jLabel4.text"));
        jLabel4.setName("jLabel4");

        //---- statusMessageLabel ----
        statusMessageLabel.setText(bundle.getString("statusMessageLabel.text"));
        statusMessageLabel.setName("statusMessageLabel");

        //---- statusLabel ----
        statusLabel.setName("statusLabel");

        //---- jLabel6 ----
        jLabel6.setText(bundle.getString("jLabel6.text"));
        jLabel6.setName("jLabel6");

        //---- jTextFieldAttachmentPath ----
        jTextFieldAttachmentPath.setName("jTextFieldAttachmentPath");

        //---- jButtonBrowseAttachmentPath ----
        jButtonBrowseAttachmentPath.setText(bundle.getString("jButtonBrowseAttachmentPath.text"));
        jButtonBrowseAttachmentPath.setName("jButtonBrowseAttachmentPath");

        //---- jLabel7 ----
        jLabel7.setText(bundle.getString("jLabel7.text"));
        jLabel7.setName("jLabel7");

        //---- jTextFieldImagePath ----
        jTextFieldImagePath.setName("jTextFieldImagePath");

        //---- jButtonBrowseImagePath ----
        jButtonBrowseImagePath.setText(bundle.getString("jButtonBrowseImagePath.text"));
        jButtonBrowseImagePath.setName("jButtonBrowseImagePath");

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
            contentPaneLayout.createParallelGroup()
                .addComponent(jScrollPane1)
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addGroup(GroupLayout.Alignment.LEADING, contentPaneLayout.createSequentialGroup()
                            .addGroup(contentPaneLayout.createParallelGroup()
                                .addComponent(jLabel3)
                                .addComponent(jLabel5)
                                .addComponent(jLabel1))
                            .addGap(31, 31, 31)
                            .addGroup(contentPaneLayout.createParallelGroup()
                                .addComponent(jLabelName)
                                .addComponent(jLabelSize)
                                .addComponent(jLabelVersion)))
                        .addGroup(GroupLayout.Alignment.LEADING, contentPaneLayout.createSequentialGroup()
                            .addComponent(jLabel4)
                            .addGap(37, 37, 37)
                            .addComponent(statusMessageLabel)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(statusLabel))
                        .addGroup(contentPaneLayout.createSequentialGroup()
                            .addComponent(jButtonClose)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButtonApply))
                        .addGroup(GroupLayout.Alignment.LEADING, contentPaneLayout.createSequentialGroup()
                            .addComponent(jLabel2)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jTextFieldPath)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButtonOpenDir, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE))
                        .addGroup(GroupLayout.Alignment.LEADING, contentPaneLayout.createSequentialGroup()
                            .addComponent(jLabel6)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jTextFieldAttachmentPath)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButtonBrowseAttachmentPath))
                        .addGroup(GroupLayout.Alignment.LEADING, contentPaneLayout.createSequentialGroup()
                            .addComponent(jLabel7)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jTextFieldImagePath)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButtonBrowseImagePath)))
                    .addContainerGap())
                .addComponent(jScrollPane2, GroupLayout.Alignment.TRAILING)
        );
        contentPaneLayout.setVerticalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addGroup(contentPaneLayout.createSequentialGroup()
                            .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel1)
                                .addComponent(jLabelName))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel5)
                                .addComponent(jLabelSize))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel3)
                                .addComponent(jLabelVersion))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                .addComponent(jLabel4)
                                .addComponent(statusMessageLabel)
                                .addComponent(statusLabel))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel2)
                                .addComponent(jTextFieldPath, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                        .addComponent(jButtonOpenDir))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel6)
                        .addComponent(jTextFieldAttachmentPath, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonBrowseAttachmentPath))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel7)
                        .addComponent(jTextFieldImagePath, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonBrowseImagePath))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jScrollPane2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonClose)
                        .addComponent(jButtonApply))
                    .addGap(3, 3, 3))
        );
        pack();
        setLocationRelativeTo(getOwner());
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Ralf Barkow
    private JLabel jLabel1;
    private JLabel jLabelName;
    private JLabel jLabel2;
    private JTextField jTextFieldPath;
    private JButton jButtonOpenDir;
    private JLabel jLabel3;
    private JLabel jLabelVersion;
    private JLabel jLabel5;
    private JLabel jLabelSize;
    private JScrollPane jScrollPane1;
    private JTextArea jTextAreaDescription;
    private JButton jButtonClose;
    private JButton jButtonApply;
    private JScrollPane jScrollPane2;
    private JTextArea jTextAreaSysInfo;
    private JLabel jLabel4;
    private JLabel statusMessageLabel;
    private JLabel statusLabel;
    private JLabel jLabel6;
    private JTextField jTextFieldAttachmentPath;
    private JButton jButtonBrowseAttachmentPath;
    private JLabel jLabel7;
    private JTextField jTextFieldImagePath;
    private JButton jButtonBrowseImagePath;
    // End of variables declaration//GEN-END:variables

}
