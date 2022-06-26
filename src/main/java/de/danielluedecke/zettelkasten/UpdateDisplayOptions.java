package de.danielluedecke.zettelkasten;

/**
 * Options for when calling UpdateDisplay.
 */
public class UpdateDisplayOptions {
	private boolean updateNoteSequencesTree;

	private UpdateDisplayOptions(UpdateDisplayOptionsBuilder builder) {
		this.updateNoteSequencesTree = builder.updateNoteSequencesTree;
	}

	public boolean isUpdateNoteSequencesTree() {
		return updateNoteSequencesTree;
	}

	public static UpdateDisplayOptions defaultOptions() {
		return new UpdateDisplayOptions.UpdateDisplayOptionsBuilder().build();
	}

	public static class UpdateDisplayOptionsBuilder {
		// Defaults
		private boolean updateNoteSequencesTree = true;

		public UpdateDisplayOptionsBuilder updateNoteSequencesTab(boolean should) {
			this.updateNoteSequencesTree = should;
			return this;
		}

		public UpdateDisplayOptions build() {
			UpdateDisplayOptions options = new UpdateDisplayOptions(this);
			return options;
		}
	}

}
