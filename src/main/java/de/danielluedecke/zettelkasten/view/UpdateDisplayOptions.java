package de.danielluedecke.zettelkasten.view;

/**
 * Options for when calling UpdateDisplay.
 */
public class UpdateDisplayOptions {
	private boolean updateNoteSequencesTree;
	private boolean updateLinksTab;
	private boolean updateTitlesTab;

	private UpdateDisplayOptions(UpdateDisplayOptionsBuilder builder) {
		this.updateNoteSequencesTree = builder.updateNoteSequencesTree;
		this.updateLinksTab = builder.updateLinksTab;
		this.updateTitlesTab = builder.updateTitlesTab;
	}

	public boolean shouldUpdateNoteSequencesTree() {
		return updateNoteSequencesTree;
	}
	public boolean shouldUpdateLinksTable() {
		return updateLinksTab;
	}
	public boolean shouldUpdateTitlesTab() {
		return updateTitlesTab;
	}

	public static UpdateDisplayOptions defaultOptions() {
		return new UpdateDisplayOptions.UpdateDisplayOptionsBuilder().build();
	}

	public static class UpdateDisplayOptionsBuilder {		
		// Defaults
		private boolean updateNoteSequencesTree = true;
		private boolean updateLinksTab = true;
		private boolean updateTitlesTab = true;
		
		// Default builder.
		public UpdateDisplayOptionsBuilder() {}
		
		// Create a builder on top of an existing options.
		public UpdateDisplayOptionsBuilder(UpdateDisplayOptions options) {
			this.updateNoteSequencesTree = options.updateNoteSequencesTree;
			this.updateLinksTab = options.updateLinksTab;
			this.updateTitlesTab = options.updateTitlesTab;
		}

		public UpdateDisplayOptionsBuilder updateNoteSequencesTab(boolean should) {
			this.updateNoteSequencesTree = should;
			return this;
		}

		public UpdateDisplayOptionsBuilder updateLinksTab(boolean should) {
			this.updateLinksTab = should;
			return this;
		}
		
		public UpdateDisplayOptionsBuilder updateTitlesTab(boolean should) {
			this.updateTitlesTab = should;
			return this;
		}

		public UpdateDisplayOptions build() {
			UpdateDisplayOptions options = new UpdateDisplayOptions(this);
			return options;
		}
	}

}
