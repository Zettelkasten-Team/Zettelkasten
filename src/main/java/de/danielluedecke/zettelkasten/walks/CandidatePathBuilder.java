package de.danielluedecke.zettelkasten.walks;

import java.util.List;
import java.util.Optional;

public final class CandidatePathBuilder {

    private CandidatePathBuilder() {}

    /**
     * Builds the best candidate simple path from history.
     * Tie-break: longer path, then longest suffix match to raw window ending at activeIndex,
     * then larger window size, then first encountered in iteration order.
     */
    public static Optional<int[]> bestCandidatePath(
            int[] historySnapshot,
            int activeIndex,
            int maxWindowSize,
            ProjectionMode projectionMode,
            IntNarrativeEdgePredicate predicate) {
        if (historySnapshot == null) {
            throw new IllegalArgumentException("historySnapshot must not be null");
        }
        if (historySnapshot.length == 0) {
            return Optional.empty();
        }
        if (activeIndex < 0 || activeIndex >= historySnapshot.length) {
            throw new IllegalArgumentException("activeIndex out of range: " + activeIndex);
        }
        if (maxWindowSize < 1) {
            throw new IllegalArgumentException("maxWindowSize must be >= 1");
        }
        if (projectionMode == null) {
            throw new IllegalArgumentException("projectionMode must not be null");
        }
        if (predicate == null) {
            throw new IllegalArgumentException("predicate must not be null");
        }

        List<int[]> windows = HistorySegmenter.candidateWindows(historySnapshot, activeIndex, maxWindowSize);
        int[] best = null;
        int bestLength = -1;
        int bestSuffixMatch = -1;
        int bestWindowSize = -1;

        for (int[] window : windows) {
            int[] path = PathProjection.simplePathFromHistory(window, window.length - 1, projectionMode);
            if (!PathValidator.isNarrativePath(path, predicate)) {
                continue;
            }
            int length = path.length;
            int suffixMatch = suffixMatchLength(path, window);
            int windowSize = window.length;

            if (isBetter(length, suffixMatch, windowSize, bestLength, bestSuffixMatch, bestWindowSize)) {
                best = path;
                bestLength = length;
                bestSuffixMatch = suffixMatch;
                bestWindowSize = windowSize;
            }
        }

        return Optional.ofNullable(best);
    }

    private static boolean isBetter(
            int length,
            int suffixMatch,
            int windowSize,
            int bestLength,
            int bestSuffixMatch,
            int bestWindowSize) {
        if (length > bestLength) {
            return true;
        }
        if (length < bestLength) {
            return false;
        }
        if (suffixMatch > bestSuffixMatch) {
            return true;
        }
        if (suffixMatch < bestSuffixMatch) {
            return false;
        }
        if (windowSize > bestWindowSize) {
            return true;
        }
        if (windowSize < bestWindowSize) {
            return false;
        }
        return false;
    }

    private static int suffixMatchLength(int[] path, int[] window) {
        int i = path.length - 1;
        int j = window.length - 1;
        int match = 0;
        while (i >= 0 && j >= 0 && path[i] == window[j]) {
            match++;
            i--;
            j--;
        }
        return match;
    }
}
