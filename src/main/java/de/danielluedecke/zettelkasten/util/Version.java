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
        if (instance ==null) {
            instance = new Version();
            try (InputStream is = Version.class.getResourceAsStream("/de/danielluedecke/zettelkasten/version.properties")) {
                Properties versionProp = new Properties();
                versionProp.load(is);
                instance.version = versionProp.getProperty("version");
                instance.build = versionProp.getProperty("git.commit.id.abbrev");
            } catch (IOException e) {
                Constants.zknlogger.severe("Could not load version.properties");
            }
        }
        return instance;
    }

    public String getVersionString() {
        return version + " (Build " + build + ")";
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public int compare(Version other) {
        String thisVersion = this.getVersion().replaceAll("[a-zA-Z]", "");
        String otherVersion = other.getVersion().replaceAll("[a-zA-Z]", "");
        System.out.println("This version: " + thisVersion);
        System.out.println("Other version: " + otherVersion);
        String[] otherVersionParts = otherVersion.split("\\.");
        String[] thisVersionParts = thisVersion.split("\\.");

        if (Integer.parseInt(otherVersionParts[0])>Integer.parseInt(thisVersionParts[0])) {
            return -1;
        } else if (Integer.parseInt(otherVersionParts[0])<Integer.parseInt(thisVersionParts[0])) {
            return 1;
        } else {
            if (otherVersionParts.length==1) return 1;
            if (thisVersionParts.length==1) return -1;

            if (Integer.parseInt(otherVersionParts[1])>Integer.parseInt(thisVersionParts[1])) {
                return -1;
            } else if (Integer.parseInt(otherVersionParts[1])<Integer.parseInt(thisVersionParts[1])) {
                return 1;
            } else {
                if (otherVersionParts.length == 2) return 1;
                if (thisVersionParts.length == 2) return -1;
                if (Integer.parseInt(otherVersionParts[2]) > Integer.parseInt(thisVersionParts[2])) {
                    return -1;
                } else if (Integer.parseInt(otherVersionParts[2]) < Integer.parseInt(thisVersionParts[2])) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }
    }


}
