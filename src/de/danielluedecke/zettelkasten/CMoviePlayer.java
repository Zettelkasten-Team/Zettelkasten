/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.danielluedecke.zettelkasten;

/*
import java.awt.BorderLayout;
import java.awt.Component;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import javax.media.CannotRealizeException;
import javax.media.Manager;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.swing.JFrame;
import javax.swing.JPanel;
*/

/**
 *
 * @author danielludecke
 */
public class CMoviePlayer {

/*
    Player mediaPlayer;
    String movieURL;
    boolean mediaPanelOK;

    CMoviePlayer(String mu) {
        movieURL = mu;
    }

    public void showPlayer() {
        MediaPanel mediaPanel = new MediaPanel(movieURL);

        if (mediaPanelOK) {
//            HudWindow hud = new HudWindow("Window");
//            hud.getJDialog().setSize(500, 400);
//            hud.getJDialog().setLocationRelativeTo(null);
//            hud.getJDialog().setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//            hud.getContentPane().add(mediaPanel);
//            hud.getJDialog().setVisible(true);
            JFrame mediaTest = new JFrame( "Media Tester" );
            mediaTest.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
            mediaTest.add( mediaPanel );
            mediaTest.setSize( 300, 300 );
            mediaTest.validate();
            mediaTest.setVisible( true );
            mediaPlayer.start(); // start playing the media clip
        }
    }

    private class MediaPanel extends JPanel {

        MediaPanel(String movieURL) {
            mediaPanelOK = false;
            setLayout(new BorderLayout());
            Manager.setHint(Manager.LIGHTWEIGHT_RENDERER, true);
            try {
                URL moviepath = new URL("file://"+movieURL);
                // create a player to play the media specified in the URL
                mediaPlayer = Manager.createRealizedPlayer(moviepath);
                // get the components for the video and the playback controls
                Component video = mediaPlayer.getVisualComponent();
                Component controls = mediaPlayer.getControlPanelComponent();

                if (video!=null) add(video, BorderLayout.CENTER); else System.out.println("Video ist null!"); // add video component
                if (controls!=null) add(controls, BorderLayout.SOUTH); // add controls
                mediaPanelOK = true;
            } // end try
            catch (NoPlayerException noPlayerException) {
                CConstants.zknlogger.log(Level.WARNING, noPlayerException.getLocalizedMessage()+" No media player found for "+movieURL+".");
            } // end catch
            catch (CannotRealizeException cannotRealizeException) {
                CConstants.zknlogger.log(Level.WARNING, cannotRealizeException.getLocalizedMessage()+" Could not realize media player for "+movieURL+".");
            } // end catch
            catch (IOException iOException) {
                CConstants.zknlogger.log(Level.WARNING, iOException.getLocalizedMessage()+" Error reading from the source.");
            } // end catch
        }
    }
*/
}