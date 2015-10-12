/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.danielluedecke.zettelkasten.util.classes;

import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.util.Constants;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Luedeke
 */
public class TitleTableCellRenderer extends JLabel implements TableCellRenderer {
    
    // column which contains icons instead of text
    private final JLabel iconLabelParentLuhmann;
    private final JLabel iconLabelAnyLuhmann;
    private final JLabel noIconLabel;
    private final Daten dataObj;
    private Color selectionBackground = javax.swing.UIManager.getColor("Table[Enabled+Selected].textBackground");
    private final Color tableRowBackground = Color.WHITE; // javax.swing.UIManager.getColor("Table.foreground");
    private Color tableRowAlternate = javax.swing.UIManager.getColor("Table.alternateRowColor");
    
    public TitleTableCellRenderer(Daten d) {
        this.iconLabelParentLuhmann = new JLabel(Constants.iconTopLuhmann);
        this.iconLabelAnyLuhmann = new JLabel(Constants.iconNoTopLuhmann);
        this.noIconLabel = new JLabel("");
        if (null == tableRowAlternate) {
            tableRowAlternate = Color.WHITE;
        }
        if (null == selectionBackground) {
            selectionBackground = new Color(60, 106, 137);
        }
        dataObj = d;
        setOpaque(true);
    }
    
    @Override
    public Component getTableCellRendererComponent(
                            JTable table, Object color,
                            boolean isSelected, boolean hasFocus,
                            int row, int column) {
        Object value = table.getValueAt(row, 0);
        int nr = -1;

        if (value != null) {
            try {
                nr = Integer.parseInt(value.toString());
            } catch (NumberFormatException e) {
            }
        }
        
        JLabel returnLabel;
        if (nr != -1) {
            // get note
            if (dataObj.isTopLevelLuhmann(nr)) {
                returnLabel = this.iconLabelParentLuhmann;
            } else if (dataObj.findParentlLuhmann(nr, true) != -1) {
                returnLabel = this.iconLabelAnyLuhmann;
            } else {
                returnLabel = this.noIconLabel;
            }
        } else {
            returnLabel = this.noIconLabel;
        }
        if (isSelected) {
            returnLabel.setBackground(selectionBackground);
        } else {
            returnLabel.setBackground(row % 2 == 0 ? 
                    tableRowBackground : 
                    tableRowAlternate);
        }
        returnLabel.setOpaque(true);
        return returnLabel;
    }    
}
