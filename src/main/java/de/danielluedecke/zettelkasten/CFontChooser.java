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
import de.danielluedecke.zettelkasten.mac.MacSourceList;
import de.danielluedecke.zettelkasten.util.Constants;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.application.Action;

/**
 * This class is a simple font-chooser. The font passed as parameter is set as
 * default selections in the lists.
 * 
 * @author  danielludecke
 */
public class CFontChooser extends javax.swing.JDialog {

    /**
     * This variable stores the font-selection of the user. we pass the current used
     * font as parameter, and store the changes in this variable.
     */
    public Font selectedFont;
    /**
     * DefaultListModel to get access to the lists' data
     */
    private DefaultListModel<String> listmodelFont = new DefaultListModel<String>();
    /**
     * DefaultListModel to get access to the lists' data
     */
    private DefaultListModel<String> listmodelSize = new DefaultListModel<String>();
    /**
     * DefaultListModel to get access to the lists' data
     */
    private DefaultListModel<String> listmodelStyle = new DefaultListModel<String>();
    /**
     * get the strings for file descriptions from the resource map
     */
    private org.jdesktop.application.ResourceMap resourceMap = 
        org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
        getContext().getResourceMap(CFontChooser.class);
    
    
    /** Creates new form CFontChooser */
    public CFontChooser(java.awt.Frame parent, Font f) {
        super(parent);
        initComponents();
        // set application icon
        setIconImage(Constants.zknicon.getImage());
        selectedFont=f;
        //
        // init font-list
        //
        // retrieve a list with all installed fonts
        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        // and set them to the list view
        for (String fts : fonts) listmodelFont.addElement(fts);
        //
        // init size-list
        //
        // build a list with size-values
        for (int i=8; i<=72; i++) listmodelSize.addElement(Integer.toString(i));
        // init style-list
        listmodelStyle.addElement(resourceMap.getString("fontStylePlain"));
        listmodelStyle.addElement(resourceMap.getString("fontStyleItalic"));
        listmodelStyle.addElement(resourceMap.getString("fontStyleBold"));
        listmodelStyle.addElement(resourceMap.getString("fontStyleBoldItalic"));
        // make the default selection for the font family
        int index = listmodelFont.indexOf(selectedFont.getFamily());
        // if we found a valid font-name, select it
        if (index!=-1) jListFont.setSelectedIndex(index);
        // else select first font per default
        else jListFont.setSelectedIndex(0);
        // make sure, the selected item is visible
        jListFont.ensureIndexIsVisible(index);
        
        // make the default selection for the font style
        switch (selectedFont.getStyle()) {
            case Font.PLAIN: jListStyle.setSelectedIndex(0); break;
            case Font.ITALIC: jListStyle.setSelectedIndex(1); break;
            case Font.BOLD: jListStyle.setSelectedIndex(2); break;
            case Font.ITALIC+Font.BOLD: jListStyle.setSelectedIndex(3); break;
            default: jListStyle.setSelectedIndex(0); break;
        }
        // these codelines add an escape-listener to the dialog. so, when the user
        // presses the escape-key, the same action is performed as if the user
        // presses the cancel button...
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        ActionListener cancelAction = new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                cancelFont();
            }
        };
        getRootPane().registerKeyboardAction(cancelAction, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

        // get font-size
        int size=selectedFont.getSize();
        // try to find the size number
        index = listmodelSize.indexOf(size);
        // and select the related value in the list
        if (index!=-1) jListSize.setSelectedIndex(index);
        else jListSize.setSelectedIndex(3);
        // make sure, the selected item is visible
        jListSize.ensureIndexIsVisible(index);
        // add listeners manually, so we don't fire any events when initiating the lists
        jListFont.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            @Override public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                fontChanged();
            }
        });
        jListSize.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            @Override public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                fontChanged();
            }
        });
        jListStyle.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            @Override public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                fontChanged();
            }
        });
        
        // add document listener, that instantly check for a correct filepath
        jTextFieldFilter.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void changedUpdate(DocumentEvent e) { gotoFont(); }
            @Override public void insertUpdate(DocumentEvent e) { gotoFont(); }
            @Override public void removeUpdate(DocumentEvent e) { gotoFont(); }
        });
        
        updatePreview(selectedFont);
    }

    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    // Generated using JFormDesigner Evaluation license - Ralf Barkow
    private void initComponents() {
        ResourceBundle bundle = ResourceBundle.getBundle("de.danielluedecke.zettelkasten.resources.CFontChooser");
        jPanel1 = new JPanel();
        jPanel2 = new JPanel();
        jLabel1 = new JLabel();
        jScrollPane1 = new JScrollPane();
        jListFont = MacSourceList.createMacSourceList();
        jTextFieldFilter = new JTextField();
        jPanel3 = new JPanel();
        jLabel2 = new JLabel();
        jScrollPane2 = new JScrollPane();
        jListStyle = MacSourceList.createMacSourceList();
        jButtonCancel = new JButton();
        jPanel4 = new JPanel();
        jLabel3 = new JLabel();
        jScrollPane3 = new JScrollPane();
        jListSize = MacSourceList.createMacSourceList();
        jButtonApply = new JButton();
        jPanel5 = new JPanel();
        jScrollPane4 = new JScrollPane();
        jEditorPanePreview = new JEditorPane();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(bundle.getString("FormFontChooser.title"));
        setModal(true);
        setName("FormFontChooser");
        setResizable(false);
        Container contentPane = getContentPane();

        //======== jPanel1 ========
        {
            jPanel1.setName("jPanel1");
            jPanel1.setBorder (new javax. swing. border. CompoundBorder( new javax .swing .border .TitledBorder (new javax. swing
            . border. EmptyBorder( 0, 0, 0, 0) , "JF\u006frm\u0044es\u0069gn\u0065r \u0045va\u006cua\u0074io\u006e", javax. swing. border. TitledBorder
            . CENTER, javax. swing. border. TitledBorder. BOTTOM, new java .awt .Font ("D\u0069al\u006fg" ,java .
            awt .Font .BOLD ,12 ), java. awt. Color. red) ,jPanel1. getBorder( )) )
            ; jPanel1. addPropertyChangeListener (new java. beans. PropertyChangeListener( ){ @Override public void propertyChange (java .beans .PropertyChangeEvent e
            ) {if ("\u0062or\u0064er" .equals (e .getPropertyName () )) throw new RuntimeException( ); }} )
            ;

            //======== jPanel2 ========
            {
                jPanel2.setName("jPanel2");

                //---- jLabel1 ----
                jLabel1.setText(bundle.getString("jLabel1.text"));
                jLabel1.setName("jLabel1");

                //======== jScrollPane1 ========
                {
                    jScrollPane1.setName("jScrollPane1");

                    //---- jListFont ----
                    jListFont.setModel(listmodelFont);
                    jListFont.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    jListFont.setName("jListFont");
                    jScrollPane1.setViewportView(jListFont);
                }

                //---- jTextFieldFilter ----
                jTextFieldFilter.setText(bundle.getString("jTextFieldFilter.text"));
                jTextFieldFilter.setName("jTextFieldFilter");

                GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
                jPanel2.setLayout(jPanel2Layout);
                jPanel2Layout.setHorizontalGroup(
                    jPanel2Layout.createParallelGroup()
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(jPanel2Layout.createParallelGroup()
                                .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE)
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addComponent(jLabel1)
                                    .addContainerGap(159, Short.MAX_VALUE))
                                .addComponent(jTextFieldFilter, GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE)))
                );
                jPanel2Layout.setVerticalGroup(
                    jPanel2Layout.createParallelGroup()
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(jLabel1)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jTextFieldFilter, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE))
                );
            }

            //======== jPanel3 ========
            {
                jPanel3.setName("jPanel3");

                //---- jLabel2 ----
                jLabel2.setText(bundle.getString("jLabel2.text"));
                jLabel2.setName("jLabel2");

                //======== jScrollPane2 ========
                {
                    jScrollPane2.setName("jScrollPane2");

                    //---- jListStyle ----
                    jListStyle.setModel(listmodelStyle);
                    jListStyle.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    jListStyle.setName("jListStyle");
                    jScrollPane2.setViewportView(jListStyle);
                }

                //---- jButtonCancel ----
                jButtonCancel.setName("jButtonCancel");

                GroupLayout jPanel3Layout = new GroupLayout(jPanel3);
                jPanel3.setLayout(jPanel3Layout);
                jPanel3Layout.setHorizontalGroup(
                    jPanel3Layout.createParallelGroup()
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(jPanel3Layout.createParallelGroup()
                                .addComponent(jButtonCancel)
                                .addComponent(jScrollPane2, GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                                .addComponent(jLabel2))
                            .addContainerGap())
                );
                jPanel3Layout.setVerticalGroup(
                    jPanel3Layout.createParallelGroup()
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addComponent(jLabel2)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jScrollPane2, GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButtonCancel))
                );
            }

            //======== jPanel4 ========
            {
                jPanel4.setName("jPanel4");

                //---- jLabel3 ----
                jLabel3.setText(bundle.getString("jLabel3.text"));
                jLabel3.setName("jLabel3");

                //======== jScrollPane3 ========
                {
                    jScrollPane3.setName("jScrollPane3");

                    //---- jListSize ----
                    jListSize.setModel(listmodelSize);
                    jListSize.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    jListSize.setName("jListSize");
                    jScrollPane3.setViewportView(jListSize);
                }

                //---- jButtonApply ----
                jButtonApply.setName("jButtonApply");

                GroupLayout jPanel4Layout = new GroupLayout(jPanel4);
                jPanel4.setLayout(jPanel4Layout);
                jPanel4Layout.setHorizontalGroup(
                    jPanel4Layout.createParallelGroup()
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(jPanel4Layout.createParallelGroup()
                                .addComponent(jScrollPane3, GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)
                                .addGroup(jPanel4Layout.createParallelGroup()
                                    .addComponent(jLabel3)
                                    .addComponent(jButtonApply, GroupLayout.Alignment.TRAILING)))
                            .addContainerGap())
                );
                jPanel4Layout.setVerticalGroup(
                    jPanel4Layout.createParallelGroup()
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addComponent(jLabel3)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jScrollPane3, GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButtonApply))
                );
            }

            //======== jPanel5 ========
            {
                jPanel5.setBorder(new TitledBorder("Vorschau"));
                jPanel5.setName("jPanel5");

                //======== jScrollPane4 ========
                {
                    jScrollPane4.setBorder(null);
                    jScrollPane4.setName("jScrollPane4");

                    //---- jEditorPanePreview ----
                    jEditorPanePreview.setBorder(null);
                    jEditorPanePreview.setContentType(bundle.getString("jEditorPanePreview.contentType"));
                    jEditorPanePreview.setEditable(false);
                    jEditorPanePreview.setName("jEditorPanePreview");
                    jScrollPane4.setViewportView(jEditorPanePreview);
                }

                GroupLayout jPanel5Layout = new GroupLayout(jPanel5);
                jPanel5.setLayout(jPanel5Layout);
                jPanel5Layout.setHorizontalGroup(
                    jPanel5Layout.createParallelGroup()
                        .addGroup(jPanel5Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jScrollPane4, GroupLayout.DEFAULT_SIZE, 463, Short.MAX_VALUE)
                            .addContainerGap())
                );
                jPanel5Layout.setVerticalGroup(
                    jPanel5Layout.createParallelGroup()
                        .addGroup(jPanel5Layout.createSequentialGroup()
                            .addComponent(jScrollPane4, GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE)
                            .addContainerGap())
                );
            }

            GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
            jPanel1.setLayout(jPanel1Layout);
            jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup()
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(jPanel5, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())
            );
            jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup()
                    .addGroup(GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup()
                            .addComponent(jPanel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel4, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
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


    /**
     * When the user cancels the dialog, we set the font-variable to null. since we
     * check "selectedFont" as return value, a "null" indicates the cancel-action
     */
    @Action
    public void cancelFont() {
        selectedFont = null;
        dispose();
        setVisible(false);
    }
    
    
    /**
     * When the user applies the changes, the selected font value contains the new font.
     */
    @Action
    public void applyFont() {
        dispose();
        setVisible(false);
    }
    
    
    /**
     * This method gets the input from the textfield jTextFieldFilter and searched the font-list
     * for fonts that begin with the entered text. If a matching font was found, it is selected.
     */
    private void gotoFont() {
        // get the entered text
        String f = jTextFieldFilter.getText().toLowerCase();
        // retrieve the list model
        listmodelFont = (DefaultListModel<String>) jListFont.getModel();
        // go through all fonts in the list
        for (int cnt=0; cnt<listmodelFont.getSize(); cnt++) {
            // get current font
            String font = listmodelFont.get(cnt).toString().toLowerCase();
            // if the font starts with the entered text, select it
            if (font.startsWith(f)) {
                jListFont.setSelectedIndex(cnt);
                jListFont.ensureIndexIsVisible(cnt);
                return;
            }
        }
    }
    
    
    /**
     * This method is called each time the user chooses a selection from any list. The new
     * style, fontname or size are immediately applied to the variable "selectedFont" and
     * the preview-label is being updated.
     */
    private void fontChanged() { 
        int size = jListSize.getSelectedIndex()+8;
        String name = jListFont.getSelectedValue().toString();
        if (0==jListStyle.getSelectedIndex()) // Regular
        selectedFont = new Font(name, Font.PLAIN, size);
        else if (1==jListStyle.getSelectedIndex()) // Italic
        selectedFont = new Font(name, Font.ITALIC, size);
        else if (2==jListStyle.getSelectedIndex()) // Bold
        selectedFont = new Font(name, Font.BOLD, size);
        else if (3==jListStyle.getSelectedIndex()) // Bold Italic
        selectedFont = new Font(name, Font.BOLD+Font.ITALIC, size);
        else // default, none selected
        selectedFont = new Font(name, Font.PLAIN, size);
        updatePreview(selectedFont);
    } 
    
    
    /**
     * This method creates a html-page with css-style-definition from the font-properties.
     * The html-page is then set to the editorpane und used as preview panel...
     * @param f the font we want to preview
     */
    private void updatePreview(Font f) {
        StringBuilder css = new StringBuilder("");
        
        String style="normal";
        String weight="normal";
        
        if (Font.PLAIN==f.getStyle()) {
            style="normal";
            weight="normal";
        }
        else if (Font.BOLD==f.getStyle()) {
            style="normal";
            weight="bold";
        }
        else if (Font.ITALIC==f.getStyle()) {
            style="italic";
            weight="normal";
        }
        else if ((Font.BOLD+Font.ITALIC)==f.getStyle()) {
            style="italic";
            weight="bold";
        }
        
        // body-tag with main font settings
        css.append("<html>\n<head>\n<style>\nbody{font-family:");
        css.append(f.getFamily());
        css.append(";font-size:");
        css.append(String.valueOf(f.getSize()));
        css.append("px;color:#000000");
        css.append(";font-style:");
        css.append(style);
        css.append(";font-weight:");
        css.append(weight);
        css.append(";}\n</style>\n</head>\n<body>\n");
        css.append("<p>abcdef ABCDEG<br>1234567890<br>Zettelkasten3 - Preview!</p></body>\n</html>");
        
        jEditorPanePreview.setText(css.toString());
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Ralf Barkow
    private JPanel jPanel1;
    private JPanel jPanel2;
    private JLabel jLabel1;
    private JScrollPane jScrollPane1;
    private JList jListFont;
    private JTextField jTextFieldFilter;
    private JPanel jPanel3;
    private JLabel jLabel2;
    private JScrollPane jScrollPane2;
    private JList jListStyle;
    private JButton jButtonCancel;
    private JPanel jPanel4;
    private JLabel jLabel3;
    private JScrollPane jScrollPane3;
    private JList jListSize;
    private JButton jButtonApply;
    private JPanel jPanel5;
    private JScrollPane jScrollPane4;
    private JEditorPane jEditorPanePreview;
    // End of variables declaration//GEN-END:variables

}
