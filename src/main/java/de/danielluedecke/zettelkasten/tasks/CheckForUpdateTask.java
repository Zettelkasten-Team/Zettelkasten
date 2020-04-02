package de.danielluedecke.zettelkasten.tasks;


import de.danielluedecke.zettelkasten.ZettelkastenView;
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.util.Version;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
        // Your Task's code here.  This method runs
        // on a background thread, so don't reference
        // the Swing GUI from here.
        String updateinfo = accessUpdateFile(new URL(Constants.UPDATE_INFO_URI));
        // check for valid access
        if (null == updateinfo || updateinfo.isEmpty()) {
            return null;
        }
        // retrieve update info and split them into an array. this array will hold the latest
        // build-version-number in the first field, and the type of update in the 2. field.
        String[] updateversion = updateinfo.split("\n");
        // check whether we have a valid array with content
        if (updateversion != null && updateversion.length > 0) {
            // retrieve start-index of the build-number within the version-string.
            int substringindex = Version.get().getVersionString().indexOf("(Build") + 7;
            // only copy buildinfo into string, other information of version-info are not needed
            String curversion = Version.get().getVersionString().substring(substringindex, substringindex + 8);
            // store build number of update
            updateBuildNr = updateversion[0];
            // check whether there's a newer version online
            updateavailable = (curversion.compareTo(updateBuildNr) < 0);
            // check whether update hint should be shown for this version or not
            showUpdateMsg = (updateBuildNr.compareTo(settingsObj.getShowUpdateHintVersion()) != 0);
            // if no update available and user wants to check for nightly versions,
            // check this now
            if (!updateavailable && settingsObj.getAutoNightlyUpdate()) {
                updateinfo = accessUpdateFile(new URL(Constants.UPDATE_NIGHTLY_INFO_URI));
                // check for valid access
                if (null == updateinfo || updateinfo.isEmpty()) {
                    return null;
                }
                // retrieve update info and split them into an array. this array will hold the latest
                // build-version-number in the first field, and the type of update in the 2. field.
                updateversion = updateinfo.split("\n");
                if (updateversion != null && updateversion.length > 0) {
                    updateavailable = (curversion.compareTo(updateversion[0]) < 0);
                    if (updateavailable) {
                        zknframe.setUpdateURI(Constants.UPDATE_NIGHTLY_URI);
                    }
                }
            }
        }
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
