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
package de.danielluedecke.zettelkasten.util;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 *
 * @author danielludecke
 */
public class ExtractFormInformation {
    private int markcount;
    public int getMarkCount() {
        return markcount;
    }
    private boolean reentry;
    public boolean hasReentry() {
        return reentry;
    }
    private String description;
    public String getDescription() {
        return description;
    }
    private String unmarkedSpace;
    public String getUnmarkedSpace() {
        return unmarkedSpace;
    }
    private String[] distinctions;
    public String[] getDistinctions() {
        return distinctions;
    }
    
    private String formTag;
    
    public ExtractFormInformation(String ft) {
        formTag = ft;
        // init mark-count-variable
        markcount = 0;
        // init re-entry-hook-variable
        reentry = false;
        description = null;
        distinctions = null;
        unmarkedSpace = null;
        convertFormTag();
    }
    
    private void convertFormTag() {
        try {
            // remove brackets from form-tag
            if (formTag.startsWith("[")) {
                formTag = formTag.substring(1);
            }
            // remove brackets from form-tag
            if (formTag.endsWith("]")) {
                formTag = formTag.substring(0,formTag.length()-1);
            }
            // remove "form"-text
            formTag = formTag.substring(Constants.FORMAT_FORM_TAG.length()-1);
            // check whether re-entry-hook is requested
            reentry = formTag.startsWith("#");
            // check whether we have a reentry-tag, and if so, remove it
            if (reentry) {
                formTag = formTag.substring(1).trim();
            }
            // split char at = sign
            String[] bothSidesOfForm = formTag.split("=");
            // retrieve distinctions
            String[] dummydistinctions = null;
            // check whether we have a valid array
            if (bothSidesOfForm!=null) {
                // check whether we have any description left of = sign
                // split distinctions into another array
                if (bothSidesOfForm.length>1) {
                    // retrieve description
                    description = bothSidesOfForm[0].trim();
                    // and we have distinctions in the form notation
                    dummydistinctions = bothSidesOfForm[1].trim().split("\\|");
                }
                else if (bothSidesOfForm.length==1) {
                    // we only have distinctions in the form notation, but no
                    // text on the left side of the =. "description" remains "null".
                    dummydistinctions = bothSidesOfForm[0].trim().split("\\|");
                }
                // check whether we have a valid array
                if (dummydistinctions!=null) {
                    // create new array
                    distinctions = new String[dummydistinctions.length];
                    // trim distinctions
                    for (int cnt=0; cnt<dummydistinctions.length; cnt++) {
                        // ...
                        distinctions[cnt] = dummydistinctions[cnt].trim();
                    }
                    try {
                        // retrieve last distincion and split it at the ^ char. if we have another string starting
                        // after the ^, it is the unmarked space.
                        String[] dummylast = distinctions[distinctions.length-1].trim().split(Pattern.quote("^"));
                        // check whether we have an unmarked space
                        if (dummylast.length>1) {
                            // "clean" last distinction from unmarked space
                            distinctions[distinctions.length-1] = dummylast[0];
                            // retrieve unmarked space
                            unmarkedSpace = dummylast[1];
                        }
                    }
                    catch (PatternSyntaxException ex) {
                    }
                }
                // get markcount
                markcount = distinctions.length-1;
            }
        }
        catch (IndexOutOfBoundsException ex) {
        }
    }
}
