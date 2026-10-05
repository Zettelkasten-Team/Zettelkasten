package de.danielluedecke.zettelkasten;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ZettelkastenLauncherTest {

    @Test
    public void parsesLegacyVersionScheme() {
        assertEquals(8, ZettelkastenLauncher.parseFeatureVersion("1.8"));
    }

    @Test
    public void parsesCurrentVersionScheme() {
        assertEquals(24, ZettelkastenLauncher.parseFeatureVersion("24"));
        assertEquals(25, ZettelkastenLauncher.parseFeatureVersion("25"));
    }

    @Test
    public void returnsZeroForUnparsableVersion() {
        assertEquals(0, ZettelkastenLauncher.parseFeatureVersion(""));
        assertEquals(0, ZettelkastenLauncher.parseFeatureVersion("abc"));
    }
}
