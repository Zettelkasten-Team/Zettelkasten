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
package de.danielluedecke.zettelkasten.util;

import java.awt.event.KeyEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Daniel Luedecke
 */
public class TableUtils {

    /**
     * This method selects a certain row that contains the value {@code value}
     * in the column {@code column} in the jTable {@code table}.
     *
     * @param table the table where the row should be selected
     * @param value the value that should be selected
     * @param column the table-column that contains the value {@code value}.
     */
    public static void selectValueInTable(JTable table, String value, int column) {
        for (int cnt = 0; cnt < table.getRowCount(); cnt++) {
            String val = table.getValueAt(cnt, column).toString();
            if (val.equals(value)) {
                table.getSelectionModel().setSelectionInterval(cnt, cnt);
                table.scrollRectToVisible(table.getCellRect(cnt, column, false));
                // and leave method
                return;
            }
        }
    }

    public static void filterTable(JTable table, DefaultTableModel dtm, String text, int[] columns, boolean forceRegEx) {
        if (null == table || null == dtm || null == text || text.isEmpty() || null == columns || columns.length < 1) {
            return;
        }
        if (!forceRegEx) {
            text = text.toLowerCase();
        }
        for (int cnt = table.getRowCount() - 1; cnt >= 0; cnt--) {
            int rowindex = table.convertRowIndexToModel(cnt);
            String value = "";
            for (int ci = 0; ci < columns.length; ci++) {
                value = value + dtm.getValueAt(rowindex, columns[ci]).toString().toLowerCase() + " ";
            }
            value = value.trim();
            if (forceRegEx) {
                try {
                    Pattern pattern = Pattern.compile(text);
                    Matcher matcher = pattern.matcher(value);
                    if (!matcher.find()) {
                        dtm.removeRow(rowindex);
                    }
                } catch (PatternSyntaxException ex) {
                    if (!value.contains(text)) {
                        dtm.removeRow(rowindex);
                    }
                }
            } else if (text.contains("?")) {
                try {
                    String dummy = text.replace("?", ".");
                    dummy = dummy.replace("\\.", "\\?").toLowerCase();
                    Pattern pattern = Pattern.compile(dummy);
                    Matcher matcher = pattern.matcher(value);
                    if (!matcher.find()) {
                        dtm.removeRow(rowindex);
                    }
                } catch (PatternSyntaxException ex) {
                    if (!value.contains(text)) {
                        dtm.removeRow(rowindex);
                    }
                }
            } else if (!value.contains(text)) {
                dtm.removeRow(rowindex);
            }
        }
    }

    /**
     * This message retrieve selected values from a jTable and copies them into
     * a line-separated string, where each line contains the cell-data of each
     * row. each cell is tab-separated, each row is newline-separated.<br><br>
     * Thus, a return value might look like this:<br>
     * {@code 3   This is the third entry}<br> {@code 6   This is number six}<br>
     * {@code 9   My last entry}
     *
     * @param table the table where the data was dragged (drag-source)
     * @return a prepared string that contains the copied data in proper
     * "clipboard"-format.
     */
    public static String prepareStringForTransferHandler(JTable table) {
        int[] rows = table.getSelectedRows();
        StringBuilder sb = new StringBuilder("");
        if (rows.length > 0) {
            for (int row : rows) {
                sb.append(table.getValueAt(row, 0)).append("\t");
                sb.append(table.getValueAt(row, 1)).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * This method retrieves the key-code {@code keyCode} of a released key in
     * the JTable {@code table} and checks whether this key was a navigation key
     * (i.e. cursor up/down/left/right or home/end). If so, the method tries to
     * select the next related entry of that JTable, according to the pressed
     * key.<br><br>
     * Furthermore, the related content is made visible (scroll rect to visible
     * or ensure index is visible).
     *
     * @param table a reference to the JTable where the related key was released
     * @param keyCode the keycode of the released key
     */
    public static void navigateThroughList(JTable table, int keyCode) {
        if (KeyEvent.VK_LEFT == keyCode || KeyEvent.VK_RIGHT == keyCode) {
            return;
        }
        int selectedRow = table.getSelectedRow();
        if (-1 == selectedRow) {
            selectedRow = 0;
        }
        switch (keyCode) {
            case KeyEvent.VK_HOME:
                selectedRow = 0;
                break;
            case KeyEvent.VK_END:
                selectedRow = table.getRowCount() - 1;
                break;
            case KeyEvent.VK_DOWN:
                if (table.getRowCount() > (selectedRow + 1)) {
                    selectedRow++;
                }
                break;
            case KeyEvent.VK_UP:
                if (selectedRow > 0) {
                    selectedRow--;
                }
                break;
        }
        table.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
        table.scrollRectToVisible(table.getCellRect(selectedRow, 0, false));
    }

    /**
     * This method selects the first entry in the JTable {@code table} that
     * start with the text that is entered in the filter-textfield
     * {@code textfield}.
     *
     * @param table the jTable where the item should be selected
     * @param textfield the related filtertextfield that contains the user-input
     * @param column the column where the filtering-comparison should be applied
     * to. in most cases, the relevant information (i.e. the string/text) is in
     * column 0, but sometimes also in column 1
     */
    public static void selectByTyping(JTable table, javax.swing.JTextField textfield, int column) {
        String text = textfield.getText().toLowerCase();
        for (int cnt = 0; cnt < table.getRowCount(); cnt++) {
            String val = table.getValueAt(cnt, column).toString();
            if (val.toLowerCase().startsWith(text)) {
                table.getSelectionModel().setSelectionInterval(cnt, cnt);
                table.scrollRectToVisible(table.getCellRect(cnt, column, false));
                // and leave method
                return;
            }
        }
    }

}
