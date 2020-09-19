package de.danielluedecke.zettelkasten.tasks;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.danielluedecke.zettelkasten.ZettelkastenView;
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.util.Version;
import org.kohsuke.github.*;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Luedeke
 */
public class CheckForUpdateTask extends org.jdesktop.application.Task<Object, Void> {

    // indicates whether the zettelkasten has updates or not.

    private boolean updateavailable = false;
    private boolean showUpdateMsg = true;
    private String updateBuildNr = "0";
    private long cachingDuration = 4*60*60*1000;
    private final Settings settingsObj;
    /**
     * Reference to the main frame.
     */
    private final ZettelkastenView zknframe;

    public CheckForUpdateTask(org.jdesktop.application.Application app,
            ZettelkastenView zkn, Settings s) {
        // Runs on the EDT.  Copy GUI state that
        // doInBackground() depends on from parameters
        // to ImportFileTask fields, here.
        super(app);
        zknframe = zkn;
        settingsObj = s;
    }

    protected String accessUpdateFile(URL updatetext) {
        // input stream that will read the update text
        InputStream is = null;
        // stringbuilder that will contain the content of the update-file
        StringBuilder updateinfo = new StringBuilder("");
        try {
            // open update-file on server
            is = updatetext.openStream();
            // buffer for stream
            int buff = 0;
            // read update-file and copy content to string builder
            while (buff != -1) {
                buff = is.read();
                if (buff != -1) {
                    updateinfo.append((char) buff);
                }
            }
        } catch (IOException e) {
            // tell about fail
            Constants.zknlogger.log(Level.INFO, "No access to Zettelkasten-Website. Automatic update-check failed.");
            updateavailable = false;
            return null;
        } finally {
            try {
                if (is != null) {
                    is.close();
                } 
            }catch(IOException e) {
                Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
            }
        }
        return updateinfo.toString();
    }

    @Override
    protected Object doInBackground() throws IOException {

        Path zkdir = Paths.get(System.getProperty("user.home") + File.separatorChar + ".Zettelkasten");
        Path versionPath = zkdir.resolve("version.properties");
        Properties versionProperties = new Properties();
        String versionName;
        String releaseUrl;
        if (!zkdir.toFile().exists()) {
            zkdir.toFile().mkdirs();
        }

        if (versionPath.toFile().exists()
                && (new Date().getTime()-versionPath.toFile().lastModified() <= cachingDuration)) {
            try (FileInputStream fos = new FileInputStream(versionPath.toAbsolutePath().toString())) {
                versionProperties.load(fos);
                versionName = versionProperties.getProperty("version");
                updateBuildNr = versionProperties.getProperty("buildNr");
                releaseUrl = versionProperties.getProperty("releaseUrl");
            }
            Constants.zknlogger.info("Found and loaded version properties file.");
        } else {
            if (versionPath.toFile().exists()){
                Constants.zknlogger.info("Found version properties file, but it is older than "
                        + cachingDuration/1000/60/60 + " hours.");
            } else {
                Constants.zknlogger.info("Did not find any version properties file.");
            }
            Constants.zknlogger.info("Loading version information from github.");
            GitHub github = GitHub.connectAnonymously();
            GHRelease release = null;
            GHRepository zettelkasten = github.getOrganization("Zettelkasten-Team").getRepository("Zettelkasten");
            PagedIterable<GHRelease> ghReleases = zettelkasten.listReleases();
            PagedIterable<GHTag> ghTags = zettelkasten.listTags();
            for (GHRelease r : ghReleases) {
                if (r.isPrerelease() && settingsObj.getAutoNightlyUpdate()) {
                    release = r;
                } else if (!r.isDraft()) {
                    release = r;
                }
                if (release != null) break;
            }
            if (release == null) return null;

            for (GHTag tag : ghTags) {
                if (tag.getName().equals(release.getName())) {
                    updateBuildNr = tag.getCommit().getSHA1().substring(0, 7);
                    break;
                }
            }
            versionName = release.getName();
            releaseUrl = release.getHtmlUrl().toString();
        }

        versionProperties.setProperty("version", versionName);
        versionProperties.setProperty("buildNr", updateBuildNr);
        versionProperties.setProperty("releaseUrl", releaseUrl);
        try (FileOutputStream fos = new FileOutputStream(versionPath.toAbsolutePath().toString())){
            versionProperties.store(fos, null);
            Constants.zknlogger.fine("stored version properties file.");
        } catch(IOException e) {
            Constants.zknlogger.warning("Could not write version file.");
        }

        Version otherVersion = new Version();
        otherVersion.setBuild(updateBuildNr);
        otherVersion.setVersion(versionName);

        updateavailable = Version.get().compare(otherVersion) < 0;

        showUpdateMsg = (updateBuildNr.compareTo(settingsObj.getShowUpdateHintVersion()) != 0);
        zknframe.setUpdateURI(releaseUrl);

        return null;  // return your result
    }

    @Override
    protected void succeeded(Object result) {
        // Runs on the EDT.  Update the GUI based on
        // the result computed by doInBackground().
    }

    @Override
    protected void finished() {
        if (updateavailable) {
            //log info
            Constants.zknlogger.log(Level.INFO, "A new version of the Zettelkasten is available!");
            if (showUpdateMsg) {
                zknframe.updateZettelkasten(updateBuildNr);
            }
        }
    }
}
