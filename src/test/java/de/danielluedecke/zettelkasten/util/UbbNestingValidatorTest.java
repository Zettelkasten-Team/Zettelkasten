package de.danielluedecke.zettelkasten.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UbbNestingValidatorTest {

    @Test
    public void validBalancedReturnsValid() {
        UbbNestingValidator.Result result = UbbNestingValidator.validate("[c]A[m 0.5]B[/m]C[/c]");
        assertTrue(result.valid);
    }

    @Test
    public void strayCloseReturnsInvalidWithRawPos() {
        String input = "A[/c]B";
        UbbNestingValidator.Result result = UbbNestingValidator.validate(input);
        assertFalse(result.valid);
        assertEquals(1, result.rawPos);
    }

    @Test
    public void crossingCloseReturnsInvalid() {
        String input = "[c]A[m 0.5]B[/c]C[/m]";
        UbbNestingValidator.Result result = UbbNestingValidator.validate(input);
        assertFalse(result.valid);
        assertTrue(result.message.contains("crossing close"));
    }

    @Test
    public void unclosedOpenReturnsInvalidAtOpenPos() {
        String input = "[c]A";
        UbbNestingValidator.Result result = UbbNestingValidator.validate(input);
        assertFalse(result.valid);
        assertEquals(0, result.rawPos);
    }
}
