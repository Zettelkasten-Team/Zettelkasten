package de.danielluedecke.zettelkasten;

import de.danielluedecke.zettelkasten.util.Constants;

import java.io.File;
import java.util.Locale;
import java.util.logging.Level;

public class ZettelkastenAppRefactored extends ZettelkastenApp {

    @Override
    void initializeLocale() {
        Locale locale = determineLocale(getSettings().getLanguage());
        Locale.setDefault(locale);
    }

    private Locale determineLocale(String languageFromSettings) {
        Locale locale;
        switch (languageFromSettings) {
            case "en":
                locale = new Locale("en", "GB");
                break;
            case "de":
                locale = new Locale("de", "DE");
                break;
            case "es":
                locale = new Locale("es", "ES");
                break;
            case "pt":
                locale = new Locale("pt", "BR");
                break;
            default:
                locale = new Locale("en", "GB");
                break;
        }
        return locale;
    }

    //@Override
    void updateSettingsWithCommandLineParams(String[] params) {
        for (String param : params) {
            if (param.endsWith(Constants.ZKN_FILEEXTENSION)) {
                processDataFile(param);
            } else {
                processInitialEntryNumber(param);
            }
        }
    }

    private void processDataFile(String param) {
        File file = new File(param);
        if (file.exists()) {
            getSettings().setMainDataFile(file);
            Constants.zknlogger.log(Level.INFO, "Data file set: {0}", param);
        } else {
            Constants.zknlogger.log(Level.WARNING, "Invalid data file: {0}", param);
        }
    }

    private void processInitialEntryNumber(String param) {
        try {
            int entryNumber = Integer.parseInt(param);
            if (entryNumber > 0) {
                getSettings().setInitialParamZettel(entryNumber);
            } else {
                Constants.zknlogger.log(Level.WARNING, "Entry number not positive: {0}", param);
            }
        } catch (NumberFormatException e) {
            Constants.zknlogger.log(Level.WARNING, "Invalid entry number: {0}", param);
        }
    }
}
