package de.danielluedecke.zettelkasten.walks;

import de.danielluedecke.zettelkasten.data.History;

import java.util.Optional;

public final class HistoryWalkAdapter {

    private HistoryWalkAdapter() {}

    public static Optional<int[]> candidatePathP(History history, int maxWindowSize, ProjectionMode mode) {
        if (history == null) {
            throw new IllegalArgumentException("history must not be null");
        }
        int[] snapshot = history.snapshot();
        if (snapshot.length == 0) {
            return Optional.empty();
        }
        int activeIndex = history.getHistoryPosition();
        return CandidatePathBuilder.bestCandidatePath(
                snapshot,
                activeIndex,
                maxWindowSize,
                mode,
                IntNarrativeEdges.NO_SELF_LOOPS);
    }
}
