package de.danielluedecke.zettelkasten.util;

import static org.mockito.Mockito.mockStatic;

import org.mockito.MockedStatic;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class PlatformUtilTest {

    @Test
    public void testIsMacOS() {
        // Mock the SystemUtils call to return true for isMacOS
        try (MockedStatic<PlatformUtil> mockedStatic = mockStatic(PlatformUtil.class)) {
            mockedStatic.when(PlatformUtil::isMacOS).thenReturn(true);

            // Call the method being tested
            boolean isMacOS = PlatformUtil.isMacOS();

            // Assert the result
            AssertJUnit.assertTrue(isMacOS);
        }
    }
}
