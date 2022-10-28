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
import de.danielluedecke.zettelkasten.database.AutoKorrektur;
import de.danielluedecke.zettelkasten.database.AcceleratorKeys;
import de.danielluedecke.zettelkasten.database.StenoData;
import de.danielluedecke.zettelkasten.database.Synonyms;
import de.danielluedecke.zettelkasten.database.TasksData;
import de.danielluedecke.zettelkasten.util.Constants;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

import javax.swing.*;

/**
 * The main class of the application.
 */
public class ZettelkastenApp extends SingleFrameApplication {

	// we load the settings just after startup
	Settings settings;
	// and so we do with the user defined accelerator keys
	AcceleratorKeys accKeys;
	// and so we do with the user defined accelerator keys
	AutoKorrektur autoKorrekt;
	Synonyms synonyms;
	StenoData steno;
	TasksData taskData;

	private String[] params;

	private void initLocale(Settings settings) {
		String languageFromSettings = settings.getLanguage();

		// Init supported locales.
		String englishCountryCode = new Locale("en", "", "").getLanguage();
		String germanCountryCode = new Locale("de", "", "").getLanguage();
		String spanishCountryCode = new Locale("es", "", "").getLanguage();
		String portugueseCountryCode = new Locale("pt", "", "").getLanguage();

		// Defaults to English.
		Locale newLocale = new Locale("en", "GB");

		if (languageFromSettings.equals(englishCountryCode))
			newLocale = new Locale("en", "GB");
		if (languageFromSettings.equals(germanCountryCode))
			newLocale = new Locale("de", "DE");
		if (languageFromSettings.equals(spanishCountryCode))
			newLocale = new Locale("es", "ES");
		if (languageFromSettings.equals(portugueseCountryCode))
			newLocale = new Locale("pt", "BR");

		Locale.setDefault(newLocale);
	}

	private void updateSettingsWithCommandLineParams(String[] params) {
		// Check params for:
		// - data file (first param that ends with .zkn3).
		// - initial entry number (first param that is a valid number)
		for (String param : params) {
			// Is param a data file?
			if (param.toLowerCase().endsWith(Constants.ZKN_FILEEXTENSION)) {
				File file = new File(param);
				if (file.exists()) {
					Constants.zknlogger.log(Level.INFO,
							"Setting data file to '{0}' from the Zettelkasten command line arguments.",
							file.toString());
					settings.setMainDataFile(file);
					break;
				}
			}
			// Is param a number?
			try {
				int initalZettellNr = Integer.parseInt(param);
				if (initalZettellNr > 0) {
					settings.setInitialParamZettel(initalZettellNr);
					Constants.zknlogger.log(Level.INFO,
							"Setting initial entry number to '{0}' from the Zettelkasten command line arguments.",
							initalZettellNr);
					break;
				}
			} catch (NumberFormatException ex) {
			}
		}
	}

	/**
	 * At startup create and show the main frame of the application.
	 */
	@Override
	protected void startup() {
		// Initialize taskData.
		taskData = new TasksData();

		// Initialize settings.
		settings = new Settings();
		updateSettingsWithCommandLineParams(params);

		initLocale(settings);

		Constants.zknlogger.log(Level.INFO, String.format("Starting Main Window."));
		// Show main window.
		try {
			show(new ZettelkastenView(this, settings, taskData));
		} catch (ClassNotFoundException | UnsupportedLookAndFeelException | InstantiationException
				| IllegalAccessException | IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * A convenient static getter for the application instance.
	 * 
	 * @return the instance of ZettelkastenApp
	 */
	public static ZettelkastenApp getApplication() {
		return Application.getInstance(ZettelkastenApp.class);
	}

	/**
	 * Main method launching the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		launch(ZettelkastenApp.class, args);
	}

	@Override
	protected void initialize(String[] args) {
		this.params = args;
		super.initialize(args);
	}
}
