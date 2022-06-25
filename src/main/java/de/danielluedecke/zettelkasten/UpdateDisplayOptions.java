package de.danielluedecke.zettelkasten;

/**
 * Options for when calling UpdateDisplay.
 */
public class UpdateDisplayOptions {
	private boolean updateNoteSequencesTab;

	private UpdateDisplayOptions(UpdateDisplayOptionsBuilder builder) {
		this.updateNoteSequencesTab = builder.updateNoteSequencesTab;
	}

	public boolean isUpdateNoteSequencesTab() {
		return updateNoteSequencesTab;
	}
	
	public static UpdateDisplayOptions defaultOptions() {
		return new UpdateDisplayOptions.UpdateDisplayOptionsBuilder().build();
	}

	public static class UpdateDisplayOptionsBuilder {
		// Defaults
		private boolean updateNoteSequencesTab = true;

		public UpdateDisplayOptionsBuilder updateNoteSequencesTab(boolean should) {
			this.updateNoteSequencesTab= should;
			return this;
		}
		public UpdateDisplayOptions build() {
			UpdateDisplayOptions options = new UpdateDisplayOptions(this);
			return options;
		}
	}

}
