package de.danielluedecke.zettelkasten.settings;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import de.danielluedecke.zettelkasten.util.PlatformUtil;


public class AcceleratorKeysTest  {

    private AcceleratorKeys acceleratorKeys;

    @BeforeMethod
    public void setUp() {
        acceleratorKeys = new AcceleratorKeys();
    }

    @Test
    public void testInitDefaultAcceleratorKeysIfMissing_MacOS() {
        if (!PlatformUtil.isMacOS()) {
            throw new SkipException("Skipping MacOS-specific test on non-MacOS");
        }

        acceleratorKeys.initDefaultAcceleratorKeysIfMissing();

        Assert.assertEquals("meta", acceleratorKeys.getMask());
        Assert.assertEquals("BACK_SPACE", acceleratorKeys.getDelkey());
        Assert.assertEquals("CLOSE_BRACKET", acceleratorKeys.getPluskey());
        Assert.assertEquals("SLASH", acceleratorKeys.getMinuskey());
        Assert.assertEquals("meta ENTER", acceleratorKeys.getRenamekey());
        Assert.assertEquals("control shift", acceleratorKeys.getHistorykey());
        Assert.assertEquals("BACK_SLASH", acceleratorKeys.getNumbersign());
        Assert.assertEquals("control", acceleratorKeys.getCtrlkey());
    }

    @Test
    public void testInitDefaultAcceleratorKeysIfMissing_NonMacOS() {
        if (PlatformUtil.isMacOS()) {
            throw new SkipException("Skipping non-MacOS test on MacOS");
        }

        acceleratorKeys.initDefaultAcceleratorKeysIfMissing();

        Assert.assertEquals("control", acceleratorKeys.getMask());
        Assert.assertEquals("DELETE", acceleratorKeys.getDelkey());
        Assert.assertEquals("EQUALS", acceleratorKeys.getPluskey());
        Assert.assertEquals("MINUS", acceleratorKeys.getMinuskey());
        Assert.assertEquals("F2", acceleratorKeys.getRenamekey());
        Assert.assertEquals("alt", acceleratorKeys.getHistorykey());
        Assert.assertEquals("shift 3", acceleratorKeys.getNumbersign());
        Assert.assertEquals("control", acceleratorKeys.getCtrlkey());
    }

    // Mocked methods to verify if other init methods are called
    @Test
    public void testInitDefaultAcceleratorKeysIfMissing_CallsOtherInitMethods() {
        AcceleratorKeys spyKeys = spy(acceleratorKeys);

        spyKeys.initDefaultAcceleratorKeysIfMissing();

        verify(spyKeys).initDefaultMainKeysIfMissing();
        verify(spyKeys).initDefaultNewEntryKeysIfMissing();
        verify(spyKeys).initDefaultDesktopKeysIfMissing();
        verify(spyKeys).initDefaultSearchResultsKeysIfMissing();
    }
}
