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

import de.danielluedecke.zettelkasten.database.AutoKorrektur;
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.database.StenoData;
import java.awt.event.KeyEvent;
import javax.swing.text.BadLocationException;

/**
 *
 * @author Luedeke
 */
public class NewEntryFrameUtil {

    public static void checkSteno(Settings settingsObj, StenoData stenoObj, javax.swing.JTextArea ta) {
        if (settingsObj.getStenoActivated()) {
            int caret = ta.getCaretPosition();
            if (caret > 2) {
                try {
                    String text = ta.getText();
                    int longest = stenoObj.retrieveLongestAbbrLength();
                    int start = caret - longest - 1;
                    int start2 = text.lastIndexOf(System.lineSeparator(), caret - 2);
                    if (start2 > start) {
                        start = start2;
                    }
                    String searchstring = text.substring(start, caret);
                    String abbr = stenoObj.findAbbreviationFromText(searchstring);
                    if (abbr != null) {
                        String longword = stenoObj.getStenoWord(abbr);
                        if (longword != null) {
                            StringBuilder sb = new StringBuilder(text);
                            sb.replace(caret - abbr.length(), caret, longword);
                            ta.setText(sb.toString());
                            caret = caret - abbr.length() + longword.length();
                            ta.setCaretPosition(caret);
                        } else {
                            if (!ta.getName().equals("jTextAreaAuthor")) {
                                ta.replaceSelection("\t");
                            }
                        }
                    } else {
                        if (!ta.getName().equals("jTextAreaAuthor")) {
                            ta.replaceSelection("\t");
                        }
                    }
                } catch (IndexOutOfBoundsException | IllegalArgumentException ex) {
                }
            } else {
                if (!ta.getName().equals("jTextAreaAuthor")) {
                    ta.replaceSelection("\t");
                }
            }
        } else {
            if (!ta.getName().equals("jTextAreaAuthor")) {
                ta.replaceSelection("\t");
            }
        }
    }

    public static void checkSpelling(int key, javax.swing.JTextArea ta, Settings settingsObj, AutoKorrektur spellObj) {
        if (KeyEvent.VK_SPACE == key || KeyEvent.VK_PERIOD == key || KeyEvent.VK_COMMA == key || KeyEvent.VK_BRACELEFT == key || KeyEvent.VK_ENTER == key || KeyEvent.VK_OPEN_BRACKET == key || KeyEvent.VK_COLON == key || KeyEvent.VK_QUOTE == key) {
            if (ta != null) {
                try {
                    int caret = ta.getCaretPosition();
                    String text = ta.getText();
                    if (settingsObj.getSpellCorrect()) {
                        if (caret > 2) {
                            int start = text.lastIndexOf(" ", caret - 2);
                            int start2 = text.lastIndexOf(System.lineSeparator(), caret - 2);
                            if (start2 > start) {
                                start = start2;
                            }
                            String wrong = text.substring(start + 1, caret - 1);
                            String correct = spellObj.getCorrectSpelling(wrong);
                            if (correct != null) {
                                StringBuilder sb = new StringBuilder(text);
                                sb.replace(start + 1, caret - 1, correct);
                                ta.setText(sb.toString());
                                caret = caret - wrong.length() + correct.length();
                                ta.setCaretPosition(caret);
                            }
                        }
                    }
                    if (KeyEvent.VK_QUOTE == key) {
                        // TODO Anfrührungszeichen ersetzen
                        // <ALT>+0132 bzw.
                        // <ALT>+0147 typographische Anführungszeichen oben / unten hin.
                    }
                } catch (IndexOutOfBoundsException | IllegalArgumentException ex) {
                }
            }
        }
    }

    /**
     * 
     * @param ta
     * @param key 
     */
    public static void autoCompleteTags(javax.swing.JTextArea ta, char key) {
        if (']' == key) {
            int caret = ta.getCaretPosition();
            try {
                String tag = ta.getText(caret - 3, 3);
                String closetag = "";
                switch (tag) {
                    case Constants.FORMAT_BOLD_OPEN:
                        closetag = Constants.FORMAT_BOLD_CLOSE;
                        break;
                    case Constants.FORMAT_ITALIC_OPEN:
                        closetag = Constants.FORMAT_ITALIC_CLOSE;
                        break;
                    case Constants.FORMAT_UNDERLINE_OPEN:
                        closetag = Constants.FORMAT_UNDERLINE_CLOSE;
                        break;
                    case Constants.FORMAT_STRIKE_OPEN:
                        closetag = Constants.FORMAT_STRIKE_CLOSE;
                        break;
                    case Constants.FORMAT_ALIGNCENTER_OPEN:
                        closetag = Constants.FORMAT_ALIGNCENTER_CLOSE;
                        break;
                    case Constants.FORMAT_ALIGNJUSTIFY_OPEN:
                        closetag = Constants.FORMAT_ALIGNJUSTIFY_CLOSE;
                        break;
                    case Constants.FORMAT_ALIGNLEFT_OPEN:
                        closetag = Constants.FORMAT_ALIGNLEFT_CLOSE;
                        break;
                    case Constants.FORMAT_ALIGNRIGHT_OPEN:
                        closetag = Constants.FORMAT_ALIGNRIGHT_CLOSE;
                        break;
                    case Constants.FORMAT_LIST_OPEN:
                        closetag = Constants.FORMAT_LIST_CLOSE;
                        break;
                    case Constants.FORMAT_LISTITEM_OPEN:
                        closetag = Constants.FORMAT_LISTITEM_CLOSE;
                        break;
                    case Constants.FORMAT_NUMBEREDLIST_OPEN:
                        closetag = Constants.FORMAT_NUMBEREDLIST_CLOSE;
                        break;
                    case Constants.FORMAT_SUP_OPEN:
                        closetag = Constants.FORMAT_SUP_CLOSE;
                        break;
                    case Constants.FORMAT_SUB_OPEN:
                        closetag = Constants.FORMAT_SUB_CLOSE;
                        break;
                    case Constants.FORMAT_QUOTE_OPEN:
                        closetag = Constants.FORMAT_QUOTE_CLOSE;
                        break;
                    case Constants.FORMAT_H1_OPEN:
                        closetag = Constants.FORMAT_H1_CLOSE;
                        break;
                    case Constants.FORMAT_H2_OPEN:
                        closetag = Constants.FORMAT_H2_CLOSE;
                        break;
                }
                ta.insert(closetag, caret);
                ta.setCaretPosition(caret);
            } catch (BadLocationException | IllegalArgumentException ex) {
            }
        }
    }
    
}
