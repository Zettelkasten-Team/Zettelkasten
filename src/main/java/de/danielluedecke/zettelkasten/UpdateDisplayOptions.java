package de.danielluedecke.zettelkasten;

/**
 * Options for when calling UpdateDisplay.
 */
public class UpdateDisplayOptions {
	private boolean updateNoteSequencesTree;
	private boolean updateLinksTable;

	private UpdateDisplayOptions(UpdateDisplayOptionsBuilder builder) {
		this.updateNoteSequencesTree = builder.updateNoteSequencesTree;
		this.updateLinksTable = builder.updateLinksTable;
	}

	public boolean isUpdateNoteSequencesTree() {
		return updateNoteSequencesTree;
	}
	public boolean isUpdateLinksTable() {
		return updateLinksTable;
	}

	public static UpdateDisplayOptions defaultOptions() {
		return new UpdateDisplayOptions.UpdateDisplayOptionsBuilder().build();
	}

	public static class UpdateDisplayOptionsBuilder {
		// Defaults
		private boolean updateNoteSequencesTree = true;
		private boolean updateLinksTable = true;

		public UpdateDisplayOptionsBuilder updateNoteSequencesTab(boolean should) {
			this.updateNoteSequencesTree = should;
			return this;
		}
		
		public UpdateDisplayOptionsBuilder updateLinksTab(boolean should) {
			this.updateLinksTable = should;
			return this;
		}

		public UpdateDisplayOptions build() {
			UpdateDisplayOptions options = new UpdateDisplayOptions(this);
			return options;
		}
	}

}
