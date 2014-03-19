/*
 * Zettelkasten - nach Luhmann
 ** Copyright (C) 2001-2013 by Daniel Lüdecke (http://www.danielluedecke.de)
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

import com.explodingpixels.macwidgets.WidgetFactory;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.util.ColorUtil;
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.util.classes.InitStatusbarForTasks;
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

    /** Creates new form CInformation */
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
        // init the progressbar and animated icon for background tasks
        InitStatusbarForTasks isb = new InitStatusbarForTasks(statusLabel, null, statusMessageLabel);
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
            jTextAreaDescription.setBorder(WidgetFactory.getTitledBorder(resourceMap.getString("jTextAreaDescription.border.title"), null, settingsObj));
            jTextAreaSysInfo.setBorder(WidgetFactory.getTitledBorder(resourceMap.getString("jTextAreaSysInfo.border.title"), null, settingsObj));
        }
        if (settingsObj.isMacAqua()) {
            jTextAreaDescription.setBorder(WidgetFactory.getTitledBorder(resourceMap.getString("jTextAreaDescription.border.title"), ColorUtil.colorJTreeText, settingsObj));
            jTextAreaSysInfo.setBorder(WidgetFactory.getTitledBorder(resourceMap.getString("jTextAreaSysInfo.border.title"), ColorUtil.colorJTreeText, settingsObj));
        }
        // retrieve filepath
        String filepath = settingsObj.getFilePath().toString();
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
        File f = settingsObj.getFilePath();
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
        Task countWordT = countWords();
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
    public final Task countWords() {
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
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabelName = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldPath = new javax.swing.JTextField();
        jButtonOpenDir = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabelVersion = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabelSize = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextAreaDescription = new javax.swing.JTextArea();
        jButtonClose = new javax.swing.JButton();
        jButtonApply = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextAreaSysInfo = new javax.swing.JTextArea();
        jLabel4 = new javax.swing.JLabel();
        statusMessageLabel = new javax.swing.JLabel();
        statusLabel = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jTextFieldAttachmentPath = new javax.swing.JTextField();
        jButtonBrowseAttachmentPath = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jTextFieldImagePath = new javax.swing.JTextField();
        jButtonBrowseImagePath = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getResourceMap(CInformation.class);
        setTitle(resourceMap.getString("FormCInformation.title")); // NOI18N
        setModal(true);
        setName("FormCInformation"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabelName.setText(resourceMap.getString("jLabelName.text")); // NOI18N
        jLabelName.setName("jLabelName"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jTextFieldPath.setName("jTextFieldPath"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getActionMap(CInformation.class, this);
        jButtonOpenDir.setAction(actionMap.get("openFileDir")); // NOI18N
        jButtonOpenDir.setBorderPainted(false);
        jButtonOpenDir.setContentAreaFilled(false);
        jButtonOpenDir.setFocusPainted(false);
        jButtonOpenDir.setName("jButtonOpenDir"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jLabelVersion.setText(resourceMap.getString("jLabelVersion.text")); // NOI18N
        jLabelVersion.setName("jLabelVersion"); // NOI18N

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jLabelSize.setText(resourceMap.getString("jLabelSize.text")); // NOI18N
        jLabelSize.setName("jLabelSize"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTextAreaDescription.setLineWrap(true);
        jTextAreaDescription.setWrapStyleWord(true);
        jTextAreaDescription.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jTextAreaDescription.border.title"))); // NOI18N
        jTextAreaDescription.setName("jTextAreaDescription"); // NOI18N
        jScrollPane1.setViewportView(jTextAreaDescription);

        jButtonClose.setAction(actionMap.get("closeWindow")); // NOI18N
        jButtonClose.setName("jButtonClose"); // NOI18N

        jButtonApply.setAction(actionMap.get("applyDescription")); // NOI18N
        jButtonApply.setName("jButtonApply"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jTextAreaSysInfo.setLineWrap(true);
        jTextAreaSysInfo.setRows(3);
        jTextAreaSysInfo.setWrapStyleWord(true);
        jTextAreaSysInfo.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jTextAreaSysInfo.border.title"))); // NOI18N
        jTextAreaSysInfo.setName("jTextAreaSysInfo"); // NOI18N
        jScrollPane2.setViewportView(jTextAreaSysInfo);

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        statusMessageLabel.setText(resourceMap.getString("statusMessageLabel.text")); // NOI18N
        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusLabel.setIcon(resourceMap.getIcon("statusLabel.icon")); // NOI18N
        statusLabel.setName("statusLabel"); // NOI18N

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        jTextFieldAttachmentPath.setName("jTextFieldAttachmentPath"); // NOI18N

        jButtonBrowseAttachmentPath.setText(resourceMap.getString("jButtonBrowseAttachmentPath.text")); // NOI18N
        jButtonBrowseAttachmentPath.setName("jButtonBrowseAttachmentPath"); // NOI18N

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        jTextFieldImagePath.setName("jTextFieldImagePath"); // NOI18N

        jButtonBrowseImagePath.setText(resourceMap.getString("jButtonBrowseImagePath.text")); // NOI18N
        jButtonBrowseImagePath.setName("jButtonBrowseImagePath"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel5)
                            .addComponent(jLabel1))
                        .addGap(31, 31, 31)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelName)
                            .addComponent(jLabelSize)
                            .addComponent(jLabelVersion)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(37, 37, 37)
                        .addComponent(statusMessageLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(statusLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButtonClose)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonApply))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldPath)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonOpenDir, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldAttachmentPath)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonBrowseAttachmentPath))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldImagePath)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonBrowseImagePath)))
                .addContainerGap())
            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jLabelName))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(jLabelSize))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(jLabelVersion))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jLabel4)
                            .addComponent(statusMessageLabel)
                            .addComponent(statusLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jTextFieldPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jButtonOpenDir))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jTextFieldAttachmentPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonBrowseAttachmentPath))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jTextFieldImagePath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonBrowseImagePath))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonClose)
                    .addComponent(jButtonApply))
                .addGap(3, 3, 3))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonApply;
    private javax.swing.JButton jButtonBrowseAttachmentPath;
    private javax.swing.JButton jButtonBrowseImagePath;
    private javax.swing.JButton jButtonClose;
    private javax.swing.JButton jButtonOpenDir;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabelName;
    private javax.swing.JLabel jLabelSize;
    private javax.swing.JLabel jLabelVersion;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextAreaDescription;
    private javax.swing.JTextArea jTextAreaSysInfo;
    private javax.swing.JTextField jTextFieldAttachmentPath;
    private javax.swing.JTextField jTextFieldImagePath;
    private javax.swing.JTextField jTextFieldPath;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JLabel statusMessageLabel;
    // End of variables declaration//GEN-END:variables

}
