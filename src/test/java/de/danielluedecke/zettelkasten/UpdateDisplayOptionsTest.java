package de.danielluedecke.zettelkasten;

import org.testng.Assert;
import org.testng.annotations.Test;

public class UpdateDisplayOptionsTest {

    @Test
    public void testDefaultOptions() {
        UpdateDisplayOptions options = UpdateDisplayOptions.defaultOptions();
        Assert.assertTrue(options.shouldUpdateNoteSequencesTree());
        Assert.assertTrue(options.shouldUpdateLinksTable());
        Assert.assertTrue(options.shouldUpdateTitlesTab());
    }

    @Test
    public void testCustomOptions() {
        UpdateDisplayOptions options = new UpdateDisplayOptions.UpdateDisplayOptionsBuilder()
            .updateNoteSequencesTab(false)
            .updateLinksTab(true)
            .updateTitlesTab(false)
            .build();

        Assert.assertFalse(options.shouldUpdateNoteSequencesTree());
        Assert.assertTrue(options.shouldUpdateLinksTable());
        Assert.assertFalse(options.shouldUpdateTitlesTab());
    }

    @Test
    public void testBuilderWithExistingOptions() {
        UpdateDisplayOptions originalOptions = new UpdateDisplayOptions.UpdateDisplayOptionsBuilder()
            .updateNoteSequencesTab(false)
            .updateLinksTab(false)
            .updateTitlesTab(true)
            .build();

        UpdateDisplayOptions newOptions = new UpdateDisplayOptions.UpdateDisplayOptionsBuilder(originalOptions)
            .updateNoteSequencesTab(true)
            .build();

        Assert.assertTrue(newOptions.shouldUpdateNoteSequencesTree());
        Assert.assertFalse(newOptions.shouldUpdateLinksTable());
        Assert.assertTrue(newOptions.shouldUpdateTitlesTab());
    }
}
