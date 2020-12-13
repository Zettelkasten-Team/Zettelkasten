package de.danielluedecke.zettelkasten.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class Version {


    private static Version instance;
    String version;
    String build;
    public static Version get() {
        if (instance ==null)
            instance = new Version();

        return instance;
    }

    public String getVersionString() {
        if (version == null) {
            try (InputStream is = getClass().getResourceAsStream("/de/danielluedecke/zettelkasten/version.properties")) {
                Properties versionProp = new Properties();
                versionProp.load(is);
                version = versionProp.getProperty("version");
                build = versionProp.getProperty("git.commit.id.abbrev");
            } catch (IOException e) {
                Constants.zknlogger.severe("Could not load version.properties");
            }
        }
        return version + " (Build " + build + ")";
    }



}
