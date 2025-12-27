package de.danielluedecke.zettelkasten.util;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;
import org.apache.commons.lang3.SystemUtils;

public class PlatformUtilTest {

    @Test
    public void testIsMacOS() {
        AssertJUnit.assertEquals(SystemUtils.IS_OS_MAC_OSX, PlatformUtil.isMacOS());
    }
}
