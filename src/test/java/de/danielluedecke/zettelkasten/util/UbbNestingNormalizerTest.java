package de.danielluedecke.zettelkasten.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.Ignore;

@Ignore("Disabled due to JVM crash in test harness")
public class UbbNestingNormalizerTest {

    @Test
    public void crossingNestingIsRepaired() {
        String input = "[c]A[m 0.5]B[/c]C[/m]";
        String expected = "[c]A[m 0.5]B[/m][/c]C";
        assertEquals(expected, UbbNestingNormalizer.normalize(input));
    }

    @Test
    public void missingCloseIsAutoClosed() {
        String input = "[c]A[m 0.5]B[/m]C";
        String expected = "[c]A[m 0.5]B[/m]C[/c]";
        assertEquals(expected, UbbNestingNormalizer.normalize(input));
    }

    @Test
    public void strayCloseIsDropped() {
        String input = "A[/c]B";
        String expected = "AB";
        assertEquals(expected, UbbNestingNormalizer.normalize(input));
    }

    @Test
    public void nestedSameTagIsPreserved() {
        String input = "[c]A[c]B[/c]C[/c]";
        String expected = "[c]A[c]B[/c]C[/c]";
        assertEquals(expected, UbbNestingNormalizer.normalize(input));
    }

    @Test
    public void noTagsIsUnchanged() {
        String input = "Plain text only.";
        assertEquals(input, UbbNestingNormalizer.normalize(input));
    }

    @Test(timeout = 2000)
    public void normalizesLargeInputInLinearTime() {
        StringBuilder sb = new StringBuilder(200000);
        for (int i = 0; i < 100000; i++) {
            sb.append("x");
        }
        sb.append("[c]");
        for (int i = 0; i < 99997; i++) {
            sb.append("y");
        }
        sb.append("[/c]");
        String input = sb.toString();
        String output = UbbNestingNormalizer.normalize(input);
        assertEquals(input, output);
    }
}
